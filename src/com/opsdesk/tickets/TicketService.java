package com.opsdesk.tickets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * All ticket business logic lives here.
 *
 * WHY SEPARATE FROM THE MENU?
 * The menu (TicketMenu) reads input and prints output — it knows
 * about Scanner and System.out. This class knows NOTHING about I/O.
 * It just manages tickets.
 *
 * This separation means:
 * 1. You can test the logic without a terminal (important in Part 4).
 * 2. You could swap the CLI for a web UI without changing this class.
 * 3. Each class has ONE job (Single Responsibility Principle).
 */
public class TicketService {

    // All tickets stored in memory. A Map would be faster for lookups,
    // but we also need ordered iteration and index-based access,
    // so a List is the simpler choice. We use streams for filtering.
    private final List<Ticket> tickets = new ArrayList<>();

    // Auto-incrementing counter for generating unique ticket ids
    private int nextId = 1;

    /**
     * Creates a new ticket and adds it to the store.
     * Returns the created ticket so the caller can confirm.
     */
    public Ticket addTicket(String title, int priority, String type, String extra) {
        String id = String.format("TK-%03d", nextId++);
        Ticket ticket;

        // Build the right subclass based on the type string
        if ("support".equalsIgnoreCase(type)) {
            ticket = new SupportTicket(id, title, TicketStatus.OPEN, priority, extra);
        } else if ("asset".equalsIgnoreCase(type)) {
            ticket = new AssetRequest(id, title, TicketStatus.OPEN, priority, extra);
        } else {
            ticket = new Ticket(id, title, TicketStatus.OPEN, priority);
        }

        tickets.add(ticket);
        return ticket;
    }

    /**
     * Looks up a ticket by id. Throws if not found.
     *
     * This is the method that uses our custom exception.
     * The menu catches TicketNotFoundException in ONE place.
     */
    public Ticket getTicket(String id) {
        return findTicket(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    /**
     * Looks up a ticket by id, returning Optional.
     *
     * WHAT IS OPTIONAL?
     * Instead of returning null (which the caller might forget to check,
     * causing a NullPointerException), we return Optional<Ticket>.
     * The caller is FORCED to deal with the empty case explicitly:
     *   - .orElseThrow() — throw if empty
     *   - .orElse(default) — use a default if empty
     *   - .ifPresent(t -> ...) — do something only if present
     *
     * STREAM EXPLANATION:
     * tickets.stream()          — create a stream from the list
     *   .filter(t -> ...)       — keep only items matching the condition
     *                             (like JS: array.filter(t => ...))
     *   .findFirst()            — take the first match, returns Optional
     */
    public Optional<Ticket> findTicket(String id) {
        return tickets.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    /** Returns all tickets (unmodifiable view would be safer, but kept simple). */
    public List<Ticket> listAll() {
        return tickets;
    }

    /**
     * Filters tickets by status using a stream.
     *
     * BEFORE (for-loop version you saw in CollectionsDemo):
     *   List<Ticket> result = new ArrayList<>();
     *   for (Ticket t : tickets) {
     *       if (t.getStatus() == status) result.add(t);
     *   }
     *   return result;
     *
     * AFTER (stream version):
     *   The stream reads like English: "from tickets, keep those
     *   whose status matches, collect into a list."
     */
    public List<Ticket> filterByStatus(TicketStatus status) {
        return tickets.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Returns tickets sorted by priority (lowest number = highest priority),
     * with ties broken alphabetically by title.
     *
     * BEFORE (imperative):
     *   List<Ticket> sorted = new ArrayList<>(tickets);
     *   sorted.sort(Comparator.comparingInt(Ticket::getPriority)
     *                          .thenComparing(Ticket::getTitle));
     *   return sorted;
     *
     * AFTER (stream — same result, doesn't modify the original list):
     *   Ticket::getPriority is a METHOD REFERENCE — shorthand for
     *   the lambda t -> t.getPriority(). Java knows which method to call.
     */
    public List<Ticket> sortedByPriority() {
        return tickets.stream()
                .sorted(Comparator.comparingInt(Ticket::getPriority)
                                   .thenComparing(Ticket::getTitle))
                .collect(Collectors.toList());
    }

    /**
     * Assign a ticket to a user. Throws if the ticket doesn't exist.
     */
    public void assignTicket(String ticketId, String user) {
        Ticket ticket = getTicket(ticketId);  // throws if missing
        ticket.assignTo(user);
    }

    /**
     * Change a ticket's status. Throws if the ticket doesn't exist.
     */
    public void changeStatus(String ticketId, TicketStatus newStatus) {
        Ticket ticket = getTicket(ticketId);  // throws if missing
        ticket.setStatus(newStatus);
    }

    /**
     * Demonstrates the record + streams combo:
     * Map each ticket to a lightweight TicketSummary (the record we created).
     *
     * .map() transforms each element — like JS array.map().
     * The result is a list of shallowly immutable snapshots
     * (fields can't be reassigned, but mutable field contents could
     * still change — TicketSummary is safe because all its fields
     * are String/enum, which are themselves immutable).
     */
    public List<TicketSummary> getUniqueSummaries() {
        return tickets.stream()
                .map(t -> new TicketSummary(t.getId(), t.getTitle(), t.getStatus()))
                .collect(Collectors.toList());
    }

    /** Allows setting the next id counter (used when loading from file). */
    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    /** Returns the current next id value. */
    public int getNextId() {
        return nextId;
    }
}
