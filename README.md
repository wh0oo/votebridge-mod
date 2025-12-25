# VoteBridge

A lightweight Fabric mod that bridges vote events from **NuVotifier / VoteListener** into Minecraft chat with full placeholder and color support.

## Why It Exists

votelistener comes with a placeholder that I could not get to display on the discord channel. 

VoteBridge was created to solve this problem by providing a mod-level command handler that:
1. Resolves Placeholder API variables (via **PB4 Placeholder API**).
2. Constructs a valid JSON `/tellraw` message.
3. Executes it as a real `/tellraw` command via the server console (so it’s visible to players and Discord bridges alike).

## What It Does

When a player votes through a registered site (e.g. MCTools.org, minecraft-server.net), VoteListener triggers the internal `voteannounce` command with the voting player and service name as arguments.

The VoteBridge mod handles the `/voteannounce` command and:
1. Fetches the player context for placeholder expansion.
2. Resolves `%votelistener:vote_count%` using the PB4 Placeholder API.
3. Formats a clean, colored `/tellraw` message:
   ```
   Steve voted on MCTools.org (25 total votes)
   ```
4. Executes the `/tellraw` command as the server console, ensuring proper color formatting and visibility through Discord chat relays.

> The `/voteannounce` command is intended for internal use by VoteListener and is not designed to be run manually by players.

## Dependencies

- **Fabric Loader** 0.18.2+
- **Minecraft** 1.21.11
- **votelistener** 1.1.0+1.21.x
- **Fabric API** 0.140.2+1.21.11
- **PB4 Placeholder API** latest 2.8.x for 1.21.x

## Example Output

In Minecraft chat:
```
Steve voted on MCTools.org (25 total votes)
```

In Discord (via Minecord bridge):
```
Steve voted on MCTools.org (25 total votes)
```

## License

MIT License
