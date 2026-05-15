package com.heremobby.heremobby.model;

import java.util.HashMap;
import java.util.Map;

public class PricesData {
    private Map<String, Map<String, Long>> categories = new HashMap<>();
    private Map<String, EnchantCost> enchantments = new HashMap<>();

    public PricesData() {
        // Default categories
        Map<String, Long> weapons = new HashMap<>();
        weapons.put("IRON_SWORD", 100L);
        weapons.put("DIAMOND_SWORD", 500L);
        categories.put("weapons", weapons);

        Map<String, Long> armor = new HashMap<>();
        armor.put("IRON_CHESTPLATE", 150L);
        armor.put("DIAMOND_CHESTPLATE", 800L);
        categories.put("armor", armor);

        Map<String, Long> materials = new HashMap<>();
        materials.put("IRON_INGOT", 20L);
        materials.put("GOLD_INGOT", 50L);
        categories.put("materials", materials);

        // Default enchants
        enchantments.put("SHARPNESS", new EnchantCost(50, 1.2));
        enchantments.put("PROTECTION", new EnchantCost(50, 1.2));
    }

    public Map<String, Map<String, Long>> getCategories() {
        return categories;
    }

    public Map<String, EnchantCost> getEnchantments() {
        return enchantments;
    }

    public static class EnchantCost {
        private long baseValue;
        private double levelMultiplier;

        public EnchantCost() {}

        public EnchantCost(long baseValue, double levelMultiplier) {
            this.baseValue = baseValue;
            this.levelMultiplier = levelMultiplier;
        }

        public long getBaseValue() {
            return baseValue;
        }

        public double getLevelMultiplier() {
            return levelMultiplier;
        }

        public long calculateCost(int level) {
            return (long) (baseValue + (baseValue * levelMultiplier * (level - 1)));
        }
    }
}
