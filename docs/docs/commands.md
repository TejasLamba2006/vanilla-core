---
sidebar_position: 3
---

# Commands

This page documents all commands available in Vanilla Core.

## Main Commands

The `/vanilla` command is the primary entry point for plugin management.

| Command | Description | Permission |
|---------|-------------|------------|
| `/vanilla` | Opens the main configuration GUI | `vanillacore.admin` |
| `/vanilla reload` | Reloads all reloadable modules (`config`, `messages`, `menus`, `features`) | `vanillacore.reload` |
| `/vanilla reload <module>` | Reloads one module only: `all`, `config`, `messages`, `menus`, or `features` | `vanillacore.reload` |
| `/vanilla version` | Displays the currently running plugin version | `vanillacore.version` |

## Ritual Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ritual` or `/ritual start` | Starts a ritual using your held item with the default time and color from config | `vanillacore.ritual` |
| `/ritual start <time>` | Starts a ritual with custom duration (`30s`, `1m`, `1h`, `1d`, `1h30m`) | `vanillacore.ritual` |
| `/ritual start <time> <color>` | Starts a ritual with custom duration and particle color | `vanillacore.ritual` |
| `/ritual status` | Shows currently active ritual status | `vanillacore.ritual` |
| `/ritual cancel` | Cancels the active ritual | `vanillacore.ritual.cancel` |

Ritual color values support tab completion.

### Reload Behavior

When you run `/vanilla reload`:

1. `config` is reloaded and verbose logging is refreshed
2. `messages.yml` is reloaded and message cache is rebuilt
3. Menu config and menu instances are rebuilt
4. Every loaded feature receives `reload()`
5. You get a summary with module success/failure counts and elapsed time

When you run `/vanilla reload <module>`:

1. Only that module is reloaded
2. You get per-module timing feedback
3. If a module fails, the failure is reported without masking other module states

Note: If you changed values in both the GUI and the config file, reload uses the file on disk.

## Dimension Lock Commands

### Nether Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/nether` | Displays current Nether lock status | `vanillacore.nether` |
| `/nether open` | Unlocks the Nether for all players | `vanillacore.nether.open` |
| `/nether close` | Locks the Nether, preventing entry | `vanillacore.nether.close` |

### End Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/end` | Displays current End lock status | `vanillacore.end` |
| `/end open` | Unlocks the End for all players | `vanillacore.end.open` |
| `/end close` | Locks the End, preventing entry | `vanillacore.end.close` |

## Item Limiter Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/itemlimit` | Opens the item limiter configuration GUI | `vanillacore.itemlimit` |
| `/itemlimit set <item> <amount>` | Creates or updates a limit for the specified item | `vanillacore.itemlimit.set` |
| `/itemlimit remove <item>` | Removes the limit for the specified item | `vanillacore.itemlimit.remove` |
| `/itemlimit list` | Displays all currently configured item limits | `vanillacore.itemlimit.list` |

### Item Name Format

Use Minecraft's material names for the `<item>` argument:

- `totem_of_undying` (not "totem" or "undying")
- `enchanted_golden_apple` (not "god apple" or "gap")
- `ender_pearl` (not "pearl")

Names are case-insensitive.

## Enchantment Limiter Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/enchantlimit` | Opens the enchantment limiter configuration GUI | `vanillacore.enchantlimit` |
| `/enchantlimit set <enchant> <level>` | Sets the maximum level for an enchantment | `vanillacore.enchantlimit.set` |
| `/enchantlimit remove <enchant>` | Removes the cap from an enchantment | `vanillacore.enchantlimit.remove` |
| `/enchantlimit list` | Displays all currently configured enchantment caps | `vanillacore.enchantlimit.list` |

### Enchantment Name Format

Use Bukkit enchantment names:

- `sharpness` (not "sharp" or "damage")
- `protection` (not "prot")
- `power` (not "arrow damage")

## Mace Limiter Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/mace` | Displays current mace limiter status and settings | `vanillacore.mace` |
| `/mace toggle` | Enables or disables the mace limiter feature | `vanillacore.mace.toggle` |

## Netherite Disabler Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/netherite` | Displays current netherite disabler status | `vanillacore.netherite` |
| `/netherite toggle` | Enables or disables netherite restrictions | `vanillacore.netherite.toggle` |

## Command Examples

### Server Setup Workflow

When setting up a new SMP, you might run these commands:

```
# Lock dimensions for staged progression
/nether close
/end close

# Set up item limits for PvP balance
/itemlimit set totem_of_undying 2
/itemlimit set enchanted_golden_apple 3
/itemlimit set end_crystal 4

# Cap enchantments
/enchantlimit set sharpness 3
/enchantlimit set protection 3
/enchantlimit set power 3
```

### Opening Dimensions for an Event

```
# Check current status
/nether
/end

# Open the Nether (week 2)
/nether open

# Later, open the End for dragon fight event
/end open
```

### Quick Config Reload After Manual Edits

```
/vanilla reload
```

## Tab Completion

All commands support tab completion for arguments:

- Item names auto-complete when typing
- Enchantment names auto-complete when typing
- Subcommands auto-complete based on permissions

---

Next: Learn about [Permissions](./permissions).
