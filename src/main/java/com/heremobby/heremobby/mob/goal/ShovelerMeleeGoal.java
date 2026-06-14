package com.heremobby.heremobby.mob.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.heremobby.heremobby.HereMobbyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.EnumSet;

/**
 * A heavy armor-ignoring melee attack goal for the Shoveler boss.
 */
public class ShovelerMeleeGoal implements Goal<Mob> {
    private final HereMobbyPlugin plugin;
    private final Mob mob;
    private final GoalKey<Mob> key;
    private long lastAttack = 0;
    private final long cooldown = 1500; // 1.5 seconds between hits

    public ShovelerMeleeGoal(HereMobbyPlugin plugin, Mob mob) {
        this.plugin = plugin;
        this.mob = mob;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "shoveler_melee"));
    }

    @Override
    public boolean shouldActivate() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isValid() || target.isDead()) return false;
        
        return mob.getLocation().distanceSquared(target.getLocation()) < 6.25; // 2.5 block reach
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack >= cooldown) {
            mob.swingMainHand();
            
            // Ignore armor using the Magic damage source
            DamageType magicType = org.bukkit.Bukkit.getRegistry(DamageType.class).get(NamespacedKey.minecraft("magic"));
            DamageSource source;
            if (magicType != null) {
                source = DamageSource.builder(magicType).withDirectEntity(mob).withCausingEntity(mob).build();
            } else {
                source = DamageSource.builder(DamageType.MAGIC).withDirectEntity(mob).withCausingEntity(mob).build();
            }
            
            target.damage(20.0, source); // Deal exactly 10 hearts of armor-ignoring damage
            
            // Freeze target for 2 seconds (40 ticks) using vignette + slowness V
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 40, 4));
            new org.bukkit.scheduler.BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 40 || !target.isValid()) {
                        cancel();
                        return;
                    }
                    target.setFreezeTicks(140);
                    ticks += 2;
                }
            }.runTaskTimer(plugin, 0, 2);
            
            lastAttack = now;
        }
    }

    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
