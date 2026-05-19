package com.heremobby.heremobby.model;

import java.util.List;

public class CustomBoss {
    private String id;
    private String displayName;
    private String baseType;
    private CustomMob.Equipment equipment;
    private List<CustomMob.LootItem> customLoot;
    private long kroinReward = 20;
    private double scale = 1.0;
    private LocationData location;
    private int respawnSeconds;
    private List<String> spells;

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getBaseType() { return baseType; }
    public CustomMob.Equipment getEquipment() { return equipment; }
    public List<CustomMob.LootItem> getCustomLoot() { return customLoot; }
    public long getKroinReward() { return kroinReward; }
    public double getScale() { return scale; }
    public LocationData getLocation() { return location; }
    public int getRespawnSeconds() { return respawnSeconds; }
    public List<String> getSpells() { return spells; }

    public static class LocationData {
        private String world;
        private double x;
        private double y;
        private double z;

        public String getWorld() { return world; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
    }
}
