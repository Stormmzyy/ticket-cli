package com.opsdesk.tickets;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Entry point for the OpsDesk ticket manager CLI.
 *
 * Main is deliberately tiny — it just wires things together and starts
 * the menu. The logic lives in TicketService, the I/O in TicketMenu,
 * and file persistence in TicketStore.
 */
public class Main {
    public static void main(String[] args) {
        // "tickets.txt" is relative to wherever you run the java command.
        // Paths.get() is the java.nio.file way to build a Path object.
        // Named .txt not .csv — the format is pipe-delimited, not comma-separated.
        Path dataFile = Paths.get("tickets.txt");

        TicketStore store = new TicketStore(dataFile);
        TicketService service = new TicketService();

        // Load any previously saved tickets before showing the menu
        List<Ticket> saved = store.load();
        if (!saved.isEmpty()) {
            service.loadTickets(saved);
            System.out.println("Loaded " + saved.size() + " ticket(s) from " + dataFile);
        }

        TicketMenu menu = new TicketMenu(service, store);
        menu.run();
    }
}
