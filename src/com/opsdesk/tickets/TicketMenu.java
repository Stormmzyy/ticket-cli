package com.opsdesk.tickets;

import java.util.List;
import java.util.Scanner;

/**
 * The interactive command-line menu.
 *
 * THIS CLASS handles I/O only: reading from Scanner, printing to System.out.
 * All business logic lives in TicketService.
 *
 * WHY THIS SEPARATION MATTERS:
 * - TicketService can be tested without a terminal (Part 4's TicketDemo).
 * - If we replaced the CLI with a web UI, TicketService wouldn't change.
 * - Each class has one job: Menu = user interaction, Service = logic.
 *
 * INPUT HANDLING NOTE:
 * Every input is read with scanner.nextLine(), NEVER scanner.nextInt().
 * Why? nextInt() reads only the number and leaves the newline character
 * sitting in the buffer. The next nextLine() call then reads that leftover
 * newline and returns an empty string, making it look like the user hit
 * Enter without typing anything. By always using nextLine() and parsing
 * with Integer.parseInt(), we consume the entire line every time and
 * avoid this "phantom empty line" bug.
 */
public class TicketMenu {

    private final TicketService service;
    private final TicketStore store;
    private final Scanner scanner;

    public TicketMenu(TicketService service, TicketStore store) {
        this.service = service;
        this.store = store;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main loop: show the menu, read a choice, dispatch to the right handler.
     * Runs until the user picks Quit.
     */
    public void run() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            /*
             * We catch TicketNotFoundException in ONE place — right here.
             * Every menu option that touches a ticket by id goes through
             * this try/catch. The user sees a friendly message, never
             * a stack trace. This is the "catch at the boundary" pattern.
             *
             * We do NOT catch bare Exception here. That would silently
             * swallow bugs like NullPointerException or ClassCastException
             * that should actually crash loudly so we can find and fix them.
             */
            try {
                switch (choice) {
                    case "1" -> handleCreate();
                    case "2" -> handleList();
                    case "3" -> handleAssign();
                    case "4" -> handleChangeStatus();
                    case "5" -> handleFilterByStatus();
                    case "6" -> running = false;
                    default  -> System.out.println("Unknown option. Try again.");
                }
            } catch (TicketNotFoundException e) {
                // Friendly message, no stack trace
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\n--- OpsDesk Ticket Manager ---");
        System.out.println("1. Create ticket");
        System.out.println("2. List all tickets");
        System.out.println("3. Assign ticket");
        System.out.println("4. Change ticket status");
        System.out.println("5. Filter by status");
        System.out.println("6. Quit");
        System.out.print("Choose: ");
    }

    /**
     * WHY REJECT PIPES INSTEAD OF QUOTING?
     * Our file format uses | as the delimiter. We could support pipes
     * in text by adding CSV-style quoting (wrap fields in quotes, escape
     * quotes inside), but that adds real parsing complexity for a rare
     * edge case. Rejecting pipes at input is simpler, harder to get wrong,
     * and teaches the principle: validate at the boundary so the rest of
     * the system can trust its data.
     */
    private boolean containsPipe(String input) {
        return input.contains("|");
    }

    private void handleCreate() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }
        if (containsPipe(title)) {
            System.out.println("Title cannot contain the '|' character (used as file delimiter).");
            return;
        }

        System.out.print("Priority (1=highest, 5=lowest): ");
        int priority = parseIntInRange(scanner.nextLine().trim(), 1, 5);
        if (priority == -1) {
            System.out.println("Invalid priority. Must be 1-5. Using default (3).");
            priority = 3;
        }

        System.out.print("Type (support/asset/general): ");
        String type = scanner.nextLine().trim();

        String extra = "";
        if ("support".equalsIgnoreCase(type)) {
            System.out.print("Urgency level (critical/normal/low): ");
            extra = scanner.nextLine().trim();
            if (containsPipe(extra)) {
                System.out.println("Urgency level cannot contain '|'.");
                return;
            }
        } else if ("asset".equalsIgnoreCase(type)) {
            System.out.print("Asset type (laptop/monitor/software license/etc): ");
            extra = scanner.nextLine().trim();
            if (containsPipe(extra)) {
                System.out.println("Asset type cannot contain '|'.");
                return;
            }
        }

        Ticket created = service.addTicket(title, priority, type, extra);
        store.save(service.listAll());  // persist immediately so a crash doesn't lose data
        System.out.println("Created: " + created.describe());
    }

    /**
     * Parses an integer from a string and checks it's within [min, max].
     * Returns -1 if the string is not a valid integer or is out of range.
     *
     * This handles "abc", "", "999999999999" (overflow), negative numbers,
     * and numbers outside the allowed range — all in one place.
     */
    private int parseIntInRange(String input, int min, int max) {
        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            // Covers: non-numeric strings, empty strings, and numbers
            // too large for int (like "999999999999" which overflows)
            return -1;
        }
    }

    private void handleList() {
        List<Ticket> all = service.sortedByPriority();
        if (all.isEmpty()) {
            System.out.println("No tickets yet.");
            return;
        }
        System.out.println("\nAll tickets (sorted by priority):");
        for (Ticket t : all) {
            String assignee = t.getAssignee() != null ? t.getAssignee() : "unassigned";
            System.out.println("  " + t.describe() + " [" + assignee + "]");
        }
    }

    private void handleAssign() {
        System.out.print("Ticket ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Ticket ID cannot be empty.");
            return;
        }

        System.out.print("Assign to: ");
        String user = scanner.nextLine().trim();
        if (user.isEmpty()) {
            System.out.println("User name cannot be empty.");
            return;
        }
        if (containsPipe(user)) {
            System.out.println("User name cannot contain '|'.");
            return;
        }

        service.assignTicket(id, user);
        store.save(service.listAll());
        System.out.println("Assigned " + id + " to " + user);
    }

    private void handleChangeStatus() {
        System.out.print("Ticket ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Ticket ID cannot be empty.");
            return;
        }

        System.out.println("Statuses: OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        System.out.print("New status: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        try {
            TicketStatus newStatus = TicketStatus.valueOf(statusStr);
            service.changeStatus(id, newStatus);
            store.save(service.listAll());
            System.out.println("Updated " + id + " to " + newStatus);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + statusStr);
        }
    }

    private void handleFilterByStatus() {
        System.out.println("Statuses: OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        System.out.print("Filter by: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        try {
            TicketStatus status = TicketStatus.valueOf(statusStr);
            List<Ticket> filtered = service.filterByStatus(status);
            if (filtered.isEmpty()) {
                System.out.println("No tickets with status " + status);
                return;
            }
            System.out.println("\nTickets with status " + status + ":");
            for (Ticket t : filtered) {
                System.out.println("  " + t.describe());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + statusStr);
        }
    }
}
