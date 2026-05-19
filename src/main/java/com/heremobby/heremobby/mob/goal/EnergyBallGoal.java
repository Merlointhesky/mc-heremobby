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
 * Shoots a slow but powerful energy ball that explodes on contact.
 */
public class EnergyBallGoal extends AbstractSpellGoal {

    public EnergyBallGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "energy_ball", 12000); // 12s cooldown
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
        if (target == null) return;

        Location origin = boss.getEyeLocation();
        Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(origin.toVector()).normalize();

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            Location currentLoc = origin.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100 || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                currentLoc.add(direction.clone().multiply(0.6));
                boss.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 10, 0.2, 0.2, 0.2, 0.05);
                boss.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, currentLoc, 5, 0.1, 0.1, 0.1, 0.02);

                if (ticks % 2 == 0) {
                    boss.getWorld().playSound(currentLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.5f);
                }

                // Check for collision
                if (currentLoc.getBlock().getType().isSolid()) {
                    explode();
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (org.bukkit.entity.Entity e : currentLoc.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (e instanceof LivingEntity le && le != boss) {
                        explode();
                        activeBoss.stopCurrentSpell();
                        return;
                    }
                }

                ticks++;
            }

            private void explode() {
                boss.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, currentLoc, 1);
                boss.getWorld().spawnParticle(Particle.FLASH, currentLoc, 10, 0.5, 0.5, 0.5, 0.1);
                boss.getWorld().playSound(currentLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                
                currentLoc.getNearbyEntities(3.0, 3.0, 3.0).forEach(e -> {
                    if (e instanceof LivingEntity le && le != boss) {
                        le.damage(8.0, boss);
                        le.setVelocity(le.getLocation().toVector().subtract(currentLoc.toVector()).normalize().multiply(1.5).setY(0.5));
                    }
                });
            }
        }.runTaskTimer(plugin, 10, 1)); // 0.5s delay before firing
    }
}
