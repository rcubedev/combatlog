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

## Credits
This project is a port of [CombatLogX](https://github.com/SirBlobman/CombatLogX) by SirBlobman and contributors.
