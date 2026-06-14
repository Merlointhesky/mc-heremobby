package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

public class SummonExplodingZombiesGoal extends AbstractSpellGoal {

    public SummonExplodingZombiesGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "summon_exploding_zombies", 20000); // 20s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.2f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    Location spawnLoc = boss.getLocation().add(
                        (Math.random() - 0.5) * 6,
                        0,
                        (Math.random() - 0.5) * 6
                    );
                    
                    Zombie zombie = (Zombie) boss.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    zombie.addScoreboardTag("exploding_zombie");
                    zombie.setTarget(target);
                    
                    // Create visual effects on summon
                    boss.getWorld().spawnParticle(Particle.FLAME, spawnLoc.add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.05);
                    boss.getWorld().playSound(spawnLoc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.6f);

                    // Add dynamic detonation tracking task
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!zombie.isValid() || zombie.isDead()) {
                                cancel();
                                return;
                            }
                            
                            // Check distance to players
                            for (org.bukkit.entity.Player p : zombie.getWorld().getPlayers()) {
                                if (p.getLocation().distanceSquared(zombie.getLocation()) < 2.25) { // 1.5 blocks
                                    zombie.getWorld().createExplosion(zombie.getLocation(), 2.5f, false, false);
                                    zombie.remove();
                                    cancel();
                                    return;
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0, 5); // check every 5 ticks (0.25s)
                }

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L)); // 0.5s cast delay
    }
}
