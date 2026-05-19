package com.heremobby.heremobby.mob.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.heremobby.heremobby.HereMobbyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.EnumSet;

/**
 * A basic melee attack goal for custom mobs that might not have one (e.g., Pigs).
 */
public class MeleeAttackGoal implements Goal<Mob> {
    private final HereMobbyPlugin plugin;
    private final Mob mob;
    private final GoalKey<Mob> key;
    private long lastAttack = 0;
    private final long cooldown = 1000; // 1 second between melee hits

    public MeleeAttackGoal(HereMobbyPlugin plugin, Mob mob) {
        this.plugin = plugin;
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "melee_attack"));
    }

    @Override
    public boolean shouldActivate() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isValid() || target.isDead()) return false;
        
        // Only activate if very close to target
        return mob.getLocation().distanceSquared(target.getLocation()) < 4.0;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack >= cooldown) {
            mob.swingMainHand();
            // Damage the target. Using a default of 3.0 if ATTACK_DAMAGE is not set/available
            double damage = 3.0;
            try {
                var attr = mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
                if (attr != null) damage = attr.getValue();
            } catch (Exception ignored) {}
            
            target.damage(damage, mob);
            lastAttack = now;
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
