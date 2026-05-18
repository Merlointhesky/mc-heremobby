package com.heremobby.heremobby.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

/**
 * Utility methods for custom spell mechanics.
 */
public class SpellUtils {
    public static final String IMMOBILIZED_KEY = "immobilized_by_spell";

    /**
     * Pushes or pulls a target relative to an origin.
     * @param origin The source of the force (e.g., the boss location)
     * @param target The entity to move
     * @param force Positive for push, negative for pull
     */
    public static void applyVelocity(Location origin, Entity target, double force) {
        Vector targetVec = target.getLocation().toVector();
        Vector originVec = origin.toVector();
        
        Vector direction = targetVec.subtract(originVec);
        if (direction.lengthSquared() < 0.01) {
            direction = new Vector(0, 0, 1);
        }
        direction.normalize();
        direction.multiply(force);
        
        // Apply slight upward modification to counter friction and make the push/pull effective
        direction.setY(direction.getY() + 0.2);
        
        target.setVelocity(direction);
    }

    /**
     * Immobilizes a target by mounting them to an invisible, stationary armor stand.
     * @param target The entity to immobilize
     * @param key The NamespacedKey for tagging
     * @return The ArmorStand used as a tether
     */
    public static ArmorStand immobilize(LivingEntity target, NamespacedKey key) {
        ArmorStand stand = target.getWorld().spawn(target.getLocation(), ArmorStand.class, s -> {
            s.setVisible(false);
            s.setGravity(false);
            s.setMarker(true); // Small hitbox, doesn't interact
            s.setSmall(true);
            s.setBasePlate(false);
            s.setArms(false);
            s.setCanTick(false);
            s.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        });
        
        stand.addPassenger(target);
        return stand;
    }
}
