package com.heremobby.heremobby.framework.mount;

import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class MountTask implements Runnable {

    public static final java.util.Set<UUID> cruiseControlActive = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof ArmorStand && vehicle.getScoreboardTags().contains("heremobby_rocket")) {
                // This is a rocket seat. Let's find the linked ItemDisplay
                String displayUuidStr = vehicle.getPersistentDataContainer().get(CustomMount.DISPLAY_UUID_KEY, PersistentDataType.STRING);
                if (displayUuidStr != null) {
                    try {
                        UUID displayUuid = UUID.fromString(displayUuidStr);
                        Entity display = Bukkit.getEntity(displayUuid);
                        if (display != null && display.isValid()) {
                            // Check if this seat is the driver seat
                            String seatType = vehicle.getPersistentDataContainer().get(CustomMount.SEAT_TYPE_KEY, PersistentDataType.STRING);
                                if ("driver".equals(seatType)) {
                                    org.bukkit.Input input = player.getCurrentInput();
                                    Vector moveVec = new Vector(0, 0, 0);
                                    double speed = 0.3;
                                    
                                    float yaw = player.getLocation().getYaw();
                                    float pitch = player.getLocation().getPitch();
                                    
                                    Vector forward = player.getLocation().getDirection().normalize();
                                    Vector forwardHorizontal = forward.clone();
                                    forwardHorizontal.setY(0);
                                    if (forwardHorizontal.lengthSquared() > 0) {
                                        forwardHorizontal.normalize();
                                    }
                                    
                                    Vector left = new Vector(forwardHorizontal.getZ(), 0, -forwardHorizontal.getX());
                                    if (left.lengthSquared() > 0) {
                                        left.normalize();
                                    }
                                    
                                    UUID uuid = player.getUniqueId();
                                    boolean hasCruise = cruiseControlActive.contains(uuid);

                                    // If user has cruise active but presses W (Forward) or S (Backward), cancel it!
                                    if (hasCruise && (input.isForward() || input.isBackward())) {
                                        cruiseControlActive.remove(uuid);
                                        hasCruise = false;
                                        player.sendMessage(net.kyori.adventure.text.Component.text("🚀 Rocket Cruise Control: DISABLED").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                                    }

                                    if (hasCruise) {
                                        // Cruise mode: move forward automatically!
                                        moveVec.add(forward.multiply(speed));
                                    } else {
                                        // W: Forward
                                        if (input.isForward()) {
                                            moveVec.add(forward.multiply(speed));
                                        }
                                        // S: Backward
                                        if (input.isBackward()) {
                                            moveVec.add(forward.multiply(-speed));
                                        }
                                    }
                                    // A: Strafe Left
                                    if (input.isLeft()) {
                                        moveVec.add(left.multiply(speed));
                                    }
                                    // D: Strafe Right
                                    if (input.isRight()) {
                                        moveVec.add(left.multiply(-speed));
                                    }
                                    // Space (Jump): Fly Up
                                    if (input.isJump()) {
                                        moveVec.add(new Vector(0, speed, 0));
                                    }
                                
                                if (moveVec.lengthSquared() > 0) {
                                    // Move display with sliding collision detection to prevent flying into blocks
                                    Location currentLoc = display.getLocation();
                                    double stepX = moveVec.getX();
                                    double stepY = moveVec.getY();
                                    double stepZ = moveVec.getZ();
                                    
                                    double finalX = currentLoc.getX();
                                    double finalY = currentLoc.getY();
                                    double finalZ = currentLoc.getZ();
                                    
                                    // Check X collision
                                    Location checkX = currentLoc.clone().add(stepX, 0, 0);
                                    if (!checkX.getBlock().getType().isSolid() && !checkX.clone().add(0, 1.0, 0).getBlock().getType().isSolid()) {
                                        finalX += stepX;
                                    }
                                    
                                    // Check Y collision
                                    Location checkY = currentLoc.clone().add(0, stepY, 0);
                                    if (!checkY.getBlock().getType().isSolid() && !checkY.clone().add(0, 1.0, 0).getBlock().getType().isSolid()) {
                                        finalY += stepY;
                                    } else if (stepY < 0) {
                                        // If moving down and hitting a solid block, snap to the surface
                                        finalY = Math.floor(currentLoc.getY());
                                        while (new Location(currentLoc.getWorld(), currentLoc.getX(), finalY, currentLoc.getZ()).getBlock().getType().isSolid()) {
                                            finalY += 1.0;
                                        }
                                    }
                                    
                                    // Check Z collision
                                    Location checkZ = currentLoc.clone().add(0, 0, stepZ);
                                    if (!checkZ.getBlock().getType().isSolid() && !checkZ.clone().add(0, 1.0, 0).getBlock().getType().isSolid()) {
                                        finalZ += stepZ;
                                    }
                                    
                                    Location newLoc = new Location(currentLoc.getWorld(), finalX, finalY, finalZ);
                                    newLoc.setYaw(yaw);
                                    newLoc.setPitch(pitch);
                                    display.teleport(newLoc);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            } else if (vehicle instanceof ArmorStand && vehicle.getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
                // Standard non-rocket mount movement (grounded)
                Vector direction = player.getLocation().getDirection();
                direction.setY(0); // Keep it on the ground
                
                if (direction.lengthSquared() > 0) {
                    direction.normalize();
                    
                    double speed = 0.3;
                    vehicle.setVelocity(direction.multiply(speed));
                    
                    // Sync rotation
                    vehicle.setRotation(player.getLocation().getYaw(), 0);
                }
            }
        }

        // Handle driverless rockets (stopping and floating to the ground or above water)
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(org.bukkit.entity.ItemDisplay.class)) {
                if (entity.getScoreboardTags().contains("rideable_rocket_vehicle")) {
                    org.bukkit.entity.ItemDisplay display = (org.bukkit.entity.ItemDisplay) entity;
                    boolean hasDriver = false;
                    ArmorStand driverSeat = null;

                    // Find the driver seat for this rocket
                    for (Entity near : display.getWorld().getNearbyEntities(display.getLocation(), 3.0, 3.0, 3.0)) {
                        if (near instanceof ArmorStand as && near.getScoreboardTags().contains("heremobby_rocket")) {
                            String seatDisplayUuid = as.getPersistentDataContainer().get(CustomMount.DISPLAY_UUID_KEY, PersistentDataType.STRING);
                            if (display.getUniqueId().toString().equals(seatDisplayUuid)) {
                                String type = as.getPersistentDataContainer().get(CustomMount.SEAT_TYPE_KEY, PersistentDataType.STRING);
                                if ("driver".equals(type)) {
                                    driverSeat = as;
                                    break;
                                }
                            }
                        }
                    }

                    if (driverSeat != null && !driverSeat.getPassengers().isEmpty()) {
                        for (Entity passenger : driverSeat.getPassengers()) {
                            if (passenger instanceof Player) {
                                hasDriver = true;
                                break;
                            }
                        }
                    }

                    if (!hasDriver) {
                        // Apply float-down gravity descent
                        Location loc = display.getLocation();
                        Location feetLoc = loc.clone().add(0, -1.2, 0);

                        // Check if already on a solid block or floating on water/liquid
                        if (!(feetLoc.getBlock().getType().isSolid() || feetLoc.getBlock().isLiquid())) {
                            Location newLoc = loc.clone().add(0, -0.1, 0);
                            Location newFeetLoc = newLoc.clone().add(0, -1.2, 0);
                            if (newFeetLoc.getBlock().getType().isSolid() || newFeetLoc.getBlock().isLiquid()) {
                                double surfaceY = Math.floor(newFeetLoc.getY()) + 1.0;
                                newLoc.setY(surfaceY + 1.2);
                                display.teleport(newLoc);
                            } else {
                                display.teleport(newLoc);
                            }
                        }
                    }
                }
            }
        }
    }
}
