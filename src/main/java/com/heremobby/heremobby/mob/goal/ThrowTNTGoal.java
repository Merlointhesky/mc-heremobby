package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ThrowTNTGoal extends AbstractSpellGoal {

    public ThrowTNTGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "throw_tnt", 6000); // 6s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 576; // 24 block range
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.2f, 0.8f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                Location origin = boss.getEyeLocation();
                Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(origin.toVector());
                double distance = direction.length();
                direction.normalize();

                // Launch primed TNT with a nice parabolic arc
                TNTPrimed tnt = (TNTPrimed) boss.getWorld().spawnEntity(origin, EntityType.TNT);
                tnt.setFuseTicks(40); // 2 seconds fuse
                
                // Add vertical boost based on distance to make it look thrown
                Vector velocity = direction.multiply(1.0);
                velocity.setY(velocity.getY() + Math.min(0.4, distance * 0.03));
                tnt.setVelocity(velocity);

                boss.getWorld().playSound(origin, Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 0.8f);
                boss.getWorld().spawnParticle(Particle.EXPLOSION, origin, 5, 0.2, 0.2, 0.2, 0.1);

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L)); // 0.5s cast delay
    }
}
