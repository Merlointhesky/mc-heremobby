# HereMobby: Custom Spellcasting & AI Implementation Plan

**Objective:** Replace vanilla boss AI with a custom, Paper API-driven spellcasting system. This system will feature D&D-style magic mechanics and a skill-based interruption ("Poise") system.

---

## Phase 1: Core Architecture & Boss Wrapper Setup
Before creating custom spells, the plugin needs a structured way to track custom boss entities and manage their active casting states.

### Step 1.1: Create a Boss Wrapper
Enhance the existing class or set up a data structure or wrapper class to store the custom boss's runtime data. The most critical data points to track are:
*   The underlying Bukkit/Paper `Mob` entity.
*   A boolean flag indicating if the boss is currently `isChanneling` a continuous spell.
*   A reference to the active `BukkitTask` (if applicable) so that ongoing spells can be tracked and canceled.

### Step 1.2: Clear Vanilla AI on Spawn
When spawning a custom boss, ensure it acts as a blank canvas by stripping away its default behaviors.
*   **Suggested Action:** Use Paper's native `MobGoals` API to remove default `TARGET`, `MELEE_ATTACK`, and `RANGED_ATTACK` goals immediately after the entity is spawned.

---

## Phase 2: Building the Spell Goal Framework
Utilize Paper's native Goal interface to define the custom attacks in a modular, update-proof way.

### Step 2.1: Create an Abstract Spell Goal Structure
Create a base class or interface that all future spells (e.g., Flamethrower, Thunderwave) will implement or extend.
*   **Suggested Action:** Implement Paper's `Goal<Mob>` interface.
*   Ensure the structure defines methods for:
    *   Evaluating when the spell should activate (handling cooldowns, distance checks, phase transitions).
    *   Executing the actual spell logic (the "start" or "tick" methods).

---

## Phase 3: Implementing the Magic Mechanics
Create utility methods to handle the complex spatial and physics manipulations required for D&D-style spells, relying heavily on Vanilla assets (Particles, Sounds, Velocity).

### Step 3.1: The Velocity Engine (Pushes & Pulls)
For displacement spells like *Thunderwave* or *Lightning Lure*.
*   **Suggested Action:** Calculate the vector between the boss and the target player. Normalize this vector, apply the desired force multiplier, and apply it to the player's velocity.
*   **Crucial Detail:** To counter ground friction, always apply a slight upward modification to the Y-axis vector before pushing the player horizontally. Consider temporarily mitigating fall damage for the target to prevent accidental one-shot kills.

### Step 3.2: The Levitation Engine (Immobilization)
For immobilization spells like *Hold Person* or *Mage Hand*, where standard levitation potion effects are insufficient.
*   **Suggested Action:** Spawn an invisible, gravity-less `ArmorStand` at the target's location and force the target to mount it as a passenger. 
*   **Crucial Detail:** Because the player is mounted to a static entity, their movement keys are disabled. Schedule a task to remove the `ArmorStand` after the spell duration. You will also need to intercept and cancel dismount events so the player cannot manually exit the trap early.

---

## Phase 4: The Interruption & "Poise" System
Implement a system that allows players to break a boss's channeling state through aggressive, calculated attacks.

### Step 4.1: Intercept Damage Events
Create a system to listen for physical attacks against the custom boss.
*   **Suggested Action:** Set up an event listener for entity damage. When a boss is struck, verify its identity and check the `isChanneling` flag established in Phase 1.

### Step 4.2: Define and Execute the Interruption Logic
Determine what constitutes a heavy enough blow to break concentration, and penalize the boss for it.
*   **Suggested Action:** Define thresholds for interruption, such as high raw damage values or the presence of specific enchantments (like Knockback).
*   If the threshold is met:
    1.  Cancel the active `BukkitTask` associated with the spell.
    2.  Reset the boss's channeling flag.
    3.  Provide immediate visual and audio feedback (e.g., a glass breaking sound and critical hit particles).
    4.  *(Optional)* Apply a brief negative status effect, such as Slowness, to simulate a "Staggered" state.

---

## Phase 5: Assembly (Example: Flamethrower)
To bring it all together, implement a set of spells to be used by the server admin out of the box:
*   **Suggested Action:** Create a series of abilities like "flamethrower", "rain of fire", "lightning bolt", "energy ball", "vine whip", "water cannon", "sand rain","gravity drop". Suggest more spells that could be implemented based on d&d spellbook.

## Documentation:
- Provide documentation on how to override and edit default mobs in mobs and bosses folders.
- Provide documentation on how to create custom mobs and bosses.
- Provide documentation on the shop system, custom items, and price calculation (Base + Enchants).
- Provide examples of empty/default JSON files for all systems.