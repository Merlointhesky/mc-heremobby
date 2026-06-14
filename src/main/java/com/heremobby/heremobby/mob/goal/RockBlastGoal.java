package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Shoots a rock that explodes into flint shards on impact.
 */
public class RockBlastGoal extends AbstractSpellGoal {
    private final Random random = new Random();

    public RockBlastGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "rock_blast", 10000); // 10s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            Location currentLoc = origin.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 60 || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                currentLoc.add(direction.clone().multiply(0.8));
                boss.getWorld().spawnParticle(Particle.BLOCK, currentLoc, 5, 0.1, 0.1, 0.1, Material.STONE.createBlockData());

                if (currentLoc.getBlock().getType().isSolid()) {
                    shatter();
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (org.bukkit.entity.Entity e : currentLoc.getNearbyEntities(0.8, 0.8, 0.8)) {
                    if (e instanceof LivingEntity le && le != boss) {
                        le.damage(15.0, boss);
                        shatter();
                        activeBoss.stopCurrentSpell();
                        return;
                    }
                }

                ticks++;
            }

            private void shatter() {
                boss.getWorld().playSound(currentLoc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
                boss.getWorld().spawnParticle(Particle.ITEM, currentLoc, 20, 0.3, 0.3, 0.3, 0.1, new ItemStack(Material.FLINT));
                
                // Flint shards deal minor damage
                currentLoc.getNearbyEntities(2.0, 2.0, 2.0).forEach(e -> {
                    if (e instanceof LivingEntity le && le != boss) {
                        le.damage(6.0, boss);
                    }
                });
            }
        }.runTaskTimer(plugin, 5, 1));
    }
}
