package com.heremobby.heremobby.shop;

import com.heremobby.heremobby.economy.BankManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopListener implements Listener {
    private final ShopManager shopManager;
    private final BankManager bankManager;
    private final ShopGUI shopGUI;

    public ShopListener(ShopManager shopManager, BankManager bankManager, ShopGUI shopGUI) {
        this.shopManager = shopManager;
        this.bankManager = bankManager;
        this.shopGUI = shopGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6HereMobby Shop")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.LEVER) {
            long refreshCost = shopManager.getDataManager().getShopData().getRefreshCost();
            if (bankManager.withdraw(player.getUniqueId(), refreshCost)) {
                shopManager.refreshShop();
                shopGUI.open(player);
                player.sendMessage("§aShop refreshed!");
            } else {
                player.sendMessage("§cYou don't have enough Kroins to refresh the shop!");
            }
            return;
        }

        if (clicked.getType() == Material.GOLD_INGOT || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Handle item purchase
        long price = shopManager.calculatePrice(clicked);
        if (bankManager.withdraw(player.getUniqueId(), price)) {
            ItemStack toGive = clicked.clone();
            ItemMeta meta = toGive.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 3) {
                // Remove the last 3 lines of shop lore
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
                meta.setLore(lore);
            }
            toGive.setItemMeta(meta);
            
            player.getInventory().addItem(toGive);
            player.sendMessage("§aYou purchased §e" + (meta.hasDisplayName() ? meta.getDisplayName() : clicked.getType().name()) + " §afor §e" + price + " Kroins!");
            
            // Refresh GUI to show new balance
            shopGUI.open(player);
        } else {
            player.sendMessage("§cYou don't have enough Kroins!");
        }
    }
}
