package com.opsdesk.tickets;

/**
 * The possible states a ticket can be in.
 *
 * WHY an enum instead of strings?
 * With strings, "OPNE" is a valid String — Java can't tell it's a typo.
 * With an enum, TicketStatus.OPNE is a COMPILE ERROR — Java knows
 * exactly which values are allowed and rejects anything else instantly.
 *
 * Enums also give you free == comparison (no .equals() needed),
 * work beautifully in switch statements, and can carry methods if needed.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
