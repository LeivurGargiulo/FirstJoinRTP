# FirstJoinRTP  

FirstJoinRTP is a lightweight Paper plugin that automatically teleports players to random safe locations within a configurable area when they first join a specific world. Perfect for survival servers where you want players to start in random locations rather than at spawn.

## Features

- **Automatic Teleportation**: Players are automatically teleported when entering a configured world
- **One-Time Teleportation**: Each player is only teleported once per world (prevents repeated teleports)
- **Configurable Area**: Define a square radius where players can be teleported
- **Countdown Timer**: Configurable countdown before teleportation (default 3 seconds)
- **Safe Location Finding**: Automatically finds safe locations with infinite retries
- **Performance Optimized**: Uses async chunk loading to prevent server lag
- **Customizable Messages**: Fully customizable messages (defaults in Spanish)
- **Data Persistence**: Player teleportation status is saved automatically
- **Safety Checks**: Avoids water, lava, void, and cave locations (minimum Y=60)

## Requirements

- **Server Software**: Paper or Spigot 1.20 or higher
- **Java Version**: Java 21 or higher

## Installation

1. Download the latest JAR file from Modrinth
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use `/reload`
4. The plugin will automatically create a `config.yml` file in `plugins/randomteleport/`

## Configuration

The configuration file is located at `plugins/randomteleport/config.yml`. After making changes, reload the plugin or restart the server.

### Configuration Options

#### `target-world` (default: `"world"`)

The name of the world where teleportation should occur. Players entering this world will be teleported (if they haven't been teleported before).

```yaml
target-world: "world"
```

#### `radius` (default: -1000 to 1000)

Defines the square area where players can be teleported. Coordinates are relative to the world's spawn location.

```yaml
radius:
  min-x: -1000
  max-x: 1000
  min-z: -1000
  max-z: 1000
```

**Example**: With spawn at (0, 64, 0), `min-x: -1000, max-x: 1000` creates a 2000x2000 block area centered at spawn.

#### `countdown-seconds` (default: `3`)

Duration of the countdown before teleportation in seconds. Players see countdown messages during this time.

```yaml
countdown-seconds: 3
```

**Note**: Setting this to 0 will teleport immediately (not recommended as players won't see any warning).

#### `messages` (all configurable)

All messages support Minecraft color codes using the `&` symbol. The placeholder `{seconds}` is replaced with the countdown number.

```yaml
messages:
  countdown-start: "&aSerás teletransportado en {seconds} segundos..."
  countdown-remaining: "&aTeletransporte en {seconds} segundos..."
  teleporting: "&a¡Teletransportando!"
  teleport-failed: "&cBuscando ubicación segura..."
  already-teleported: "&7Ya has sido teletransportado a este mundo anteriormente."
```

**Available Messages:**
- `countdown-start`: Shown when countdown begins
- `countdown-remaining`: Shown each second during countdown
- `teleporting`: Shown when teleportation occurs
- `teleport-failed`: Shown if location finding fails (shouldn't happen in normal operation)
- `already-teleported`: Shown if player has already been teleported to this world

**Color Code Reference:**
- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Aqua
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Aqua
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White
- `&r` - Reset
- `&l` - Bold
- `&o` - Italic
- `&n` - Underline
- `&m` - Strikethrough
- `&k` - Obfuscated

## How It Works

1. When a player enters the configured target world, the plugin checks if they've been teleported before
2. If not teleported before, a countdown starts (default 3 seconds)
3. During the countdown, players see messages indicating when they'll be teleported
4. After the countdown, the plugin finds a random safe location within the configured radius
5. The plugin retries indefinitely until a safe location is found
6. Once found, the player is teleported using async chunk loading
7. The player is marked as teleported for this world (stored in `players.yml`)

## Safety Features

The plugin ensures players are teleported to safe locations by:

- **Solid Ground**: Checking for solid ground below the player
- **Air Space**: Ensuring at least 2 blocks of air above the player
- **Liquid Avoidance**: Avoiding water and lava blocks
- **Void Protection**: Avoiding void locations
- **Cave Avoidance**: Only teleporting to locations above Y=60 (avoids caves)
- **Infinite Retries**: Retrying indefinitely until a safe location is found

## Player Data

Player teleportation status is stored in `plugins/randomteleport/players.yml`. This file:

- Tracks which players have been teleported to which worlds
- Auto-saves every 5 minutes
- Saves immediately when a player is teleported
- Saves on server shutdown

**To reset a player's teleportation status:**
1. Edit `plugins/randomteleport/players.yml`
2. Find the player's UUID entry under `players:`
3. Remove the world name from their `worlds:` list, or remove their entire entry
4. Save the file

**To reset all players:**
- Delete the `players.yml` file (it will be recreated automatically)

## Performance

The plugin is optimized for performance:

- **Async Chunk Loading**: Uses asynchronous chunk loading to prevent server lag
- **Async Location Finding**: Safe location finding runs asynchronously
- **Batched Saves**: Player data saves are batched (every 5 minutes)
- **Minimal Impact**: Designed to have minimal impact on server performance

## Troubleshooting

### Players Not Being Teleported

- Check that `target-world` matches your world name exactly (case-sensitive)
- Verify the world exists and is loaded
- Check server logs for errors
- Ensure the player hasn't been teleported before (check `players.yml`)

### Players Teleported to Unsafe Locations

- This shouldn't happen, but if it does, check that the world has valid terrain
- Ensure the radius area contains valid terrain (not all void/water)
- Verify the world is properly generated

### Countdown Not Showing

- Check that messages are properly configured in `config.yml`
- Verify color codes are using `&` not `§`
- Check server console for any errors

### Reset Player Teleportation Status

- Edit `plugins/randomteleport/players.yml` and remove the player's UUID entry
- Or delete the file to reset all players
- Restart the server or wait for the next auto-save

### Plugin Not Loading

- Ensure you're using Paper or Spigot 1.20 or higher
- Check that Java 21 or higher is installed
- Review server logs for error messages
- Verify the JAR file is not corrupted

## API Version

- Requires Paper API version 1.20 or higher

## Commands & Permissions

This plugin has no commands or permissions - it works automatically based on world changes.

## Integration

The plugin provides public getters for managers if you want to integrate with other plugins:

- `getConfigManager()` - Access configuration
- `getPlayerDataManager()` - Access player data
- `getTeleportManager()` - Access teleportation logic
- `getCountdownManager()` - Access countdown management

Configuration can be reloaded by calling the `reload()` method via other plugins.

## Support

If you encounter any issues or have questions:

1. Check the troubleshooting section above
2. Review server logs for error messages
3. Verify your configuration matches the examples
4. Ensure you're using a compatible server version

## License

Check the plugin's license file for information about usage and distribution.
