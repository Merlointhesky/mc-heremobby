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

public class SummonBabyZombiesGoal extends AbstractSpellGoal {

    public SummonBabyZombiesGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "summon_baby_zombies", 15000); // 15s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.2f);
        boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (int i = 0; i < 10; i++) {
                    Location spawnLoc = target.getLocation().add(
                        (Math.random() - 0.5) * 4,
                        0,
                        (Math.random() - 0.5) * 4
                    );
                    
                    Zombie baby = (Zombie) boss.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    baby.setBaby(true);
                    baby.addScoreboardTag("summoned_baby_zombie");
                    baby.setTarget(target);
                    
                    boss.getWorld().spawnParticle(Particle.WITCH, spawnLoc.add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.02);
                    boss.getWorld().playSound(spawnLoc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.4f);
                }

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 15L)); // 0.75s cast delay
    }
}
