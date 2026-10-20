package com.opsdesk.tickets;

import com.opsdesk.warmup.BankAccount;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // --- BankAccount demo: encapsulation in action ---
        System.out.println("=== BankAccount Demo ===");
        BankAccount account = new BankAccount(100.0);
        System.out.println("Opening balance: " + account.getBalance());

        account.deposit(50.0);
        System.out.println("After depositing 50: " + account.getBalance());

        account.withdraw(30.0);
        System.out.println("After withdrawing 30: " + account.getBalance());

        // Try an invalid operation — this proves the encapsulation works
        try {
            account.withdraw(200.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Overdraw rejected: " + e.getMessage());
        }

        try {
            account.deposit(-10.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Negative deposit rejected: " + e.getMessage());
        }

        // --- Polymorphism demo ---
        System.out.println("\n=== Ticket Polymorphism Demo ===");

        /*
         * POLYMORPHISM: We declare ONE list of type List<Ticket>, but
         * we put DIFFERENT subclass objects into it. When we call
         * describe() on each one, Java calls the correct version —
         * SupportTicket.describe() or AssetRequest.describe() —
         * automatically, based on the ACTUAL type of the object.
         *
         * The loop doesn't know or care which subclass each ticket is.
         * It just knows they're all Tickets, and all Tickets have describe().
         * That's polymorphism: one interface, many forms.
         *
         * List<Ticket> is a GENERIC type. The <Ticket> part tells Java
         * "this list only holds Ticket objects (or subclasses of Ticket)."
         * Without generics, you'd get an untyped list and would need
         * to cast everything back to Ticket when you read it out.
         * The <> (diamond) on the right side is shorthand — Java infers
         * the type from the left side.
         */
        List<Ticket> tickets = new ArrayList<>();

        tickets.add(new SupportTicket("TK-001", "Login page broken", "OPEN", 1, "critical"));
        tickets.add(new SupportTicket("TK-002", "Slow dashboard load", "OPEN", 3, "normal"));
        tickets.add(new AssetRequest("TK-003", "New laptop for intern", "OPEN", 2, "laptop"));
        tickets.add(new AssetRequest("TK-004", "Photoshop license", "OPEN", 4, "software license"));

        // One loop, different describe() output for each type
        for (Ticket ticket : tickets) {
            System.out.println(ticket.describe());
        }

        // --- Assignable demo ---
        System.out.println("\n=== Assignable Demo ===");

        // We can also treat any ticket as Assignable, because Ticket
        // implements the Assignable interface.
        for (Ticket ticket : tickets) {
            ticket.assignTo("alice");
        }
        System.out.println("All tickets assigned to: " + tickets.get(0).getAssignee());

        // Assign one to someone else to prove it works per-ticket
        tickets.get(2).assignTo("bob");
        for (Ticket ticket : tickets) {
            System.out.println(ticket.getId() + " -> " + ticket.getAssignee());
        }
    }
}
