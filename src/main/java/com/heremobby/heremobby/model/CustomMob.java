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
    private int xpReward = -1;
    private double scale = 1.0;
    private double maxHealth = -1;
    private double defense = 0;
    private List<LootItem> customLoot;
    private List<String> spells;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public SpawnConditions getSpawnConditions() {
        return spawnConditions;
    }

    public void setSpawnConditions(SpawnConditions spawnConditions) {
        this.spawnConditions = spawnConditions;
    }

    public long getKroinReward() {
        return kroinReward;
    }

    public void setKroinReward(long kroinReward) {
        this.kroinReward = kroinReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public List<LootItem> getCustomLoot() {
        return customLoot;
    }

    public void setCustomLoot(List<LootItem> customLoot) {
        this.customLoot = customLoot;
    }

    public List<String> getSpells() {
        return spells;
    }

    public void setSpells(List<String> spells) {
        this.spells = spells;
    }

    public static class Equipment {
        private String mainHand;
        private String offHand;
        private String helmet;
        private String chestplate;
        private String leggings;
        private String boots;

        public String getMainHand() { return mainHand; }
        public void setMainHand(String mainHand) { this.mainHand = mainHand; }
        public String getOffHand() { return offHand; }
        public void setOffHand(String offHand) { this.offHand = offHand; }
        public String getHelmet() { return helmet; }
        public void setHelmet(String helmet) { this.helmet = helmet; }
        public String getChestplate() { return chestplate; }
        public void setChestplate(String chestplate) { this.chestplate = chestplate; }
        public String getLeggings() { return leggings; }
        public void setLeggings(String leggings) { this.leggings = leggings; }
        public String getBoots() { return boots; }
        public void setBoots(String boots) { this.boots = boots; }
    }

    public static class SpawnConditions {
        private List<String> biomes;
        private String time; // DAY, NIGHT, BOTH
        private int minLight = 0;
        private int maxLight = 15;
        private double chance;

        public List<String> getBiomes() { return biomes; }
        public void setBiomes(List<String> biomes) { this.biomes = biomes; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public int getMinLight() { return minLight; }
        public void setMinLight(int minLight) { this.minLight = minLight; }
        public int getMaxLight() { return maxLight; }
        public void setMaxLight(int maxLight) { this.maxLight = maxLight; }
        public double getChance() { return chance; }
        public void setChance(double chance) { this.chance = chance; }
    }

    public static class LootItem {
        private String material;
        private int minAmount;
        private int maxAmount;
        private double chance;

        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        public int getMinAmount() { return minAmount; }
        public void setMinAmount(int minAmount) { this.minAmount = minAmount; }
        public int getMaxAmount() { return maxAmount; }
        public void setMaxAmount(int maxAmount) { this.maxAmount = maxAmount; }
        public double getChance() { return chance; }
        public void setChance(double chance) { this.chance = chance; }
    }
}
