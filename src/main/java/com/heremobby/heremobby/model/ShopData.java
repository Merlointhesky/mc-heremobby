package com.heremobby.heremobby.model;

import java.util.List;

public class ShopData {
    private int maxItems = 27;
    private List<String> activeCategories = List.of("weapons", "armor", "materials");
    private EnchantSettings enchants = new EnchantSettings();
    private long refreshCost = 0;

    public int getMaxItems() {
        return maxItems;
    }

    public List<String> getActiveCategories() {
        return activeCategories;
    }

    public EnchantSettings getEnchants() {
        return enchants;
    }

    public long getRefreshCost() {
        return refreshCost;
    }

    public static class EnchantSettings {
        private double chance = 0.2;
        private int maxEnchants = 2;
        private int maxLevel = 3;

        public double getChance() {
            return chance;
        }

        public int getMaxEnchants() {
            return maxEnchants;
        }

        public int getMaxLevel() {
            return maxLevel;
        }
    }
}
