package com.opsdesk.tickets;

/**
 * A read-only snapshot of a ticket's key information.
 *
 * "record" is a special kind of class introduced in Java 16.
 * This single line gives you FOR FREE:
 *   - A constructor: new TicketSummary(id, title, status)
 *   - Accessor methods: id(), title(), status()  (no "get" prefix)
 *   - equals() and hashCode() based on ALL fields
 *   - A useful toString() like "TicketSummary[id=TK-001, title=..., status=OPEN]"
 *
 * In JS terms, it's like Object.freeze({ id, title, status }) but with
 * compile-time guarantees: you literally cannot change the fields.
 *
 * WHEN IS A RECORD THE WRONG CHOICE?
 * - When you need to mutate fields after creation (records are immutable)
 * - When you need inheritance (records can't extend other classes)
 * - When you need custom field visibility (record fields are always final)
 * That's why Ticket itself is NOT a record — it has mutable status
 * and assignee, and it uses inheritance for SupportTicket/AssetRequest.
 */
public record TicketSummary(String id, String title, TicketStatus status) {
}
