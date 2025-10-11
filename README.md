# VoteBridge

One small Fabric mod to announce votes with a single message that:
- resolves `%votelistener:vote_count%` in player context (via PB4 Placeholder API),
- shows **colored** text in-game,
- and is relayed to Discord bridges (because it's emitted as **vanilla `/tellraw`**).

## Build
- Java 21
- Gradle 8.x
```
./gradlew build
```
Jar will be in `build/libs/`.

## Install
Drop the jar on the server alongside:
- Fabric Loader
- Fabric API
- PB4 Placeholder API
- VoteListener

## Configure VoteListener
In `config/votelistener/votelistener.json`:
```json
{
  "commands": [],
  "onlineCommands": [
    "voteannounce ${username} ${serviceName}"
  ]
}
```

## Command
```
/voteannounce <player> <service...>
```
Console/op only. VoteListener will call it automatically for real votes.
