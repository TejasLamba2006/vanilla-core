---
sidebar_position: 2
---

# Installation

This guide covers everything you need to install and configure Vanilla Core on your Minecraft server.

## Server Requirements

Before installing Vanilla Core, verify your server meets these requirements:

| Component | Minimum | Recommended | Notes |
|-----------|---------|-------------|-------|
| Server Software | Spigot 1.21.1 | Paper 1.21.1+ | Paper provides better performance and additional API features |
| Java Version | Java 21 | Java 21+ | Minecraft 1.21.1 requires Java 21 |
| RAM | 2GB | 4GB+ | Vanilla Core itself uses minimal memory |
| WorldGuard | 7.0 | 7.0.9+ | Optional - only needed for Mob Manager region features |
| WorldEdit | 7.3 | 7.3+ | Required if using WorldGuard |

:::warning Java 21 Required
Minecraft 1.21.1 servers require Java 21 or newer. If you're on Java 17 or earlier, the server won't start. Download Java 21 from [Adoptium](https://adoptium.net/) before proceeding.
:::

## Installation Steps

### Step 1: Download the Plugin

Download the latest version of Vanilla Core from one of these sources:

- [Modrinth](https://modrinth.com/plugin/vanillacorewastaken) (Recommended)

Download the JAR file for the latest version.

:::tip Check the Version Number
Make sure you download a version that matches your Minecraft version. The filename usually includes the Minecraft version like `vanilla-core-1.21.1-v1.3.0.jar`
:::

### Step 2: Stop Your Server

Before adding new plugins, always stop your server properly:

```
/stop
```

Never use `kill` or `Ctrl+C` as this can corrupt data. Wait for the server to save all chunks and shut down cleanly.

### Step 3: Add the Plugin

1. Navigate to your server's `plugins` folder
2. Copy the downloaded JAR file into this folder
3. Do NOT rename the JAR file
4. Do NOT extract or unzip the JAR file

:::warning Never Hot-Load Plugins
Do not use plugin managers to load Vanilla Core while the server is running. Always stop the server, add the plugin, then start again. Hot-loading can cause memory leaks and unexpected behavior.
:::

### Step 4: Start Your Server

Start your server normally. Watch the console output for any errors.

You should see lines like:
```
[Vanilla Core] Enabling Vanilla Core v1.3.0
[Vanilla Core] Configuration loaded successfully
[Vanilla Core] Registered 15 features
```

### Step 5: Verify Installation

Once the server is running:

1. Run `/plugins` to see all installed plugins
2. Vanilla Core should appear in green
3. Run `/vanilla` to open the settings menu
4. If the menu opens, installation was successful

:::tip First Time Setup
Everything is disabled by default. Open `/vanilla` and click features to enable only what you need.
:::

## Post-Installation Setup

### Configure Features

1. Open the settings GUI with `/vanilla`
2. Click items to toggle features on/off
3. Right-click items to access feature-specific settings
4. Changes save automatically

Or edit `plugins/Vanilla Core/config.yml` directly if you prefer YAML.

:::tip GUI is Beginner-Friendly
New to server management? Use the GUI. It prevents syntax errors and shows all available options with helpful descriptions.
:::

### Set Permissions

If you want specific players to access admin features:

```yaml
permissions:
  vanillacore.admin:
    description: Full access to Vanilla Core
    default: op
```

See [Permissions](./permissions) for the full list.

### Test Features

After enabling features:

1. Test them in-game
2. Check console for errors
3. Adjust settings if needed
4. Use `/vanilla reload` to apply config changes without restarting

:::info WorldGuard Integration
The Mob Manager feature works without WorldGuard, but you can only control global mob spawning. Region-specific controls require WorldGuard.
:::

## Troubleshooting Installation

### Plugin Doesn't Load

**Symptom:** Vanilla Core doesn't appear in `/plugins` or appears in red

**Solutions:**

1. Check Java version: Run `java -version` in console. Must be 21 or newer
2. Check server version: Must be Paper/Spigot 1.21.1 or newer
3. Check console for specific error messages
4. Verify the JAR file isn't corrupted (re-download if needed)
5. Ensure file permissions allow reading the JAR
6. Remove any old versions of Vanilla Core from the plugins folder

:::warning One JAR Only
Having multiple versions of Vanilla Core in the plugins folder will cause conflicts. Keep only the latest version.
:::

### Console Shows Errors on Startup

**Symptom:** Red error messages mentioning Vanilla Core during startup

**Common error messages and solutions:**

**"java.lang.UnsupportedClassVersionError"**
- Your Java version is too old
- Solution: Upgrade to Java 21 or newer

**"Could not load config.yml"**
- Config file has invalid YAML syntax
- Solution: Delete `plugins/Vanilla Core/config.yml` to regenerate a clean config

**"Missing dependency: WorldGuard"**
- This is just a warning, not an error
- WorldGuard is optional unless you need region-specific mob control

**"Failed to initialize feature: [name]"**
- Something went wrong loading a specific feature
- Solution: Report this on GitHub with the full error trace

### Commands Don't Work

**Symptom:** `/vanilla` returns "Unknown command"

**Solutions:**

1. Verify the plugin loaded: `/plugins` should show Vanilla Core in green
2. Check permissions: You need `vanillacore.admin` or be OP
3. If you're OP, commands should work automatically
4. Check for conflicting plugins that might intercept the command
5. Try `/minecraft:vanilla` to bypass possible command conflicts

### GUI Doesn't Open

**Symptom:** `/vanilla` runs but no GUI appears

**Solutions:**

1. Check console for inventory-related errors
2. Verify you're not in spectator mode
3. Check if another plugin is blocking inventory opens
4. Try closing any other GUIs first
5. Try running `/vanilla reload` first
6. Check if you have inventory space (shouldn't matter but sometimes does)

### Configuration Not Saving

**Symptom:** Changes revert after server restart

**Solutions:**

1. Stop the server properly with `/stop` (don't kill the process)
2. Check file permissions on `plugins/Vanilla Core/` folder
3. Verify disk space is available
4. Look for write errors in the console during shutdown
5. Don't edit config.yml while server is running

:::warning Edit Config Offline
Always edit config.yml while the server is stopped, or use `/vanilla reload` after making changes. The plugin reads the config on startup and when you reload.
:::

## Server Compatibility

### Tested Server Software

| Software | Status | Notes |
|----------|--------|-------|
| Paper | Fully Supported | Recommended |
| Spigot | Supported | Full functionality |
| Purpur | Supported | Based on Paper |
| Pufferfish | Supported | Based on Paper |
| Folia | Not Supported | Region-threaded servers require special handling |

:::info Why Paper?
Paper includes performance optimizations and additional APIs that make plugins run better. Vanilla Core works on Spigot but runs smoother on Paper.
:::

### Known Plugin Conflicts

Vanilla Core is designed to be compatible with most plugins. However, conflicts may occur with:

- Other enchantment limiting plugins (disable one or the other)
- Other item restriction plugins (may double-restrict items)
- Plugins that modify inventory events aggressively
- Ancient versions of EssentialsX (update to latest)

:::tip Report Conflicts
If you experience conflicts, [report them on GitHub](https://github.com/TejasLamba2006/vanilla-core/issues) with:
- Plugin name and version
- Vanilla Core version
- Server software and version
- Steps to reproduce the conflict
:::

## Performance Considerations

Vanilla Core is designed to be lightweight and performant:

- Event listeners use early returns to avoid unnecessary processing
- Features only run when enabled
- Memory usage is minimal (under 10MB typically)
- No database required - all data stored in YAML configs

:::tip Performance Mode
For large servers (100+ players), consider:
- Using item limiter "on-hit" mode instead of "on-move" (less frequent checks)
- Disabling enchantment scans if not needed
- Using WorldGuard regions to limit mob checks to specific areas
:::

## Getting Help

If you're stuck:

1. Check this documentation first
2. Search [existing GitHub issues](https://github.com/TejasLamba2006/vanilla-core/issues)
3. Join our [Discord](https://discord.gg/7fQPG4Grwt) for quick help
4. Open a new GitHub issue if you found a bug

Include:
- Vanilla Core version
- Server software and version
- Java version
- Full error message from console (if any)
- Steps to reproduce the problem

---

Next: Learn about [Commands](./commands) and [Permissions](./permissions).
