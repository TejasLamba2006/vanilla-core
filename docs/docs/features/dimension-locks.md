---
sidebar_position: 5
---

# Dimension Locks

Control access to the Nether and End dimensions on your server.

## Overview

This feature locks the Nether and End dimensions, preventing players from entering.

## Why Lock Dimensions?

On a new SMP server, experienced players often rush to the Nether and End within the first few days:

- Players obtain powerful gear (netherite, elytra) before others have iron
- The Ender Dragon is killed before most players can participate
- The shared exploration experience is lost
- Server economy becomes unbalanced quickly

Locking dimensions initially keeps all players at the same progression level.

## How It Works

Dimension locks prevent players from entering the Nether or End through portals:

- **Nether Portals** - Blocked, player sees a customizable message
- **End Portals** - Blocked, player sees a customizable message

When a player attempts to enter a locked dimension, they receive a configurable message explaining that the dimension is not yet available.

## Configuration

### Nether Lock

```yaml
features:
  dimension-lock-nether:
    enabled: false
    locked: false
    locked-message: "§cThe Nether is currently locked!"
```

### End Lock

```yaml
features:
  dimension-lock-end:
    enabled: false
    locked: false
    locked-message: "§cThe End is currently locked!"
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Whether the lock feature is active |
| `locked` | boolean | false | Whether the dimension is currently locked |
| `locked-message` | string | (see above) | Message shown when access is blocked |

### Message Formatting

The message supports Minecraft color codes:

```yaml
locked-message: "§c§lACCESS DENIED! §7The Nether opens on §eJanuary 15th§7."
```

| Code | Color |
|------|-------|
| `§c` | Red |
| `§7` | Gray |
| `§e` | Yellow |
| `§a` | Green |
| `§l` | Bold |

## Using the GUI

### Opening Dimension Lock Settings

1. Run `/vanilla` to open the main menu
2. Click on "Nether Lock" or "End Lock"

### Toggling the Lock

In the dimension lock menu:

1. The current status is displayed prominently
2. Left-click to toggle the feature on/off
3. Right-click to toggle the lock state (locked/unlocked)

## Commands

### Nether Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/nether` | Display current Nether lock status | `vanillacore.nether` |
| `/nether open` | Unlock the Nether | `vanillacore.nether.open` |
| `/nether close` | Lock the Nether | `vanillacore.nether.close` |

### End Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/end` | Display current End lock status | `vanillacore.end` |
| `/end open` | Unlock the End | `vanillacore.end.open` |
| `/end close` | Lock the End | `vanillacore.end.close` |

## Bypass Permissions

Staff members may need to enter locked dimensions for testing or administration:

| Permission | Description |
|------------|-------------|
| `vanillacore.bypass.nether` | Enter the Nether even when locked |
| `vanillacore.bypass.end` | Enter the End even when locked |

## Progression Strategies

### Weekly Unlock Schedule

A common approach for new SMPs:

| Week | Status | Milestone |
|------|--------|-----------|
| Week 1 | Both Locked | Players establish bases, gather overworld resources |
| Week 2 | Nether Opens | Players can access nether materials, brewing |
| Week 3 | Both Open | Players can fight the dragon, access End cities |

### Event-Based Unlocks

Unlock dimensions as community milestones or events:

**Dragon Fight Event:**

1. Keep the End locked until you're ready
2. Announce the event date in advance
3. Have all players gather at spawn at the scheduled time
4. Run `/end open` to unlock
5. Players race to the stronghold and fight together

**Nether Rush Event:**

1. Announce a specific time when the Nether will unlock
2. Players prepare by gathering obsidian and building portals
3. At the designated time, run `/nether open`
4. The race for nether resources begins

## Integration with Other Features

- **Netherite Disabler**: Combine with Nether Lock for complete netherite control
- **Mob Manager**: Control mob spawning in dimensions when unlocked
