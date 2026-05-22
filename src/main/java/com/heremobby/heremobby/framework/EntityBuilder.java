package com.heremobby.heremobby.framework;

import com.heremobby.heremobby.HereMobbyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

public class EntityBuilder {
    public static final String TAG_HEREMOBBY = "heremobby_entity";
    public static final NamespacedKey CUSTOM_ID_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "custom_id");

    public static Entity createBaseEntity(Location loc, EntityType type, String customId) {
        Entity entity = loc.getWorld().spawnEntity(loc, type);
        entity.addScoreboardTag(TAG_HEREMOBBY);
        entity.getPersistentDataContainer().set(CUSTOM_ID_KEY, PersistentDataType.STRING, customId);
        
        if (entity instanceof ArmorStand as) {
            as.setVisible(false);
            as.setGravity(true);
            as.setBasePlate(false);
            as.setPersistent(true);
        } else if (entity instanceof LivingEntity le) {
            le.setInvisible(true);
            le.setSilent(true);
            le.setRemoveWhenFarAway(false);
            le.setPersistent(true);
        }
        
        return entity;
    }

    public static void applyBetterModel(Entity entity, String modelId) {
        if (modelId == null || modelId.isEmpty()) return;
        
        if (Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            try {
                // Reflective call to kr.toxicity.model.api.BetterModel.model(modelId)
                Class<?> bmClass = Class.forName("kr.toxicity.model.api.BetterModel");
                java.lang.reflect.Method modelMethod = bmClass.getMethod("model", String.class);
                Object model = modelMethod.invoke(null, modelId);
                
                if (model != null) {
                    // Try common methods for applying models
                    try {
                        java.lang.reflect.Method applyMethod = model.getClass().getMethod("apply", Entity.class);
                        applyMethod.invoke(model, entity);
                    } catch (NoSuchMethodException e) {
                        java.lang.reflect.Method showMethod = model.getClass().getMethod("show", Entity.class);
                        showMethod.invoke(model, entity);
                    }
                }
            } catch (Exception e) {
                HereMobbyPlugin.getInstance().getLogger().warning("Could not apply BetterModel " + modelId + " to " + entity.getUniqueId());
            }
        }
    }
}
