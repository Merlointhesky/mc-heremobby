package com.heremobby.heremobby.mob;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.model.CustomBoss;
import com.heremobby.heremobby.model.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.entity.ai.GoalType;
import com.heremobby.heremobby.mob.goal.*;
import org.bukkit.entity.Mob;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MobManager {
    private final HereMobbyPlugin plugin;
    private final DataManager dataManager;
    private final NamespacedKey customMobKey;
    private final NamespacedKey customBossKey;
    private final Map<UUID, ActiveBoss> activeBosses = new HashMap<>();

    public MobManager(HereMobbyPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.customMobKey = new NamespacedKey(plugin, "custom_mob");
        this.customBossKey = new NamespacedKey(plugin, "custom_boss");
        
        startBossRespawnTask();
        startBossBarUpdateTask();

        // Scan loaded entities on startup to restore AI and BossBars
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                registerExistingEntity(entity);
            }
        }
    }

    public void registerExistingEntity(Entity entity) {
        if (!(entity instanceof Mob mob)) return;
        
        ActiveBoss existing = activeBosses.get(mob.getUniqueId());
        if (existing != null) {
            if (existing.getEntity() == mob && mob.isValid()) {
                return; // Already registered and valid
            }
            removeActiveBoss(mob.getUniqueId());
        }

        // Check if it's a custom boss
        var bossConfig = getCustomBossConfig(mob);
        if (bossConfig.isPresent()) {
            CustomBoss config = bossConfig.get();
            applyEquipment(mob, config.getEquipment());
            applyScale(mob, config.getScale());
            applyHealth(mob, config.getMaxHealth());
            applyEntityProperties(mob);
            setupCustomBossGoals(mob, config);
            return;
        }

        // Check if it's a custom mob
        var mobConfig = getCustomMobConfig(mob);
        if (mobConfig.isPresent()) {
            CustomMob config = mobConfig.get();
            applyEquipment(mob, config.getEquipment());
            applyScale(mob, config.getScale());
            applyHealth(mob, config.getMaxHealth());
            applyEntityProperties(mob);
            setupCustomMobGoals(mob, config);
        }
    }

    public void setupCustomMobGoals(Mob mob, CustomMob config) {
        // Clear ALL vanilla AI to ensure custom abilities/behavior take precedence
        Bukkit.getMobGoals().removeAllGoals(mob);
        
        // Ensure passive entities have attack damage attribute so they can actually hurt players
        AttributeInstance attackAttr = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttr != null && attackAttr.getBaseValue() == 0) {
            attackAttr.setBaseValue(2.0); // Give passive mobs some bite
        }

        ActiveBoss activeCaster = new ActiveBoss(mob);
        activeBosses.put(mob.getUniqueId(), activeCaster);

        var goals = Bukkit.getMobGoals();
        goals.addGoal(mob, 1, new TargetNearestPlayerGoal(plugin, mob));

        int priority = 2;
        if (config.getSpells() != null && !config.getSpells().isEmpty()) {
            for (String spellName : config.getSpells()) {
                addSpellGoal(mob, activeCaster, spellName, priority);
                priority++;
            }
        }

        goals.addGoal(mob, priority, new MoveToTargetGoal(plugin, mob, 1.2));
        goals.addGoal(mob, priority + 1, new MeleeAttackGoal(plugin, mob));
    }

    public void setupCustomBossGoals(Mob mob, CustomBoss config) {
        // Clear Vanilla AI and register as ActiveBoss
        Bukkit.getMobGoals().removeAllGoals(mob);

        if ("storm_archmage".equals(config.getId()) || "overworld_wither".equals(config.getId())) {
            mob.setGravity(false);
        }

        // Ensure attributes are set for combat (base 10.0 for unarmed, 20.0 for armed)
        AttributeInstance attackAttr = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttr != null) {
            EntityEquipment ee = mob.getEquipment();
            boolean hasWeapon = ee != null && ee.getItemInMainHand() != null && ee.getItemInMainHand().getType() != org.bukkit.Material.AIR;
            if (hasWeapon) {
                attackAttr.setBaseValue(20.0);
            } else {
                attackAttr.setBaseValue(10.0);
            }
        }

        ActiveBoss activeBoss = new ActiveBoss(mob);
        activeBosses.put(mob.getUniqueId(), activeBoss);

        // Create BossBar for bosses
        BossBar bossBar = Bukkit.createBossBar(
            config.getDisplayName() != null ? config.getDisplayName() : "Boss",
            BarColor.RED,
            BarStyle.SOLID
        );
        activeBoss.setBossBar(bossBar);
        
        // Assign custom goals for the boss
        var goals = Bukkit.getMobGoals();
        goals.addGoal(mob, 1, new TargetNearestPlayerGoal(plugin, mob));

        int priority = 2;
        if (config.getSpells() != null) {
            for (String spellName : config.getSpells()) {
                addSpellGoal(mob, activeBoss, spellName, priority);
                priority++;
            }
        } else {
            // Default spells if none specified (for backward compatibility or convenience)
            addSpellGoal(mob, activeBoss, "FLAMETHROWER", priority++);
            addSpellGoal(mob, activeBoss, "THUNDERWAVE", priority++);
            addSpellGoal(mob, activeBoss, "MAGE_HAND", priority++);
        }

        goals.addGoal(mob, priority, new MoveToTargetGoal(plugin, mob, 1.2));
        if ("shoveler".equals(config.getId())) {
            goals.addGoal(mob, priority + 1, new ShovelerMeleeGoal(plugin, mob));
        } else {
            goals.addGoal(mob, priority + 1, new MeleeAttackGoal(plugin, mob));
        }
    }

    public void spawnCustomMob(CustomMob config, Location loc) {
        EntityType type = EntityType.valueOf(config.getBaseType().toUpperCase());
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
        
        if (config.getDisplayName() != null) {
            entity.setCustomName(config.getDisplayName());
            entity.setCustomNameVisible(true);
        }

        applyEquipment(entity, config.getEquipment());
        applyScale(entity, config.getScale());
        applyHealth(entity, config.getMaxHealth());
        applyEntityProperties(entity);
        entity.getPersistentDataContainer().set(customMobKey, PersistentDataType.STRING, config.getId());

        if (entity instanceof Mob mob) {
            setupCustomMobGoals(mob, config);
        }
    }

    public void spawnCustomBoss(CustomBoss config) {
        World world = Bukkit.getWorld(config.getLocation().getWorld());
        if (world == null) return;

        Location loc = new Location(world, config.getLocation().getX(), config.getLocation().getY(), config.getLocation().getZ());
        spawnCustomBoss(config, loc);
    }

    public void spawnCustomBoss(CustomBoss config, Location loc) {
        EntityType type = EntityType.valueOf(config.getBaseType().toUpperCase());
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, type);

        if (config.getDisplayName() != null) {
            entity.setCustomName(config.getDisplayName());
            entity.setCustomNameVisible(true);
        }

        applyEquipment(entity, config.getEquipment());
        applyScale(entity, config.getScale());
        applyHealth(entity, config.getMaxHealth());
        applyEntityProperties(entity);
        entity.getPersistentDataContainer().set(customBossKey, PersistentDataType.STRING, config.getId());

        if (entity instanceof Mob mob) {
            setupCustomBossGoals(mob, config);
        }
    }

    private void addSpellGoal(Mob mob, ActiveBoss activeBoss, String spellName, int priority) {
        var goals = Bukkit.getMobGoals();
        switch (spellName.toUpperCase()) {
            case "FLAMETHROWER" -> goals.addGoal(mob, priority, new FlamethrowerGoal(plugin, activeBoss));
            case "THUNDERWAVE" -> goals.addGoal(mob, priority, new ThunderwaveGoal(plugin, activeBoss));
            case "MAGE_HAND" -> goals.addGoal(mob, priority, new MageHandGoal(plugin, activeBoss));
            case "LIGHTNING_BOLT" -> goals.addGoal(mob, priority, new LightningBoltGoal(plugin, activeBoss));
            case "RAIN_OF_FIRE" -> goals.addGoal(mob, priority, new RainOfFireGoal(plugin, activeBoss));
            case "GRAVITY_DROP" -> goals.addGoal(mob, priority, new GravityDropGoal(plugin, activeBoss));
            case "VINE_WHIP" -> goals.addGoal(mob, priority, new VineWhipGoal(plugin, activeBoss));
            case "WATER_CANNON" -> goals.addGoal(mob, priority, new WaterCannonGoal(plugin, activeBoss));
            case "ENERGY_BALL" -> goals.addGoal(mob, priority, new EnergyBallGoal(plugin, activeBoss));
            case "RAY_ATTACK" -> goals.addGoal(mob, priority, new RayAttackGoal(plugin, activeBoss));
            case "CHAIN_LIGHTNING" -> goals.addGoal(mob, priority, new ChainLightningGoal(plugin, activeBoss));
            case "TELEPORTATION" -> goals.addGoal(mob, priority, new TeleportationGoal(plugin, activeBoss));
            case "SAND_RAIN" -> goals.addGoal(mob, priority, new SandRainGoal(plugin, activeBoss));
            case "ROCK_BLAST" -> goals.addGoal(mob, priority, new RockBlastGoal(plugin, activeBoss));
            case "TERRAIN_CYCLE" -> goals.addGoal(mob, priority, new TerrainCycleGoal(plugin, activeBoss));
            case "SUMMON_BABY_ZOMBIES" -> goals.addGoal(mob, priority, new SummonBabyZombiesGoal(plugin, activeBoss));
            case "THROW_TNT" -> goals.addGoal(mob, priority, new ThrowTNTGoal(plugin, activeBoss));
            case "SUMMON_SPIDERS" -> goals.addGoal(mob, priority, new SummonSpidersGoal(plugin, activeBoss));
            case "THROW_AXE" -> goals.addGoal(mob, priority, new ThrowAxeGoal(plugin, activeBoss));
            case "SUMMON_WOLVES" -> goals.addGoal(mob, priority, new SummonWolvesGoal(plugin, activeBoss));
            case "SUMMON_SNOW_GOLEMS" -> goals.addGoal(mob, priority, new SummonSnowGolemsGoal(plugin, activeBoss));
            case "SUMMON_EXPLODING_ZOMBIES" -> goals.addGoal(mob, priority, new SummonExplodingZombiesGoal(plugin, activeBoss));
            case "PULL_HOOK" -> goals.addGoal(mob, priority, new PullHookGoal(plugin, activeBoss));
            case "FIREBALL" -> goals.addGoal(mob, priority, new FireballGoal(plugin, activeBoss));
            case "FLOOR_IS_LAVA" -> goals.addGoal(mob, priority, new FloorIsLavaGoal(plugin, activeBoss));
        }
    }

    public ActiveBoss getActiveBoss(Entity entity) {
        return activeBosses.get(entity.getUniqueId());
    }

    public void removeActiveBoss(UUID uuid) {
        ActiveBoss activeBoss = activeBosses.remove(uuid);
        if (activeBoss != null && activeBoss.getBossBar() != null) {
            activeBoss.getBossBar().removeAll();
        }
    }

    private void applyEquipment(LivingEntity entity, CustomMob.Equipment equip) {
        if (equip == null) return;
        EntityEquipment ee = entity.getEquipment();
        if (ee == null) return;

        if (equip.getMainHand() != null) ee.setItemInMainHand(createItem(equip.getMainHand()));
        if (equip.getOffHand() != null) ee.setItemInOffHand(createItem(equip.getOffHand()));
        if (equip.getHelmet() != null) ee.setHelmet(createItem(equip.getHelmet()));
        if (equip.getChestplate() != null) ee.setChestplate(createItem(equip.getChestplate()));
        if (equip.getLeggings() != null) ee.setLeggings(createItem(equip.getLeggings()));
        if (equip.getBoots() != null) ee.setBoots(createItem(equip.getBoots()));
    }

    private void applyScale(LivingEntity entity, double scale) {
        if (entity.getType() == EntityType.GIANT) {
            scale = scale / 6.0;
        }
        AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(scale);
        }
    }

    private ItemStack createItem(String materialName) {
        Material mat = Material.matchMaterial(materialName);
        return mat != null ? new ItemStack(mat) : new ItemStack(Material.AIR);
    }

    public Optional<CustomMob> getCustomMobConfig(Entity entity) {
        String id = entity.getPersistentDataContainer().get(customMobKey, PersistentDataType.STRING);
        if (id == null) return Optional.empty();
        return dataManager.getCustomMobs().stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public Optional<CustomBoss> getCustomBossConfig(Entity entity) {
        String id = entity.getPersistentDataContainer().get(customBossKey, PersistentDataType.STRING);
        if (id == null) return Optional.empty();
        return dataManager.getCustomBosses().stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    private void startBossRespawnTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (CustomBoss boss : dataManager.getCustomBosses()) {
                World world = Bukkit.getWorld(boss.getLocation().getWorld());
                if (world == null) continue;

                Location spawnLoc = new Location(world, boss.getLocation().getX(), boss.getLocation().getY(), boss.getLocation().getZ());
                // Only spawn or check if the spawn chunk is loaded.
                // If it is not loaded, we do not attempt to check or spawn.
                if (!world.isChunkLoaded(spawnLoc.getBlockX() >> 4, spawnLoc.getBlockZ() >> 4)) {
                    continue;
                }

                long lastDeath = dataManager.getBossState().getLastDeath(boss.getId());
                if (lastDeath > 0 && (now - lastDeath) >= (boss.getRespawnSeconds() * 1000L)) {
                    // Check if already spawned (simple check: any boss with this ID in the world)
                    if (!isBossAlive(boss.getId())) {
                        spawnCustomBoss(boss);
                        dataManager.getBossState().setLastDeath(boss.getId(), 0);
                        dataManager.saveBossState();
                    }
                } else if (lastDeath == 0 && !isBossAlive(boss.getId())) {
                    // Initial spawn or lost state
                    spawnCustomBoss(boss);
                }
            }
        }, 200L, 200L); // Every 10 seconds
    }

    private void applyHealth(LivingEntity entity, double maxHealth) {
        if (maxHealth <= 0) return;
        AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(maxHealth);
            entity.setHealth(maxHealth);
        }
    }

    private void startBossBarUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (ActiveBoss activeBoss : activeBosses.values()) {
                // Update leash (do this even if bossBar is null, in case a boss has no boss bar but is still a boss)
                checkBossLeash(activeBoss);

                BossBar bossBar = activeBoss.getBossBar();
                if (bossBar == null) continue;

                Mob entity = activeBoss.getEntity();
                if (!entity.isValid() || entity.isDead()) {
                    bossBar.removeAll();
                    continue;
                }

                // Update health progress
                double health = entity.getHealth();
                double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, health / maxHealth)));

                // Update players (within 30 blocks)
                for (Player player : entity.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(entity.getLocation()) < 900) {
                        bossBar.addPlayer(player);
                    } else {
                        bossBar.removePlayer(player);
                    }
                }
            }
        }, 20L, 20L); // Every second
    }

    private void checkBossLeash(ActiveBoss activeBoss) {
        Mob entity = activeBoss.getEntity();
        if (entity == null || !entity.isValid() || entity.isDead()) return;

        var bossConfigOpt = getCustomBossConfig(entity);
        if (bossConfigOpt.isEmpty()) return;

        CustomBoss config = bossConfigOpt.get();
        if (config.getLocation() == null) return;

        World world = Bukkit.getWorld(config.getLocation().getWorld());
        if (world == null || !entity.getWorld().equals(world)) return;

        Location spawnLoc = new Location(world, config.getLocation().getX(), config.getLocation().getY(), config.getLocation().getZ());
        double distSq = entity.getLocation().distanceSquared(spawnLoc);
        double maxDist = 48.0; // 48 blocks leash radius
        if (distSq > maxDist * maxDist) {
            entity.teleport(spawnLoc);
            world.spawnParticle(org.bukkit.Particle.PORTAL, spawnLoc, 30, 0.5, 1.0, 0.5, 0.1);
            world.playSound(spawnLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            entity.setTarget(null);
        }
    }

    private boolean isBossAlive(String bossId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String id = entity.getPersistentDataContainer().get(customBossKey, PersistentDataType.STRING);
                if (bossId.equals(id)) return true;
            }
        }
        return false;
    }

    public NamespacedKey getCustomBossKey() {
        return customBossKey;
    }

    private void applyEntityProperties(LivingEntity entity) {
        if (entity instanceof org.bukkit.entity.Ageable ageable) {
            ageable.setAdult();
        }
        if (entity instanceof org.bukkit.entity.Zombie zombie) {
            zombie.setBaby(false);
        }
        if (entity instanceof org.bukkit.entity.PiglinAbstract piglin) {
            piglin.setImmuneToZombification(true);
        }
        if (entity instanceof org.bukkit.entity.Hoglin hoglin) {
            hoglin.setImmuneToZombification(true);
        }
    }
}
