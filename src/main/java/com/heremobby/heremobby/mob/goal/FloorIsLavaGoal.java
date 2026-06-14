package com.heremobby.heremobby.mob.goal;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.mob.ActiveBoss;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spawns 5 lava sources around the target that disappear after 20 seconds.
 */
public class FloorIsLavaGoal extends AbstractSpellGoal {
    private static final Map<Location, BlockData> changedBlocks = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public FloorIsLavaGoal(HereMobbyPlugin plugin, ActiveBoss activeBoss) {
        super(plugin, activeBoss, "floor_is_lava", 40000); // 40s cooldown
    }

    @Override
    protected boolean canActivate(LivingEntity target) {
        return activeBoss.getEntity().getLocation().distanceSquared(target.getLocation()) < 400; // 20 block range
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

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.2f, 0.5f);

        activeBoss.setActiveTask(new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || target == null || !target.isValid()) {
                    activeBoss.stopCurrentSpell();
                    return;
                }

                Location targetLoc = target.getLocation();
                boss.getWorld().spawnParticle(Particle.FLAME, targetLoc, 50, 1.5, 0.5, 1.5, 0.05);
                boss.getWorld().playSound(targetLoc, Sound.ITEM_FIRECHARGE_USE, 1.2f, 0.8f);

                Set<Location> spots = new HashSet<>();
                spots.add(findFloorLocation(targetLoc));

                int attempts = 0;
                while (spots.size() < 5 && attempts < 50) {
                    attempts++;
                    int rx = random.nextInt(9) - 4; // -4 to 4
                    int rz = random.nextInt(9) - 4; // -4 to 4
                    spots.add(findFloorLocation(targetLoc.clone().add(rx, 0, rz)));
                }

                Map<Location, BlockData> batchOriginals = new HashMap<>();

                for (Location loc : spots) {
                    Block block = loc.getBlock();
                    BlockData original = changedBlocks.get(loc);
                    if (original == null) {
                        original = block.getBlockData().clone();
                        changedBlocks.put(loc, original);
                        batchOriginals.put(loc, original);
                    }
                    block.setType(Material.LAVA);
                    loc.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.05);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<Location, BlockData> entry : batchOriginals.entrySet()) {
                            Location l = entry.getKey();
                            BlockData orig = entry.getValue();
                            l.getBlock().setBlockData(orig);
                            changedBlocks.remove(l);
                            l.getWorld().spawnParticle(Particle.SMOKE, l.clone().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }.runTaskLater(plugin, 400L); // 20 seconds

                activeBoss.stopCurrentSpell();
            }
        }.runTaskLater(plugin, 10L)); // 0.5s cast delay
    }

    private Location findFloorLocation(Location start) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int y = start.getBlockY();
        int z = start.getBlockZ();
        
        for (int dy = 3; dy >= -3; dy--) {
            Block block = world.getBlockAt(x, y + dy, z);
            Block below = world.getBlockAt(x, y + dy - 1, z);
            if (!block.getType().isSolid() && below.getType().isSolid()) {
                return block.getLocation();
            }
        }
        return new Location(world, x, y, z);
    }

    public static void restoreAll() {
        for (Map.Entry<Location, BlockData> entry : changedBlocks.entrySet()) {
            entry.getKey().getBlock().setBlockData(entry.getValue());
        }
        changedBlocks.clear();
    }
}
