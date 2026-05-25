package com.heremobby.heremobby.framework.pet;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.framework.CustomEntity;
import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;

public class CustomPet implements CustomEntity {
    private final String typeId;
    private final String modelId;
    private final UUID ownerId;
    private final EntityType baseType;
    private final boolean killable;
    private final double maxHealth;
    private final Material material;
    private final int customModelData;
    private Entity baseEntity;

    public static final NamespacedKey OWNER_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "pet_owner");
    public static final NamespacedKey KILLABLE_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "pet_killable");

    public CustomPet(String typeId, String modelId, UUID ownerId, EntityType baseType, boolean killable, double maxHealth, Material material, int customModelData) {
        this.typeId = typeId;
        this.modelId = modelId;
        this.ownerId = ownerId;
        this.baseType = baseType;
        this.killable = killable;
        this.maxHealth = maxHealth;
        this.material = material;
        this.customModelData = customModelData;
    }

    public void spawn(Location loc) {
        // Spawn configured base entity type (e.g. ALLAY for flying, WOLF for land)
        this.baseEntity = EntityBuilder.createBaseEntity(loc, baseType, typeId);
        this.baseEntity.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
        
        if (killable) {
            this.baseEntity.getPersistentDataContainer().set(KILLABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        }

        // Set custom maximum health if configured and entity is living
        if (this.baseEntity instanceof LivingEntity le && maxHealth > 0) {
            var attribute = le.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (attribute != null) {
                attribute.setBaseValue(maxHealth);
                le.setHealth(maxHealth);
            }
        }
        
        // Try applying BetterModel if enabled (backwards compatibility)
        if (modelId != null && !modelId.equalsIgnoreCase("vanilla")) {
            EntityBuilder.applyBetterModel(baseEntity, modelId);
        }
        
        // Vanilla-friendly rendering: if it's a vanilla model, make the living entity invisible
        if (modelId != null && modelId.equalsIgnoreCase("vanilla") && baseEntity instanceof org.bukkit.entity.LivingEntity le) {
            le.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                1,
                false,
                false
            ));
        }

        if (material != null) {
            Location displayLoc = baseEntity.getLocation();
            
            if (material.isBlock()) {
                org.bukkit.entity.BlockDisplay display = (org.bukkit.entity.BlockDisplay) baseEntity.getWorld().spawnEntity(displayLoc, EntityType.BLOCK_DISPLAY);
                display.setBlock(Bukkit.createBlockData(material));
                
                // Add custom entity tagging to display so it is skipped from targeting, etc.
                display.addScoreboardTag(EntityBuilder.TAG_HEREMOBBY);
                display.addScoreboardTag("heremobby_display");
                
                display.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
                
                org.bukkit.util.Transformation transformation = display.getTransformation();
                // Center the block display. Block origin is at 0,0,0 corner.
                transformation.getTranslation().set(-0.5f, 0.5f, -0.5f);
                display.setTransformation(transformation);
                
                baseEntity.addPassenger(display);
            } else {
                ItemDisplay display = (ItemDisplay) baseEntity.getWorld().spawnEntity(displayLoc, EntityType.ITEM_DISPLAY);
                
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (modelId != null && !modelId.equalsIgnoreCase("vanilla")) {
                        meta.setCustomModelData(customModelData);
                        if (!modelId.isEmpty()) {
                            meta.setItemModel(NamespacedKey.minecraft(modelId));
                        }
                    }
                    item.setItemMeta(meta);
                }
                display.setItemStack(item);
                display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                display.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15)); // Make it glow bright!
                
                // Add custom entity tagging to display so it is skipped from targeting, etc.
                display.addScoreboardTag(EntityBuilder.TAG_HEREMOBBY);
                display.addScoreboardTag("heremobby_display");
                
                baseEntity.addPassenger(display);
            }
        }
        
        if (baseEntity instanceof Mob mob) {
            Bukkit.getMobGoals().removeAllGoals(mob);
            Bukkit.getMobGoals().addGoal(mob, 1, new FollowOwnerGoal(HereMobbyPlugin.getInstance(), mob, ownerId));
        }
    }

    @Override
    public UUID getUniqueId() {
        return baseEntity != null ? baseEntity.getUniqueId() : null;
    }

    @Override
    public Entity getBaseEntity() {
        return baseEntity;
    }

    @Override
    public void remove() {
        if (baseEntity != null) {
            // Safely remove any mounted visual passenger displays first
            for (Entity passenger : baseEntity.getPassengers()) {
                passenger.remove();
            }
            baseEntity.remove();
        }
    }

    @Override
    public String getTypeId() {
        return typeId;
    }
}
