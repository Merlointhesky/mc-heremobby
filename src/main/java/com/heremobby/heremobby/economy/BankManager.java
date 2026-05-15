package com.heremobby.heremobby.economy;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.config.DataManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BankManager {
    private final DataManager dataManager;

    public BankManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public long getBalance(UUID uuid) {
        return dataManager.getBankData().getBalance(uuid);
    }

    public void setBalance(UUID uuid, long amount) {
        dataManager.getBankData().setBalance(uuid, Math.max(0, amount));
        dataManager.saveBank();
    }

    public void addBalance(UUID uuid, long amount) {
        dataManager.getBankData().addBalance(uuid, amount);
        dataManager.saveBank();
    }

    public boolean hasBalance(UUID uuid, long amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, long amount) {
        if (hasBalance(uuid, amount)) {
            addBalance(uuid, -amount);
            return true;
        }
        return false;
    }
}
