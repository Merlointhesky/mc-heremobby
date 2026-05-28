package com.heremobby.heremobby.framework.mount;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.entity.EntityDismountEvent;
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

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof ArmorStand && vehicle.getScoreboardTags().contains("heremobby_rocket")) {
            String seatType = vehicle.getPersistentDataContainer().get(CustomMount.SEAT_TYPE_KEY, PersistentDataType.STRING);
            if ("driver".equals(seatType)) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                if (MountTask.cruiseControlActive.contains(uuid)) {
                    MountTask.cruiseControlActive.remove(uuid);
                    player.sendMessage(net.kyori.adventure.text.Component.text("🚀 Rocket Cruise Control: DISABLED").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                } else {
                    MountTask.cruiseControlActive.add(uuid);
                    player.sendMessage(net.kyori.adventure.text.Component.text("🚀 Rocket Cruise Control: ENABLED (Press F to toggle, or W/S to cancel)").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
                }
            }
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            MountTask.cruiseControlActive.remove(player.getUniqueId());
        }
    }
}
