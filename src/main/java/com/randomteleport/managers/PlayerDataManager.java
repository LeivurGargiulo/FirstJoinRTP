package com.randomteleport.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages player data persistence using YAML file storage
 */
public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private BukkitRunnable saveTask;
    
    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        loadData();
        startAutoSave();
    }
    
    /**
     * Loads player data from the YAML file
     */
    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create players.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Saves player data to the YAML file
     */
    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players.yml: " + e.getMessage());
        }
    }
    
    /**
     * Starts the auto-save task that saves data every 5 minutes
     */
    private void startAutoSave() {
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                // File I/O must be done on main thread
                saveData();
            }
        };
        saveTask.runTaskTimer(plugin, 6000L, 6000L); // Every 5 minutes (6000 ticks)
    }
    
    /**
     * Checks if a player has been teleported to a specific world
     * 
     * @param playerId The player's UUID
     * @param worldName The world name
     * @return true if the player has been teleported to this world, false otherwise
     */
    public boolean hasPlayerBeenTeleported(UUID playerId, String worldName) {
        List<String> worlds = dataConfig.getStringList("players." + playerId.toString() + ".worlds");
        return worlds.contains(worldName);
    }
    
    /**
     * Marks a player as teleported to a specific world
     * This method should be called from the main thread
     * 
     * @param playerId The player's UUID
     * @param worldName The world name
     */
    public void markPlayerTeleported(UUID playerId, String worldName) {
        String path = "players." + playerId.toString() + ".worlds";
        List<String> worlds = new ArrayList<>(dataConfig.getStringList(path));
        if (!worlds.contains(worldName)) {
            worlds.add(worldName);
            dataConfig.set(path, worlds);
            // Save data on main thread (this method should be called from main thread)
            saveData();
        }
    }
    
    /**
     * Shuts down the manager and saves data
     */
    public void shutdown() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        saveData();
    }
}
