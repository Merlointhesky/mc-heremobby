package com.heremobby.heremobby.listener;

import com.heremobby.heremobby.mob.ActiveBoss;
import com.heremobby.heremobby.mob.MobManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles the "Poise" system, allowing players to interrupt boss spells.
 */
public class BossPoiseListener implements Listener {
    private final MobManager mobManager;

    public BossPoiseListener(MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ActiveBoss activeBoss = mobManager.getActiveBoss(victim);
        if (activeBoss == null || !activeBoss.isChanneling()) return;

        // Interruption logic: 
        // We consider a "heavy blow" to be > 8.0 damage (final damage after armor)
        // or any damage if we want to be more lenient.
        double damage = event.getFinalDamage();
        
        if (damage >= 8.0) {
            interruptBoss(activeBoss);
        }
    }

    private void interruptBoss(ActiveBoss activeBoss) {
        activeBoss.stopCurrentSpell();
        
        LivingEntity entity = activeBoss.getEntity();
        
        // Visual and audio feedback
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        entity.getWorld().spawnParticle(Particle.BLOCK_MARKER, entity.getLocation().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.1);
        
        // Staggered effect (Slowness)
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4));
        
        // Optional: Message to damager? (Not implemented here for brevity)
    }
}
