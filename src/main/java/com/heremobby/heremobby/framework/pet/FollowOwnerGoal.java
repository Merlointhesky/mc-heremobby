package com.heremobby.heremobby.framework.pet;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.EnumSet;
import java.util.UUID;

public class FollowOwnerGoal implements Goal<Mob> {
    private final Plugin plugin;
    private final Mob mob;
    private final UUID ownerId;
    private Player owner;

    public FollowOwnerGoal(Plugin plugin, Mob mob, UUID ownerId) {
        this.plugin = plugin;
        this.mob = mob;
        this.ownerId = ownerId;
    }

    @Override
    public boolean shouldActivate() {
        owner = plugin.getServer().getPlayer(ownerId);
        if (owner == null) return false;
        if (!owner.getWorld().equals(mob.getWorld())) return false;
        return owner.getLocation().distanceSquared(mob.getLocation()) > 9; // Follow if more than 3 blocks away
    }

    @Override
    public boolean shouldStayActive() {
        if (owner == null || !owner.isOnline()) return false;
        if (!owner.getWorld().equals(mob.getWorld())) return false;
        return owner.getLocation().distanceSquared(mob.getLocation()) > 4; // Stay active until 2 blocks away
    }

    @Override
    public void tick() {
        if (owner != null) {
            mob.getPathfinder().moveTo(owner.getLocation(), 1.5);
        }
    }

    @Override
    public GoalKey<Mob> getKey() {
        return GoalKey.of(Mob.class, new NamespacedKey(plugin, "follow_owner"));
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
