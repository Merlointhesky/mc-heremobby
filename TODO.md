# HEREMOBBY
The purpose of this plugin will be to manage existing mobs and add to their loot table or allow creating custom ones with existing minecraft models.
It will also allow defining custom loot and a custom shop where randomly generated equipment and items will be available.
The plugin will use a custom currency "Kroin", so each item in the shop will cost an amount of kroins proportionate to its base value plus additional cost for enchants, if any.
Kroins can be earned by defeating ANY mob. This value can be configured (default, standard mob award 1 coin, bosses award 20 coins).

## Implementation:
- **heremobby info**: allows the player to view in a GUI (buttons and text based) information about mobs, their loot and the coins.
- **heremobby shop**: allows the player to view in a GUI (inventory based) items for sale for kroins. 
    - Display an icon to refresh the shop (Lever icon).
    - Display an icon that tells the player how many kroins they own.
    - Refresh logic: Picks items from categories defined in `shop.json` and prices from `prices.json`. (Fixed: Persistent between opens, cursed enchants skipped, expanded items like Mace/Turtle Helmet added, Netherite excluded).
- **heremobby reload**: reloads configs from the server (to help when making edits and adding custom mobs)

## Data Storage (JSON):
- **bank.json**: Persistent storage for each player's Kroin balance.
- **prices.json**: Define base Kroin values for materials (grouped by category) and enchantment costs (base value + level multiplier).
- **shop.json**: Defines shop behavior, enchant chances, max items, and active categories (weapons, armor, materials, custom).
- **custom_items/**: Folder for custom item definitions (name, material, enchants, `sellable` flag).
- **custom_mobs/**: Folder for new entities. Define base model, equipment, display name, and spawn conditions (biomes, time, light level, chance).
- **custom_bosses/**: Folder for boss entities. Define coordinates, respawn timer, base entity type, equipment, and loot.
- **boss_state.json**: Internal persistence for last death timestamps to ensure respawn timers survive server restarts.
- **standard mobs/bosses**: Override loot and Kroin rewards via JSON.

## Documentation:
- Provide documentation on how to override and edit default mobs in mobs and bosses folders.
- Provide documentation on how to create custom mobs and bosses.
- Provide documentation on the shop system, custom items, and price calculation (Base + Enchants).
- Provide examples of empty/default JSON files for all systems.