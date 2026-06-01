package com.heremobby.heremobby.model;

import java.util.Map;

public class CustomItem {
    private String id;
    private String name;
    private String material;
    private Map<String, Integer> enchants;
    private boolean sellable;
    private long customPrice = -1; // -1 means use price calculation

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public Map<String, Integer> getEnchants() {
        return enchants;
    }

    public boolean isSellable() {
        return sellable;
    }

    public long getCustomPrice() {
        return customPrice;
    }
}
