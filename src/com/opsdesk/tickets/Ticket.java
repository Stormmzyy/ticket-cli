package com.opsdesk.tickets;

import java.util.Objects;

/**
 * Base class for all ticket types.
 *
 * DESIGN CHOICE: fields are "protected" not "private".
 * - private: only THIS class can see them. Subclasses would need
 *   a getter for every single field just to build their describe() text.
 * - protected: this class AND subclasses can see them directly.
 *   Unrelated code still can't touch them.
 * - public: anyone can change them — defeats the purpose of encapsulation.
 *
 * We chose protected because SupportTicket and AssetRequest are
 * tightly coupled to Ticket (they ARE tickets), so sharing fields
 * with them is safe. If these were unrelated classes, private + getters
 * would be better.
 */
public class Ticket implements Assignable {

    protected String id;
    protected String title;
    protected String status;
    protected int priority;      // 1 = highest, 5 = lowest

    // Assignee is tracked here because every ticket can be assigned.
    // It starts as null (unassigned).
    private String assignee;

    /**
     * Constructor: the this.x = x idiom.
     * When parameter names match field names, "this." refers to the
     * field. Without "this.", Java thinks you mean the parameter.
     * This is the most common Java constructor pattern you'll see.
     */
    public Ticket(String id, String title, String status, int priority) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.priority = priority;
    }

    /**
     * Describes this ticket in a human-readable way.
     * Subclasses OVERRIDE this to add their own details.
     *
     * Why override instead of replace?
     * "Override" means the subclass provides its OWN version of the
     * same method. The method signature stays the same, so code that
     * holds a Ticket reference can call describe() without knowing
     * which subclass it actually is. That's polymorphism.
     */
    public String describe() {
        return String.format("[%s] %s (status: %s, priority: %d)", id, title, status, priority);
    }

    // --- Assignable interface implementation ---

    /**
     * @Override is a safety annotation. It tells the compiler:
     * "I intend to override a method from a parent class or interface."
     * If you misspell the method name, the compiler catches the error
     * instead of silently creating a brand-new method.
     */
    @Override
    public void assignTo(String user) {
        this.assignee = user;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }

    // --- Getters for fields that outside code needs to read ---

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- equals and hashCode: identity by ticket id ---

    /**
     * Two tickets are "equal" if they have the same id.
     *
     * WHY override this? By default, Java's equals() checks if two
     * variables point to the SAME object in memory (like === in JS).
     * But we want two different Ticket objects with id "TK-001" to
     * count as the same ticket — e.g. when deduplicating in a Set.
     *
     * The pattern below is the standard Java equals() recipe:
     * 1. Same object in memory? Return true immediately.
     * 2. Null or different class? Return false.
     * 3. Cast to Ticket and compare the field(s) that define identity.
     *
     * "instanceof" checks if an object is a particular type.
     * It's like JS's instanceof but used more carefully in Java.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;                    // same reference
        if (!(obj instanceof Ticket)) return false;      // null or wrong type
        Ticket other = (Ticket) obj;                     // safe cast
        return Objects.equals(this.id, other.id);        // compare by id
    }

    /**
     * hashCode() MUST agree with equals(). If two tickets are equal
     * (same id), they MUST return the same hash code. Otherwise,
     * HashMap and HashSet put them in different buckets and can never
     * find the match — your "equal" objects silently become duplicates.
     *
     * Objects.hash() is a convenience method that computes a hash
     * from whatever fields you pass it.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
