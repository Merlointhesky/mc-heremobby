package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import com.heremobby.heremobby.util.SpellUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A spell that immobilizes the target for a few seconds.
 */
public class MageHandGoal extends AbstractSpellGoal {
    private final NamespacedKey immobilizedKey;

    public MageHandGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "magehand", 15000); // 15s cooldown
        this.immobilizedKey = new NamespacedKey(plugin, "immobilized_by_spell");
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        // Range: 15 blocks
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 0.8f);
        
        // Mechanically immobilize
        ArmorStand tether = SpellUtils.immobilize(target, immobilizedKey);
        
        activeBoss.setActiveTask(new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100; // 5 seconds

            @Override
            public void run() {
                // If boss is interrupted, stopCurrentSpell() will cancel this task.
                // We also need to remove the tether.
                
                if (ticks >= maxTicks || !target.isValid() || !boss.isValid()) {
                    cleanup();
                    return;
                }
                
                // Visual effect
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                
                if (ticks % 20 == 0) {
                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 2.0f);
                }
                
                ticks++;
            }

            @Override
            public void cancel() {
                super.cancel();
                cleanup();
            }

            private void cleanup() {
                if (tether.isValid()) {
                    tether.remove();
                }
                activeBoss.stopCurrentSpell();
            }
        }.runTaskTimer(plugin, 0, 1));
    }
}
