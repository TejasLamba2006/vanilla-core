---
sidebar_position: 7
---

# One Player Sleep

Allow a single player to skip the night for the entire server.

## Overview

In vanilla Minecraft, all players must sleep simultaneously to skip the night. This becomes problematic on multiplayer servers where players are in different timezones, AFK, or simply busy building. One Player Sleep removes this requirement, allowing any single player to skip the night for everyone.

:::tip Beginner-Friendly Feature
This is one of the easiest features to set up! Just toggle it on in the GUI and you're done. Perfect for new server owners.
:::

## Why Use One Player Sleep

Common problems solved by this feature:

- **Timezone differences**: Players from different regions are online at different times
- **AFK players**: One AFK player blocks everyone from sleeping
- **Base builders**: Players busy building underground or in the nether
- **Phantom prevention**: Makes it easier to reset phantom timers
- **Server flow**: No more waiting and asking "can everyone sleep?"

## GUI Configuration Walkthrough

:::info Recommended Method
Using the GUI is the easiest way to configure this feature. No need to edit YAML files!
:::

### Step-by-Step Setup

1. **Open the Main Menu**
   - Run `/vanilla` command in-game
   - You must have the `vanillacore.admin` permission

2. **Find One Player Sleep**
   - Look for the red bed icon (🛏️)
   - It's usually near the middle of the menu

3. **Quick Toggle (Left-Click)**
   - Left-click the red bed to instantly toggle the feature on/off
   - You'll see a message confirming the change

4. **Open Configuration (Right-Click)**
   - Right-click the red bed to open the configuration menu
   - This opens the full settings interface

### Configuration Menu Layout

```
┌─────────────────────────────────────────┐
│    One Player Sleep Configuration      │
├─────────────────────────────────────────┤
│           [🛏️] Feature Toggle           │
│         Current: Enabled/Disabled       │
│                                         │
│        [📝] Sleep Message               │
│    "{player} is sleeping..."           │
│         Click to edit                   │
│                                         │
│        [📝] Skip Message                │
│         "☀ Good morning!"              │
│         Click to edit                   │
│                                         │
│  [📖] How It Works   (Info display)    │
│                                         │
│ [⬅️ Back]        [💾 Save]    [❓ Help] │
└─────────────────────────────────────────┘
```

### Editing Messages

1. Click on "Sleep Message" or "Skip Message"
2. You'll be prompted to type in chat
3. Enter your custom message
4. Type `empty` to disable that message
5. The menu will refresh with your changes

:::tip Pro Tip
Use color codes to make your messages stand out! 
- `§a` = Green
- `§e` = Yellow  
- `§6` = Gold
- `§c` = Red

Example: `§e{player} §7is sleeping...` (yellow name, gray text)
:::

6. **Save Your Changes**
   - Click the green "Save Changes" button
   - Your config.yml will be automatically updated
   - The feature will reload with new settings

:::warning Don't Forget to Save!
Changes won't take effect until you click "Save Changes". If you close the menu without saving, your changes will be lost.
:::

## YAML Configuration (Advanced)

:::caution For Advanced Users
Only edit the config.yml file directly if you're comfortable with YAML syntax. GUI configuration is recommended for beginners.
:::

```yaml
features:
  one-player-sleep:
    enabled: false
    sleep-message: "§e{player} §7is sleeping..."
    skip-message: "§a☀ Good morning!"
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |
| `sleep-message` | string | "§e{player} §7is sleeping..." | Message broadcast when a player starts sleeping |
| `skip-message` | string | "§a☀ Good morning!" | Message broadcast when night is skipped |

### Message Placeholders

The `sleep-message` supports these placeholders:

| Placeholder | Replaced With |
|-------------|---------------|
| `{player}` | Player's name |
| `{displayname}` | Player's display name (with nickname if set) |

Set either message to empty (`""`) to disable that message.

## How It Works

When enabled, Vanilla Core modifies Minecraft's `playersSleepingPercentage` gamerule:

- **Enabled**: Sets `playersSleepingPercentage` to `0` (only 1 player needed)
- **Disabled**: Resets to `100` (all players must sleep)

This uses the vanilla game mechanic introduced in Minecraft 1.17, ensuring full compatibility with all Minecraft features.

:::info Technical Note
This feature uses Minecraft's built-in gamerule system, so it has zero performance impact and works perfectly with vanilla mechanics.
:::

## Configuration Examples

### Small Private SMP (2-5 players)

Best for friend groups who want friendly sleep notifications:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: "§e{player} §7is sleeping..."
  skip-message: "§a☀ Good morning!"
```

