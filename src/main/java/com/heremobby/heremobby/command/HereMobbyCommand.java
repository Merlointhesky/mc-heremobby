package com.heremobby.heremobby.command;

import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.shop.ShopGUI;
import com.heremobby.heremobby.shop.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HereMobbyCommand implements CommandExecutor {
    private final InfoGUI infoGUI;
    private final ShopGUI shopGUI;
    private final DataManager dataManager;
    private final ShopManager shopManager;

    public HereMobbyCommand(InfoGUI infoGUI, ShopGUI shopGUI, DataManager dataManager, ShopManager shopManager) {
        this.infoGUI = infoGUI;
        this.shopGUI = shopGUI;
        this.dataManager = dataManager;
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (!player.hasPermission("heremobby.use")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                infoGUI.open(player);
                break;
            case "shop":
                if (!player.hasPermission("heremobby.use")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                shopGUI.open(player);
                break;
            case "reload":
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                dataManager.reload();
                shopManager.refreshShop();
                player.sendMessage("§aHereMobby configuration reloaded!");
                break;
            default:
                return false;
        }

        return true;
    }
}
