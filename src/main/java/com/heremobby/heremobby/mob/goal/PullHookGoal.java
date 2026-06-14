package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import com.heremobby.heremobby.util.SpellUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PullHookGoal extends AbstractSpellGoal {

    public PullHookGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "pull_hook", 6000); // 6s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 625; // 25 block range
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.2f, 0.8f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            Location currentLoc = boss.getEyeLocation();
            Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(currentLoc.toVector()).normalize();
            int ticks = 0;
            final ItemStack hookItem = new ItemStack(Material.IRON_NUGGET);

            @Override
            public void run() {
                if (ticks > 50 || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                currentLoc.add(direction.clone().multiply(0.8));

                // Spawn line particle (critical hits and nugget particles)
                boss.getWorld().spawnParticle(Particle.CRIT, currentLoc, 4, 0.05, 0.05, 0.05, 0.02);
                boss.getWorld().spawnParticle(Particle.ITEM, currentLoc, 2, 0.05, 0.05, 0.05, 0.01, hookItem);

                // Check block collision
                if (currentLoc.getBlock().getType().isSolid()) {
                    boss.getWorld().playSound(currentLoc, Sound.ENTITY_ARROW_HIT, 0.5f, 1.2f);
                    activeBoss.stopCurrentSpell();
                    return;
                }

                // Check player / entity collision
                for (org.bukkit.entity.Entity e : currentLoc.getNearbyEntities(0.8, 0.8, 0.8)) {
                    if (e instanceof LivingEntity le && le != boss) {
                        boss.getWorld().playSound(currentLoc, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);
                        boss.getWorld().spawnParticle(Particle.FLASH, currentLoc, 5, 0.1, 0.1, 0.1, 0.0);
                        
                        // Pull the target towards the boss
                        SpellUtils.applyVelocity(boss.getLocation(), le, -1.8);
                        le.damage(10.0, boss);
                        
                        // Spawn 3 exploding zombies around the target
                        for (int i = 0; i < 3; i++) {
                            Location spawnLoc = le.getLocation().add(
                                (Math.random() - 0.5) * 4,
                                0,
                                (Math.random() - 0.5) * 4
                            );
                            org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) boss.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
                            zombie.addScoreboardTag("exploding_zombie");
                            zombie.setTarget(le);
                            boss.getWorld().spawnParticle(Particle.FLAME, spawnLoc.clone().add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.05);
                            boss.getWorld().playSound(spawnLoc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.6f);
                            
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!zombie.isValid() || zombie.isDead()) {
                                        cancel();
                                        return;
                                    }
                                    for (org.bukkit.entity.Player p : zombie.getWorld().getPlayers()) {
                                        if (p.getLocation().distanceSquared(zombie.getLocation()) < 2.25) {
                                            zombie.getWorld().createExplosion(zombie.getLocation(), 2.5f, false, false);
                                            zombie.remove();
                                            cancel();
                                            return;
                                        }
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 5);
                        }
                        
                        activeBoss.stopCurrentSpell();
                        return;
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 10, 1)); // 0.5s cast delay
    }
}
