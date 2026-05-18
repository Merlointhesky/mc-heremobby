# HereMobby Documentation

## 1. Overriding Default Mobs & Bosses

To change the behavior, loot, or rewards of vanilla Minecraft entities, use the `mobs.json` and `bosses.json` files in the plugin's data folder.

### Structure (`mobs.json` & `bosses.json`)
```json
{
  "overrides": {
    "ZOMBIE": {
      "kroinReward": 5,
      "customLoot": [
        { "material": "IRON_INGOT", "chance": 0.1, "minAmount": 1, "maxAmount": 3 }
      ]
    },
    "ENDER_DRAGON": {
      "kroinReward": 5000,
      "customLoot": [
        { "material": "DRAGON_BREATH", "chance": 1.0, "minAmount": 5, "maxAmount": 10 }
      ]
    }
  }
}
```

## 2. Creating Custom Mobs & Bosses

Custom entities are defined in the `custom_mobs/` and `custom_bosses/` directories as individual JSON files.

### Custom Mob Example (`custom_mobs/skeleton_warrior.json`)
```json
{
  "id": "skeleton_warrior",
  "displayName": "§cSkeleton Warrior",
  "baseType": "SKELETON",
  "equipment": {
    "mainHand": "IRON_SWORD",
    "helmet": "IRON_HELMET"
  },
  "scale": 1.2,
  "kroinReward": 50,
  "spawnConditions": {
    "chance": 0.05,
    "biomes": ["PLAINS", "FOREST"],
    "time": "NIGHT",
    "minLight": 0,
    "maxLight": 7
  }
}
```

### Custom Boss Example (`custom_bosses/giant_zombie.json`)
Bosses are static entities that respawn at a specific location.
```json
{
  "id": "giant_zombie",
  "displayName": "§4The Colossus",
  "baseType": "ZOMBIE",
  "scale": 5.0,
  "kroinReward": 1000,
  "location": {
    "world": "world",
    "x": 100,
    "y": 64,
    "z": 100
  },
  "respawnSeconds": 300
}
```

## 3. Shop System & Custom Items

HereMobby integrates with **HereShoppy** for economy.

### Custom Item Definition (`custom_items/god_sword.json`)
```json
{
  "id": "god_sword",
  "material": "NETHERITE_SWORD",
  "displayName": "§6God Sword",
  "lore": ["§7A blade forged in the heavens."],
  "enchantments": {
    "SHARPNESS": 10,
    "FIRE_ASPECT": 2
  }
}
```

### Price Calculation
When selling custom items via the Shop GUI (if enabled), the price is calculated as:
`Base Price + (Enchantment Levels * Multiplier)`

## 4. Default JSON Examples

### `mobs.json` / `bosses.json` (Empty Template)
```json
{
  "overrides": {}
}
```

### `shop.json` (Default Structure)
```json
{
  "categories": [
    {
      "id": "weapons",
      "displayName": "Weapons",
      "icon": "DIAMOND_SWORD",
      "items": ["god_sword"]
    }
  ]
}
```

## 5. Spellcasting & Poise System (Advanced)

HereMobby features a custom AI system for bosses.

### Spellcasting
Bosses have access to custom spells like:
- **Flamethrower**: A channeled fire attack.
- **Thunderwave**: An AOE pushback and damage spell.
- **Mage Hand**: Immobilizes the target for several seconds.

### The Poise System
Bosses can be interrupted while channeling a spell. If a player deals significant damage (threshold: **8.0** final damage), the boss will:
1.  Cancel its current spell.
2.  Enter a **Staggered** state (Slowness effect).
3.  Play a glass-breaking sound and particle effect.
