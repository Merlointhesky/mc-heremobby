package com.heremobby.heremobby.mob.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.EnumSet;

/**
 * A goal that makes the boss move towards its target.
 */
public class MoveToTargetGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;
    private final double speed;

    public MoveToTargetGoal(JavaPlugin plugin, Mob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "move_to_target"));
    }

    @Override
    public boolean shouldActivate() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isValid() && mob.getLocation().distanceSquared(target.getLocation()) > 16;
    }

    @Override
    public boolean shouldStayActive() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isValid() && mob.getLocation().distanceSquared(target.getLocation()) > 4;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getPathfinder().moveTo(target.getLocation(), speed);
        }
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
