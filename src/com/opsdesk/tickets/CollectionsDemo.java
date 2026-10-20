package com.opsdesk.tickets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Demonstrates Java collections with the ticket hierarchy.
 * Run this class directly to see each collection in action.
 */
public class CollectionsDemo {

    public static void main(String[] args) {

        // ============================================================
        // 1. LIST<TICKET> — an ordered collection you loop over
        // ============================================================
        // List is like a JS array: ordered, allows duplicates, index-based.
        // The <Ticket> part is a GENERIC TYPE PARAMETER — it tells the
        // compiler "this list only holds Tickets." Before generics (pre-Java 5),
        // you'd use a raw List of Object and cast on every get().
        //
        // ArrayList<>() — the <> is the DIAMOND OPERATOR. Java infers
        // the type from the left side, so you don't write it twice.
        System.out.println("=== 1. List<Ticket> ===");

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new SupportTicket("TK-001", "Login page broken", TicketStatus.OPEN, 1, "critical"));
        tickets.add(new SupportTicket("TK-002", "Slow dashboard", TicketStatus.IN_PROGRESS, 3, "normal"));
        tickets.add(new AssetRequest("TK-003", "New laptop for intern", TicketStatus.OPEN, 2, "laptop"));
        tickets.add(new AssetRequest("TK-004", "Photoshop license", TicketStatus.RESOLVED, 4, "software license"));
        tickets.add(new SupportTicket("TK-005", "Email not sending", TicketStatus.OPEN, 1, "critical"));

        System.out.println("All tickets (insertion order):");
        for (Ticket t : tickets) {
            System.out.println("  " + t.describe());
        }

        // ============================================================
        // 2. SET<STRING> — unique assignees, no duplicates
        // ============================================================
        // A Set is like a JS Set: it holds unique values only.
        // If you add the same value twice, the second add is ignored.
        // HashSet uses hashCode() to find the bucket, then equals()
        // to check for duplicates — that's why we overrode both.
        System.out.println("\n=== 2. Set<String> — unique assignees ===");

        tickets.get(0).assignTo("alice");
        tickets.get(1).assignTo("bob");
        tickets.get(2).assignTo("alice");   // duplicate — alice again
        tickets.get(3).assignTo("carol");
        // TK-005 intentionally left unassigned

        Set<String> assignees = new HashSet<>();
        for (Ticket t : tickets) {
            String assignee = t.getAssignee();
            if (assignee != null) {
                assignees.add(assignee);
            }
        }
        System.out.println("Unique assignees: " + assignees);
        System.out.println("(alice appears once even though two tickets are assigned to her)");

        // ============================================================
        // 3. MAP<STRING, TICKET> — look up a ticket by id
        // ============================================================
        // A Map is like a JS object/Map: key-value pairs.
        // map.get("TK-001") jumps straight to the ticket — O(1) average.
        // With a List, you'd loop through every item to find it — O(n).
        // The cost: more memory for the hash table and keys.
        System.out.println("\n=== 3. Map<String, Ticket> — lookup by id ===");

        Map<String, Ticket> ticketById = new HashMap<>();
        for (Ticket t : tickets) {
            ticketById.put(t.getId(), t);
        }

        // Instant lookup — no looping
        String lookupId = "TK-003";
        Ticket found = ticketById.get(lookupId);
        System.out.println("Looking up " + lookupId + ": " + (found != null ? found.describe() : "not found"));

        String missingId = "TK-999";
        Ticket notFound = ticketById.get(missingId);
        System.out.println("Looking up " + missingId + ": " + (notFound != null ? notFound.describe() : "not found"));

        // ============================================================
        // 4. MAP<STRING, LIST<TICKET>> — group tickets by status
        // ============================================================
        // This is like JS's reduce() building an object of arrays.
        // The key is the status string, the value is a list of tickets
        // with that status. Useful for dashboards: "show me all OPEN tickets."
        System.out.println("\n=== 4. Map<String, List<Ticket>> — group by status ===");

        Map<TicketStatus, List<Ticket>> byStatus = new HashMap<>();
        for (Ticket t : tickets) {
            // computeIfAbsent: if the key doesn't exist yet, create
            // an empty list for it. Then add the ticket to that list.
            // This avoids the null-check-then-create boilerplate.
            byStatus.computeIfAbsent(t.getStatus(), k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<TicketStatus, List<Ticket>> entry : byStatus.entrySet()) {
            System.out.println(entry.getKey() + " (" + entry.getValue().size() + " tickets):");
            for (Ticket t : entry.getValue()) {
                System.out.println("  " + t.describe());
            }
        }

        // ============================================================
        // 5. SORTING with Comparator — priority first, then title
        // ============================================================
        // In JS you'd do: tickets.sort((a, b) => a.priority - b.priority || a.title.localeCompare(b.title))
        // Java's Comparator does the same thing with a more explicit API.
        // Comparator.comparing() takes a function that extracts the sort key.
        // .thenComparing() adds a secondary sort for ties.
        System.out.println("\n=== 5. Sorted by priority, then title ===");

        List<Ticket> sorted = new ArrayList<>(tickets);  // copy so we don't change the original
        sorted.sort(
            Comparator.comparingInt(Ticket::getPriority)
                       .thenComparing(Ticket::getTitle)
        );

        for (Ticket t : sorted) {
            System.out.println("  P" + t.getPriority() + " " + t.getTitle());
        }

        // ============================================================
        // 6. EQUALS + HASHCODE proof — deduplication in a Set
        // ============================================================
        // We create two DIFFERENT objects with the SAME id.
        // Without equals()/hashCode(), the Set would keep both (different memory addresses).
        // With our overrides, the Set recognizes them as the same ticket.
        System.out.println("\n=== 6. equals()/hashCode() proof ===");

        Ticket original = new SupportTicket("TK-001", "Login page broken", TicketStatus.OPEN, 1, "critical");
        Ticket duplicate = new SupportTicket("TK-001", "Login page broken", TicketStatus.OPEN, 1, "critical");

        System.out.println("original == duplicate (same object?): " + (original == duplicate));
        System.out.println("original.equals(duplicate) (same id?): " + original.equals(duplicate));
        System.out.println("original.hashCode(): " + original.hashCode());
        System.out.println("duplicate.hashCode(): " + duplicate.hashCode());

        Set<Ticket> deduped = new HashSet<>();
        deduped.add(original);
        deduped.add(duplicate);
        System.out.println("Set size after adding both: " + deduped.size());
        System.out.println("(Expected 1 — the Set treats them as the same ticket)");
    }
}
