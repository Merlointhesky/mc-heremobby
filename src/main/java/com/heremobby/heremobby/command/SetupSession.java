package com.heremobby.heremobby.command;

import org.bukkit.entity.Entity;
import java.util.UUID;

public class SetupSession {
    public enum InputState {
        NONE,
        AWAITING_NAME,
        AWAITING_HEALTH,
        AWAITING_KROIN,
        AWAITING_XP
    }

    private final UUID playerUUID;
    private final Entity dummyEntity;
    private final String id;
    private final String baseType;
    private final boolean isBoss;

    private String displayName;
    private double maxHealth = -1;
    private long kroinReward;
    private int xpReward = -1;

    private InputState inputState = InputState.NONE;

    public SetupSession(UUID playerUUID, Entity dummyEntity, String id, String baseType, boolean isBoss) {
        this.playerUUID = playerUUID;
        this.dummyEntity = dummyEntity;
        this.id = id;
        this.baseType = baseType;
        this.isBoss = isBoss;
        this.displayName = id; // default displayName to id
        this.kroinReward = isBoss ? 20 : 1; // default rewards
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public Entity getDummyEntity() { return dummyEntity; }
    public String getId() { return id; }
    public String getBaseType() { return baseType; }
    public boolean isBoss() { return isBoss; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }

    public long getKroinReward() { return kroinReward; }
    public void setKroinReward(long kroinReward) { this.kroinReward = kroinReward; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public InputState getInputState() { return inputState; }
    public void setInputState(InputState inputState) { this.inputState = inputState; }
}
