---
sidebar_position: 12
---

# Frequently Asked Questions

Common questions and answers about Vanilla Core.

## General Questions

### What is Vanilla Core?

Vanilla Core is a plugin for Minecraft servers that adds PvP balance features, item limits, dimension locks, mob control, and quality-of-life improvements. It replaces the need for 10+ separate plugins.

### What versions does Vanilla Core support?

Vanilla Core supports **Paper/Spigot 1.21.1+** with **Java 21+**.

Older Minecraft versions are not supported. Each major Minecraft update requires plugin updates due to API changes.

### Does it work with Spigot?

Yes, but **Paper is recommended**. Paper includes performance optimizations and additional APIs that make Vanilla Core run smoother.

You won't lose any features on Spigot, but you might notice better performance on Paper, especially with larger player counts.

### Is Vanilla Core free?

Yes! Vanilla Core is completely free and open source under the MIT License. You can:
- Use it on any server (commercial or personal)
- Modify the code for your own use
- View the source code on GitHub

### Where can I download it?

- [Modrinth](https://modrinth.com/plugin/vanillacorewastaken) (Primary download source)

### Can I use this on a cracked/offline server?

Yes, Vanilla Core works on both online and offline mode servers. All features function the same way.

### How often is it updated?

Updates are released when new features are added or bugs are fixed. Follow updates on:
- Modrinth (download page shows version history)
- Discord (update announcements)
- GitHub (releases page)

---

## Installation & Setup

### The plugin doesn't load

Check the following in order:

1. **Java version**: Run `java -version` in console. Must show version 21 or newer
2. **Server version**: Must be Paper or Spigot 1.21.1 or newer
3. **Console errors**: Look for red error messages during startup
4. **File name**: Make sure it's `vanilla-core-xxx.jar`, not `vanilla-core-xxx.jar.zip`
5. **Plugins folder**: JAR must be directly in `plugins/`, not in a subfolder
6. **Duplicate files**: Remove any old versions of Vanilla Core

If still not working, copy the full error message from console and ask for help on Discord.

### Config file is missing

The config is generated automatically on first run. If it's missing:

1. Check if plugin loaded: `/plugins` should show Vanilla Core in green
2. Look in `plugins/Vanilla Core/` (note the space and capital letters)
3. Check console for write permission errors
4. Verify server has write access to the plugins folder

### How do I update?

Safe update process:

1. Download the new version from Modrinth
2. Stop your server with `/stop`
3. Backup your `plugins/Vanilla Core/` folder (optional but recommended)
4. Replace the old JAR with the new one in `plugins/`
5. Start your server
6. Check console for any warnings about config changes

Config files update automatically. If there are breaking changes, they will be mentioned in the changelog.

:::warning Read Changelogs Before Major Updates
Major version updates (like 1.x to 2.x) may include breaking changes. Always read the changelog on Modrinth before updating.
:::

### Do I need to configure it after installing?

No! Everything is disabled by default for safety. The plugin won't affect your server until you enable features.

To start using it:
1. Run `/vanilla` in-game
2. Click features you want to enable (they turn green)
3. Right-click features to configure them
4. Changes save automatically

### Should I use the GUI or edit config.yml?

For beginners: Use the GUI. It prevents YAML syntax errors and shows all available options.

For advanced users: Edit config.yml if you prefer. Just remember to run `/vanilla reload` after saving changes.

:::tip Mix and Match
You can enable features via GUI, then fine-tune specific settings in config.yml. Both methods work together.
:::

---

## Features

### Why isn't [feature] working?

Troubleshooting checklist:

1. **Is it enabled?** Check `/vanilla` or `config.yml` under `features.[feature-name].enabled`
2. **Do you have bypass?** OP players and those with bypass permissions ignore restrictions
3. **Conflicting plugins?** Another plugin might be interfering
4. **Console errors?** Check for red errors when the feature should trigger
5. **Correct version?** Some features require specific Minecraft versions

### How do I know which features I should enable?

Depends on your server type:

**For balanced PvP SMP:**
- Item Limiter (gaps and totems)
- Enchantment Limiter (cap sharpness/protection)
- Mace Limiter
- Netherite Disabler (if you want diamond meta)

**For casual SMP:**
- One Player Sleep
- Infinite Restock
- Dimension Locks (optional)

**For competitive PvP:**
- All combat balance features
- Item explosion immunity (so deaths don't lose items to TNT)

Start with basics, add more as needed. You can always change it later.

### Can I use only some features?

Yes! Every feature can be individually enabled/disabled. Enable only what you need, ignore the rest.

Features don't depend on each other - they work independently.

### Does it conflict with other plugins?

Vanilla Core is designed to be compatible with most plugins. Confirmed compatible with:

- EssentialsX
- LuckPerms
- WorldGuard
- Vault
- CMI
- PlaceholderAPI
- ProtocolLib

Possible conflicts with:
- Other enchantment limiting plugins (choose one)
- Other item restriction plugins (may double-restrict)
- Very old versions of EssentialsX (update to latest)

:::info Report Conflicts
Found a conflict? [Report it on GitHub](https://github.com/TejasLamba2006/vanilla-core/issues) with plugin name, version, and what broke.
:::

### Can I suggest new features?

Absolutely! Feature requests are welcome. To suggest a feature:

1. Check [existing requests](https://github.com/TejasLamba2006/vanilla-core/issues) first
2. If it's new, open an issue on GitHub
3. Explain: What does it do? Why is it useful? How should it work?
4. Be specific and include examples

Popular requests are more likely to be implemented.

### Will you add [specific feature]?

Maybe! Features are prioritized based on:
- How many people want it
- How well it fits the plugin's purpose
- Complexity and maintenance cost
- Whether it's better as a separate plugin

PvP balance and SMP quality-of-life features are most likely to be added.

---

## Performance

### Will this lag my server?

No. Vanilla Core is optimized for performance:

- Event listeners exit early when features are disabled
- Cooldown systems prevent spam checks
- No database or external API calls
- Uses efficient data structures (ConcurrentHashMap, etc.)
- Thread-safe where needed

Most features have zero performance impact when disabled.

### How much RAM does it use?

Vanilla Core has a minimal memory footprint, typically under 10MB.

For comparison, a typical Minecraft server uses 2-4GB+. Vanilla Core's impact is negligible.

### Which features are most expensive?

If you're worried about performance (100+ player servers):

**Cheapest** (basically free):
- One Player Sleep
- Dimension Locks
- Infinite Restock

**Moderate** (still very cheap):
- Item Limiter (on-hit mode)
- Enchantment Limiter
- Mace Limiter

**Slightly More Expensive** (still minimal):
- Item Limiter (on-move mode) - checks on every player movement
- Mob Manager with many disabled mobs

:::tip Optimize for Large Servers
For 100+ players, use Item Limiter in "on-hit" mode instead of "on-move". It checks less frequently but still works fine.
:::

### Can I check performance stats?

Use Spark or Timings to profile your server:

```
/spark profiler
```

Vanilla Core should appear minimal in the profiling results.

---

## Permissions

### Players can bypass restrictions

Common causes:

1. **Player has OP** - OPs have all permissions by default
2. **Has bypass permission** - Check for `vanillacore.bypass.*` or specific bypass perms
3. **Permission plugin issue** - Permissions might not be applying correctly
4. **Feature not enabled** - Verify feature is actually on

To test, remove OP temporarily and check if restrictions apply.

### How do I give staff bypass permissions?

Using LuckPerms:

```
/lp group staff permission set vanillacore.bypass.* true
```

This gives bypass for ALL features. For specific features:

```
/lp group staff permission set vanillacore.bypass.item-limiter true
```

See [Permissions](./permissions) for the full list.

### Do OPs bypass everything automatically?

Yes, by default. If you want OPs to be restricted too, you need to:

1. Use a permission plugin (LuckPerms recommended)
2. Remove OP from players
3. Give them admin permissions manually (without bypass permissions)

### How do I let players use `/vanilla` but not change settings?

Currently not supported - `/vanilla` requires admin permissions. We may add view-only access in the future.

Workaround: Show them the config.yml or wiki page to see current settings.

---

## Configuration

### Where is the config file?

`plugins/Vanilla Core/config.yml`

Note the space in "Vanilla Core" and capital letters.

### How do I reload config changes?

Two options:

1. **In-game:** `/vanilla reload` (instant, no downtime)
2. **Server restart:** Stop and start the server (safest for major changes)

Use `/vanilla reload` for quick tweaks. Use restart for major config overhauls.

:::warning Edit Config While Server is Stopped
If editing config.yml directly, stop the server first to avoid overwriting your changes. Or use `/vanilla reload` after saving.
:::

### Can I copy config to another server?

Yes! Copy the entire `plugins/Vanilla Core/` folder to the new server.

Make sure both servers run the same Vanilla Core version. Config format may change between major versions.

### What happens if config.yml gets corrupted?

The plugin will fail to load. To fix:

1. Delete `config.yml`
2. Restart server
3. A fresh config will be generated
4. Re-apply your settings

This is why backups are important before major changes.

### Can I use environment variables or placeholders?

Not currently supported. Config values are static and read from YAML directly.

---

## Troubleshooting

### Commands say "Unknown command"

1. Check plugin loaded: `/plugins` should show Vanilla Core in green
2. Use correct command: `/vanilla` not `/vanillacore`
3. Check permissions: Need `vanillacore.admin` or OP
4. Try full command: `/minecraft:vanilla` to bypass command conflicts

### GUI is empty or broken

1. Update to latest version
2. Check console for errors when opening GUI
3. Try `/vanilla reload` first
4. Delete config.yml and let it regenerate
5. Check if another plugin is blocking inventory opens

### Features reset after restart

If settings revert to defaults after restarting:

1. **Server not stopping properly** - Use `/stop`, don't kill process
2. **Permission errors** - Check console for write errors
3. **Disk full** - Verify server has free disk space
4. **Config being overwritten** - Check if another plugin/tool is modifying it

### Reload command doesn't work

`/vanilla reload` should work instantly. If it doesn't:

1. Check console for errors
2. Verify you have `vanillacore.reload` permission or OP
3. Try a full server restart instead
4. Check if config has syntax errors (invalid YAML)

### Items still get restricted even though feature is disabled

1. Check config: `features.[feature-name].enabled` must be `false`
2. Run `/vanilla reload` after changing config
3. Check for another plugin doing the same thing
4. Verify you're not testing with a bypassed account

---

## Advanced Topics

### Can I run Vanilla Core on a Bungeecord/Velocity network?

Yes, install it on each backend server where you want features enabled.

Note: Features work per-server. Item limits are per-server, not network-wide.

### Does it work with Geyser (Bedrock players)?

Yes! Bedrock players connecting through Geyser will work normally.

Some features might behave slightly differently due to Bedrock client limitations, but all core functionality works.

### Can I use this with custom items from other plugins?

Yes! Item Limiter and other features work with custom items. Refer to items by their Material name or custom identifier if the plugin exposes one.

### How do I use WorldGuard regions with Mob Manager?

1. Install WorldGuard and WorldEdit
2. Create regions as normal
3. Enable Mob Manager in Vanilla Core
4. Mobs will respect region flags automatically

See [Mob Manager](./features/mob-manager) for details.

### Can I make per-world configs?

Not currently supported. All features apply globally or based on specific conditions (like WorldGuard regions).

Per-world config support may be added in the future if there's demand.

---

## Development & Contributing

### Is Vanilla Core open source?

Yes! Released under MIT License. View source on [GitHub](https://github.com/TejasLamba2006/vanilla-core).

You can:
- Fork it
- Modify it
- Learn from it
- Contribute to it

### Can I contribute code?

Yes! We welcome contributions:

1. Fork the repository
2. Create a feature branch
3. Make your changes (follow code style, no comments in code)
4. Test thoroughly
5. Submit a pull request

See [CONTRIBUTING.md](https://github.com/TejasLamba2006/vanilla-core/blob/main/CONTRIBUTING.md) for guidelines.

### How do I report bugs?

[Open an issue on GitHub](https://github.com/TejasLamba2006/vanilla-core/issues) with:

- Vanilla Core version
- Server software and version (Paper 1.21.1, etc.)
- Java version
- Full error message from console (if any)
- Steps to reproduce the bug
- Expected behavior vs actual behavior

The more details, the faster we can fix it.

### How can I support development?

- ⭐ Star the [GitHub repository](https://github.com/TejasLamba2006/vanilla-core)
- 💬 Join the [Discord](https://discord.gg/7fQPG4Grwt) and help others
- 🐛 Report bugs and test new features
- 💡 Suggest features and improvements
- 💻 Contribute code via pull requests
- 💰 [Donate via PayPal](https://paypal.me/tejaslamba) (donors get priority support)

---

## Still need help?

- 📚 Check the [Documentation](/docs)
- 💬 Ask on [Discord](https://discord.gg/7fQPG4Grwt) for quick help
- 🐛 [Report bugs on GitHub](https://github.com/TejasLamba2006/vanilla-core/issues)
- 💰 [Donate for priority support](https://paypal.me/tejaslamba)
