package com.heremobby.heremobby;

import com.heremobby.heremobby.command.HereMobbyCommand;
import com.heremobby.heremobby.config.DataManager;
import com.heremobby.heremobby.economy.BankManager;
import com.heremobby.heremobby.gui.InfoGUI;
import com.heremobby.heremobby.gui.InfoListener;
import com.heremobby.heremobby.listener.MobListener;
import com.heremobby.heremobby.mob.MobManager;
import com.heremobby.heremobby.shop.ShopGUI;
import com.heremobby.heremobby.shop.ShopListener;
import com.heremobby.heremobby.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class HereMobbyPlugin extends JavaPlugin {

    private static HereMobbyPlugin instance;
    private DataManager dataManager;
    private BankManager bankManager;
    private MobManager mobManager;
    private ShopManager shopManager;
    private InfoGUI infoGUI;
    private ShopGUI shopGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize Managers
        this.dataManager = new DataManager(this);
        this.bankManager = new BankManager(this.dataManager);
        this.mobManager = new MobManager(this, this.dataManager);
        this.shopManager = new ShopManager(this.dataManager);
        
        // Initialize GUIs
        this.infoGUI = new InfoGUI(this.dataManager, this.bankManager);
        this.shopGUI = new ShopGUI(this.shopManager, this.bankManager);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new MobListener(this.bankManager, this.mobManager, this.dataManager), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this.shopManager, this.bankManager, this.shopGUI), this);
        getServer().getPluginManager().registerEvents(new InfoListener(), this);

        // Register Commands
        getCommand("heremobby").setExecutor(new HereMobbyCommand(this.infoGUI, this.shopGUI, this.dataManager, this.shopManager));
        
        getLogger().info("HereMobby has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveBank();
            dataManager.saveBossState();
        }
        getLogger().info("HereMobby has been disabled!");
    }

    public static HereMobbyPlugin getInstance() {
        return instance;
    }
}
