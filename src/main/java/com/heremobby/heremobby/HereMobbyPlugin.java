package com.heremobby.heremobby;

import com.heremobby.heremobby.command.HereMobbyCommand;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.gui.InfoListener;
import com.heremobby.heremobby.listener.BossPoiseListener;
import com.heremobby.heremobby.listener.MobListener;
import com.heremobby.heremobby.listener.SpellListener;
import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.framework.item.ItemRegistry;
import com.heremobby.heremobby.framework.mount.MountListener;
import com.heremobby.heremobby.framework.mount.MountTask;
import com.heremobby.heremobby.framework.pet.PetListener;
import com.heremobby.heremobby.framework.pet.PetLightTask;
import com.heremobby.heremobby.framework.wild.WildAnimalListener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class HereMobbyPlugin extends JavaPlugin {

    private static HereMobbyPlugin instance;
    private DataManager dataManager;
    private MobManager mobManager;
    private InfoGUI infoGUI;
    private ItemRegistry itemRegistry;
    private PetLightTask petLightTask;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize Managers
        this.dataManager = new DataManager(this);
        this.mobManager = new MobManager(this, this.dataManager);
        this.itemRegistry = new ItemRegistry(this);
        
        // Initialize GUIs
        this.infoGUI = new InfoGUI(this.dataManager);

        // Register Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new MobListener(this.mobManager, this.dataManager), this);
        pm.registerEvents(new InfoListener(), this);
        pm.registerEvents(new SpellListener(this), this);
        pm.registerEvents(new BossPoiseListener(this.mobManager), this);
        
        // Register New Framework Listeners
        pm.registerEvents(new MountListener(), this);
        pm.registerEvents(new PetListener(), this);
        pm.registerEvents(new WildAnimalListener(), this);
        pm.registerEvents(this.itemRegistry, this);

        // Register Commands
        HereMobbyCommand cmd = new HereMobbyCommand(this.infoGUI, this.dataManager, this.mobManager);
        getCommand("heremobby").setExecutor(cmd);
        getCommand("heremobby").setTabCompleter(cmd);
        
        // Save Default Resources
        File mountsDir = new File(getDataFolder(), "mounts");
        if (!mountsDir.exists()) mountsDir.mkdirs();
        File petsDir = new File(getDataFolder(), "pets");
        if (!petsDir.exists()) petsDir.mkdirs();
        File wildDir = new File(getDataFolder(), "wild_animals");
        if (!wildDir.exists()) wildDir.mkdirs();
        
        saveResource("mounts/rideable_rocket.yml", false);
        saveResource("pets/lamp.yml", false);

        // Load Custom Items and Recipes
        this.itemRegistry.loadConfig();
        
        // Start Tasks
        getServer().getScheduler().runTaskTimer(this, new MountTask(), 20L, 1L);
        
        this.petLightTask = new PetLightTask();
        getServer().getScheduler().runTaskTimer(this, this.petLightTask, 20L, 2L); // run every 2 ticks
        
        getLogger().info("HereMobby has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveBossState();
        }
        if (petLightTask != null) {
            petLightTask.cleanupAll();
        }
        getLogger().info("HereMobby has been disabled!");
    }

    public static HereMobbyPlugin getInstance() {
        return instance;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
}
