package com.heremobby.heremobby.mob.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import java.util.EnumSet;

/**
 * Base class for custom spell-casting goals for bosses.
 */
public abstract class AbstractSpellGoal implements Goal<Mob> {
    protected final HereMobbyPlugin plugin;
    protected final ActiveBoss activeBoss;
    protected final GoalKey<Mob> key;
    protected final long cooldownMillis;
    protected long lastUsed = 0;

    public AbstractSpellGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss, String goalName, long cooldownMillis) {
        this.plugin = plugin;
        this.activeBoss = activeBoss;
        this.cooldownMillis = cooldownMillis;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, goalName));
    }

    @Override
    public boolean shouldActivate() {
        if (activeBoss.isChanneling()) return false;
        if (System.currentTimeMillis() - lastUsed < cooldownMillis) return false;

        LivingEntity target = activeBoss.getEntity().getTarget();
        if (target == null || !target.isValid() || target.isDead()) return false;

        return canActivate(target);
    }

    /**
     * Subclasses define specific conditions (distance, etc.) to start the spell.
     */
    protected abstract boolean canActivate(LivingEntity target);

    @Override
    public boolean shouldStayActive() {
        return activeBoss.isChanneling();
    }

    @Override
    public void start() {
        lastUsed = System.currentTimeMillis();
        activeBoss.setChanneling(true);
    }

    @Override
    public void stop() {
        activeBoss.stopCurrentSpell();
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
