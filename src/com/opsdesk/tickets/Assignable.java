package com.opsdesk.tickets;

/**
 * An interface is a CONTRACT: any class that implements it
 * PROMISES to provide the listed methods.
 *
 * Think of it like a TypeScript interface — it says "I guarantee
 * I have an assignTo method" without dictating how it works.
 *
 * Why an interface instead of putting assignTo in Ticket?
 * Not everything assignable is a ticket. Later we might have
 * tasks, incidents, or change requests that are also assignable.
 * The interface lets unrelated classes share this capability.
 */
public interface Assignable {

    /**
     * Assign this item to a user.
     * @param user the username to assign to
     */
    void assignTo(String user);

    /**
     * Get the currently assigned user, or null if unassigned.
     */
    String getAssignee();
}
