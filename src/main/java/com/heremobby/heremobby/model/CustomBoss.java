package com.heremobby.heremobby.model;

import java.util.List;

public class CustomBoss {
    private String id;
    private String displayName;
    private String baseType;
    private CustomMob.Equipment equipment;
    private List<CustomMob.LootItem> customLoot;
    private long kroinReward = 20;
    private int xpReward = -1;
    private double scale = 1.0;
    private double maxHealth = -1;
    private double defense = 0;
    private LocationData location;
    private int respawnSeconds;
    private List<String> spells;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBaseType() { return baseType; }
    public void setBaseType(String baseType) { this.baseType = baseType; }
    public CustomMob.Equipment getEquipment() { return equipment; }
    public void setEquipment(CustomMob.Equipment equipment) { this.equipment = equipment; }
    public List<CustomMob.LootItem> getCustomLoot() { return customLoot; }
    public void setCustomLoot(List<CustomMob.LootItem> customLoot) { this.customLoot = customLoot; }
    public long getKroinReward() { return kroinReward; }
    public void setKroinReward(long kroinReward) { this.kroinReward = kroinReward; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }
    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
    public double getDefense() { return defense; }
    public void setDefense(double defense) { this.defense = defense; }
    public LocationData getLocation() { return location; }
    public void setLocation(LocationData location) { this.location = location; }
    public int getRespawnSeconds() { return respawnSeconds; }
    public void setRespawnSeconds(int respawnSeconds) { this.respawnSeconds = respawnSeconds; }
    public List<String> getSpells() { return spells; }
    public void setSpells(List<String> spells) { this.spells = spells; }

    public static class LocationData {
        private String world;
        private double x;
        private double y;
        private double z;

        public String getWorld() { return world; }
        public void setWorld(String world) { this.world = world; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public double getZ() { return z; }
        public void setZ(double z) { this.z = z; }
    }
}
