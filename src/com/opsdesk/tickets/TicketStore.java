package com.opsdesk.tickets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes tickets to a plain-text file.
 *
 * WHY java.nio.file INSTEAD OF java.io.File?
 * The old File API has a dangerous design: methods like File.delete()
 * return false on failure instead of throwing an exception. You can
 * silently lose data and never know. The newer java.nio.file.Files
 * throws IOException on failure, so errors are impossible to ignore.
 * It also provides convenient one-liners like Files.readAllLines()
 * and Files.write() that the old API doesn't have.
 *
 * WHY A SEPARATE CLASS?
 * TicketService is deliberately I/O-free — it doesn't know about files,
 * terminals, or networks. Keeping persistence in its own class means:
 * 1. TicketService stays testable without touching the filesystem.
 * 2. We could swap CSV for a database later without changing the service.
 * 3. Each class has one job (Single Responsibility Principle again).
 *
 * FILE FORMAT (pipe-delimited, one ticket per line):
 *   type|id|title|status|priority|assignee|extra
 *
 * We use pipes instead of commas because ticket titles might contain
 * commas. A real app would use a proper CSV library with quoting, but
 * pipes are simpler and good enough for learning.
 */
public class TicketStore {

    private final Path filePath;

    public TicketStore(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads tickets from the file. Returns an empty list if the file
     * doesn't exist yet (first run). Skips corrupt lines with a warning
     * instead of crashing — one bad line shouldn't destroy all your data.
     */
    public List<Ticket> load() {
        List<Ticket> tickets = new ArrayList<>();

        // First run: no file yet, that's fine — start with nothing
        if (!Files.exists(filePath)) {
            return tickets;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            System.err.println("Warning: could not read " + filePath + " — starting empty. (" + e.getMessage() + ")");
            return tickets;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;  // skip blank lines
            }

            // Split on pipe — we expect exactly 7 fields.
            // The -1 limit is critical: without it, Java's split() silently
            // drops trailing empty strings. A line like "general|TK-001|Title|OPEN|3||"
            // would produce only 6 parts instead of 7, and the ticket would be
            // skipped as corrupt. The -1 tells split to keep every field.
            String[] parts = line.split("\\|", -1);
            if (parts.length != 7) {
                System.err.println("Warning: skipping corrupt line " + (i + 1)
                        + " (expected 7 fields, got " + parts.length + ")");
                continue;
            }

            try {
                Ticket ticket = parseLine(parts);
                tickets.add(ticket);
            } catch (IllegalArgumentException e) {
                // Bad enum value, bad integer, etc. — skip this line
                System.err.println("Warning: skipping corrupt line " + (i + 1)
                        + " (" + e.getMessage() + ")");
            }
        }

        return tickets;
    }

    /**
     * Parses one pipe-delimited line into a Ticket (or subclass).
     *
     * Fields: type|id|title|status|priority|assignee|extra
     *
     * Throws IllegalArgumentException if any field can't be parsed,
     * which the caller catches to skip the line gracefully.
     */
    private Ticket parseLine(String[] parts) {
        String type     = parts[0].trim();
        String id       = parts[1].trim();
        String title    = parts[2].trim();
        TicketStatus status = TicketStatus.valueOf(parts[3].trim());
        int priority    = Integer.parseInt(parts[4].trim());
        String assignee = parts[5].trim();
        String extra    = parts[6].trim();

        Ticket ticket;
        if ("support".equalsIgnoreCase(type)) {
            ticket = new SupportTicket(id, title, status, priority, extra);
        } else if ("asset".equalsIgnoreCase(type)) {
            ticket = new AssetRequest(id, title, status, priority, extra);
        } else {
            ticket = new Ticket(id, title, status, priority);
        }

        // Restore the assignee (empty string in the file means unassigned)
        if (!assignee.isEmpty()) {
            ticket.assignTo(assignee);
        }

        return ticket;
    }

    /**
     * Saves all tickets to the file, overwriting whatever was there.
     *
     * WHY OVERWRITE THE WHOLE FILE?
     * Appending sounds simpler, but then deletes and edits become
     * impossible. Writing the full list every time is the simplest
     * approach that handles all operations. For a few hundred tickets
     * this is instant; a database would be better for thousands.
     */
    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();

        for (Ticket t : tickets) {
            String type;
            String extra;

            // Determine the type and extra field based on the subclass
            if (t instanceof SupportTicket) {
                type = "support";
                extra = ((SupportTicket) t).getUrgencyLevel();
            } else if (t instanceof AssetRequest) {
                type = "asset";
                extra = ((AssetRequest) t).getAssetType();
            } else {
                type = "general";
                extra = "";
            }

            String assignee = t.getAssignee() != null ? t.getAssignee() : "";

            // Build the pipe-delimited line
            String line = String.join("|",
                    type,
                    t.getId(),
                    t.getTitle(),
                    t.getStatus().name(),
                    String.valueOf(t.getPriority()),
                    assignee,
                    extra
            );
            lines.add(line);
        }

        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            System.err.println("Error: could not save tickets to " + filePath
                    + " (" + e.getMessage() + ")");
        }
    }
}
