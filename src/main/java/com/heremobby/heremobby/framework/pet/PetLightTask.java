package com.heremobby.heremobby.framework.pet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PetLightTask implements Runnable {
    
    // Map of Entity UUID to the Location of the light block we placed for it
    private final Map<UUID, Location> activeLights = new HashMap<>();
    
    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Mob && entity.getPersistentDataContainer().has(CustomPet.OWNER_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                    // Check if this pet is supposed to emit light (e.g. if it's a lantern)
                    // We can check if it has a block display passenger with lantern
                    boolean isLightPet = false;
                    for (Entity pass : entity.getPassengers()) {
                        if (pass instanceof org.bukkit.entity.BlockDisplay bd) {
                            if (bd.getBlock().getMaterial() == Material.LANTERN || bd.getBlock().getMaterial() == Material.SOUL_LANTERN) {
                                isLightPet = true;
                                break;
                            }
                        }
                    }
                    
                    if (isLightPet) {
                        UUID uuid = entity.getUniqueId();
                        // Get location at eye level to light up correctly
                        Location currentBlockLoc = entity.getLocation().add(0, 1, 0).getBlock().getLocation();
                        
                        Location previousLoc = activeLights.get(uuid);
                        if (previousLoc != null) {
                            if (!previousLoc.equals(currentBlockLoc)) {
                                removeLight(previousLoc);
                                placeLight(currentBlockLoc);
                                activeLights.put(uuid, currentBlockLoc);
                            }
                        } else {
                            placeLight(currentBlockLoc);
                            activeLights.put(uuid, currentBlockLoc);
                        }
                    }
                }
            }
        }
        
        // Cleanup lights for pets that are no longer valid
        Iterator<Map.Entry<UUID, Location>> it = activeLights.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Location> entry = it.next();
            Entity e = Bukkit.getEntity(entry.getKey());
            if (e == null || !e.isValid() || e.isDead()) {
                removeLight(entry.getValue());
                it.remove();
            }
        }
    }
    
    private void placeLight(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.WATER) {
            boolean waterlogged = block.getType() == Material.WATER;
            block.setType(Material.LIGHT, false);
            if (block.getBlockData() instanceof Light lightData) {
                lightData.setLevel(15);
                lightData.setWaterlogged(waterlogged);
                block.setBlockData(lightData, true);
            }
        }
    }
    
    private void removeLight(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() == Material.LIGHT) {
            Light lightData = (Light) block.getBlockData();
            if (lightData.isWaterlogged()) {
                block.setType(Material.WATER, false);
            } else {
                block.setType(Material.AIR, false);
            }
        }
    }
    
    // Call this when plugin disables
    public void cleanupAll() {
        for (Location loc : activeLights.values()) {
            removeLight(loc);
        }
        activeLights.clear();
    }
}
