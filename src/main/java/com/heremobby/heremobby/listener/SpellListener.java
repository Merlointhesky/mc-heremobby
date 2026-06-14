package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.HereMobbyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.entity.EntityDismountEvent;

public class SpellListener implements Listener {
    private final HereMobbyPlugin plugin;
    private final NamespacedKey immobilizedKey;
    private final NamespacedKey timeKey;

    public SpellListener(HereMobbyPlugin plugin) {
        this.plugin = plugin;
        this.immobilizedKey = new NamespacedKey(plugin, "immobilized_by_spell");
        this.timeKey = new NamespacedKey(plugin, "immobilized_time");

        // Periodic cleanup task to guarantee players are never locked for > 10s
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                for (org.bukkit.entity.ArmorStand stand : world.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)) {
                    if (stand.getPersistentDataContainer().has(immobilizedKey, PersistentDataType.BYTE)) {
                        Long spawnTime = stand.getPersistentDataContainer().get(timeKey, PersistentDataType.LONG);
                        if (spawnTime == null || (System.currentTimeMillis() - spawnTime) >= 10000) {
                            stand.remove();
                        }
                    }
                }
            }
        }, 20L, 20L); // Check every second (20 ticks)
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        org.bukkit.entity.Entity dismounted = event.getDismounted();
        if (dismounted.getPersistentDataContainer().has(immobilizedKey, PersistentDataType.BYTE)) {
            Long spawnTime = dismounted.getPersistentDataContainer().get(timeKey, PersistentDataType.LONG);
            if (spawnTime != null && (System.currentTimeMillis() - spawnTime) >= 10000) {
                dismounted.remove();
                return;
            }
            // Cancel dismount if the entity is still valid (meaning the spell hasn't ended)
            if (dismounted.isValid()) {
                event.setCancelled(true);
            }
        }
    }
}
