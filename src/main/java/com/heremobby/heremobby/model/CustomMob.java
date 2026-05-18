package com.heremobby.heremobby.model;

import java.util.List;
import java.util.Map;

public class CustomMob {
    private String id;
    private String displayName;
    private String baseType; // e.g., ZOMBIE
    private Equipment equipment;
    private SpawnConditions spawnConditions;
    private long kroinReward = 1;
    private double scale = 1.0;
    private List<LootItem> customLoot;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseType() {
        return baseType;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public SpawnConditions getSpawnConditions() {
        return spawnConditions;
    }

    public long getKroinReward() {
        return kroinReward;
    }

    public double getScale() {
        return scale;
    }

    public List<LootItem> getCustomLoot() {
        return customLoot;
    }

    public static class Equipment {
        private String mainHand;
        private String offHand;
        private String helmet;
        private String chestplate;
        private String leggings;
        private String boots;

        public String getMainHand() { return mainHand; }
        public String getOffHand() { return offHand; }
        public String getHelmet() { return helmet; }
        public String getChestplate() { return chestplate; }
        public String getLeggings() { return leggings; }
        public String getBoots() { return boots; }
    }

    public static class SpawnConditions {
        private List<String> biomes;
        private String time; // DAY, NIGHT, BOTH
        private int minLight = 0;
        private int maxLight = 15;
        private double chance;

        public List<String> getBiomes() { return biomes; }
        public String getTime() { return time; }
        public int getMinLight() { return minLight; }
        public int getMaxLight() { return maxLight; }
        public double getChance() { return chance; }
    }

    public static class LootItem {
        private String material;
        private int minAmount;
        private int maxAmount;
        private double chance;

        public String getMaterial() { return material; }
        public int getMinAmount() { return minAmount; }
        public int getMaxAmount() { return maxAmount; }
        public double getChance() { return chance; }
    }
}
