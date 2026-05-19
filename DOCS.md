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


## 5. Spellcasting & Poise System (Advanced)

HereMobby features a custom AI system for bosses.

### Spellcasting
Custom mobs and bosses can be configured with a list of spells. If a mob has spells, it will use a custom AI that targets the nearest player and cycles through its spells.

#### Available Spells
- `FLAMETHROWER`: A channeled fire attack.
- `THUNDERWAVE`: An AOE pushback and damage spell.
- `MAGE_HAND`: Immobilizes the target for several seconds.
- `LIGHTNING_BOLT`: Strikes the target with lightning.
- `RAIN_OF_FIRE`: Rains fireballs around the target.
- `GRAVITY_DROP`: Lifts the target and drops them.
- `VINE_WHIP`: Pulls the target towards the caster and deals damage.

### Examples

#### Custom Mob with Spells (`custom_mobs/fire_mage.json`)
```json
{
  "id": "fire_mage",
  "displayName": "§6Fire Mage",
  "baseType": "WITCH",
  "spells": ["FLAMETHROWER", "RAIN_OF_FIRE"],
  "kroinReward": 75,
  "spawnConditions": {
    "chance": 0.02,
    "biomes": ["DESERT", "NETHER_WASTES"],
    "time": "BOTH"
  }
}
```

#### Custom Boss with Spells (`custom_bosses/lich_king.json`)
```json
{
  "id": "lich_king",
  "displayName": "§b§lThe Lich King",
  "baseType": "WITHER_SKELETON",
  "scale": 2.5,
  "spells": ["MAGE_HAND", "LIGHTNING_BOLT", "THUNDERWAVE"],
  "location": {
    "world": "world",
    "x": 500,
    "y": 70,
    "z": -500
  },
  "respawnSeconds": 1800,
  "kroinReward": 5000
}
```

### The Poise System
Bosses can be interrupted while channeling a spell. If a player deals significant damage (threshold: **8.0** final damage), the boss will:
1.  Cancel its current spell.
2.  Enter a **Staggered** state (Slowness effect).
3.  Play a glass-breaking sound and particle effect.
