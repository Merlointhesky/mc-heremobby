package com.heremobby.heremobby.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final HereMobbyPlugin plugin;
    
    private BankData bankData;
    private PricesData pricesData;
    private ShopData shopData;
    private BossState bossState;
    private StandardOverrides standardMobOverrides;
    private StandardOverrides standardBossOverrides;
    private List<CustomItem> customItems = new ArrayList<>();
    private List<CustomMob> customMobs = new ArrayList<>();
    private List<CustomBoss> customBosses = new ArrayList<>();

    public DataManager(HereMobbyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.bankData = loadFile("bank.json", BankData.class, new BankData());
        this.pricesData = loadFile("prices.json", PricesData.class, new PricesData());
        this.shopData = loadFile("shop.json", ShopData.class, new ShopData());
        this.bossState = loadFile("boss_state.json", BossState.class, new BossState());
        this.standardMobOverrides = loadFile("mobs.json", StandardOverrides.class, new StandardOverrides());
        this.standardBossOverrides = loadFile("bosses.json", StandardOverrides.class, new StandardOverrides());
        
        this.customItems = loadDirectory("custom_items", CustomItem.class);
        this.customMobs = loadDirectory("custom_mobs", CustomMob.class);
        this.customBosses = loadDirectory("custom_bosses", CustomBoss.class);
    }

    private <T> T loadFile(String filename, Class<T> clazz, T defaultValue) {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            saveFile(filename, defaultValue);
            return defaultValue;
        }
        try (Reader reader = new FileReader(file)) {
            T data = GSON.fromJson(reader, clazz);
            return data != null ? data : defaultValue;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load " + filename + ": " + e.getMessage());
            return defaultValue;
        }
    }

    public <T> void saveFile(String filename, T data) {
        File file = new File(plugin.getDataFolder(), filename);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + filename + ": " + e.getMessage());
        }
    }

    private <T> List<T> loadDirectory(String dirName, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        File dir = new File(plugin.getDataFolder(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
            return list;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    T item = GSON.fromJson(reader, clazz);
                    if (item != null) list.add(item);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not load " + file.getName() + " from " + dirName + ": " + e.getMessage());
                }
            }
        }
        return list;
    }

    public BankData getBankData() { return bankData; }
    public PricesData getPricesData() { return pricesData; }
    public ShopData getShopData() { return shopData; }
    public BossState getBossState() { return bossState; }
    public StandardOverrides getStandardMobOverrides() { return standardMobOverrides; }
    public StandardOverrides getStandardBossOverrides() { return standardBossOverrides; }
    public List<CustomItem> getCustomItems() { return customItems; }
    public List<CustomMob> getCustomMobs() { return customMobs; }
    public List<CustomBoss> getCustomBosses() { return customBosses; }

    public void saveBank() { saveFile("bank.json", bankData); }
    public void saveBossState() { saveFile("boss_state.json", bossState); }
}
