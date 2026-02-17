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
tip WorldGuard Not Required
The Mob Manager feature works without WorldGuard, but you can only control global mob spawning. Region-specific controls require WorldGuard.
info Breaking Changes
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

