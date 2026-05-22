package com.heremobby.heremobby.framework.mount;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MountListener implements Listener {

    @EventHandler
    public void onMount(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        
        // Handle rocket specific entities (ArmorStand with heremobby_rocket tag)
        if (entity instanceof ArmorStand && entity.getScoreboardTags().contains("heremobby_rocket")) {
            event.setCancelled(true);
            if (entity.getPassengers().isEmpty()) {
                entity.addPassenger(event.getPlayer());
            }
            return;
        }

        // Standard mount logic fallback
        if (entity.getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            if (entity instanceof ArmorStand) {
                entity.addPassenger(event.getPlayer());
            }
        }
    }
}
