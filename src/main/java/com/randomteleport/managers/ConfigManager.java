package com.randomteleport.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages configuration loading and access
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * Reloads the configuration from disk
     */
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    /**
     * Gets the target world name where teleportation should occur
     * 
     * @return The world name
     */
    public String getTargetWorld() {
        return config.getString("target-world", "world");
    }
    
    /**
     * Gets the minimum X coordinate for the teleportation radius
     * 
     * @return The minimum X coordinate
     */
    public int getMinX() {
        return config.getInt("radius.min-x", -1000);
    }
    
    /**
     * Gets the maximum X coordinate for the teleportation radius
     * 
     * @return The maximum X coordinate
     */
    public int getMaxX() {
        return config.getInt("radius.max-x", 1000);
    }
    
    /**
     * Gets the minimum Z coordinate for the teleportation radius
     * 
     * @return The minimum Z coordinate
     */
    public int getMinZ() {
        return config.getInt("radius.min-z", -1000);
    }
    
    /**
     * Gets the maximum Z coordinate for the teleportation radius
     * 
     * @return The maximum Z coordinate
     */
    public int getMaxZ() {
        return config.getInt("radius.max-z", 1000);
    }
    
    /**
     * Gets the countdown duration in seconds
     * 
     * @return The countdown duration
     */
    public int getCountdownSeconds() {
        return config.getInt("countdown-seconds", 3);
    }
    
    /**
     * Gets a message from the configuration
     * 
     * @param key The message key
     * @return The message string, or empty string if not found
     */
    public String getMessage(String key) {
        return config.getString("messages." + key, "");
    }
}
