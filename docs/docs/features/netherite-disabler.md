---
sidebar_position: 4
---

# Netherite Disabler

Disable specific netherite items on your server.

## Overview

Netherite represents the ultimate tier of equipment in Minecraft. The Netherite Disabler allows you to selectively disable specific netherite items, preventing players from using them. This is useful for creating more balanced PvP or maintaining a diamond-tier meta.

## Why Control Netherite Access?

Reasons to control netherite access:

- **PvP Balance**: Diamond vs diamond fights are more skill-based
- **Armor Balance**: Netherite armor has knockback resistance which affects combat
- **Selective Control**: Disable just netherite armor but allow tools, or vice versa

## Configuration

```yaml
features:
  netherite-disabler:
    enabled: false
    disabled-items:
      sword: true
      axe: true
      pickaxe: true
      shovel: true
      hoe: true
      helmet: true
      chestplate: true
      leggings: true
      boots: true
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the netherite disabler |
| `disabled-items` | map | all true | Map of item types to enable/disable |

### Disabled Items

Each item type can be individually toggled:

| Item | Description |
|------|-------------|
| `sword` | Netherite Sword |
| `axe` | Netherite Axe |
| `pickaxe` | Netherite Pickaxe |
| `shovel` | Netherite Shovel |
| `hoe` | Netherite Hoe |
| `helmet` | Netherite Helmet |
| `chestplate` | Netherite Chestplate |
| `leggings` | Netherite Leggings |
| `boots` | Netherite Boots |

Set to `true` to disable the item, `false` to allow it.

## How It Works

When an item is disabled:

1. **Crafting/Upgrading Prevention**: Players cannot upgrade diamond items to netherite at the smithing table
2. **Usage Prevention**: Disabled netherite items cannot be used effectively
3. **Per-Item Control**: Only the specific items you disable are affected

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on the Netherite Disabler option
3. Toggle individual items on/off
4. Changes take effect immediately

The GUI displays each netherite item type with its current status:

- Red = Disabled
- Green = Allowed

## Command Reference

| Command | Permission | Description |
|---------|------------|-------------|
| `/netherite` | `vanillacore.command.netherite` | View netherite disabler status |

## Presets

### No Netherite Armor (PvP Balance)

Allow netherite tools but disable armor for balanced combat:

```yaml
netherite-disabler:
  enabled: true
  disabled-items:
    sword: false
    axe: false
    pickaxe: false
    shovel: false
    hoe: false
    helmet: true
    chestplate: true
    leggings: true
    boots: true
```

### Tools Only

Completely disable all netherite weapons and armor:

```yaml
netherite-disabler:
  enabled: true
  disabled-items:
    sword: true
    axe: true
    pickaxe: false
    shovel: false
    hoe: false
    helmet: true
    chestplate: true
    leggings: true
    boots: true
```

### Full Diamond Meta

Disable everything netherite:

```yaml
netherite-disabler:
  enabled: true
  disabled-items:
    sword: true
    axe: true
    pickaxe: true
    shovel: true
    hoe: true
    helmet: true
    chestplate: true
    leggings: true
    boots: true
```

## Integration with Dimension Locks

For complete netherite control, combine with Nether Lock to prevent ancient debris mining:

1. Lock the Nether to prevent debris acquisition
2. Enable Netherite Disabler to prevent any existing netherite usage
3. Unlock gradually as the server progresses
