package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.model.CustomBoss;
import com.heremobby.heremobby.model.CustomMob;
import com.heremobby.heremobby.config.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class MobListener implements Listener {
    private final HereMobbyPlugin plugin;
    private final MobManager mobManager;
    private final DataManager dataManager;
    private final Random random = new Random();
    private final boolean hereShoppyEnabled;

    public MobListener(HereMobbyPlugin plugin, MobManager mobManager, DataManager dataManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
        this.dataManager = dataManager;
        this.hereShoppyEnabled = Bukkit.getPluginManager().isPluginEnabled("HereShoppy");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // Captain Hook melee attack check (damage = 10, 15% chance for 10s stun)
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent edbe) {
            if (edbe.getDamager() instanceof LivingEntity damager) {
                var damagerBossConfig = mobManager.getCustomBossConfig(damager);
                if (damagerBossConfig.isPresent() && "captain_hook".equals(damagerBossConfig.get().getId())) {
                    event.setDamage(10.0);
                    if (random.nextDouble() < 0.15) {
                        final NamespacedKey stunKey = new NamespacedKey(plugin, "stunned_by_hook");
                        org.bukkit.entity.ArmorStand tether = com.heremobby.heremobby.util.SpellUtils.immobilize(entity, stunKey);
                        entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0f, 1.5f);
                        if (entity instanceof Player p) {
                            p.sendMessage("§cYou are stunned by Captain Hook!");
                        }
                        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (tether.isValid()) {
                                tether.remove();
                            }
                        }, 200L); // 10 seconds
                    }
                }
            }
        }

        double defense = 0;
        var bossConfig = mobManager.getCustomBossConfig(entity);
        if (bossConfig.isPresent()) {
            CustomBoss boss = bossConfig.get();
            double maxHealth = entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            double finalHealth = entity.getHealth() - event.getFinalDamage();

            // Captain Hook 20% health trigger (Giant pufferfish)
            if ("captain_hook".equals(boss.getId()) && !entity.getScoreboardTags().contains("triggered_puffer")) {
                if (finalHealth <= maxHealth * 0.2) {
                    entity.addScoreboardTag("triggered_puffer");
                    entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_PUFFER_FISH_BLOW_UP, 2.0f, 0.5f);
                    
                    org.bukkit.entity.PufferFish puffer = (org.bukkit.entity.PufferFish) entity.getWorld().spawnEntity(entity.getLocation(), org.bukkit.entity.EntityType.PUFFERFISH);
                    puffer.setPuffState(2);
                    puffer.setInvulnerable(true);
                    var scaleAttr = puffer.getAttribute(org.bukkit.attribute.Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(4.0);
                    }
                    
                    final LivingEntity bossEntity = entity;
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            if (puffer.isValid()) {
                                org.bukkit.Location loc = puffer.getLocation();
                                loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, loc, 3, 1.0, 1.0, 1.0, 0.1);
                                loc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, loc, 100, 5.0, 2.0, 5.0, 0.1);
                                loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                                
                                loc.getNearbyEntities(10.0, 10.0, 10.0).forEach(near -> {
                                    if (near instanceof LivingEntity le && le != bossEntity && !(near instanceof org.bukkit.entity.PufferFish)) {
                                        le.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.POISON, 200, 1));
                                        le.damage(4.0, bossEntity);
                                    }
                                });
                                puffer.remove();
                            }
                        }
                    }.runTaskLater(plugin, 100L);
                }
            }

            // Harvest Witch 20% health trigger (Heal and summon witches)
            if ("harvest_witch".equals(boss.getId()) && !entity.getScoreboardTags().contains("triggered_heal")) {
                if (finalHealth <= maxHealth * 0.2) {
                    entity.addScoreboardTag("triggered_heal");
                    event.setCancelled(true);
                    entity.setHealth(maxHealth * 0.8);
                    entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 2.0f, 1.0f);
                    entity.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, entity.getLocation(), 100, 1.0, 2.0, 1.0, 0.1);
                    
                    for (int i = 0; i < 5; i++) {
                        org.bukkit.Location spawnLoc = entity.getLocation().add(
                            (Math.random() - 0.5) * 6,
                            0,
                            (Math.random() - 0.5) * 6
                        );
                        org.bukkit.entity.Witch witch = (org.bukkit.entity.Witch) entity.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.WITCH);
                        witch.addScoreboardTag("summoned_witch");
                        if (entity instanceof org.bukkit.entity.Mob mobEntity && mobEntity.getTarget() != null) {
                            witch.setTarget(mobEntity.getTarget());
                        }
                        entity.getWorld().spawnParticle(org.bukkit.Particle.WITCH, spawnLoc, 30, 0.5, 1.0, 0.5, 0.05);
                        entity.getWorld().playSound(spawnLoc, org.bukkit.Sound.ENTITY_WITCH_AMBIENT, 1.0f, 1.0f);
                    }
                    return;
                }
            }

            // Mace immunity check for Void Necromancer and Overworld Wither
            if (("void_necromancer".equals(bossConfig.get().getId()) || "overworld_wither".equals(bossConfig.get().getId())) && event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent edbe) {
                if (edbe.getDamager() instanceof Player player) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (mainHand != null && mainHand.getType() == Material.MACE) {
                        event.setCancelled(true);
                        entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                        return;
                    }
                }
            }
            defense = bossConfig.get().getDefense();
        } else {
            var mobConfig = mobManager.getCustomMobConfig(entity);
            if (mobConfig.isPresent()) {
                defense = mobConfig.get().getDefense();
            }
        }

        if (defense > 0) {
            // Treat defense as percentage reduction (e.g., 0.1 = 10% reduction)
            double reduction = 1.0 - defense;
            if (reduction < 0) reduction = 0;
            event.setDamage(event.getDamage() * reduction);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (entity.getScoreboardTags().contains("exploding_zombie")) {
            entity.getWorld().createExplosion(entity.getLocation(), 2.5f, false, false);
        }

        double reward = 2.0; // Default standard mob reward
        List<CustomMob.LootItem> customLoot = null;
        int xpReward = -1;

        // Check if it's a custom boss
        var bossConfig = mobManager.getCustomBossConfig(entity);
        if (bossConfig.isPresent()) {
            reward = bossConfig.get().getKroinReward();
            customLoot = bossConfig.get().getCustomLoot();
            xpReward = bossConfig.get().getXpReward();
            dataManager.getBossState().setLastDeath(bossConfig.get().getId(), System.currentTimeMillis());
            dataManager.saveBossState();
            mobManager.removeActiveBoss(entity.getUniqueId());
        } else {
            // Check if it's a custom mob
            var mobConfig = mobManager.getCustomMobConfig(entity);
            if (mobConfig.isPresent()) {
                reward = mobConfig.get().getKroinReward();
                customLoot = mobConfig.get().getCustomLoot();
                xpReward = mobConfig.get().getXpReward();
            } else {
                // Check for standard overrides
                String typeName = entity.getType().name();
                boolean isStandardBoss = typeName.contains("DRAGON") || typeName.contains("WITHER") || typeName.contains("WARDEN");
                
                var overrides = isStandardBoss ? dataManager.getStandardBossOverrides() : dataManager.getStandardMobOverrides();
                if (overrides.getOverrides().containsKey(typeName)) {
                    var override = overrides.getOverrides().get(typeName);
                    reward = override.getKroinReward();
                    customLoot = override.getCustomLoot();
                    xpReward = override.getXpReward();
                } else if (isStandardBoss) {
                    reward = 2000.0;
                }
            }
        }

        if (xpReward >= 0) {
            event.setDroppedExp(xpReward);
        }

        if (killer != null && hereShoppyEnabled) {
            try {
                com.hereshoppy.hereshoppy.api.HereshoppyAPI.addKroins(killer.getUniqueId(), reward);
                killer.sendMessage("§aYou earned §e" + String.format("%.2f", reward) + " Kroins §afor defeating " + entity.getName() + "!");
            } catch (NoClassDefFoundError ignored) {
                // HereShoppy might have been disabled after start
            }
        }

        if (customLoot != null) {
            event.getDrops().clear();
            for (CustomMob.LootItem item : customLoot) {
                if (random.nextDouble() < item.getChance()) {
                    int amount = item.getMinAmount() + random.nextInt(item.getMaxAmount() - item.getMinAmount() + 1);
                    
                    // Check if it's a CustomItem ID first
                    var customItemOpt = dataManager.getCustomItems().stream()
                        .filter(ci -> ci.getId() != null && ci.getId().equalsIgnoreCase(item.getMaterial()))
                        .findFirst();
                    
                    if (customItemOpt.isPresent()) {
                        event.getDrops().add(createCustomItemStack(customItemOpt.get(), amount));
                    } else {
                        // Standard material drops
                        Material mat = Material.matchMaterial(item.getMaterial());
                        if (mat != null) {
                            event.getDrops().add(new ItemStack(mat, amount));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        
        // Don't mess with entities that already have custom data
        if (entity.getPersistentDataContainer().has(mobManager.getCustomBossKey(), PersistentDataType.STRING)) return;

        // Custom Mob Random Spawn Logic
        for (CustomMob config : dataManager.getCustomMobs()) {
            if (config.getBaseType().equalsIgnoreCase(entity.getType().name())) {
                if (shouldSpawn(config, entity)) {
                    // Replace standard mob with custom one
                    mobManager.spawnCustomMob(config, entity.getLocation());
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldSpawn(CustomMob config, Entity entity) {
        CustomMob.SpawnConditions cond = config.getSpawnConditions();
        if (cond == null) return false;

        if (random.nextDouble() >= cond.getChance()) return false;

        if (cond.getBiomes() != null && !cond.getBiomes().isEmpty()) {
            String biome = entity.getLocation().getBlock().getBiome().name();
            if (!cond.getBiomes().contains(biome)) return false;
        }

        long time = entity.getWorld().getTime();
        boolean isDay = time < 12300 || time > 23850;
        if (cond.getTime() != null) {
            if (cond.getTime().equalsIgnoreCase("DAY") && !isDay) return false;
            if (cond.getTime().equalsIgnoreCase("NIGHT") && isDay) return false;
        }

        int light = entity.getLocation().getBlock().getLightLevel();
        if (light < cond.getMinLight() || light > cond.getMaxLight()) return false;

        return true;
    }

    @EventHandler
    public void onEntitiesLoad(org.bukkit.event.world.EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            mobManager.registerExistingEntity(entity);
        }
    }

    @EventHandler
    public void onEntitiesUnload(org.bukkit.event.world.EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            mobManager.removeActiveBoss(entity.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        if (event.getEntityType() == org.bukkit.entity.EntityType.ENDER_DRAGON) {
            Entity dragon = event.getEntity();
            if (dragon != null && (dragon.getPersistentDataContainer().has(mobManager.getCustomBossKey(), PersistentDataType.STRING) ||
                dragon.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_mob"), PersistentDataType.STRING))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        if (event.getEntityType() == org.bukkit.entity.EntityType.ENDER_DRAGON) {
            Entity dragon = event.getEntity();
            if (dragon != null && (dragon.getPersistentDataContainer().has(mobManager.getCustomBossKey(), PersistentDataType.STRING) ||
                dragon.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_mob"), PersistentDataType.STRING))) {
                event.blockList().clear();
            }
        }
    }

    private ItemStack createCustomItemStack(com.heremobby.heremobby.model.CustomItem customItem, int amount) {
        Material mat = Material.matchMaterial(customItem.getMaterial());
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemStack(mat, amount);
        var meta = item.getItemMeta();
        if (meta != null) {
            if (customItem.getName() != null) {
                meta.setDisplayName(customItem.getName());
            }
            if (customItem.getEnchants() != null) {
                for (var entry : customItem.getEnchants().entrySet()) {
                    org.bukkit.enchantments.Enchantment enchant = getEnchantment(entry.getKey());
                    if (enchant != null) {
                        meta.addEnchant(enchant, entry.getValue(), true);
                    }
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private org.bukkit.enchantments.Enchantment getEnchantment(String name) {
        name = name.toLowerCase().replace(" ", "_");
        NamespacedKey key = NamespacedKey.fromString(name);
        if (key == null) {
            key = NamespacedKey.minecraft(name);
        }
        org.bukkit.enchantments.Enchantment enchant = org.bukkit.Registry.ENCHANTMENT.get(key);
        if (enchant != null) return enchant;
        return org.bukkit.enchantments.Enchantment.getByName(name.toUpperCase());
    }

    @EventHandler
    public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player player) {
            if (event.getEntity() instanceof org.bukkit.entity.Snowball snowball) {
                if (snowball.getShooter() instanceof org.bukkit.entity.Snowman snowman) {
                    if (snowman.getScoreboardTags().contains("evil_snow_golem")) {
                        player.damage(2.0, snowman);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(org.bukkit.event.entity.EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();
        boolean isMinion = entity.getScoreboardTags().contains("evil_snow_golem") ||
                           entity.getScoreboardTags().contains("summoned_wolf") ||
                           entity.getScoreboardTags().contains("summoned_spider") ||
                           entity.getScoreboardTags().contains("exploding_zombie") ||
                           entity.getScoreboardTags().contains("summoned_baby_zombie");

        if (!isMinion) return;

        LivingEntity currentTarget = event.getTarget();

        // If target is not a player (either null, boss, or something else)
        if (!(currentTarget instanceof Player)) {
            // Find nearest player in survival/adventure mode
            Player targetPlayer = null;
            double nearestDist = 900; // 30 block radius
            for (Player p : entity.getWorld().getPlayers()) {
                if (p.getGameMode() == org.bukkit.GameMode.SURVIVAL || p.getGameMode() == org.bukkit.GameMode.ADVENTURE) {
                    double dist = p.getLocation().distanceSquared(entity.getLocation());
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        targetPlayer = p;
                    }
                }
            }

            if (targetPlayer != null) {
                event.setTarget(targetPlayer);
            } else {
                event.setTarget(null);
                event.setCancelled(true);
            }
        }
    }
}
