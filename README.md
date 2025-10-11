# VoteBridge

A lightweight Fabric mod that bridges vote events from **NuVotifier / VoteListener** into Minecraft chat with full placeholder and color support.

## Why It Exists

The original VoteListener JSON configuration only supported `/tellraw`, which **does not natively process Placeholder API variables**.  
As a result, placeholders like `%votelistener:vote_count%` were shown literally in chat instead of expanding to the player’s actual vote total.

While `/tellform` could display color and placeholders, it didn’t forward messages to Discord bridges like Minecord or Dyno because it wasn’t treated as standard chat output.  
This created a limitation: you could have **placeholders or Discord visibility**, but not both.

VoteBridge was created to solve that problem by providing a mod-level command handler that:
1. Resolves Placeholder API variables (via **PB4 Placeholder API**).
2. Constructs a valid JSON `/tellraw` message.
3. Executes it server-side (so it’s visible to players and Discord bridges alike).

## What It Does

When a player votes through a registered site (e.g. MCTools.org), VoteListener runs this command:

```json
"execute as ${username} run voteannounce ${username} ${serviceName}"
```

The VoteBridge mod intercepts the `/voteannounce` command and:
1. Fetches the player context for placeholder expansion.
2. Resolves `%votelistener:vote_count%` using the PB4 Placeholder API.
3. Formats a clean, colored `/tellraw` message:
   ```
   dontflex voted on MCTools.org (25 total votes)
   ```
4. Executes it as a true `/tellraw`, ensuring proper color formatting and visibility through Discord chat relays.

## Dependencies

- **Fabric Loader** 0.16.14+
- **Minecraft** 1.21.9
- **Fabric API** 0.133.14+1.21.9
- **PB4 Placeholder API** 2.8.0+1.21.9

## Example Output

In Minecraft chat:
```
dontflex voted on MCTools.org (25 total votes)
```

In Discord (via Minecord bridge):
```
**dontflex** voted on MCTools.org (25 total votes)
```

## Limitations

- `/tellraw` cannot parse placeholders on its own — this mod bridges that gap.
- Only vote events routed through VoteListener will trigger the broadcast.
- Currently limited to a single formatted broadcast (no milestones or per-site variation).

## License

MIT License — feel free to fork or modify for personal or server use.
