package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ThrowAxeGoal extends AbstractSpellGoal {

    public ThrowAxeGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "throw_axe", 8000); // 8s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.2f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            Location currentLoc = boss.getEyeLocation();
            Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(currentLoc.toVector()).normalize();
            int ticks = 0;
            boolean returning = false;
            final Set<UUID> hitEntities = new HashSet<>();
            final ItemStack axeItem = new ItemStack(Material.GOLDEN_AXE);

            @Override
            public void run() {
                if (ticks > 40 || !boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                if (!returning) {
                    // Forward path
                    currentLoc.add(direction.clone().multiply(1.0));
                    if (ticks >= 15) {
                        returning = true;
                        hitEntities.clear(); // Allow hitting them again on the way back!
                    }
                } else {
                    // Return path (track the boss's current position)
                    Location bossLoc = boss.getEyeLocation();
                    Vector returnDir = bossLoc.toVector().subtract(currentLoc.toVector());
                    if (returnDir.lengthSquared() < 2.25) { // within 1.5 blocks of boss
                        activeBoss.stopCurrentSpell();
                        return;
                    }
                    returnDir.normalize();
                    currentLoc.add(returnDir.multiply(1.0));
                }

                // Visual effects
                boss.getWorld().spawnParticle(Particle.ITEM, currentLoc, 5, 0.1, 0.1, 0.1, 0.05, axeItem);
                boss.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 1, 0.1, 0.1, 0.1, 0.0);
                
                if (ticks % 3 == 0) {
                    boss.getWorld().playSound(currentLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 0.8f);
                }

                // Check collisions with entities
                for (org.bukkit.entity.Entity e : currentLoc.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity le && le != boss && !hitEntities.contains(le.getUniqueId())) {
                        le.damage(35.2, boss); // Deal 17.6 hearts damage (10% increase)
                        hitEntities.add(le.getUniqueId());
                        boss.getWorld().playSound(currentLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 10, 1)); // 0.5s cast delay
    }
}
