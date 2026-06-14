package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashSet;
import java.util.Set;

/**
 * A lightning strike that jumps between nearby players.
 */
public class ChainLightningGoal extends AbstractSpellGoal {

    public ChainLightningGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "chain_lightning", 15000); // 15s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 225;
    }

    @Override
    public void start() {
        super.start();
        
        Mob boss = activeBoss.getEntity();
        LivingEntity initialTarget = boss.getTarget();
        if (!(initialTarget instanceof Player firstPlayer)) {
            activeBoss.stopCurrentSpell();
            return;
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);

        new BukkitRunnable() {
            int jumps = 0;
            final int maxJumps = 5;
            double damage = 24.0;
            LivingEntity currentTarget = firstPlayer;
            final Set<LivingEntity> hitEntities = new HashSet<>();

            @Override
            public void run() {
                if (jumps >= maxJumps || currentTarget == null || !currentTarget.isValid()) {
                    activeBoss.stopCurrentSpell();
                    this.cancel();
                    return;
                }

                // Strike current target
                Location loc = currentTarget.getLocation();
                boss.getWorld().strikeLightningEffect(loc);
                currentTarget.damage(damage, boss);
                hitEntities.add(currentTarget);
                boss.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
                boss.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc.add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);

                // Find next target
                LivingEntity nextTarget = null;
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player != currentTarget && !hitEntities.contains(player) && player.getLocation().distanceSquared(loc) < 100) {
                        nextTarget = player;
                        break;
                    }
                }

                if (nextTarget != null) {
                    // Draw a small line between them
                    Location start = currentTarget.getLocation().add(0, 1, 0);
                    Location end = nextTarget.getLocation().add(0, 1, 0);
                    drawLink(start, end);
                    
                    currentTarget = nextTarget;
                    damage *= 0.8; // Reduced damage for next jump
                    jumps++;
                } else {
                    activeBoss.stopCurrentSpell();
                    this.cancel();
                }
            }

            private void drawLink(Location start, Location end) {
                double distance = start.distance(end);
                org.bukkit.util.Vector vec = end.toVector().subtract(start.toVector()).normalize();
                for (double d = 0; d < distance; d += 0.3) {
                    start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, start.clone().add(vec.clone().multiply(d)), 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0, 10); // Jumps every 0.5s
    }
}
