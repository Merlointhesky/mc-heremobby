package com.heremobby.heremobby.model;

import java.util.HashMap;
import java.util.Map;

public class BossState {
    // Map of boss ID to last death timestamp (epoch millis)
    private Map<String, Long> lastDeaths = new HashMap<>();

    public Map<String, Long> getLastDeaths() {
        return lastDeaths;
    }

    public void setLastDeath(String bossId, long timestamp) {
        lastDeaths.put(bossId, timestamp);
    }

    public long getLastDeath(String bossId) {
        return lastDeaths.getOrDefault(bossId, 0L);
    }
}
