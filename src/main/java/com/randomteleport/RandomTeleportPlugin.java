package com.randomteleport;

import com.randomteleport.listeners.WorldChangeListener;
import com.randomteleport.managers.ConfigManager;
import com.randomteleport.managers.CountdownManager;
import com.randomteleport.managers.PlayerDataManager;
import com.randomteleport.managers.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RandomTeleport Plugin
 * 
 * Automatically teleports players to random safe locations within a configurable
 * square radius when they first join a specific configured world.
 * Features:
 * - Configurable target world
 * - Square radius teleportation area
 * - 3-second countdown before teleportation
 * - Safe location finding with infinite retries
 * - Async chunk loading for performance
 * - Spanish configurable messages
 * - One-time teleportation per player per world
 */
public class RandomTeleportPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private TeleportManager teleportManager;
    private CountdownManager countdownManager;
    
    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        
        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);
        
        // Initialize teleport manager
        teleportManager = new TeleportManager(this, configManager);
        
        // Initialize countdown manager
        countdownManager = new CountdownManager(this, configManager, teleportManager, playerDataManager);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(
            new WorldChangeListener(configManager, playerDataManager, countdownManager), 
            this
        );
        
        getLogger().info("RandomTeleport has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Cancel all active countdowns
        if (countdownManager != null) {
            countdownManager.cancelAll();
        }
        
        // Save player data
        if (playerDataManager != null) {
            playerDataManager.shutdown();
        }
        
        getLogger().info("RandomTeleport has been disabled!");
    }
    
    /**
     * Reloads the plugin configuration
     */
    public void reload() {
        if (configManager != null) {
            configManager.reload();
        }
    }
    
    // Getters for other plugins if needed
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public CountdownManager getCountdownManager() {
        return countdownManager;
    }
}