### Large Public Server (50+ players)

Reduce chat spam by disabling the sleep message:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: ""
  skip-message: "§a☀ Morning!"
```

### Silent Mode

If you don't want any sleep announcements:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: ""
  skip-message: ""
```

## Common Mistakes

### ❌ Editing config.yml while server is running
- Changes made directly to the file won't take effect
- Always use `/vanilla reload` after editing, or use the GUI

### ❌ Forgetting to enable the feature
- The feature must be toggled to `enabled: true`
- Check the status in the GUI (green = enabled, red = disabled)

### ❌ YAML syntax errors
- Missing quotes around messages with special characters
- Incorrect indentation (must be exactly 2 spaces per level)
- **Solution**: Use the GUI instead! It prevents all syntax errors.

### ❌ Using curly braces wrong
- Correct: `{player}` 
- Wrong: `{{player}}` or `<player>` or `%player%`

## Troubleshooting

### Night doesn't skip when I sleep

**Check these first:**

1. **Is the feature enabled?**
   - Run `/vanilla` and check if the bed icon is green
   - Or check `config.yml`: `enabled: true`

2. **Are you in the Overworld?**
   - This only works in normal world dimension
   - Nether and End beds still explode!

3. **Is it actually nighttime?**
   - Must be between 12542 and 23459 ticks
   - Check with `/time query daytime`

4. **Are monsters nearby?**
   - Vanilla Minecraft prevents sleep if monsters are too close
   - This is normal game behavior, not a plugin issue

5. **Is another plugin interfering?**
   - Some sleep plugins may conflict
   - Try `/vanilla` to see if other features are remotely disabled

### Messages aren't showing

1. **Check if messages are set**
   - Empty string (`""`) disables messages
   - Use the GUI to set a message

2. **Check Minecraft chat settings**
   - Make sure chat isn't hidden (`T` key)
   - Check client-side chat settings

### Gamerule gets reset

If the `playersSleepingPercentage` gamerule keeps resetting:

1. **Another plugin might be managing gamerules**
   - Check for conflicts with other plugins
   - Vanilla Core sets it when feature is enabled

2. **Feature is being toggled**
   - When disabled, gamerule resets to 100
   - Keep feature enabled for persistent effect

## Performance Considerations

:::tip Lightweight Feature
This feature has **zero performance impact**! It only modifies a gamerule once when enabled/disabled. No event listeners running constantly.
:::

- **CPU Usage**: None (gamerule-based)
- **Memory Usage**: Negligible (stores 2 string values)
- **Network Traffic**: Minimal (2 chat messages per sleep cycle)
- **Disk I/O**: Only on config save

**Recommended for servers of any size**: 1 player to 1000+ players ✅

## Compatibility Notes

### ✅ Compatible With:
- All vanilla sleep mechanics
- Custom beds from datapacks
- Sleep speed modifiers
- Phantom spawning mechanics
- Player sleep statistics

### ⚠️ May Conflict With:
- Other sleep percentage plugins
- Plugins that manage gamerules globally
- Sleep voting systems (they become redundant)

:::warning Plugin Conflicts
If you have another plugin that controls sleep requirements (like a sleep voting plugin), you should disable one or the other to avoid conflicts.
:::

### 🔌 Works Great With:
- Essentials (doesn't interfere)
- LuckPerms (permission support)
- CoreProtect (logging compatible)

## Frequently Asked Questions

**Q: Can I set it so 50% of players need to sleep instead of just 1?**
A: Not through this feature. If you need percentage-based sleep, use the vanilla `/gamerule playersSleepingPercentage <value>` command directly.

**Q: Does this work in modded dimensions?**
A: Yes, as long as the dimension is marked as an Overworld environment type.

**Q: Will this affect my existing gamerule settings?**
A: Only the `playersSleepingPercentage` gamerule is modified. All other gamerules remain untouched.

**Q: Can regular players use this feature?**
A: Regular players can sleep to skip the night. Only admins with `vanillacore.admin` permission can configure it.

## Support

Need help? Check these resources:

- 📖 [Main Documentation](https://vanillacore.tejaslamba.com)
- 💬 [Discord Support](https://discord.gg/7fQPG4Grwt)
- 🐛 [Report Issues](https://github.com/TejasLamba2006/vanilla-core/issues)
- 📝 [Configuration Builder](https://vanillacore.tejaslamba.com/config-builder)

---

**Last Updated**: February 2026  
**Minecraft Version**: 1.21+  
**Plugin Version**: 3.0.0+
