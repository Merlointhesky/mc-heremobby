package com.heremobby.heremobby.framework.item;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.framework.EntityBuilder;
import com.heremobby.heremobby.framework.mount.CustomMount;
import com.heremobby.heremobby.framework.pet.CustomPet;
import com.heremobby.heremobby.framework.wild.CustomWildAnimal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRegistry implements Listener {
    private final HereMobbyPlugin plugin;
    private final Map<Integer, EntityTypeInfo> registry = new HashMap<>();

    public ItemRegistry(HereMobbyPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(int cmd, String entityId, EntityCategory category, String modelId, Material material, String displayName, org.bukkit.entity.EntityType baseType, boolean killable, double maxHealth) {
        registry.put(cmd, new EntityTypeInfo(entityId, category, modelId, material, displayName, baseType, killable, maxHealth));
        plugin.getLogger().info("Registered custom item: " + displayName + " (CMD: " + cmd + ")");
    }

    public void loadConfig() {
        String[] directories = {"mounts", "pets", "wild_animals"};
        for (String dirName : directories) {
            File dir = new File(plugin.getDataFolder(), dirName);
            if (!dir.exists()) {
                dir.mkdirs();
                plugin.getLogger().info("Created directory: " + dir.getPath());
            }
            loadFromDirectory(dir);
        }
    }

    private void loadFromDirectory(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        plugin.getLogger().info("Scanning directory: " + directory.getName() + " (Found " + files.length + " files)");

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String entityId = file.getName().replace(".yml", "");
                String displayName = ChatColor.translateAlternateColorCodes('&', config.getString("display_name", entityId));
                Material material = Material.matchMaterial(config.getString("material", "PAPER"));
                int cmd = config.getInt("custom_model_data", 0);
                String categoryStr = config.getString("category", "WILD").toUpperCase();
                EntityCategory category = EntityCategory.valueOf(categoryStr);
                String modelId = config.getString("model_id", entityId);

                org.bukkit.entity.EntityType baseType = org.bukkit.entity.EntityType.WOLF;
                if (config.contains("base_type")) {
                    try {
                        baseType = org.bukkit.entity.EntityType.valueOf(config.getString("base_type").toUpperCase());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid base_type: " + config.getString("base_type") + " for " + entityId);
                    }
                } else if (category == EntityCategory.WILD) {
                    baseType = org.bukkit.entity.EntityType.COW;
                }

                boolean killable = config.getBoolean("killable", false);
                double maxHealth = config.getDouble("max_health", -1.0);

                register(cmd, entityId, category, modelId, material, displayName, baseType, killable, maxHealth);

                // Register Recipe
                if (config.contains("recipe")) {
                    ConfigurationSection recipeSection = config.getConfigurationSection("recipe");
                    List<String> shape = recipeSection.getStringList("shape");
                    if (shape.size() == 3) {
                        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_" + entityId);
                        // Remove old recipe if exists (for reloads)
                        Bukkit.removeRecipe(recipeKey);
                        
                        ItemStack result = createCustomItem(material, cmd, displayName);
                        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
                        recipe.shape(shape.get(0), shape.get(1), shape.get(2));

                        ConfigurationSection ingredients = recipeSection.getConfigurationSection("ingredients");
                        if (ingredients != null) {
                            for (String ingredientKey : ingredients.getKeys(false)) {
                                Material ingredientMat = Material.matchMaterial(ingredients.getString(ingredientKey));
                                if (ingredientMat != null) {
                                    recipe.setIngredient(ingredientKey.charAt(0), ingredientMat);
                                }
                            }
                            Bukkit.addRecipe(recipe);
                            plugin.getLogger().info("Registered recipe for: " + entityId);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load custom item from " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public ItemStack createCustomItem(Material material, int cmd, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setCustomModelData(cmd);
            
            // Check registry for 1.21.4 item model
            EntityTypeInfo info = registry.get(cmd);
            if (info != null && info.modelId != null && !info.modelId.isEmpty() && !info.modelId.equalsIgnoreCase("vanilla")) {
                meta.setItemModel(NamespacedKey.minecraft(info.modelId));
            } else if (material == Material.FIREWORK_ROCKET && cmd == 1001) {
                meta.setItemModel(NamespacedKey.minecraft("rideable_rocket"));
            }
            
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onSpawnItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return;

        EntityTypeInfo info = registry.get(meta.getCustomModelData());
        if (info == null) return;

        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        Player player = event.getPlayer();
        var loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
        loc.setYaw(player.getLocation().getYaw());
        loc.setPitch(0);

        switch (info.category) {
            case MOUNT -> {
                CustomMount mount = new CustomMount(info.entityId, info.modelId);
                mount.spawn(loc);
                if (info.entityId.equals("rideable_rocket")) {
                    mount.getBaseEntity().addScoreboardTag("rideable_rocket_vehicle");
                }
            }
            case PET -> new CustomPet(info.entityId, info.modelId, player.getUniqueId(), info.baseType, info.killable, info.maxHealth, info.material, meta.getCustomModelData()).spawn(loc);
            case WILD -> new CustomWildAnimal(info.entityId, info.modelId, info.baseType).spawn(loc);
        }
    }

    @EventHandler
    public void onDeconstruct(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        var entity = event.getEntity();

        if (entity.getScoreboardTags().contains("heremobby_rocket")) {
            event.setCancelled(true);

            ItemStack item = null;
            for (Map.Entry<Integer, EntityTypeInfo> entry : registry.entrySet()) {
                if (entry.getValue().entityId.equals("rideable_rocket") || entry.getValue().entityId.toLowerCase().contains("rocket")) {
                    item = createCustomItem(entry.getValue().material, entry.getKey(), entry.getValue().displayName);
                    break;
                }
            }
            if (item != null) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), item);
            }

            for (Entity near : entity.getWorld().getNearbyEntities(entity.getLocation(), 3.0, 3.0, 3.0)) {
                if (near.getScoreboardTags().contains("heremobby_rocket")) {
                    near.remove();
                }
            }
            return;
        }

        if (entity.getScoreboardTags().contains(EntityBuilder.TAG_HEREMOBBY)) {
            String customId = entity.getPersistentDataContainer().get(EntityBuilder.CUSTOM_ID_KEY, PersistentDataType.STRING);
            if (customId == null) return;

            for (Map.Entry<Integer, EntityTypeInfo> entry : registry.entrySet()) {
                if (entry.getValue().entityId.equals(customId)) {
                    event.setCancelled(true);
                    for (Entity passenger : entity.getPassengers()) {
                        passenger.remove();
                    }
                    entity.remove();

                    EntityTypeInfo info = entry.getValue();
                    ItemStack item = createCustomItem(info.material, entry.getKey(), info.displayName);
                    entity.getWorld().dropItemNaturally(entity.getLocation(), item);
                    break;
                }
            }
        }
    }

    public enum EntityCategory { MOUNT, PET, WILD }
    private record EntityTypeInfo(String entityId, EntityCategory category, String modelId, Material material, String displayName, org.bukkit.entity.EntityType baseType, boolean killable, double maxHealth) {}
}
