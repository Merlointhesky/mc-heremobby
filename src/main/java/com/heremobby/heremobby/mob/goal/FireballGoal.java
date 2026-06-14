package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
20.  * Shoots a fireball that deals armor-ignoring damage and sets the target on fire.
21.  */
public class FireballGoal extends AbstractSpellGoal {

    public FireballGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "fireball", 12000); // 12s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 400; // 20 block range
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

        Location origin = boss.getEyeLocation();
        Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(origin.toVector()).normalize();

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            Location currentLoc = origin.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100 || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                currentLoc.add(direction.clone().multiply(0.8));
                boss.getWorld().spawnParticle(Particle.FLAME, currentLoc, 8, 0.1, 0.1, 0.1, 0.05);
                boss.getWorld().spawnParticle(Particle.LAVA, currentLoc, 2, 0.05, 0.05, 0.05, 0.01);

                if (ticks % 3 == 0) {
                    boss.getWorld().playSound(currentLoc, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.5f);
                }

                // Check for block collision
                if (currentLoc.getBlock().getType().isSolid()) {
                    explode();
                    activeBoss.stopCurrentSpell();
                    return;
                }

                // Check for entity collision
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
                boss.getWorld().playSound(currentLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.2f, 1.0f);
                
                DamageType magicType = org.bukkit.Bukkit.getRegistry(DamageType.class).get(NamespacedKey.minecraft("magic"));
                DamageSource source;
                if (magicType != null) {
                    source = DamageSource.builder(magicType).withDirectEntity(boss).withCausingEntity(boss).build();
                } else {
                    source = DamageSource.builder(DamageType.MAGIC).withDirectEntity(boss).withCausingEntity(boss).build();
                }

                final DamageSource finalSource = source;
                currentLoc.getNearbyEntities(2.5, 2.5, 2.5).forEach(e -> {
                    if (e instanceof LivingEntity le && le != boss) {
                        le.damage(10.0, finalSource);
                        le.setFireTicks(200); // 10 seconds
                    }
                });
            }
        }.runTaskTimer(plugin, 10, 1)); // 0.5s delay before firing
    }
}
