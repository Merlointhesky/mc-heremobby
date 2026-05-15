package com.heremobby.heremobby.shop;

import com.heremobby.heremobby.economy.BankManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class ShopGUI {
    private final ShopManager shopManager;
    private final BankManager bankManager;

    public ShopGUI(ShopManager shopManager, BankManager bankManager) {
        this.shopManager = shopManager;
        this.bankManager = bankManager;
    }

    public void open(Player player) {
        int size = 45; // 5 rows
        Inventory inv = Bukkit.createInventory(null, size, "§6HereMobby Shop");

        List<ItemStack> items = shopManager.getCurrentShopItems();
        for (int i = 0; i < Math.min(items.size(), 36); i++) {
            inv.setItem(i, items.get(i).clone());
        }

        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, filler);
        }

        // Refresh icon
        long refreshCost = shopManager.getDataManager().getShopData().getRefreshCost();
        ItemStack refresh = new ItemStack(Material.LEVER);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName("§bRefresh Shop");
        String costText = refreshCost == 0 ? "§aFree" : "§e" + refreshCost + " Kroins";
        refreshMeta.setLore(Collections.singletonList("§7Cost: " + costText));
        refresh.setItemMeta(refreshMeta);
        inv.setItem(40, refresh);

        // Balance icon
        ItemStack balance = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balance.getItemMeta();
        balanceMeta.setDisplayName("§6Your Balance");
        balanceMeta.setLore(Collections.singletonList("§e" + bankManager.getBalance(player.getUniqueId()) + " Kroins"));
        balance.setItemMeta(balanceMeta);
        inv.setItem(44, balance);

        player.openInventory(inv);
    }
}
