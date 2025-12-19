package com.randomteleport.managers;

import com.randomteleport.utils.MessageHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages countdown timers for player teleportation
 */
public class CountdownManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final TeleportManager teleportManager;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, BukkitTask> activeCountdowns;
    
    public CountdownManager(JavaPlugin plugin, ConfigManager configManager, 
                           TeleportManager teleportManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.teleportManager = teleportManager;
        this.playerDataManager = playerDataManager;
        this.activeCountdowns = new HashMap<>();
    }
    
    /**
     * Starts a countdown for a player
     * 
     * @param player The player to start the countdown for
     */
    public void startCountdown(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Cancel any existing countdown for this player
        cancelCountdown(playerId);
        
        int countdownSeconds = configManager.getCountdownSeconds();
        
        // Send initial countdown message
        String startMessage = configManager.getMessage("countdown-start");
        if (!startMessage.isEmpty()) {
            String formatted = MessageHelper.format(startMessage, "seconds", String.valueOf(countdownSeconds));
            MessageHelper.sendMessage(player, formatted);
        }
        
        // Create countdown task
        BukkitRunnable countdownRunnable = new BukkitRunnable() {
            private int remaining = countdownSeconds;
            
            @Override
            public void run() {
                // Check if player is still online
                Player currentPlayer = plugin.getServer().getPlayer(playerId);
                if (currentPlayer == null || !currentPlayer.isOnline()) {
                    cancelCountdown(playerId);
                    return;
                }
                
                remaining--;
                
                if (remaining > 0) {
                    // Send countdown message
                    String message = configManager.getMessage("countdown-remaining");
                    if (!message.isEmpty()) {
                        String formatted = MessageHelper.format(message, "seconds", String.valueOf(remaining));
                        MessageHelper.sendMessage(currentPlayer, formatted);
                    }
                } else {
                    // Countdown finished, teleport player
                    cancelCountdown(playerId);
                    teleportPlayer(currentPlayer);
                }
            }
        };
        
        // Run countdown task every second
        BukkitTask task = countdownRunnable.runTaskTimer(plugin, 20L, 20L); // 20 ticks = 1 second
        activeCountdowns.put(playerId, task);
    }
    
    /**
     * Cancels a countdown for a player
     * 
     * @param playerId The player's UUID
     */
    public void cancelCountdown(UUID playerId) {
        BukkitTask task = activeCountdowns.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Checks if a player has an active countdown
     * 
     * @param playerId The player's UUID
     * @return true if the player has an active countdown
     */
    public boolean hasActiveCountdown(UUID playerId) {
        return activeCountdowns.containsKey(playerId);
    }
    
    /**
     * Teleports a player to a random safe location
     * 
     * @param player The player to teleport
     */
    private void teleportPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        World world = player.getWorld();
        if (world == null) {
            return;
        }
        
        // Send teleporting message
        String teleportingMessage = configManager.getMessage("teleporting");
        if (!teleportingMessage.isEmpty()) {
            MessageHelper.sendMessage(player, teleportingMessage);
        }
        
        // Find safe location (this will retry indefinitely until found)
        // Run in async task to avoid blocking main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLocation = teleportManager.findSafeLocation(world);
            
            if (safeLocation == null) {
                // This should never happen as findSafeLocation retries indefinitely
                plugin.getLogger().warning("Failed to find safe location for player " + player.getName());
                String failedMessage = configManager.getMessage("teleport-failed");
                if (!failedMessage.isEmpty()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        MessageHelper.sendMessage(player, failedMessage);
                    });
                }
                return;
            }
            
            // Teleport player using async chunk loading
            teleportManager.teleportPlayer(player, safeLocation, () -> {
                // Only mark as teleported after successful teleportation
                String worldName = world.getName();
                playerDataManager.markPlayerTeleported(player.getUniqueId(), worldName);
                plugin.getLogger().info("Successfully teleported player " + player.getName() + 
                    " to " + safeLocation.getBlockX() + ", " + safeLocation.getBlockY() + ", " + safeLocation.getBlockZ());
            });
        });
    }
    
    /**
     * Cancels all active countdowns (used on plugin disable)
     */
    public void cancelAll() {
        for (BukkitTask task : activeCountdowns.values()) {
            task.cancel();
        }
        activeCountdowns.clear();
    }
}
