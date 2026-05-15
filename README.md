# HereMobby

A [Paper](https://papermc.io) Minecraft plugin for **mob management** — manage existing mobs, create custom ones with custom loot, and earn a custom currency "Kroin" to spend in a dynamic shop.

## Features

- **Custom Currency (Kroin)**: Earn Kroins by defeating any mob. Standard mobs give 1 Kroin, while bosses give 20 by default. Values are fully configurable.
- **Dynamic Shop System**:
  - Open via `/heremobby shop`.
  - Randomly generated items based on categories in `shop.json` and prices in `prices.json`.
  - Supports a wide variety of equipment including **Maces**, **Turtle Shell Helmets**, and various armor tiers (Leather to Diamond).
  - **Netherite equipment is excluded** from the random generator for balance.
  - Enchanted items are generated automatically (**cursed enchantments are always excluded**).
  - Refresh logic: Use a Lever icon to refresh the shop for a cost. The shop remains persistent between opens until manually refreshed or reloaded.
  - Balance display: View your current Kroin balance directly in the GUI.
- **Custom Mobs**: Create new entity types with custom equipment, display names, and specific spawn conditions (biomes, time, light level, chance).
- **Custom Bosses**: Define static boss entities with persistent respawn timers that survive server restarts.
- **Standard Overrides**: Override rewards and loot tables for vanilla Minecraft mobs and bosses via JSON.
- **Information GUI**: View detailed information about mobs, loot, and your currency via `/heremobby info`.

## Requirements

- Paper `1.21+` *(plugin `api-version: '1.21'`; built against Paper `1.21.4` API)*
- Java `21`

## Installation

1. Grab `HereMobby-1.0.0.jar` from Releases (or `./gradlew build` locally).
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
build/libs/HereMobby-1.0.0.jar
```

## Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/heremobby info` | Open the mob information GUI | `heremobby.use` |
| `/heremobby shop` | Open the Kroin shop GUI | `heremobby.use` |
| `/heremobby reload` | Reload configuration files | `heremobby.admin` |

| Permission | Description | Default |
|------------|-------------|---------|
| `heremobby.use` | Allows use of player commands | `true` |
| `heremobby.admin` | Allows use of admin commands | `op` |

## Configuration (JSON)

Detailed documentation on how to override default mobs and create custom ones can be found in the `examples/` directory and via the plugin's internal GUIs.

### Data Files
- `bank.json`: Player balances.
- `prices.json`: Material values and enchant costs.
- `shop.json`: Shop behavior and categories.
- `mobs.json` / `bosses.json`: Overrides for vanilla entities.
- `custom_items/`: Folder for custom item definitions.
- `custom_mobs/`: Folder for new entity definitions.
- `custom_bosses/`: Folder for static boss definitions.

## Version

Current release: **`1.0.0`**

## License

Licensed under [GNU GPLv3](LICENSE).
