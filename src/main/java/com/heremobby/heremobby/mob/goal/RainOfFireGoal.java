package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;

/**
 * A channeled spell that causes fire to rain down on a target area.
 */
public class RainOfFireGoal extends AbstractSpellGoal {
    private final Random random = new Random();

    public RainOfFireGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "rainoffire", 20000); // 20s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
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

        Location center = target.getLocation();
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100; // 5 seconds

            @Override
            public void run() {
                if (ticks >= maxTicks || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                // Random fire drops in 8 block radius
                for (int i = 0; i < 3; i++) {
                    double ox = (random.nextDouble() - 0.5) * 16;
                    double oz = (random.nextDouble() - 0.5) * 16;
                    Location dropLoc = center.clone().add(ox, 10, oz);
                    
                    // Visual of falling fire
                    new BukkitRunnable() {
                        double y = 10;
                        @Override
                        public void run() {
                            if (y <= 0) {
                                Location land = center.clone().add(ox, 0, oz);
                                land.getWorld().spawnParticle(Particle.LAVA, land, 5, 0.5, 0.1, 0.5, 0.05);
                                land.getWorld().playSound(land, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);
                                land.getNearbyEntities(1.5, 1.5, 1.5).forEach(e -> {
                                    if (e instanceof LivingEntity le && le != boss) {
                                        le.damage(3.0, boss);
                                        le.setFireTicks(60);
                                    }
                                });
                                this.cancel();
                                return;
                            }
                            Location pLoc = center.clone().add(ox, y, oz);
                            pLoc.getWorld().spawnParticle(Particle.FLAME, pLoc, 2, 0.1, 0.1, 0.1, 0.02);
                            y -= 0.5;
                        }
                    }.runTaskTimer(plugin, 0, 1);
                }

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0, 5));
    }
}
