package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Spider;
import org.bukkit.scheduler.BukkitRunnable;

public class SummonSpidersGoal extends AbstractSpellGoal {

    public SummonSpidersGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "summon_spiders", 20000); // 20s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.2f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                for (int i = 0; i < 10; i++) {
                    Location spawnLoc = boss.getLocation().add(
                        (Math.random() - 0.5) * 6,
                        0,
                        (Math.random() - 0.5) * 6
                    );
                    
                    Spider spider = (Spider) boss.getWorld().spawnEntity(spawnLoc, EntityType.SPIDER);
                    spider.addScoreboardTag("summoned_spider");
                    spider.setTarget(target);
                    
                    boss.getWorld().spawnParticle(Particle.POOF, spawnLoc.add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.02);
                    boss.getWorld().playSound(spawnLoc, Sound.ENTITY_SPIDER_STEP, 1.0f, 1.0f);
                }

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L)); // 0.5s cast delay
    }
}
