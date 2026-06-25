package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.command.HereMobbyCommand;
import com.heremobby.heremobby.command.SetupSession;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SetupWizardListener implements Listener {

    private boolean isSetupDummy(Entity entity) {
        if (entity == null) return false;
        NamespacedKey key = new NamespacedKey(HereMobbyPlugin.getInstance(), "setup_dummy");
        return entity.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!isSetupDummy(clicked)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        SetupSession session = HereMobbyCommand.activeSessions.get(player.getUniqueId());

        if (session == null || !session.getDummyEntity().getUniqueId().equals(clicked.getUniqueId())) {
            player.sendMessage("§cYou do not own this setup dummy!");
            return;
        }

        LivingEntity dummy = (LivingEntity) clicked;
        var eq = dummy.getEquipment();
        if (eq == null) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem != null && handItem.getType() != Material.AIR) {
            ItemStack equipItem = handItem.clone();
            equipItem.setAmount(1);
            String matName = handItem.getType().name();

            if (matName.endsWith("_HELMET") || matName.endsWith("_SKULL") || matName.endsWith("_HEAD") || handItem.getType() == Material.CARVED_PUMPKIN) {
                if (eq.getHelmet() != null && eq.getHelmet().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getHelmet());
                }
                eq.setHelmet(equipItem);
            } else if (matName.endsWith("_CHESTPLATE") || handItem.getType() == Material.ELYTRA) {
                if (eq.getChestplate() != null && eq.getChestplate().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getChestplate());
                }
                eq.setChestplate(equipItem);
            } else if (matName.endsWith("_LEGGINGS")) {
                if (eq.getLeggings() != null && eq.getLeggings().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getLeggings());
                }
                eq.setLeggings(equipItem);
            } else if (matName.endsWith("_BOOTS")) {
                if (eq.getBoots() != null && eq.getBoots().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getBoots());
                }
                eq.setBoots(equipItem);
            } else if (handItem.getType() == Material.SHIELD) {
                if (eq.getItemInOffHand() != null && eq.getItemInOffHand().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getItemInOffHand());
                }
                eq.setItemInOffHand(equipItem);
            } else {
                if (eq.getItemInMainHand() != null && eq.getItemInMainHand().getType() != Material.AIR) {
                    giveOrDropItem(player, eq.getItemInMainHand());
                }
                eq.setItemInMainHand(equipItem);
            }

            handItem.setAmount(handItem.getAmount() - 1);
            player.sendMessage("§aEquipped item: " + equipItem.getType().name());
        } else {
            // Strip items one-by-one back to the player
            if (eq.getItemInMainHand() != null && eq.getItemInMainHand().getType() != Material.AIR) {
                ItemStack item = eq.getItemInMainHand().clone();
                eq.setItemInMainHand(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Main Hand item.");
            } else if (eq.getItemInOffHand() != null && eq.getItemInOffHand().getType() != Material.AIR) {
                ItemStack item = eq.getItemInOffHand().clone();
                eq.setItemInOffHand(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Off Hand item.");
            } else if (eq.getHelmet() != null && eq.getHelmet().getType() != Material.AIR) {
                ItemStack item = eq.getHelmet().clone();
                eq.setHelmet(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Helmet.");
            } else if (eq.getChestplate() != null && eq.getChestplate().getType() != Material.AIR) {
                ItemStack item = eq.getChestplate().clone();
                eq.setChestplate(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Chestplate.");
            } else if (eq.getLeggings() != null && eq.getLeggings().getType() != Material.AIR) {
                ItemStack item = eq.getLeggings().clone();
                eq.setLeggings(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Leggings.");
            } else if (eq.getBoots() != null && eq.getBoots().getType() != Material.AIR) {
                ItemStack item = eq.getBoots().clone();
                eq.setBoots(null);
                giveOrDropItem(player, item);
                player.sendMessage("§eRemoved Boots.");
            } else {
                player.sendMessage("§7The dummy has no equipment to remove.");
            }
        }

        HereMobbyCommand.sendSetupMenu(player, session);
    }

    private void giveOrDropItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        var remaining = player.getInventory().addItem(item);
        if (!remaining.isEmpty()) {
            for (ItemStack leftover : remaining.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SetupSession session = HereMobbyCommand.activeSessions.get(player.getUniqueId());

        if (session == null || session.getInputState() == SetupSession.InputState.NONE) return;

        event.setCancelled(true);
        String msg = event.getMessage().trim();

        if (msg.equalsIgnoreCase("cancel")) {
            session.setInputState(SetupSession.InputState.NONE);
            player.sendMessage("§eEditing cancelled.");
            HereMobbyCommand.sendSetupMenu(player, session);
            return;
        }

        SetupSession.InputState state = session.getInputState();
        session.setInputState(SetupSession.InputState.NONE);

        org.bukkit.Bukkit.getScheduler().runTask(HereMobbyPlugin.getInstance(), () -> {
            switch (state) {
                case AWAITING_NAME:
                    session.setDisplayName(msg);
                    LivingEntity dummy = (LivingEntity) session.getDummyEntity();
                    if (dummy != null && !dummy.isDead()) {
                        dummy.setCustomName("§6§lDummy: §e" + msg);
                    }
                    player.sendMessage("§aDisplayName updated to: " + msg);
                    break;
                case AWAITING_HEALTH:
                    try {
                        double val = Double.parseDouble(msg);
                        session.setMaxHealth(val);
                        player.sendMessage("§aMax Health updated to: " + (val <= 0 ? "Default" : val));
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number! Max Health not changed.");
                    }
                    break;
                case AWAITING_KROIN:
                    try {
                        long val = Long.parseLong(msg);
                        if (val < 0) {
                            player.sendMessage("§cKroin reward must be 0 or positive!");
                        } else {
                             session.setKroinReward(val);
                             player.sendMessage("§aKroin Reward updated to: " + val);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number! Kroin Reward not changed.");
                    }
                    break;
                case AWAITING_XP:
                    try {
                        int val = Integer.parseInt(msg);
                        session.setXpReward(val);
                        player.sendMessage("§aXP Reward updated to: " + (val < 0 ? "Default" : val));
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number! XP Reward not changed.");
                    }
                    break;
            }
            HereMobbyCommand.sendSetupMenu(player, session);
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isSetupDummy(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (isSetupDummy(event.getEntity()) || isSetupDummy(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (isSetupDummy(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SetupSession session = HereMobbyCommand.activeSessions.remove(event.getPlayer().getUniqueId());
        if (session != null) {
            if (session.getDummyEntity() != null) {
                session.getDummyEntity().remove();
            }
        }
    }
}
