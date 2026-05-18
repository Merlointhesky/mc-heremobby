package com.heremobby.heremobby.mob.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.EnumSet;

/**
 * A simple goal to make the boss target the nearest player in survival mode.
 */
public class TargetNearestPlayerGoal implements Goal<Mob> {
    private final Mob mob;
    private final GoalKey<Mob> key;

    public TargetNearestPlayerGoal(JavaPlugin plugin, Mob mob) {
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "target_nearest_player"));
    }

    @Override
    public boolean shouldActivate() {
        if (mob.getTarget() != null && mob.getTarget().isValid() && !mob.getTarget().isDead()) {
            if (mob.getTarget() instanceof Player p && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)) {
                return false;
            }
        }
        
        Player nearest = null;
        double distSq = 1600; // 40 blocks radius
        
        for (Player p : mob.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                double d = p.getLocation().distanceSquared(mob.getLocation());
                if (d < distSq) {
                    distSq = d;
                    nearest = p;
                }
            }
        }
        
        if (nearest != null) {
            mob.setTarget(nearest);
            return true;
        }
        
        return false;
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }
}
