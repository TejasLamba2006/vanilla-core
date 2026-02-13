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

:::danger Java Version Check
Vanilla Core will not load on servers running Java versions older than 21. Before troubleshooting any issues, verify your Java version:

```bash
java -version
```

The output should show version 21 or higher.
:::

## Installation Steps

### Step 1: Download the Plugin

Download the latest version of Vanilla Core from one of these sources:

- [Modrinth](https://modrinth.com/plugin/vanillacorewastaken) (Recommended)

Download the JAR file for the latest version.

### Step 2: Stop Your Server

Before adding new plugins, always stop your server properly:

```
/stop
```

:::warning Never Hot-Load Plugins
Do not use plugin managers to load Vanilla Core while the server is running. Always stop the server, add the plugin, then start again. Hot-loading can cause memory leaks and unexpected behavior.
:::

### Step 3: Add the Plugin File

1. Navigate to your server's `plugins` folder
2. Copy the downloaded JAR into the folder
3. Do not rename the file

### Step 4: Start Your Server

Start your server normally. During startup, watch for these console messages:

```
[Vanilla Core] Loading Vanilla Core vX.X.X
[Vanilla Core] Enabling Vanilla Core vX.X.X
[Vanilla Core] Configuration loaded successfully
[Vanilla Core] Loaded 12 features
[Vanilla Core] Vanilla Core has been enabled!
```

### Step 5: Verify Installation

Once the server is running, verify the installation:

1. Run `/plugins` and confirm Vanilla Core appears in green
2. Run `/vanilla` to open the configuration GUI
3. Check the console for any error messages

## First-Time Configuration

On first run, Vanilla Core creates these files in `plugins/Vanilla Core/`:

```
plugins/Vanilla Core/
├── config.yml      # Main configuration file
├── data/           # Feature-specific data storage
│   ├── item-limits.yml
│   ├── enchant-limits.yml
│   └── ...
└── lang/           # Language files (if applicable)
```

### Default State

By default, all features are loaded but most are disabled. This allows you to enable only the features you need.

### Configuration Methods

You can configure Vanilla Core in two ways:

**Method 1: In-Game GUI (Recommended for beginners)**

Run `/vanilla` to open the configuration GUI. This provides a visual interface for toggling features and adjusting settings.

<!-- Add screenshot here: first-time-config.png -->

**Method 2: Configuration Files**

Edit `plugins/Vanilla Core/config.yml` directly. This is faster for bulk changes and allows for comments and version control.

After editing config files, run `/vanilla reload` to apply changes.

## Installing Optional Dependencies

### WorldGuard (for Mob Manager)

If you want to use region-based mob spawning control:

1. Download [WorldGuard](https://dev.bukkit.org/projects/worldguard/files)
2. Download [WorldEdit](https://dev.bukkit.org/projects/worldedit/files) (WorldGuard requires this)
3. Place both JAR files in your `plugins` folder
4. Restart your server

:::tip WorldGuard Not Required
The Mob Manager feature works without WorldGuard, but you can only control global mob spawning. Region-specific controls require WorldGuard.
:::

## Updating Vanilla Core

When a new version is released:

1. Stop your server with `/stop`
2. Delete the old plugin JAR from `plugins/`
3. Add the new version's JAR file
4. Start your server

### What Happens During Updates

- Configuration files are preserved
- New config options are added with default values
- Deprecated options are removed automatically
- Player data and limits are kept

:::info Breaking Changes
Major version updates (like 1.x to 2.x) may include breaking changes. Always read the changelog before updating major versions.
:::

## Troubleshooting Installation

### Plugin Doesn't Load

**Symptom:** Vanilla Core doesn't appear in `/plugins` or appears in red

**Solutions:**

1. Check Java version: Must be 21 or newer
2. Check server version: Must be 1.21.1 or newer
3. Check console for specific error messages
4. Verify the JAR file isn't corrupted (re-download if needed)
5. Ensure file permissions allow reading the JAR

### Console Shows Errors on Startup

**Symptom:** Red error messages mentioning Vanilla Core during startup

**Solutions:**

1. Read the full error message - it usually explains the problem
2. Common causes:
   - Missing dependency (if error mentions another plugin)
   - Corrupt configuration (delete config.yml to regenerate)
   - Version mismatch (wrong Minecraft/Java version)

### Commands Don't Work

**Symptom:** `/vanilla` returns "Unknown command"

**Solutions:**

1. Verify the plugin loaded: `/plugins`
2. Check permissions: You need `vanillacore.admin` for `/vanilla`
3. If you're OP, commands should work automatically
4. Check for conflicting plugins that might intercept the command

### GUI Doesn't Open

**Symptom:** `/vanilla` runs but no GUI appears

**Solutions:**

1. Check console for inventory-related errors
2. Verify you're not in spectator mode
3. Check if another plugin is blocking inventory opens
4. Try running `/vanilla reload` first

### Configuration Not Saving

**Symptom:** Changes revert after server restart

**Solutions:**

1. Stop the server properly with `/stop` (don't kill the process)
2. Check file permissions on `plugins/Vanilla Core/`
3. Verify disk space is available
4. Look for write errors in the console during shutdown

## Server Compatibility

### Tested Server Software

| Software | Status | Notes |
|----------|--------|-------|
| Paper | Fully Supported | Recommended |
| Spigot | Supported | Full functionality |
| Purpur | Supported | Based on Paper |
| Pufferfish | Supported | Based on Paper |
| Folia | Not Supported | Region-threaded servers require special handling |

### Known Plugin Conflicts

Vanilla Core is designed to be compatible with most plugins. However, conflicts may occur with:

- Other enchantment limiting plugins
- Other item restriction plugins
- Plugins that modify inventory events aggressively

If you experience conflicts, please [report them on GitHub](https://github.com/TejasLamba2006/vanilla-core/issues).

---

Next: Learn about [Commands](./commands) and [Permissions](./permissions).
