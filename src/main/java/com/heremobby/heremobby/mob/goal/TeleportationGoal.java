package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.Random;

/**
 * Boss teleports to a random location near the target.
 */
public class TeleportationGoal extends AbstractSpellGoal {
    private final Random random = new Random();

    public TeleportationGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "teleportation", 6000); // 6s cooldown
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
        if (target == null) return;

        Location targetLoc = target.getLocation();
        double angle = random.nextDouble() * 2 * Math.PI;
        double radius = 3 + random.nextDouble() * 5;
        
        Location teleportLoc = targetLoc.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        teleportLoc.setY(boss.getWorld().getHighestBlockYAt(teleportLoc) + 1.0);

        // Effects at old location
        boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, boss.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Teleport
        boss.teleport(teleportLoc);

        // Effects at new location
        boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        activeBoss.stopCurrentSpell();
    }
}
