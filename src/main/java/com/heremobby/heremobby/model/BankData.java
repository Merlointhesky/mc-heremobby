package com.heremobby.heremobby.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankData {
    private Map<String, Long> balances = new HashMap<>();

    public Map<String, Long> getBalances() {
        return balances;
    }

    public long getBalance(UUID uuid) {
        return balances.getOrDefault(uuid.toString(), 0L);
    }

    public void setBalance(UUID uuid, long amount) {
        balances.put(uuid.toString(), amount);
    }

    public void addBalance(UUID uuid, long amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }
}
