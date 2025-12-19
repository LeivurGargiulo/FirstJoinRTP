package com.randomteleport.listeners;

import com.randomteleport.managers.ConfigManager;
import com.randomteleport.managers.CountdownManager;
import com.randomteleport.managers.PlayerDataManager;
import com.randomteleport.utils.MessageHelper;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens to world change events and triggers teleportation logic
 */
public class WorldChangeListener implements Listener {
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private final CountdownManager countdownManager;
    
    public WorldChangeListener(ConfigManager configManager, PlayerDataManager playerDataManager, 
                               CountdownManager countdownManager) {
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
        this.countdownManager = countdownManager;
    }
    
    /**
     * Handles when a player changes worlds
     * Checks if player should be teleported and starts countdown if conditions are met
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        
        World newWorld = player.getWorld();
        if (newWorld == null) {
            return;
        }
        
        String targetWorld = configManager.getTargetWorld();
        String currentWorldName = newWorld.getName();
        
        // Check if player is now in the configured target world
        if (!currentWorldName.equals(targetWorld)) {
            return;
        }
        
        // Check if player has already been teleported to this world
        if (playerDataManager.hasPlayerBeenTeleported(player.getUniqueId(), currentWorldName)) {
            String alreadyTeleportedMessage = configManager.getMessage("already-teleported");
            if (!alreadyTeleportedMessage.isEmpty()) {
                MessageHelper.sendMessage(player, alreadyTeleportedMessage);
            }
            return;
        }
        
        // Check if player already has an active countdown
        if (countdownManager.hasActiveCountdown(player.getUniqueId())) {
            return;
        }
        
        // Start countdown for teleportation
        countdownManager.startCountdown(player);
    }
    
    /**
     * Handles when a player quits
     * Cancels any active countdown for the player
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (player != null) {
            countdownManager.cancelCountdown(player.getUniqueId());
        }
    }
}
