package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import com.heremobby.heremobby.util.SpellUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

/**
 * An instant spell that pushes back all nearby enemies.
 */
public class ThunderwaveGoal extends AbstractSpellGoal {

    public ThunderwaveGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "thunderwave", 8000); // 8s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        // Activate if target is close (within 5 blocks)
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 25;
    }

    @Override
    public void start() {
        super.start();
        LivingEntity boss = activeBoss.getEntity();
        
        // Effects
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
        boss.getWorld().spawnParticle(Particle.EXPLOSION, boss.getLocation(), 1, 0, 0, 0, 0);
        boss.getWorld().spawnParticle(Particle.CLOUD, boss.getLocation(), 100, 3, 1, 3, 0.2);

        // Mechanics
        double tempRange = 6.0;
        double tempForce = 1.8;

        org.bukkit.persistence.PersistentDataContainer pdc = boss.getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_boss");
        String bossId = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        if ("storm_archmage".equals(bossId) || "overworld_wither".equals(bossId)) {
            tempRange = 10.0;
            tempForce = 3.0;
        }

        final double range = tempRange;
        final double force = tempForce;

        boss.getNearbyEntities(range, 4, range).forEach(e -> {
            if (e instanceof LivingEntity le && le != boss) {
                SpellUtils.applyVelocity(boss.getLocation(), le, force);
                le.damage(5.0, boss);
            }
        });

        // Instant spell, so stop immediately after execution
        activeBoss.stopCurrentSpell();
    }
}
