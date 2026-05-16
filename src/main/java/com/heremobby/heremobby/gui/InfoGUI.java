package com.heremobby.heremobby.gui;

import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.model.CustomMob;
import com.heremobby.heremobby.model.CustomBoss;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InfoGUI {
    private final DataManager dataManager;

    public InfoGUI(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6HereMobby Info");

        // Player Info (Minimal now as HereShoppy handles economy)
        ItemStack playerInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta pMeta = playerInfo.getItemMeta();
        pMeta.setDisplayName("§e" + player.getName());
        List<String> pLore = new ArrayList<>();
        pLore.add("§7Use §f/hereshoppy info §7for economy stats.");
        pMeta.setLore(pLore);
        playerInfo.setItemMeta(pMeta);
        inv.setItem(4, playerInfo);

        // Custom Mobs Info
        int slot = 9;
        for (CustomMob mob : dataManager.getCustomMobs()) {
            if (slot >= 27) break;
            Material mat = Material.matchMaterial(mob.getBaseType());
            ItemStack item = new ItemStack(mat != null ? mat : Material.ZOMBIE_SPAWN_EGG);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§b" + (mob.getDisplayName() != null ? mob.getDisplayName() : mob.getBaseType()));
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §f" + mob.getBaseType());
            lore.add("§7Reward: §e" + mob.getKroinReward() + " Kroins");
            if (mob.getCustomLoot() != null) {
                lore.add("§7Loot:");
                for (CustomMob.LootItem loot : mob.getCustomLoot()) {
                    lore.add(" §8- §f" + loot.getMaterial() + " (§7" + (int)(loot.getChance() * 100) + "%§f)");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // Custom Bosses Info
        slot = 27;
        for (CustomBoss boss : dataManager.getCustomBosses()) {
            if (slot >= 45) break;
            ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c§lBOSS: " + (boss.getDisplayName() != null ? boss.getDisplayName() : boss.getBaseType()));
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §f" + boss.getBaseType());
            lore.add("§7Reward: §e" + boss.getKroinReward() + " Kroins");
            lore.add("§7Respawn: §f" + boss.getRespawnSeconds() + "s");
            if (boss.getCustomLoot() != null) {
                lore.add("§7Loot:");
                for (CustomMob.LootItem loot : boss.getCustomLoot()) {
                    lore.add(" §8- §f" + loot.getMaterial() + " (§7" + (int)(loot.getChance() * 100) + "%§f)");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }
}
