package com.heremobby.heremobby.framework.wild;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class WildAnimalListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            // Overrides default entity loot tables
            event.getDrops().clear();
            
            // In a real implementation, we would look up custom drops for this entity type.
            // For now, we just clear vanilla drops as requested.
        }
    }
}
