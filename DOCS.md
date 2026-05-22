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
  "maxHealth": 40.0,
  "defense": 0.1,
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
Bosses are static entities that respawn at a specific location and display a **Boss Health Bar** at the top of the screen when players are nearby.
```json
{
  "id": "giant_zombie",
  "displayName": "§4The Colossus",
  "baseType": "ZOMBIE",
  "scale": 5.0,
  "maxHealth": 500.0,
  "defense": 0.25,
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


## 4. Health and Defense

Both custom mobs and bosses support custom health and defense values.

- `maxHealth`: Sets the maximum health of the entity.
- `defense`: Sets damage reduction as a percentage (e.g., `0.1` is 10% reduction, `0.5` is 50% reduction).


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
- `WATER_CANNON`: A stream of water that deals damage and knocks back.
- `ENERGY_BALL`: A slow-moving projectile that explodes on impact.
- `RAY_ATTACK`: A continuous beam that tracks the target.
- `CHAIN_LIGHTNING`: Lightning that jumps between nearby players, dealing reduced damage each jump.
- `TELEPORTATION`: Boss teleports to a random location near the target.
- `SAND_RAIN`: Rains sand blocks on nearby players.
- `ROCK_BLAST`: Shoots a rock that shatters into flint shards on impact.

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

## 6. Custom Equipment and Loot Drops

To make a boss drop specific equipment, you should define it in the `customLoot` section.

### Example: Boss with guaranteed drops
```json
{
  "id": "king_skeleton",
  "displayName": "§eSkeleton King",
  "baseType": "SKELETON",
  "equipment": {
    "mainHand": "GOLDEN_SWORD",
    "helmet": "GOLDEN_HELMET"
  },
  "customLoot": [
    { "material": "GOLDEN_SWORD", "chance": 1.0, "minAmount": 1, "maxAmount": 1 },
    { "material": "GOLDEN_HELMET", "chance": 0.5, "minAmount": 1, "maxAmount": 1 },
    { "material": "DIAMOND", "chance": 0.1, "minAmount": 1, "maxAmount": 3 }
  ]
}
```
By adding the equipment materials to `customLoot` with a `1.0` chance, you ensure the boss always drops its "signature" gear.

## 7. Custom Vehicles, Pets, and Wild Animals Framework

HereMobby includes a comprehensive YAML-driven custom items framework to dynamically load mounts (vehicles), pets, and wild animals. Configurations are loaded automatically from subdirectories (`mounts/`, `pets/`, and `wild_animals/`) within the plugin folder.

Each custom item registered automatically generates its corresponding item stack, shaped crafting recipe, and custom spawning, riding, and deconstruction lifecycle.

---

### A. Custom Vehicles (Mounts)
Vehicles are registered with the category `MOUNT` and are spawned when players place their corresponding item. 

The modern **ItemDisplay** backend system (used for the rideable rocket) provides high-performance 3D visual models and a multiplayer/passenger mechanism:
- **Spawning:** Places an `ItemDisplay` base entity centered on and facing away from the player, along with 2 invisible, small, synchronized seat markers (`ArmorStand`s) for the driver and passenger.
- **Riding Priority:** Players who right-click the vehicle are assigned to empty seats. The first player to interact gains control as the driver; subsequent players mount as passengers.
- **Steering:** The driver's real-time look direction (yaw and pitch) steers the vehicle, supporting full 3D aerial flight.
- **Deconstruction:** Punching any part of the vehicle cancels standard damage mechanics, drops the custom vehicle item naturally, and deletes all linked seat/visual components safely.

#### Configuration Example (`mounts/rideable_rocket.yml`)
```yaml
display_name: "&6X-52 Nether-Rocket"
material: FIREWORK_ROCKET
custom_model_data: 1001
category: MOUNT
model_id: rideable_rocket
recipe:
  shape:
    - "GSS"
    - "RRR"
    - "RRR"
  ingredients:
    G: GLASS_PANE
    S: SADDLE
    R: FIREWORK_ROCKET
```

---

### B. Custom Pets
Pets are registered with the category `PET`. When a player spawns a pet, the entity is tagged and bound to the owner's UUID, automatically following them around.

#### Configuration Example (`pets/helper_drone.yml`)
```yaml
display_name: "&bPersonal Helper Drone"
material: PHANTOM_MEMBRANE
custom_model_data: 2001
category: PET
model_id: helper_drone
recipe:
  shape:
    - "RDR"
    - "ICI"
    - "RIR"
  ingredients:
    R: REDSTONE
    D: DIAMOND
    I: IRON_INGOT
    C: COMPARATOR
```

---

### C. Custom Wild Animals
Wild animals are registered with the category `WILD`. Spawning a wild animal creates a custom entity with standard passive AI that wanders the environment naturally.

#### Configuration Example (`wild_animals/golden_sheep.yml`)
```yaml
display_name: "&eGolden Sheep"
material: YELLOW_WOOL
custom_model_data: 3001
category: WILD
model_id: golden_sheep
recipe:
  shape:
    - "GGG"
    - "GWG"
    - "GGG"
  ingredients:
    G: GOLD_INGOT
    W: WHITE_WOOL
```

