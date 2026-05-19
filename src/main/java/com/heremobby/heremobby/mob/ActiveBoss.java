package com.heremobby.heremobby.mob;

import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;

/**
 * Represents a spawned, active custom boss in the world.
 * Tracks runtime state such as channeling and active spell tasks.
 */
public class ActiveBoss {
    private final Mob entity;
    private boolean isChanneling = false;
    private BukkitTask activeTask = null;
    private long lastSpellTime = 0;

    public ActiveBoss(Mob entity) {
        this.entity = entity;
    }

    public long getLastSpellTime() {
        return lastSpellTime;
    }

    public void setLastSpellTime(long lastSpellTime) {
        this.lastSpellTime = lastSpellTime;
    }

    public Mob getEntity() {
        return entity;
    }

    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    public boolean isChanneling() {
        return isChanneling;
    }

    public void setChanneling(boolean channeling) {
        this.isChanneling = channeling;
    }

    public BukkitTask getActiveTask() {
        return activeTask;
    }

    public void setActiveTask(BukkitTask activeTask) {
        // If there's an existing task, we might want to cancel it before replacing it.
        // However, the caller should usually handle cancellation if it's an interruption.
        this.activeTask = activeTask;
    }

    /**
     * Stops any currently running spell task and resets channeling state.
     */
    public void stopCurrentSpell() {
        if (activeTask != null) {
            activeTask.cancel();
            activeTask = null;
        }
        isChanneling = false;
    }
}
