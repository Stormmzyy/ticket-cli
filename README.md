# OpsDesk Ticket CLI

OpsDesk is a command-line ticket management application written in plain Java with no frameworks or external dependencies. It supports creating support tickets and asset requests, assigning them to users, filtering by status, and persisting everything to a local file so tickets survive between sessions.

## Quick start

**Compile** (from the project root):

```
javac -d out src/com/opsdesk/warmup/*.java src/com/opsdesk/tickets/*.java
```

**Run the interactive CLI:**

```
java -cp out com.opsdesk.tickets.Main
```

**Run the automated service demo** (no terminal interaction needed):

```
java -cp out com.opsdesk.tickets.TicketServiceDemo
```

**Run the collections demo** (standalone exercise from Part 2):

```
java -cp out com.opsdesk.tickets.CollectionsDemo
```

## Class overview

### `com.opsdesk.tickets`

| Class | Job |
|---|---|
| `Main` | Entry point — wires TicketStore, TicketService, and TicketMenu together and starts the app. |
| `Ticket` | Base class for all ticket types with id, title, status, priority, and assignee. |
| `SupportTicket` | Ticket subclass that adds an urgency level (critical/normal/low). |
| `AssetRequest` | Ticket subclass that adds an asset type (laptop/monitor/software license). |
| `Assignable` | Interface promising `assignTo()` and `getAssignee()` — lets unrelated types be assignable. |
| `TicketStatus` | Enum of ticket states: OPEN, IN_PROGRESS, RESOLVED, CLOSED. |
| `TicketSummary` | Record — a shallowly immutable read-only snapshot of a ticket's id, title, and status. |
| `TicketService` | All business logic (create, find, assign, filter, sort). Knows nothing about I/O. |
| `TicketStore` | Reads and writes tickets to a plain-text file using `java.nio.file`. |
| `TicketMenu` | Interactive CLI menu — reads from Scanner, prints to System.out, delegates logic to TicketService. |
| `TicketNotFoundException` | Unchecked exception thrown when a ticket id doesn't exist. |
| `TicketServiceDemo` | Automated demo exercising TicketService with no terminal — prints PASS/FAIL for each case. |
| `CollectionsDemo` | Standalone exercise demonstrating List, Set, Map, Comparator, and equals/hashCode. |

### `com.opsdesk.warmup`

| Class | Job |
|---|---|
| `BankAccount` | Encapsulation exercise — private balance with deposit/withdraw validation. |
| `Warmup` | Entry point for warmup exercises (variables, loops, arrays, methods). |

## Data file

Tickets are saved to `tickets.txt` in the directory where you run the `java` command. The path is relative to your working directory, not the source tree — so if you `cd` somewhere else and run the app, it will look for (or create) `tickets.txt` there.

**Format:** one ticket per line, pipe-delimited:

```
type|id|title|status|priority|assignee|extra
support|TK-001|Login page broken|OPEN|1|alice|critical
asset|TK-002|New laptop request|IN_PROGRESS|2||laptop
general|TK-003|Office cleanup|CLOSED|3|bob|
```

**Why pipes instead of commas?** Ticket titles can contain commas (`"Email not sending, urgent"`). A proper CSV library would handle that with quoting, but pipe-delimited is simpler and avoids the need for a quoting/escaping layer. Pipe characters are rejected at input so they can never appear inside a field.

If the file doesn't exist on startup (first run), the app starts with an empty list. If a line is corrupt or has the wrong number of fields, it is skipped with a warning — one bad line doesn't destroy the rest of your data.

## Design decisions

**Why does TicketService have no I/O?**
TicketService knows nothing about Scanner, System.out, or files. This means you can test it without a terminal (TicketServiceDemo proves this), swap the CLI for a web UI without changing the service, and keep each class focused on one job. TicketMenu handles user interaction, TicketStore handles file persistence, and TicketService handles logic.

**Why does `findTicket` return Optional while `getTicket` throws?**
They serve different callers. `findTicket` returns `Optional<Ticket>` for callers who want to handle the missing case gracefully (check `.isPresent()`, use `.orElse()`). `getTicket` throws `TicketNotFoundException` for callers who consider a missing ticket an error — it's a convenience wrapper that calls `findTicket` internally.

**Why is TicketNotFoundException unchecked?**
A missing ticket id is a user input error, not something every caller should be forced to handle. Making it checked (extends Exception) would force try/catch on every call site, which is noisy. As an unchecked exception (extends RuntimeException), we catch it in one place — the CLI menu loop — and show a friendly message. The service code stays clean.

**Why are records only shallowly immutable?**
Java records make their fields `final`, so you can't reassign them after construction. But if a field holds a mutable object (like a `List`), anyone with a reference can still modify the contents of that list. True deep immutability requires the field types themselves to be immutable. `TicketSummary` is safe because all its fields are `String` and `TicketStatus`, which are themselves immutable.

## Design note: composition vs inheritance

The `Assignable` interface is a good use of abstraction, but the inheritance hierarchy (`SupportTicket extends Ticket`, `AssetRequest extends Ticket`) could become a problem. If we later need a ticket type that is both a support request and an asset request, we're stuck — Java only allows one parent class. The fix would be composition: give a plain `Ticket` fields like `AssetDetails` or `SupportDetails` so any ticket can carry any combination. We kept inheritance here because two subclasses is simple enough, but in a real system with many ticket types, composition would scale better.
