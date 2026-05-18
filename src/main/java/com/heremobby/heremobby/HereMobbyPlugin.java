package com.heremobby.heremobby;

import com.heremobby.heremobby.command.HereMobbyCommand;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.gui.InfoListener;
import com.heremobby.heremobby.listener.BossPoiseListener;
import com.heremobby.heremobby.listener.MobListener;
import com.heremobby.heremobby.listener.SpellListener;
import com.heremobby.heremobby.mob.MobManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HereMobbyPlugin extends JavaPlugin {

    private static HereMobbyPlugin instance;
    private DataManager dataManager;
    private MobManager mobManager;
    private InfoGUI infoGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize Managers
        this.dataManager = new DataManager(this);
        this.mobManager = new MobManager(this, this.dataManager);
        
        // Initialize GUIs
        this.infoGUI = new InfoGUI(this.dataManager);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new MobListener(this.mobManager, this.dataManager), this);
        getServer().getPluginManager().registerEvents(new InfoListener(), this);
        getServer().getPluginManager().registerEvents(new SpellListener(this), this);
        getServer().getPluginManager().registerEvents(new BossPoiseListener(this.mobManager), this);

        // Register Commands
        HereMobbyCommand cmd = new HereMobbyCommand(this.infoGUI, this.dataManager, this.mobManager);
        getCommand("heremobby").setExecutor(cmd);
        getCommand("heremobby").setTabCompleter(cmd);
        
        getLogger().info("HereMobby has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveBossState();
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
