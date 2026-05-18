package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.HereMobbyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.entity.EntityDismountEvent;

public class SpellListener implements Listener {
    private final NamespacedKey immobilizedKey;

    public SpellListener(HereMobbyPlugin plugin) {
        this.immobilizedKey = new NamespacedKey(plugin, "immobilized_by_spell");
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getDismounted().getPersistentDataContainer().has(immobilizedKey, PersistentDataType.BYTE)) {
            // Cancel dismount if the entity is still valid (meaning the spell hasn't ended)
            if (event.getDismounted().isValid()) {
                event.setCancelled(true);
            }
        }
    }
}
