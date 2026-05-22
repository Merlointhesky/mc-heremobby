package com.heremobby.heremobby.framework.pet;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PetListener implements Listener {

    @EventHandler
    public void onPetDamage(EntityDamageEvent event) {
        if (event.getEntity().getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            // Cancel all incoming damage to custom entities (pets/mounts/wild)
            // unless we want some to be killable. The requirement said:
            // "completely canceling all incoming damage to the pet"
            event.setCancelled(true);
        }
    }
}
