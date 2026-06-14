package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TerrainCycleGoal extends AbstractSpellGoal {
    private static final Map<Location, BlockData> changedBlocks = new ConcurrentHashMap<>();

    public TerrainCycleGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "terrain_cycle", 12000); // 12s cooldown
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.8f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                Location center = target.getLocation();
                int cx = center.getBlockX();
                int cy = center.getBlockY();
                int cz = center.getBlockZ();

                boss.getWorld().spawnParticle(Particle.WITCH, center.clone().add(0, 0.5, 0), 20, 1.0, 0.2, 1.0, 0.05);
                boss.getWorld().playSound(center, Sound.BLOCK_MUD_PLACE, 1.0f, 0.8f);

                target.damage(10.0, boss);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));

                Map<Location, BlockData> batchOriginals = new HashMap<>();

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location floorLoc = new Location(center.getWorld(), cx + x, cy - 1, cz + z);
                        Block floorBlock = floorLoc.getBlock();

                        if (floorBlock.getType().isSolid()) {
                            BlockData originalFloor = changedBlocks.get(floorLoc);
                            if (originalFloor == null) {
                                originalFloor = floorBlock.getBlockData().clone();
                                changedBlocks.put(floorLoc, originalFloor);
                                batchOriginals.put(floorLoc, originalFloor);
                            }
                            floorBlock.setType(Material.MUD);
                        }
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<Location, BlockData> entry : batchOriginals.entrySet()) {
                            Location loc = entry.getKey();
                            BlockData original = entry.getValue();
                            loc.getBlock().setBlockData(original);
                            changedBlocks.remove(loc);
                        }
                    }
                }.runTaskLater(plugin, 160L); // 8 seconds delay

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L));
    }

    public static void restoreAll() {
        for (Map.Entry<Location, BlockData> entry : changedBlocks.entrySet()) {
            entry.getKey().getBlock().setBlockData(entry.getValue());
        }
        changedBlocks.clear();
    }
}
