package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.model.CustomBoss;
import com.heremobby.heremobby.model.CustomMob;
import com.heremobby.heremobby.config.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class MobListener implements Listener {
    private final MobManager mobManager;
    private final DataManager dataManager;
    private final Random random = new Random();
    private final boolean hereShoppyEnabled;

    public MobListener(MobManager mobManager, DataManager dataManager) {
        this.mobManager = mobManager;
        this.dataManager = dataManager;
        this.hereShoppyEnabled = Bukkit.getPluginManager().isPluginEnabled("HereShoppy");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        double defense = 0;
        var bossConfig = mobManager.getCustomBossConfig(entity);
        if (bossConfig.isPresent()) {
            defense = bossConfig.get().getDefense();
        } else {
            var mobConfig = mobManager.getCustomMobConfig(entity);
            if (mobConfig.isPresent()) {
                defense = mobConfig.get().getDefense();
            }
        }

        if (defense > 0) {
            // Treat defense as percentage reduction (e.g., 0.1 = 10% reduction)
            double reduction = 1.0 - defense;
            if (reduction < 0) reduction = 0;
            event.setDamage(event.getDamage() * reduction);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        double reward = 2.0; // Default standard mob reward
        List<CustomMob.LootItem> customLoot = null;

        // Check if it's a custom boss
        var bossConfig = mobManager.getCustomBossConfig(entity);
        if (bossConfig.isPresent()) {
            reward = bossConfig.get().getKroinReward();
            customLoot = bossConfig.get().getCustomLoot();
            dataManager.getBossState().setLastDeath(bossConfig.get().getId(), System.currentTimeMillis());
            dataManager.saveBossState();
            mobManager.removeActiveBoss(entity.getUniqueId());
        } else {
            // Check if it's a custom mob
            var mobConfig = mobManager.getCustomMobConfig(entity);
            if (mobConfig.isPresent()) {
                reward = mobConfig.get().getKroinReward();
                customLoot = mobConfig.get().getCustomLoot();
            } else {
                // Check for standard overrides
                String typeName = entity.getType().name();
                boolean isStandardBoss = typeName.contains("DRAGON") || typeName.contains("WITHER");
                
                var overrides = isStandardBoss ? dataManager.getStandardBossOverrides() : dataManager.getStandardMobOverrides();
                if (overrides.getOverrides().containsKey(typeName)) {
                    var override = overrides.getOverrides().get(typeName);
                    reward = override.getKroinReward();
                    customLoot = override.getCustomLoot();
                } else if (isStandardBoss) {
                    reward = 2000.0;
                }
            }
        }

        if (killer != null && hereShoppyEnabled) {
            try {
                com.hereshoppy.hereshoppy.api.HereshoppyAPI.addKroins(killer.getUniqueId(), reward);
                killer.sendMessage("§aYou earned §e" + String.format("%.2f", reward) + " Kroins §afor defeating " + entity.getName() + "!");
            } catch (NoClassDefFoundError ignored) {
                // HereShoppy might have been disabled after start
            }
        }

        if (customLoot != null) {
            event.getDrops().clear();
            for (CustomMob.LootItem item : customLoot) {
                if (random.nextDouble() < item.getChance()) {
                    Material mat = Material.matchMaterial(item.getMaterial());
                    if (mat != null) {
                        int amount = item.getMinAmount() + random.nextInt(item.getMaxAmount() - item.getMinAmount() + 1);
                        event.getDrops().add(new ItemStack(mat, amount));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        
        // Don't mess with entities that already have custom data
        if (entity.getPersistentDataContainer().has(mobManager.getCustomBossKey(), PersistentDataType.STRING)) return;

        // Custom Mob Random Spawn Logic
        for (CustomMob config : dataManager.getCustomMobs()) {
            if (config.getBaseType().equalsIgnoreCase(entity.getType().name())) {
                if (shouldSpawn(config, entity)) {
                    // Replace standard mob with custom one
                    mobManager.spawnCustomMob(config, entity.getLocation());
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldSpawn(CustomMob config, Entity entity) {
        CustomMob.SpawnConditions cond = config.getSpawnConditions();
        if (cond == null) return false;

        if (random.nextDouble() >= cond.getChance()) return false;

        if (cond.getBiomes() != null && !cond.getBiomes().isEmpty()) {
            String biome = entity.getLocation().getBlock().getBiome().name();
            if (!cond.getBiomes().contains(biome)) return false;
        }

        long time = entity.getWorld().getTime();
        boolean isDay = time < 12300 || time > 23850;
        if (cond.getTime() != null) {
            if (cond.getTime().equalsIgnoreCase("DAY") && !isDay) return false;
            if (cond.getTime().equalsIgnoreCase("NIGHT") && isDay) return false;
        }

        int light = entity.getLocation().getBlock().getLightLevel();
        if (light < cond.getMinLight() || light > cond.getMaxLight()) return false;

        return true;
    }
}
