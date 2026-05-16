package com.heremobby.heremobby.command;

import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.model.CustomBoss;
import com.heremobby.heremobby.model.CustomMob;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HereMobbyCommand implements CommandExecutor, TabCompleter {
    private final InfoGUI infoGUI;
    private final DataManager dataManager;
    private final MobManager mobManager;

    public HereMobbyCommand(InfoGUI infoGUI, DataManager dataManager, MobManager mobManager) {
        this.infoGUI = infoGUI;
        this.dataManager = dataManager;
        this.mobManager = mobManager;
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
            case "reload":
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                dataManager.reload();
                player.sendMessage("§aHereMobby configuration reloaded!");
                break;
            case "spawn":
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /heremobby spawn <id>");
                    return true;
                }
                String id = args[1];
                // Check custom bosses first
                var boss = dataManager.getCustomBosses().stream().filter(b -> b.getId().equalsIgnoreCase(id)).findFirst();
                if (boss.isPresent()) {
                    mobManager.spawnCustomBoss(boss.get());
                    player.sendMessage("§aSpawned custom boss: " + boss.get().getDisplayName());
                    return true;
                }
                // Check custom mobs
                var mob = dataManager.getCustomMobs().stream().filter(m -> m.getDisplayName().equalsIgnoreCase(id)).findFirst();
                if (mob.isPresent()) {
                    mobManager.spawnCustomMob(mob.get(), player.getLocation());
                    player.sendMessage("§aSpawned custom mob: " + mob.get().getDisplayName());
                    return true;
                }
                player.sendMessage("§cCould not find a custom mob or boss with ID/name: " + id);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("info", "reload", "spawn").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            List<String> options = new ArrayList<>();
            options.addAll(dataManager.getCustomBosses().stream().map(CustomBoss::getId).collect(Collectors.toList()));
            options.addAll(dataManager.getCustomMobs().stream().map(CustomMob::getDisplayName).collect(Collectors.toList()));
            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
