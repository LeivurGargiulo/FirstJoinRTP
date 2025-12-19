package com.randomteleport.managers;

import com.randomteleport.utils.MessageHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Manages teleportation logic with safe location finding and async chunk loading
 */
public class TeleportManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Random random;
    
    public TeleportManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.random = new Random();
    }
    
    /**
     * Finds a safe random location within the configured radius
     * Retries indefinitely until a safe location is found
     * 
     * @param world The world to search in
     * @return A safe location, or null if world is invalid
     */
    public Location findSafeLocation(World world) {
        if (world == null) {
            return null;
        }
        
        Location spawnLocation = world.getSpawnLocation();
        int minX = configManager.getMinX();
        int maxX = configManager.getMaxX();
        int minZ = configManager.getMinZ();
        int maxZ = configManager.getMaxZ();
        
        // Retry indefinitely until a safe location is found
        while (true) {
            // Generate random coordinates within the configured radius
            int x = spawnLocation.getBlockX() + minX + random.nextInt(maxX - minX + 1);
            int z = spawnLocation.getBlockZ() + minZ + random.nextInt(maxZ - minZ + 1);
            
            // Find the highest safe block at these coordinates
            Location candidate = findHighestSafeBlock(world, x, z);
            
            if (candidate != null && isLocationSafe(candidate)) {
                return candidate;
            }
        }
    }
    
    /**
     * Finds the highest safe block at given X and Z coordinates
     * 
     * @param world The world to search in
     * @param x The X coordinate
     * @param z The Z coordinate
     * @return A location at the highest safe block, or null if none found
     */
    private Location findHighestSafeBlock(World world, int x, int z) {
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();
        
        // Use getHighestBlockYAt for better performance (works even if chunk not loaded)
        int highestY = world.getHighestBlockYAt(x, z);
        
        // Search from highest Y down to Y60 minimum (to avoid caves)
        // Start from the higher of: highestY or 60, but don't exceed maxHeight
        int startY = Math.min(Math.max(60, highestY), maxHeight - 1);
        for (int y = startY; y >= Math.max(60, minHeight); y--) {
            Block block = world.getBlockAt(x, y, z);
            Block blockAbove = world.getBlockAt(x, y + 1, z);
            
            // Check if this is a solid block with air above
            if (isSolid(block.getType()) && 
                (blockAbove.getType() == Material.AIR || blockAbove.getType() == Material.CAVE_AIR)) {
                // Check if there's enough air space (at least 2 blocks)
                Block blockAbove2 = world.getBlockAt(x, y + 2, z);
                if (blockAbove2.getType() == Material.AIR || blockAbove2.getType() == Material.CAVE_AIR) {
                    return new Location(world, x + 0.5, y + 1, z + 0.5);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Validates if a location is safe for teleportation
     * 
     * @param location The location to validate
     * @return true if the location is safe, false otherwise
     */
    public boolean isLocationSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        Block block = location.getBlock();
        Block below = location.clone().subtract(0, 1, 0).getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();
        
        // Check if player position is in air
        if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR) {
            return false;
        }
        
        // Check if block above is air (need at least 2 blocks of air)
        if (above.getType() != Material.AIR && above.getType() != Material.CAVE_AIR) {
            return false;
        }
        
        // Check if block below is solid
        if (!isSolid(below.getType())) {
            return false;
        }
        
        // Avoid water and lava
        if (block.getType() == Material.WATER || block.getType() == Material.LAVA ||
            below.getType() == Material.WATER || below.getType() == Material.LAVA ||
            above.getType() == Material.WATER || above.getType() == Material.LAVA) {
            return false;
        }
        
        // Check if location is not in void
        if (location.getY() < location.getWorld().getMinHeight()) {
            return false;
        }
        
        // Ensure location is above Y60 to avoid caves
        if (location.getY() < 60) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Teleports a player to a location using async chunk loading
     * 
     * @param player The player to teleport
     * @param location The target location
     * @param onComplete Callback to execute after teleportation (success or failure)
     */
    public void teleportPlayer(Player player, Location location, Runnable onComplete) {
        if (player == null || !player.isOnline() || location == null || location.getWorld() == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        World world = location.getWorld();
        // Calculate chunk coordinates without loading the chunk
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        
        // Load chunk asynchronously
        CompletableFuture<org.bukkit.Chunk> chunkFuture = world.getChunkAtAsync(chunkX, chunkZ);
        
        chunkFuture.thenAccept(chunk -> {
            // Chunk is now loaded, teleport on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && player.getWorld().equals(world)) {
                    try {
                        player.teleport(location);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to teleport player " + player.getName() + ": " + e.getMessage());
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }).exceptionally(throwable -> {
            plugin.getLogger().warning("Failed to load chunk for teleportation: " + throwable.getMessage());
            // Try to teleport anyway (chunk might already be loaded)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && player.getWorld().equals(world)) {
                    try {
                        player.teleport(location);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to teleport player " + player.getName() + ": " + e.getMessage());
                    }
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
            return null;
        });
    }
    
    /**
     * Checks if a material is solid and safe to stand on
     * 
     * @param material The material to check
     * @return true if the material is solid and safe
     */
    private boolean isSolid(Material material) {
        return material.isSolid() && 
               material != Material.BARRIER && 
               material != Material.BEDROCK &&
               material != Material.LAVA &&
               material != Material.WATER;
    }
}
