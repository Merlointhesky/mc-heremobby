package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Snowman;
import org.bukkit.scheduler.BukkitRunnable;

public class SummonSnowGolemsGoal extends AbstractSpellGoal {

    public SummonSnowGolemsGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "summon_snow_golems", 15000); // 15s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SNOW_GOLEM_DEATH, 1.2f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (int i = 0; i < 10; i++) {
                    Location spawnLoc = boss.getLocation().add(
                        (Math.random() - 0.5) * 8,
                        0,
                        (Math.random() - 0.5) * 8
                    );
                    
                    Snowman snowman = (Snowman) boss.getWorld().spawnEntity(spawnLoc, EntityType.SNOW_GOLEM);
                    snowman.addScoreboardTag("evil_snow_golem");
                    snowman.setTarget(target);
                    
                    boss.getWorld().spawnParticle(Particle.SNOWFLAKE, spawnLoc.add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.05);
                    boss.getWorld().playSound(spawnLoc, Sound.ENTITY_SNOW_GOLEM_AMBIENT, 1.0f, 0.8f);
                }

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L)); // 0.5s cast delay
    }
}
