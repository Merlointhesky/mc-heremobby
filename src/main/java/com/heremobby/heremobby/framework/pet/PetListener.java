package com.heremobby.heremobby.framework.pet;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PetListener implements Listener {

    @EventHandler
    public void onPetDamage(EntityDamageEvent event) {
        if (event.getEntity().getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            // Check if the custom entity has the killable metadata key
            var container = event.getEntity().getPersistentDataContainer();
            if (container.has(CustomPet.KILLABLE_KEY, PersistentDataType.BYTE)) {
                return; // Do NOT cancel damage; let the pet take damage!
            }
            
            // Cancel all incoming damage for standard custom entities (invulnerable pets/mounts)
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPetDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            var container = entity.getPersistentDataContainer();
            if (container.has(CustomPet.KILLABLE_KEY, PersistentDataType.BYTE)) {
                // Clear default entity drops
                event.getDrops().clear();
                
                // Drop premium custom salvage (drone parts)
                event.getDrops().add(new ItemStack(Material.IRON_INGOT, 2));
                event.getDrops().add(new ItemStack(Material.GHAST_TEAR, 2));
                event.getDrops().add(new ItemStack(Material.LANTERN, 1));
                
                // Spawn cool explosion and electric spark particles
                var loc = entity.getLocation();
                loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5);
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 15);
                
                // Play metallic breaking and explosion sounds
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_BREAK, 0.8f, 1.2f);
                
                // Clean up visual passengers (ItemDisplay model)
                for (Entity passenger : entity.getPassengers()) {
                    passenger.remove();
                }
            }
        }
    }
}
