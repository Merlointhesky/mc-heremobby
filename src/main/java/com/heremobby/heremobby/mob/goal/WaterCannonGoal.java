package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * A continuous channeling spell that shoots a stream of water with knockback.
 */
public class WaterCannonGoal extends AbstractSpellGoal {

    public WaterCannonGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "water_cannon", 8000); // 8s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 144;
    }

    @Override
    public void start() {
        super.start();
        
        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40; // 2 seconds

            @Override
            public void run() {
                if (ticks >= maxTicks || !activeBoss.getEntity().isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }
                
                Mob boss = activeBoss.getEntity();
                LivingEntity target = boss.getTarget();
                
                if (target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                Location origin = boss.getEyeLocation();
                Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(origin.toVector()).normalize();
                
                java.util.Set<LivingEntity> hitThisTick = new java.util.HashSet<>();
                
                for (double d = 1; d <= 10; d += 0.5) {
                    Location pLoc = origin.clone().add(direction.clone().multiply(d));
                    boss.getWorld().spawnParticle(Particle.SPLASH, pLoc, 5, 0.1, 0.1, 0.1, 0.05);
                    boss.getWorld().spawnParticle(Particle.BUBBLE, pLoc, 3, 0.1, 0.1, 0.1, 0.05);
                    
                    if (ticks % 4 == 0) {
                        pLoc.getNearbyEntities(0.8, 0.8, 0.8).forEach(e -> {
                            if (e instanceof LivingEntity le && le != boss && hitThisTick.add(le)) {
                                le.damage(3.0, boss);
                                le.setVelocity(direction.clone().multiply(1.2).setY(0.3));
                            }
                        });
                    }
                }
                
                if (ticks % 5 == 0) {
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.5f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
