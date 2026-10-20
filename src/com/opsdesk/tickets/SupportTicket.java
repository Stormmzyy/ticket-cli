package com.opsdesk.tickets;

/**
 * A support ticket — someone needs help with something.
 *
 * "extends Ticket" means SupportTicket IS a Ticket. It inherits
 * all of Ticket's fields and methods, then adds its own.
 * In JS: class SupportTicket extends Ticket { ... }
 */
public class SupportTicket extends Ticket {

    // Extra field unique to support tickets.
    // How urgent is the user's problem? e.g. "critical", "normal", "low"
    private String urgencyLevel;

    /**
     * super() calls the PARENT's constructor to set up id, title, etc.
     * Then we set our own extra field. Java requires super() to be the
     * very first line — you can't do anything before the parent is built.
     */
    public SupportTicket(String id, String title, TicketStatus status, int priority, String urgencyLevel) {
        super(id, title, status, priority);
        this.urgencyLevel = urgencyLevel;
    }

    /**
     * @Override tells the compiler: "I'm intentionally replacing the
     * parent's describe() method." If I misspelled it as "desrcibe()",
     * the compiler would catch that mistake immediately.
     *
     * We can access id, title, status, priority directly because
     * they're "protected" in Ticket — visible to subclasses.
     */
    @Override
    public String describe() {
        return String.format("[%s] SUPPORT: %s (status: %s, priority: %d, urgency: %s)",
                id, title, status, priority, urgencyLevel);
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }
}
