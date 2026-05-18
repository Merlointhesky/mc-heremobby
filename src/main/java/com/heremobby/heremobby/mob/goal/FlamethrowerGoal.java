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
 * A continuous channeling spell that shoots flames at the target.
 */
public class FlamethrowerGoal extends AbstractSpellGoal {

    public FlamethrowerGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "flamethrower", 10000); // 10s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        // Activate if target is within 12 blocks
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 144;
    }

    @Override
    public void start() {
        super.start();
        
        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 3 seconds

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
                
                // Effect & Damage
                for (double d = 1; d <= 7; d += 0.5) {
                    Location pLoc = origin.clone().add(direction.clone().multiply(d));
                    boss.getWorld().spawnParticle(Particle.FLAME, pLoc, 3, 0.1, 0.1, 0.1, 0.02);
                    
                    if (ticks % 5 == 0) {
                        pLoc.getNearbyEntities(0.6, 0.6, 0.6).forEach(e -> {
                            if (e instanceof LivingEntity le && le != boss) {
                                le.damage(2.0, boss);
                                le.setFireTicks(40);
                            }
                        });
                    }
                }
                
                if (ticks % 4 == 0) {
                    boss.getWorld().playSound(boss.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.2f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
