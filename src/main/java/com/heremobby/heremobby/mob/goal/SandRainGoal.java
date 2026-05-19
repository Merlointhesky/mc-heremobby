package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Generates sand blocks that fall from the sky onto players.
 */
public class SandRainGoal extends AbstractSpellGoal {
    private final Random random = new Random();

    public SandRainGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "sand_rain", 20000); // 20s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 400;
    }

    @Override
    public void start() {
        super.start();
        
        Mob boss = activeBoss.getEntity();

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_SAND_BREAK, 1.0f, 0.5f);
        boss.getWorld().spawnParticle(Particle.BLOCK, boss.getLocation().add(0, 2, 0), 50, 1, 1, 1, Material.SAND.createBlockData());

        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100; // 5 seconds

            @Override
            public void run() {
                if (ticks >= maxTicks || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                if (ticks % 5 == 0) {
                    for (Player player : boss.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(boss.getLocation()) < 900) {
                            Location spawnLoc = player.getLocation().add(random.nextDouble() * 4 - 2, 10, random.nextDouble() * 4 - 2);
                            FallingBlock sand = player.getWorld().spawnFallingBlock(spawnLoc, Material.SAND.createBlockData());
                            sand.setDropItem(false);
                            sand.setHurtEntities(true);
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
