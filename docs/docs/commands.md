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
| `/vanilla reload` | Reloads all configuration files from disk | `vanillacore.reload` |
| `/vanilla help` | Displays help information and available commands | `vanillacore.admin` |

### Reload Behavior

When you run `/vanilla reload`:

1. All configuration files are re-read from disk
2. Feature settings are updated immediately
3. Cached data is refreshed
4. Any pending changes are applied

Note: If you have both made changes in the GUI and edited the config file manually, the reload will use the file on disk.

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
