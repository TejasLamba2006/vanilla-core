---
sidebar_position: 12
---

# FAQ

Frequently asked questions about Vanilla Core.

## General

### What versions does Vanilla Core support?

Vanilla Core supports **Paper/Spigot 1.21.1+** with **Java 21+**.

### Does it work with Spigot?

Yes, but **Paper is recommended** for better performance and more features.

### Is Vanilla Core free?

Yes! Vanilla Core is completely free and open source.

### Where can I download it?

- [Modrinth](https://modrinth.com/plugin/vanillacorewastaken) (Recommended)

---

## Installation

### The plugin doesn't load

Check the following:

1. **Java version**: Must be 21+ (`java -version`)
2. **Server version**: Must be 1.21.1+
3. **Console errors**: Look for red error messages
4. **File name**: Ensure it's `.jar` not `.jar.zip`

### Config file is missing

The config is generated on first run. Ensure:

1. The plugin loaded successfully
2. Check `plugins/Vanilla Core/` folder
3. Look for console errors during startup

### How do I update?

1. Stop your server
2. Replace the old JAR with the new one
3. Start your server
4. Config files are preserved automatically

---

## Features

### Why isn't [feature] working?

1. Check if the feature is **enabled** in config/GUI
2. Ensure you don't have a **bypass permission**
3. Check for **conflicting plugins**
4. Review the **console for errors**

### Can I use only some features?

Yes! Every feature can be individually enabled/disabled.

### Does it conflict with other plugins?

Vanilla Core is designed to be compatible with most plugins. Known compatible plugins:

- EssentialsX
- LuckPerms
- WorldGuard
- Vault
- CMI

If you find conflicts, please report them on GitHub.

### Can I suggest new features?

Absolutely! Open an issue on [GitHub](https://github.com/TejasLamba2006/smp-core/issues) with your suggestion.

---

## Performance

### Will this lag my server?

No! Vanilla Core is optimized for performance:

- Event listeners use early returns
- Cooldowns prevent spam checks
- No unnecessary database calls
- Efficient data structures

### How much RAM does it use?

Vanilla Core has a minimal memory footprint, typically under 10MB.

---

## Permissions

### Players can bypass restrictions

Check for these common issues:

1. Player has OP (OPs bypass by default)
2. Player has `vanillacore.bypass.*` permission
3. Player has specific bypass permission
4. Permission plugin misconfiguration

### How do I give staff bypass permissions?

Using LuckPerms:

```
/lp group staff permission set vanillacore.bypass.* true
```

---

## Troubleshooting

### Commands say "Unknown command"

1. Check plugin is loaded: `/plugins`
2. Ensure correct command: `/vanilla` not `/vanillacore`
3. Check permissions

### GUI is empty or broken

1. Update to latest version
2. Check console for errors
3. Try `/vanilla reload`
4. Delete config and regenerate

### Features reset after restart

1. Check if config is saving properly
2. Look for permission errors in console
3. Ensure proper server shutdown (not kill)

---

## Development

### Is Vanilla Core open source?

Yes! View the source on [GitHub](https://github.com/TejasLamba2006/smp-core).

### Can I contribute?

Yes! We welcome contributions:

1. Fork the repository
2. Make your changes
3. Submit a pull request

### How do I report bugs?

Open an issue on [Discord server](https://discord.gg/7fQPG4Grwts) with:

- Server version
- Plugin version
- Error messages
- Steps to reproduce

---

## Still need help?

- Check the [Documentation](/docs)
- [Report an Issue on GitHub](https://github.com/TejasLamba2006/smp-core/issues)
- [Support Development via PayPal](https://paypal.me/tejaslamba) - Donors get priority support
