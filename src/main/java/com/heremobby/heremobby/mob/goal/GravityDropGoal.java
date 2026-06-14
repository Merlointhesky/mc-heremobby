package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * A spell that lifts the target into the air and then slams them down.
 */
public class GravityDropGoal extends AbstractSpellGoal {

    public GravityDropGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "gravitydrop", 30000); // 30s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 225;
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

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!target.isValid() || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                if (ticks < 20) {
                    // Lift stage
                    target.setVelocity(new Vector(0, 0.6, 0));
                    target.getWorld().spawnParticle(Particle.ENCHANT, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                } else if (ticks == 20) {
                    // Pause/Hold stage
                    target.setVelocity(new Vector(0, 0.05, 0));
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
                } else if (ticks == 25) {
                    // Slam stage
                    target.setVelocity(new Vector(0, -1.0, 0));
                    target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getLocation().add(0, 1, 0), 5);
                } else if (ticks > 30) {
                    // End
                    activeBoss.stopCurrentSpell();
                    return;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
