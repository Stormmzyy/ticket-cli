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
 */
public class TicketMenu {

    private final TicketService service;
    private final Scanner scanner;

    public TicketMenu(TicketService service) {
        this.service = service;
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

    private void handleCreate() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Priority (1=highest, 5=lowest): ");
        int priority;
        try {
            priority = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid priority. Using default (3).");
            priority = 3;
        }

        System.out.print("Type (support/asset/general): ");
        String type = scanner.nextLine().trim();

        String extra = "";
        if ("support".equalsIgnoreCase(type)) {
            System.out.print("Urgency level (critical/normal/low): ");
            extra = scanner.nextLine().trim();
        } else if ("asset".equalsIgnoreCase(type)) {
            System.out.print("Asset type (laptop/monitor/software license/etc): ");
            extra = scanner.nextLine().trim();
        }

        Ticket created = service.addTicket(title, priority, type, extra);
        System.out.println("Created: " + created.describe());
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
        System.out.print("Assign to: ");
        String user = scanner.nextLine().trim();
        service.assignTicket(id, user);
        System.out.println("Assigned " + id + " to " + user);
    }

    private void handleChangeStatus() {
        System.out.print("Ticket ID: ");
        String id = scanner.nextLine().trim();
        System.out.println("Statuses: OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        System.out.print("New status: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        try {
            TicketStatus newStatus = TicketStatus.valueOf(statusStr);
            service.changeStatus(id, newStatus);
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
