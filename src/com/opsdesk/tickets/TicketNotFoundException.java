package com.opsdesk.tickets;

/**
 * Thrown when a ticket id doesn't exist in the system.
 *
 * CHECKED vs UNCHECKED exceptions:
 *
 * Checked (extends Exception):
 *   The compiler FORCES every caller to catch it or declare "throws".
 *   Good for recoverable problems the caller should handle, like
 *   "file not found" or "network timeout."
 *
 * Unchecked (extends RuntimeException):
 *   The compiler doesn't force handling. Good for programming errors
 *   or invalid input that shouldn't happen in correct code.
 *
 * WHY WE CHOSE UNCHECKED:
 * A missing ticket id is a user/input error — the caller typed a bad id.
 * Forcing every single method call to try/catch would be very noisy.
 * Instead, we catch it in ONE place at the CLI boundary and show a
 * friendly message. This keeps the service code clean.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String ticketId) {
        // super() calls RuntimeException's constructor with the message.
        // This message is what getMessage() returns later.
        super("Ticket not found: " + ticketId);
    }
}
