package com.heremobby.heremobby.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardOverrides {
    private Map<String, MobOverride> overrides = new HashMap<>();

    public Map<String, MobOverride> getOverrides() {
        return overrides;
    }

    public static class MobOverride {
        private long kroinReward;
        private int xpReward = -1;
        private List<CustomMob.LootItem> customLoot;

        public long getKroinReward() { return kroinReward; }
        public int getXpReward() { return xpReward; }
        public List<CustomMob.LootItem> getCustomLoot() { return customLoot; }
    }
}
