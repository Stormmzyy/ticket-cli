package com.opsdesk.tickets;

import java.util.Optional;

/**
 * Exercises TicketService with no terminal involved.
 *
 * THIS IS THE PAYOFF for keeping TicketService free of I/O.
 * There is no Scanner, no System.in, no user interaction anywhere
 * in this file. We call TicketService methods directly, check the
 * results, and print PASS/FAIL for each case.
 *
 * In a real project you'd use a testing framework like JUnit for this.
 * The pattern is the same: call a method, assert something about the
 * result. We're doing it by hand so there's no extra dependency.
 */
public class TicketServiceDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== TicketService Demo (no terminal needed) ===\n");

        TicketService service = new TicketService();

        // --- 1. Create a ticket ---
        Ticket created = service.addTicket("Server on fire", 1, "support", "critical");
        check("Create ticket",
                created != null
                && "TK-001".equals(created.getId())
                && "Server on fire".equals(created.getTitle())
                && created.getStatus() == TicketStatus.OPEN);

        // --- 2. Find a ticket that exists ---
        Optional<Ticket> found = service.findTicket("TK-001");
        check("Find existing ticket (Optional present)",
                found.isPresent()
                && "Server on fire".equals(found.get().getTitle()));

        // --- 3. Find a ticket that does NOT exist (empty Optional path) ---
        Optional<Ticket> missing = service.findTicket("TK-999");
        check("Find missing ticket (Optional empty)",
                missing.isEmpty());

        // --- 4. Assign a ticket ---
        service.assignTicket("TK-001", "alice");
        check("Assign ticket",
                "alice".equals(service.getTicket("TK-001").getAssignee()));

        // --- 5. Change a ticket's status ---
        service.changeStatus("TK-001", TicketStatus.IN_PROGRESS);
        check("Change status",
                service.getTicket("TK-001").getStatus() == TicketStatus.IN_PROGRESS);

        // --- 6. Trigger TicketNotFoundException and catch it ---
        boolean caughtException = false;
        try {
            service.getTicket("TK-999");  // should throw
        } catch (TicketNotFoundException e) {
            caughtException = true;
        }
        check("TicketNotFoundException thrown for missing id",
                caughtException);

        // --- Summary ---
        System.out.println("\n--- Results: " + passed + " passed, " + failed + " failed ---");
    }

    /**
     * Prints PASS or FAIL for a single test case.
     * Keeps a running count so we can summarize at the end.
     */
    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("PASS: " + name);
            passed++;
        } else {
            System.out.println("FAIL: " + name);
            failed++;
        }
    }
}
