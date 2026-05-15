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

import java.util.Optional;

public class MobManager {
    private final HereMobbyPlugin plugin;
    private final DataManager dataManager;
    private final NamespacedKey customMobKey;
    private final NamespacedKey customBossKey;

    public MobManager(HereMobbyPlugin plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.customMobKey = new NamespacedKey(plugin, "custom_mob");
        this.customBossKey = new NamespacedKey(plugin, "custom_boss");
        
        startBossRespawnTask();
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
        entity.getPersistentDataContainer().set(customMobKey, PersistentDataType.STRING, config.getDisplayName());
    }

    public void spawnCustomBoss(CustomBoss config) {
        World world = Bukkit.getWorld(config.getLocation().getWorld());
        if (world == null) return;

        Location loc = new Location(world, config.getLocation().getX(), config.getLocation().getY(), config.getLocation().getZ());
        EntityType type = EntityType.valueOf(config.getBaseType().toUpperCase());
        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);

        if (config.getDisplayName() != null) {
            entity.setCustomName(config.getDisplayName());
            entity.setCustomNameVisible(true);
        }

        applyEquipment(entity, config.getEquipment());
        applyScale(entity, config.getScale());
        entity.getPersistentDataContainer().set(customBossKey, PersistentDataType.STRING, config.getId());
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
        if (scale == 1.0) return;
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
        String name = entity.getPersistentDataContainer().get(customMobKey, PersistentDataType.STRING);
        if (name == null) return Optional.empty();
        return dataManager.getCustomMobs().stream().filter(m -> m.getDisplayName().equals(name)).findFirst();
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
}
