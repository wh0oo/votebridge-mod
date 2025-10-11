# VoteBridge

A lightweight Fabric mod that bridges vote events from **NuVotifier / VoteListener** into Minecraft chat with full placeholder and color support.

## Why It Exists

VoteBridge was created to solve the problem by providing a mod-level command handler that:
1. Resolves Placeholder API variables (via **PB4 Placeholder API**).
2. Constructs a valid JSON `/tellraw` message.
3. Executes it server-side (so it’s visible to players and Discord bridges alike).

## What It Does

When a player votes through a registered site (e.g. MCTools.org, minecraft-server.net), VoteListener runs this command:

```json
"execute as ${username} run voteannounce ${username} ${serviceName}"
```

The VoteBridge mod intercepts the `/voteannounce` command and:
1. Fetches the player context for placeholder expansion.
2. Resolves `%votelistener:vote_count%` using the PB4 Placeholder API.
3. Formats a clean, colored `/tellraw` message:
   ```
   player-name-here voted on MCTools.org (25 total votes)
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
player-here voted on MCTools.org (25 total votes)
```

In Discord (via Minecord bridge):
```
player-here voted on MCTools.org (25 total votes)
```
## License

MIT License — feel free to fork or modify for personal or server use.
