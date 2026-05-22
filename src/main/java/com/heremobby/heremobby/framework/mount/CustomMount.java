package com.heremobby.heremobby.framework.mount;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.framework.CustomEntity;
import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class CustomMount implements CustomEntity {
    public static final NamespacedKey DISPLAY_UUID_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "rocket_display_uuid");
    public static final NamespacedKey SEAT_TYPE_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "rocket_seat_type");

    private final String typeId;
    private final String modelId;
    private Entity baseEntity;

    public CustomMount(String typeId, String modelId) {
        this.typeId = typeId;
        this.modelId = modelId;
    }

    public void spawn(Location loc) {
        if (typeId != null && (typeId.trim().equalsIgnoreCase("rideable_rocket") || typeId.toLowerCase().contains("rocket"))) {
            HereMobbyPlugin.getInstance().getLogger().info("Spawning rideable rocket as ItemDisplay!");
            
            // Adjust location so rocket and its seats spawn cleanly above the ground (no sinking)
            Location spawnLoc = loc.clone().add(0, 0.5, 0);
            
            // Spawn ItemDisplay
            ItemDisplay display = (ItemDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
            
            // Set the item stack: FIREWORK_ROCKET with the modern 1.21.4 item_model component set to "minecraft:rideable_rocket"
            ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setItemModel(NamespacedKey.minecraft("rideable_rocket"));
                meta.setDisplayName(ChatColor.GOLD + "X-52 Nether-Rocket");
                itemStack.setItemMeta(meta);
            }
            display.setItemStack(itemStack);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            display.addScoreboardTag("heremobby_rocket");
            display.addScoreboardTag("rideable_rocket_vehicle");
            display.addScoreboardTag(EntityBuilder.TAG_HEREMOBBY);
            display.getPersistentDataContainer().set(EntityBuilder.CUSTOM_ID_KEY, PersistentDataType.STRING, typeId);
            
            this.baseEntity = display;
            
            // Spawn 2 invisible, small ArmorStand markers (driver and passenger seats)
            ArmorStand driverSeat = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            driverSeat.setVisible(false);
            driverSeat.setSmall(true);
            driverSeat.setMarker(false);
            driverSeat.setGravity(false);
            driverSeat.setBasePlate(false);
            driverSeat.addScoreboardTag("heremobby_rocket");
            driverSeat.addScoreboardTag(EntityBuilder.TAG_HEREMOBBY);
            driverSeat.getPersistentDataContainer().set(DISPLAY_UUID_KEY, PersistentDataType.STRING, display.getUniqueId().toString());
            driverSeat.getPersistentDataContainer().set(SEAT_TYPE_KEY, PersistentDataType.STRING, "driver");
            
            ArmorStand passengerSeat = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            passengerSeat.setVisible(false);
            passengerSeat.setSmall(true);
            passengerSeat.setMarker(false);
            passengerSeat.setGravity(false);
            passengerSeat.setBasePlate(false);
            passengerSeat.addScoreboardTag("heremobby_rocket");
            passengerSeat.addScoreboardTag(EntityBuilder.TAG_HEREMOBBY);
            passengerSeat.getPersistentDataContainer().set(DISPLAY_UUID_KEY, PersistentDataType.STRING, display.getUniqueId().toString());
            passengerSeat.getPersistentDataContainer().set(SEAT_TYPE_KEY, PersistentDataType.STRING, "passenger");

            // Create a BukkitRunnable running every tick to update the seat locations relative to the ItemDisplay's position and yaw.
            // If the display is removed, cancel the task and remove the seats.
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (display == null || !display.isValid() || display.isDead()) {
                        if (driverSeat.isValid()) driverSeat.remove();
                        if (passengerSeat.isValid()) passengerSeat.remove();
                        this.cancel();
                        return;
                    }
                    
                    Location displayLoc = display.getLocation();
                    org.bukkit.util.Vector direction = displayLoc.getDirection().normalize();
                    
                    // Driver seat: offset Z = -0.2 (slightly backward), Y = -1.2 (lower down where it sits)
                    Location driverLoc = displayLoc.clone().add(direction.clone().multiply(-0.2));
                    driverLoc.add(0, -1.2, 0);
                    driverLoc.setYaw(displayLoc.getYaw());
                    driverLoc.setPitch(0);
                    driverSeat.teleport(driverLoc);
                    
                    // Passenger seat: offset Z = -1.0 (further backward), Y = -1.2 (lower down where it sits)
                    Location passengerLoc = displayLoc.clone().add(direction.clone().multiply(-1.0));
                    passengerLoc.add(0, -1.2, 0);
                    passengerLoc.setYaw(displayLoc.getYaw());
                    passengerLoc.setPitch(0);
                    passengerSeat.teleport(passengerLoc);
                }
            }.runTaskTimer(HereMobbyPlugin.getInstance(), 0L, 1L);
            
        } else {
            this.baseEntity = EntityBuilder.createBaseEntity(loc, EntityType.ARMOR_STAND, typeId);
            EntityBuilder.applyBetterModel(baseEntity, modelId);
        }
    }

    @Override
    public UUID getUniqueId() {
        return baseEntity != null ? baseEntity.getUniqueId() : null;
    }

    @Override
    public Entity getBaseEntity() {
        return baseEntity;
    }

    @Override
    public void remove() {
        if (baseEntity != null) baseEntity.remove();
    }

    @Override
    public String getTypeId() {
        return typeId;
    }
}
