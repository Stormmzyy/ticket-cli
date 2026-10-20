package com.opsdesk.tickets;

/**
 * Entry point for the OpsDesk ticket manager CLI.
 *
 * Main is deliberately tiny — it just wires things together and starts
 * the menu. The logic lives in TicketService, the I/O in TicketMenu.
 */
public class Main {
    public static void main(String[] args) {
        TicketService service = new TicketService();
        TicketMenu menu = new TicketMenu(service);
        menu.run();
    }
}
