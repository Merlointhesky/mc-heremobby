# HereMobby

A [Paper](https://papermc.io) Minecraft plugin for **mob management** — manage existing mobs, create custom ones with custom loot, and optionally earn "Kroins" via **HereShoppy** integration.

## Features

- **Optional Economy Integration**: If **HereShoppy** is installed, players earn Kroins for defeating mobs. Standard mobs award 2 Kroins, while bosses award 2000.
- **Custom Mobs**: Create new entity types with custom equipment, display names, scale, and specific spawn conditions (biomes, time, light level, chance).
- **Custom Bosses**: Define static boss entities with persistent respawn timers and custom scale that survive server restarts.
- **Standard Overrides**: Override rewards and loot tables for vanilla Minecraft mobs and bosses via JSON.
- **Information GUI**: View detailed information about mobs and loot via `/heremobby info`.

## Requirements

- Paper `1.21+` *(plugin `api-version: '1.21'`; built against Paper `1.21.4` API)*
- Java `21`
- **HereShoppy** (Optional, for economy features)

## Installation

1. Grab `HereMobby-1.3.0.jar` from Releases (or `./gradlew build` locally).
2. Drop the JAR in `plugins/`.
3. Restart the server.

## Building from Source

```bash
./gradlew build   # POSIX
```

```powershell
.\gradlew.bat build   # Windows PowerShell/cmd
```

The compiled plugin jar is written to:

```
build/libs/HereMobby-1.3.0.jar
```

## Recent Changes (v1.3.0)

- **Interactive Setup Wizard**: Spawns a dummy entity to allow in-game setup of custom mobs/bosses. Supports right-click to equip items directly from inventory, empty hand right-click to strip items, and interactive chat menus to set health, names, and rewards.
- **Version bump & clean build** — Bumped version to `1.3.0` following V.R.B conventions.

## Commands & Permissions

All commands can be run with `/hm` instead of `/heremobby`.

| Command | Description | Permission |
|---------|-------------|------------|
| `/heremobby info` (or `/hm info`) | Open the mob information GUI | `heremobby.use` |
| `/heremobby spawn <id>` (or `/hm spawn <id>`) | Spawn a custom mob or boss | `heremobby.admin` |
| `/heremobby setup <id> <baseType> <boss|mob>` (or `/hm setup ...`) | Begin setup wizard for a custom mob or boss | `heremobby.admin` |
| `/heremobby setupedit <action>` (or `/hm setupedit ...`) | Modify active setup session details (name/health/kroin/xp/save/cancel) | `heremobby.admin` |
| `/heremobby reload` (or `/hm reload`) | Reload configuration files | `heremobby.admin` |

| Permission | Description | Default |
|------------|-------------|---------|
| `heremobby.use` | Allows use of player commands | `true` |
| `heremobby.admin` | Allows use of admin commands | `op` |

## Configuration & Spells

Detailed documentation on configuration, custom mobs/bosses, and the spellcasting system can be found in [DOCS.md](DOCS.md).

### Data Files
- `boss_state.json`: Internal persistence for last death timestamps.
- `mobs.json` / `bosses.json`: Overrides for vanilla entities.
- `custom_items/`: Folder for custom item definitions.
- `custom_mobs/`: Folder for new entity definitions.
- `custom_bosses/`: Folder for static boss definitions.
- `mounts/`: Folder for custom rideable mounts configurations.
- `pets/`: Folder for custom companion pets configurations.
- `wild_animals/`: Folder for custom wild animals configurations.

## Version

Current release: **`1.3.0`**

## License

Licensed under [GNU GPLv3](LICENSE).
