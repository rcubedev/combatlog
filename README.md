# CombatLogX Port

A port of [CombatLogX](https://github.com/SirBlobman/CombatLogX) for Fabric/NeoForge 1.21.1 and 1.21.10.

CombatLogX helps prevent players from avoiding combat by disconnecting while fighting. This project ports CombatLogX to Fabric and NeoForge, bringing the same combat logging prevention to modded Minecraft servers.

## Features
* Combat tagging system
* Punish players that disconnect in combat (e.g. kill on combat log, keep NPC body online after combat logging)
* Configurable combat timers
* Modular expansion system for additional features
* Permission support via [`fabric-permission-api`](https://modrinth.com/mod/fabric-permissions-api) and NeoForge's built in permission API
* and more...

## Supported Platforms
| Minecraft Version | Fabric | NeoForge |
| --- | --- | --- |
| 1.21.1 | Supported | Supported |
| 1.21.10 | Supported | Supported |

## Expansions
CombatLogX has an expansion system that allows additional features to be added independently.

Official expansions:
**Action Bar**
Displays combat timer information above the player's hotbar

**Boss Bar**
Displays combat timer information on the top of the player's screen

**End Crystal**
Links End Crystal entities to their placer

## Installation

This mod is intended for dedicated servers only.

1. Install Fabric or NeoForge for a supported Minecraft version on your server
2. Download the latest release from the [Releases](https://github.com/rcubedev/combatlog/releases/latest) page
3. Place the JAR into your server's `mods` folder
4. Start the server

## How to Contribute
Contributions are always welcome!

## Local Development setup
Requirements:
* Java 21
* IDE such as IntelliJ IDEA (RECOMMENDED)

1. Clone to repository
```bash
git clone https://github.com/rcubedev/combatlog.git
cd combatlog
```
2. Open the project
Open your IDE and import the folder
3. Build the project or run a test environment

Run a test environment:
**In IntelliJ:**
Run via run configs

**Via terminal:**
```bash
# Replace LOADER with loader (fabric/neoforge), version with minecraft version (1.21.1/1.21.10)
# Windows
.\gradlew :test:LOADER:VERSION:runClient
.\gradlew :test:LOADER:VERSION:runServer

# Mac/Linux
./gradlew :test:LOADER:VERSION:runClient
./gradlew :test:LOADER:VERSION:runServer
```

**Build the project:**

```bash
# Windows
.\gradlew build

# Mac/Linux
./gradlew build
```


## Submitting Code Changes
1. Fork the repository
2. Create a new branch for your feature or bugfix (git checkout -b feat/my-custom-feature)
3. Commit your changes (git commit -m 'feat: Add some amazing feature')
4. Push to your branch (git push origin feat/my-custom-feature)
5. Open a PR (Pull Request) listing your changes

## Credits
This project is a port of [CombatLogX](https://github.com/SirBlobman/CombatLogX) by SirBlobman and contributors.
