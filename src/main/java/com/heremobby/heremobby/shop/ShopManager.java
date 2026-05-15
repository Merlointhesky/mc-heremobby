package com.heremobby.heremobby.shop;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.model.CustomItem;
import com.heremobby.heremobby.model.PricesData;
import com.heremobby.heremobby.model.ShopData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopManager {
    private final DataManager dataManager;
    private final Random random = new Random();
    private List<ItemStack> currentShopItems = new ArrayList<>();

    public ShopManager(DataManager dataManager) {
        this.dataManager = dataManager;
        refreshShop();
    }

    public void refreshShop() {
        currentShopItems.clear();
        ShopData shop = dataManager.getShopData();
        
        int count = 0;
        int maxAttempts = shop.getMaxItems() * 2;
        int attempts = 0;
        
        while (count < shop.getMaxItems() && attempts < maxAttempts) {
            attempts++;
            ItemStack item = generateRandomLoot();
            if (item != null) {
                currentShopItems.add(item);
                count++;
            }
        }
    }

    private ItemStack generateRandomLoot() {
        boolean isWeapon = random.nextBoolean();
        String materialPrefix = getRandomMaterial();
        Material mat;

        if (isWeapon) {
            String[] weapons = {"SWORD", "AXE", "BOW", "CROSSBOW", "MACE", "SHIELD", "TRIDENT"};
            String type = weapons[random.nextInt(weapons.length)];
            if (type.equals("BOW") || type.equals("CROSSBOW") || type.equals("SHIELD") || type.equals("TRIDENT") || type.equals("MACE")) {
                mat = Material.matchMaterial(type);
            } else {
                mat = Material.matchMaterial(materialPrefix + "_" + type);
            }
        } else {
            String[] armorTypes = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
            String type = armorTypes[random.nextInt(armorTypes.length)];
            if (materialPrefix.equals("TURTLE") && type.equals("HELMET")) {
                mat = Material.TURTLE_HELMET;
            } else {
                mat = Material.matchMaterial(materialPrefix + "_" + type);
            }
        }

        if (mat == null) return null;
        ItemStack item = new ItemStack(mat);

        if (random.nextDouble() < 0.70) {
            applyRandomEnchantsNew(item);
        }

        long price = calculatePrice(item);
        setShopLore(item, price);
        return item;
    }

    private String getRandomMaterial() {
        String[] materials = {"LEATHER", "CHAINMAIL", "IRON", "GOLD", "DIAMOND", "TURTLE"};
        return materials[random.nextInt(materials.length)];
    }

    private void applyRandomEnchantsNew(ItemStack item) {
        List<Enchantment> available = new ArrayList<>();
        for (Enchantment ench : Enchantment.values()) {
            if (!ench.isCursed() && ench.canEnchantItem(item)) {
                available.add(ench);
            }
        }
        if (available.isEmpty()) return;

        int count = 0;
        Set<Enchantment> added = new HashSet<>();
        while (count < 5) {
            Enchantment ench = available.get(random.nextInt(available.size()));
            if (!added.contains(ench)) {
                // Ensure the level is within reasonable bounds (1 to max)
                int level = random.nextInt(ench.getMaxLevel()) + 1;
                item.addUnsafeEnchantment(ench, level);
                added.add(ench);
                count++;
            }
            if (random.nextDouble() > 0.20) break;
        }
    }

    private ItemStack createShopItem(CustomItem ci) {
        Material mat = Material.matchMaterial(ci.getMaterial());
        if (mat == null) return null;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (ci.getName() != null) meta.setDisplayName(ci.getName());
        item.setItemMeta(meta);

        if (ci.getEnchants() != null) {
            for (Map.Entry<String, Integer> entry : ci.getEnchants().entrySet()) {
                Enchantment ench = Enchantment.getByName(entry.getKey().toUpperCase());
                if (ench != null) {
                    item.addUnsafeEnchantment(ench, entry.getValue());
                }
            }
        }

        long price = ci.getCustomPrice() >= 0 ? ci.getCustomPrice() : calculatePrice(item);
        setShopLore(item, price);

        return item;
    }

    public long calculatePrice(ItemStack item) {
        long basePrice = 10; // Fallback
        String materialName = item.getType().name();
        
        // Find material in categories
        outer: for (Map<String, Long> cat : dataManager.getPricesData().getCategories().values()) {
            if (cat.containsKey(materialName)) {
                basePrice = cat.get(materialName);
                break outer;
            }
        }

        long enchantCost = 0;
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            PricesData.EnchantCost costConfig = dataManager.getPricesData().getEnchantments().get(entry.getKey().getKey().getKey().toUpperCase());
            if (costConfig != null) {
                enchantCost += costConfig.calculateCost(entry.getValue());
            } else {
                enchantCost += 50 * entry.getValue(); // Fallback
            }
        }

        return basePrice + enchantCost;
    }

    private void setShopLore(ItemStack item, long price) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add("");
        lore.add("§7Price: §e" + price + " Kroins");
        lore.add("§eClick to purchase!");
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public List<ItemStack> getCurrentShopItems() {
        return currentShopItems;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
