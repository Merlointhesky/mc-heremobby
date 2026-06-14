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
 * A laser-like ray attack that tracks the target and deals continuous damage.
 */
public class RayAttackGoal extends AbstractSpellGoal {

    public RayAttackGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "ray_attack", 15000); // 15s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 256;
    }

    @Override
    public void start() {
        super.start();
        
        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 80; // 4 seconds

            @Override
            public void run() {
                Mob boss = activeBoss.getEntity();
                LivingEntity target = boss.getTarget();

                if (ticks >= maxTicks || !boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                Location origin = boss.getEyeLocation();
                Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(origin.toVector()).normalize();

                // Draw ray
                for (double d = 0; d < 20; d += 0.5) {
                    Location pLoc = origin.clone().add(direction.clone().multiply(d));
                    if (pLoc.getBlock().getType().isSolid()) break;

                    boss.getWorld().spawnParticle(Particle.DUST, pLoc, 1, new Particle.DustOptions(org.bukkit.Color.PURPLE, 1.0f));
                    
                    if (ticks % 10 == 0) {
                        pLoc.getNearbyEntities(0.5, 0.5, 0.5).forEach(e -> {
                            if (e instanceof LivingEntity le && le != boss && le == target) {
                                le.damage(4.0, boss);
                                boss.getWorld().playSound(le.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 0.5f, 1.5f);
                            }
                        });
                    }
                }

                if (ticks % 20 == 0) {
                    boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 2.0f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
