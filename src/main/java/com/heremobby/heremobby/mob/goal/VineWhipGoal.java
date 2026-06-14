package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import com.heremobby.heremobby.util.SpellUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

/**
 * A spell that whips a vine towards the target, damaging and pulling them slightly.
 */
public class VineWhipGoal extends AbstractSpellGoal {

    public VineWhipGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "vinewhip", 7000); // 7s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        // Range: 10 blocks
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 100;
    }

    @Override
    public void start() {
        super.start();
        Mob boss = activeBoss.getEntity();
        LivingEntity target = boss.getTarget();

        if (target == null) {
            activeBoss.stopCurrentSpell();
            return;
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        
        Location origin = boss.getEyeLocation();
        Vector direction = target.getLocation().toVector().subtract(origin.toVector()).normalize();
        
        // Vine effect
        for (double d = 1; d <= 10; d += 0.5) {
            Location pLoc = origin.clone().add(direction.clone().multiply(d));
            boss.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, pLoc, 8, 0.1, 0.1, 0.1, 0.02);
            
            pLoc.getNearbyEntities(0.8, 0.8, 0.8).forEach(e -> {
                if (e instanceof LivingEntity le && le != boss) {
                    le.damage(8.0, boss);
                    // Pull the target towards the boss
                    SpellUtils.applyVelocity(boss.getLocation(), le, -0.8);
                }
            });
        }

        activeBoss.stopCurrentSpell();
    }
}
