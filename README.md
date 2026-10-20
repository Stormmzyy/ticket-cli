# ticket-cli

A command-line ticket management application written in plain Java, built for Week 5 of the OpsDesk internship.

## Design note: composition vs inheritance

The `Assignable` interface is a good use of abstraction, but the *inheritance* hierarchy
(`SupportTicket extends Ticket`, `AssetRequest extends Ticket`) could become a problem.

If we later need a ticket type that is both a support request AND an asset request — say
"set up a new laptop with VPN access" — we're stuck, because Java only allows one parent class.
The fix would be **composition**: instead of `AssetRequest extends Ticket`, you'd give a plain
`Ticket` a field like `AssetDetails assetDetails` or `SupportDetails supportDetails`. Then any
ticket can carry any combination of details, without being locked into a single inheritance chain.

We kept inheritance here because two subclasses is simple enough, but in a real system with
five or ten ticket types, composition would scale better.
