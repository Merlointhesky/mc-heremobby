package com.heremobby.heremobby.command;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.model.CustomBoss;
import com.heremobby.heremobby.model.CustomMob;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HereMobbyCommand implements CommandExecutor, TabCompleter {
    public static final Map<UUID, SetupSession> activeSessions = new ConcurrentHashMap<>();
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
                    mobManager.spawnCustomBoss(boss.get(), player.getLocation());
                    player.sendMessage("§aSpawned custom boss: " + boss.get().getDisplayName());
                    return true;
                }
                // Check custom mobs
                var mob = dataManager.getCustomMobs().stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst();
                if (mob.isPresent()) {
                    mobManager.spawnCustomMob(mob.get(), player.getLocation());
                    player.sendMessage("§aSpawned custom mob: " + mob.get().getDisplayName());
                    return true;
                }
                player.sendMessage("§cCould not find a custom mob or boss with ID/name: " + id);
                break;
            case "kill":
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    Entity target = player.getTargetEntity(10);
                    if (target instanceof LivingEntity living && (mobManager.getCustomBossConfig(target).isPresent() || mobManager.getCustomMobConfig(target).isPresent())) {
                        living.damage(living.getHealth() + 99999.0, player);
                        player.sendMessage("§aKilled looked-at custom entity: " + target.getName());
                        return true;
                    }
                    player.sendMessage("§cUsage: /heremobby kill <id> or look at a custom entity and run `/heremobby kill`");
                    return true;
                }
                String killId = args[1];
                Entity nearest = null;
                double nearestDist = Double.MAX_VALUE;
                for (Entity e : player.getWorld().getEntities()) {
                    if (e instanceof LivingEntity && !e.isDead()) {
                        var bossOpt = mobManager.getCustomBossConfig(e);
                        if (bossOpt.isPresent() && bossOpt.get().getId().equalsIgnoreCase(killId)) {
                            double dist = player.getLocation().distanceSquared(e.getLocation());
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearest = e;
                            }
                        }
                        var mobOpt = mobManager.getCustomMobConfig(e);
                        if (mobOpt.isPresent() && mobOpt.get().getId().equalsIgnoreCase(killId)) {
                            double dist = player.getLocation().distanceSquared(e.getLocation());
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearest = e;
                            }
                        }
                    }
                }
                if (nearest != null) {
                    ((LivingEntity) nearest).damage(((LivingEntity) nearest).getHealth() + 99999.0, player);
                    player.sendMessage("§aKilled nearest custom entity: " + nearest.getName());
                } else {
                    player.sendMessage("§cCould not find any active custom entity with ID: " + killId);
                }
                break;
            case "setup": {
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /heremobby setup <id> <baseType> <boss|mob>");
                    return true;
                }
                String setupId = args[1].toLowerCase();
                String baseTypeStr = args[2].toUpperCase();
                String typeFlag = args[3].toLowerCase();

                if (!typeFlag.equals("boss") && !typeFlag.equals("mob")) {
                    player.sendMessage("§cType must be either 'boss' or 'mob'.");
                    return true;
                }

                org.bukkit.entity.EntityType entityType;
                try {
                    entityType = org.bukkit.entity.EntityType.valueOf(baseTypeStr);
                    if (!entityType.isAlive()) {
                        player.sendMessage("§cBase type must be a living entity type.");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid entity type: " + baseTypeStr);
                    return true;
                }

                if (activeSessions.containsKey(player.getUniqueId())) {
                    player.sendMessage("§cYou already have an active setup session! Use /heremobby setupedit cancel or save.");
                    return true;
                }

                LivingEntity dummy = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityType);
                dummy.setAI(false);
                dummy.setSilent(true);
                dummy.setInvulnerable(true);
                dummy.setCustomName("§6§lDummy: §e" + setupId);
                dummy.setCustomNameVisible(true);

                NamespacedKey key = new NamespacedKey(HereMobbyPlugin.getInstance(), "setup_dummy");
                dummy.getPersistentDataContainer().set(key, PersistentDataType.STRING, setupId);

                boolean isBoss = typeFlag.equals("boss");
                SetupSession session = new SetupSession(player.getUniqueId(), dummy, setupId, baseTypeStr, isBoss);
                activeSessions.put(player.getUniqueId(), session);

                player.sendMessage("§aStarted setup session for " + (isBoss ? "boss" : "mob") + " '" + setupId + "'!");
                sendSetupMenu(player, session);
                break;
            }
            case "setupedit": {
                if (!player.hasPermission("heremobby.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /heremobby setupedit <name|health|kroin|xp|save|cancel>");
                    return true;
                }
                SetupSession session = activeSessions.get(player.getUniqueId());
                if (session == null) {
                    player.sendMessage("§cYou do not have an active setup session.");
                    return true;
                }
                String action = args[1].toLowerCase();
                switch (action) {
                    case "name":
                        session.setInputState(SetupSession.InputState.AWAITING_NAME);
                        player.sendMessage("§6Please type the custom DisplayName in chat. Or type 'cancel' to stop.");
                        break;
                    case "health":
                        session.setInputState(SetupSession.InputState.AWAITING_HEALTH);
                        player.sendMessage("§6Please type the Max Health in chat (number, or -1 for default).");
                        break;
                    case "kroin":
                        session.setInputState(SetupSession.InputState.AWAITING_KROIN);
                        player.sendMessage("§6Please type the Kroin Reward on kill (0 or positive number).");
                        break;
                    case "xp":
                        session.setInputState(SetupSession.InputState.AWAITING_XP);
                        player.sendMessage("§6Please type the XP Reward on kill (number, or -1 for default).");
                        break;
                    case "cancel":
                        session.getDummyEntity().remove();
                        activeSessions.remove(player.getUniqueId());
                        player.sendMessage("§cSetup session cancelled. Dummy despawned.");
                        break;
                    case "save":
                        handleSaveSession(player, session);
                        break;
                    default:
                        player.sendMessage("§cUnknown setupedit action: " + action);
                        break;
                }
                break;
            }
            default:
                return false;
        }

        return true;
    }

    public static void sendSetupMenu(Player player, SetupSession session) {
        player.sendMessage("§e==================================================");
        player.sendMessage("§6§lHereMobby Setup Wizard: §e" + session.getId() + " §7(" + (session.isBoss() ? "BOSS" : "MOB") + " - " + session.getBaseType() + ")");
        player.sendMessage("§e==================================================");

        // DisplayName row
        TextComponent nameRow = new TextComponent("§7DisplayName: §f" + session.getDisplayName() + " ");
        TextComponent nameEdit = new TextComponent("§a§l[Click to Edit]");
        nameEdit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit name"));
        nameEdit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eSet the mob's custom name in chat")));
        nameRow.addExtra(nameEdit);
        player.spigot().sendMessage(nameRow);

        // Max Health row
        double health = session.getMaxHealth();
        TextComponent healthRow = new TextComponent("§7Max Health: §f" + (health <= 0 ? "Default" : health) + " ");
        TextComponent healthEdit = new TextComponent("§a§l[Click to Edit]");
        healthEdit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit health"));
        healthEdit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eSet the mob's maximum health")));
        healthRow.addExtra(healthEdit);
        player.spigot().sendMessage(healthRow);

        // Kroin Reward row
        TextComponent kroinRow = new TextComponent("§7Kroin Reward: §f" + session.getKroinReward() + " ");
        TextComponent kroinEdit = new TextComponent("§a§l[Click to Edit]");
        kroinEdit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit kroin"));
        kroinEdit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eSet the Kroin currency reward on kill")));
        kroinRow.addExtra(kroinEdit);
        player.spigot().sendMessage(kroinRow);

        // XP Reward row
        int xp = session.getXpReward();
        TextComponent xpRow = new TextComponent("§7XP Reward: §f" + (xp < 0 ? "Default" : xp) + " ");
        TextComponent xpEdit = new TextComponent("§a§l[Click to Edit]");
        xpEdit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit xp"));
        xpEdit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eSet the XP reward on kill")));
        xpRow.addExtra(xpEdit);
        player.spigot().sendMessage(xpRow);

        // Equipment display
        LivingEntity dummy = (LivingEntity) session.getDummyEntity();
        var eq = dummy.getEquipment();
        String mainHand = eq != null && eq.getItemInMainHand() != null && eq.getItemInMainHand().getType() != org.bukkit.Material.AIR ? eq.getItemInMainHand().getType().name() : "None";
        String offHand = eq != null && eq.getItemInOffHand() != null && eq.getItemInOffHand().getType() != org.bukkit.Material.AIR ? eq.getItemInOffHand().getType().name() : "None";
        String helmet = eq != null && eq.getHelmet() != null && eq.getHelmet().getType() != org.bukkit.Material.AIR ? eq.getHelmet().getType().name() : "None";
        String chest = eq != null && eq.getChestplate() != null && eq.getChestplate().getType() != org.bukkit.Material.AIR ? eq.getChestplate().getType().name() : "None";
        String legs = eq != null && eq.getLeggings() != null && eq.getLeggings().getType() != org.bukkit.Material.AIR ? eq.getLeggings().getType().name() : "None";
        String boots = eq != null && eq.getBoots() != null && eq.getBoots().getType() != org.bukkit.Material.AIR ? eq.getBoots().getType().name() : "None";

        player.sendMessage("§7Equipment:");
        player.sendMessage("  §7Main Hand: §f" + mainHand);
        player.sendMessage("  §7Off Hand:  §f" + offHand);
        player.sendMessage("  §7Helmet:    §f" + helmet);
        player.sendMessage("  §7Chest:     §f" + chest);
        player.sendMessage("  §7Leggings:  §f" + legs);
        player.sendMessage("  §7Boots:     §f" + boots);
        player.sendMessage("§7§oRight-click dummy with an item to equip. Right-click empty-handed to remove items.");
        player.sendMessage("§e--------------------------------------------------");

        // Save & Cancel buttons
        TextComponent footer = new TextComponent("");
        TextComponent saveBtn = new TextComponent("§a§l[Save & Finish]     ");
        saveBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit save"));
        saveBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aSave custom mob configuration to file")));
        
        TextComponent cancelBtn = new TextComponent("§c§l[Cancel & Discard]");
        cancelBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heremobby setupedit cancel"));
        cancelBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cDiscard setup and despawn dummy")));

        footer.addExtra(saveBtn);
        footer.addExtra(cancelBtn);
        player.spigot().sendMessage(footer);
        player.sendMessage("§e==================================================");
    }

    private void handleSaveSession(Player player, SetupSession session) {
        LivingEntity dummy = (LivingEntity) session.getDummyEntity();
        if (dummy.isDead()) {
            player.sendMessage("§cThe setup dummy was killed/despawned! Cannot save.");
            activeSessions.remove(player.getUniqueId());
            return;
        }

        if (session.isBoss()) {
            CustomBoss boss = new CustomBoss();
            boss.setId(session.getId());
            boss.setDisplayName(session.getDisplayName());
            boss.setBaseType(session.getBaseType());
            boss.setMaxHealth(session.getMaxHealth());
            boss.setKroinReward(session.getKroinReward());
            boss.setXpReward(session.getXpReward());
            boss.setEquipment(getEquipmentFromEntity(dummy));
            boss.setScale(1.0);
            boss.setDefense(0);
            boss.setRespawnSeconds(300); // 5 minutes default
            boss.setSpells(new ArrayList<>());
            boss.setCustomLoot(new ArrayList<>());

            CustomBoss.LocationData loc = new CustomBoss.LocationData();
            loc.setWorld(dummy.getWorld().getName());
            loc.setX(dummy.getLocation().getX());
            loc.setY(dummy.getLocation().getY());
            loc.setZ(dummy.getLocation().getZ());
            boss.setLocation(loc);

            dataManager.saveFile("custom_bosses/" + session.getId() + ".json", boss);
        } else {
            CustomMob mob = new CustomMob();
            mob.setId(session.getId());
            mob.setDisplayName(session.getDisplayName());
            mob.setBaseType(session.getBaseType());
            mob.setMaxHealth(session.getMaxHealth());
            mob.setKroinReward(session.getKroinReward());
            mob.setXpReward(session.getXpReward());
            mob.setEquipment(getEquipmentFromEntity(dummy));
            mob.setScale(1.0);
            mob.setDefense(0);

            CustomMob.SpawnConditions sc = new CustomMob.SpawnConditions();
            sc.setChance(0.05);
            sc.setTime("BOTH");
            sc.setMinLight(0);
            sc.setMaxLight(15);
            sc.setBiomes(List.of("PLAINS"));
            mob.setSpawnConditions(sc);
            mob.setCustomLoot(new ArrayList<>());
            mob.setSpells(new ArrayList<>());

            dataManager.saveFile("custom_mobs/" + session.getId() + ".json", mob);
        }

        // Cleanup
        dummy.remove();
        activeSessions.remove(player.getUniqueId());
        
        // Reload configurations
        dataManager.reload();

        player.sendMessage("§aSuccessfully saved custom entity §e" + session.getId() + "§a and reloaded configuration!");
    }

    private static CustomMob.Equipment getEquipmentFromEntity(LivingEntity entity) {
        var eq = entity.getEquipment();
        if (eq == null) return null;

        CustomMob.Equipment customEq = new CustomMob.Equipment();
        
        if (eq.getItemInMainHand() != null && eq.getItemInMainHand().getType() != org.bukkit.Material.AIR) {
            customEq.setMainHand(eq.getItemInMainHand().getType().name());
        }
        if (eq.getItemInOffHand() != null && eq.getItemInOffHand().getType() != org.bukkit.Material.AIR) {
            customEq.setOffHand(eq.getItemInOffHand().getType().name());
        }
        if (eq.getHelmet() != null && eq.getHelmet().getType() != org.bukkit.Material.AIR) {
            customEq.setHelmet(eq.getHelmet().getType().name());
        }
        if (eq.getChestplate() != null && eq.getChestplate().getType() != org.bukkit.Material.AIR) {
            customEq.setChestplate(eq.getChestplate().getType().name());
        }
        if (eq.getLeggings() != null && eq.getLeggings().getType() != org.bukkit.Material.AIR) {
            customEq.setLeggings(eq.getLeggings().getType().name());
        }
        if (eq.getBoots() != null && eq.getBoots().getType() != org.bukkit.Material.AIR) {
            customEq.setBoots(eq.getBoots().getType().name());
        }

        // Check if equipment is empty
        if (customEq.getMainHand() == null && customEq.getOffHand() == null &&
            customEq.getHelmet() == null && customEq.getChestplate() == null &&
            customEq.getLeggings() == null && customEq.getBoots() == null) {
            return null;
        }

        return customEq;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("info", "reload", "spawn", "kill", "setup", "setupedit").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("kill"))) {
            List<String> options = new ArrayList<>();
            options.addAll(dataManager.getCustomBosses().stream().map(CustomBoss::getId).collect(Collectors.toList()));
            options.addAll(dataManager.getCustomMobs().stream().map(CustomMob::getId).collect(Collectors.toList()));
            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setupedit")) {
            return Arrays.asList("name", "health", "kroin", "xp", "save", "cancel").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length == 3) {
                return Arrays.stream(org.bukkit.entity.EntityType.values())
                        .filter(org.bukkit.entity.EntityType::isAlive)
                        .map(Enum::name)
                        .filter(s -> s.startsWith(args[2].toUpperCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 4) {
                return Arrays.asList("boss", "mob").stream()
                        .filter(s -> s.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
