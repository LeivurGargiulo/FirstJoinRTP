package com.randomteleport.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class for formatting and sending messages to players
 */
public class MessageHelper {
    
    /**
     * Sends a message to a player with color code translation
     * 
     * @param player The player to send the message to
     * @param message The message to send (supports & color codes)
     */
    public static void sendMessage(Player player, String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(colored);
    }
    
    /**
     * Formats a message by replacing placeholders
     * 
     * @param message The message template with {key} placeholders
     * @param replacements Pairs of key-value replacements (key1, value1, key2, value2, ...)
     * @return The formatted message
     */
    public static String format(String message, String... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                result = result.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return result;
    }
}
