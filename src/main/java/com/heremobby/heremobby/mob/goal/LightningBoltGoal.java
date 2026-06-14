package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

/**
 * A spell that strikes the target with a bolt of lightning.
 */
public class LightningBoltGoal extends AbstractSpellGoal {

    public LightningBoltGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "lightningbolt", 12000); // 12s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        // Range: 20 blocks
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 400;
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

        Location targetLoc = target.getLocation();
        
        // Visual Bolt (Vertical line of particles)
        for (double y = targetLoc.getY(); y < targetLoc.getY() + 20; y += 0.5) {
            Location pLoc = new Location(targetLoc.getWorld(), targetLoc.getX(), y, targetLoc.getZ());
            targetLoc.getWorld().spawnParticle(Particle.CLOUD, pLoc, 2, 0.1, 0.1, 0.1, 0.05);
        }
        
        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc.add(0, 1, 0), 1, 0, 0, 0, 0);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
        
        // Damage target and nearby
        targetLoc.getNearbyEntities(2, 2, 2).forEach(e -> {
            if (e instanceof LivingEntity le && le != boss) {
                le.damage(24.0, boss);
            }
        });

        activeBoss.stopCurrentSpell();
    }
}
