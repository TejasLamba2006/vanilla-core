---
sidebar_position: 3
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['popular', 'recommended']} />

# Mace Limiter

Control how many maces can be crafted on your server.

## Overview

The Mace is a powerful weapon introduced in Minecraft 1.21 with a unique smash attack mechanic. The Mace Limiter allows you to limit the **total number of maces** that can be crafted server-wide. Once the limit is reached, the mace crafting recipe is removed entirely.

## Why Limit Maces?

The mace creates balance concerns on SMP servers:

- **Smash Attack Damage**: Players can deal massive damage by falling from heights
- **Breach Enchantment**: Bypasses armor, making protection enchantments less effective
- **Density Enchantment**: Multiplies the already powerful smash attack damage
- **Resource Scarcity**: Making maces rare creates a valuable commodity

By limiting the total number of maces, you create a server-wide economy around this powerful weapon. Players must decide who gets a mace and whether to risk losing it in PvP.

## Configuration

```yaml
features:
  mace-limiter:
    enabled: false
    max-maces: 1
    maces-crafted: 0
    title:
      enabled: true
      title: "&6&lMace Crafted!"
      subtitle: "&7Someone has crafted a mace..."
      fade-in: 10
      stay: 70
      fade-out: 20
    chat:
      enabled: true
      message: "&6[SMP] &e{player} &7has crafted a mace! &8({current}/{max})"
    sound:
      enabled: true
      sound: "ENTITY_ENDER_DRAGON_GROWL"
      volume: 1.0
      pitch: 1.0
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Whether the mace limiter is active |
| `max-maces` | integer | 1 | Maximum maces that can be crafted server-wide |
| `maces-crafted` | integer | 0 | Current count of crafted maces (auto-tracked) |

### Title Settings

When a mace is crafted, all players see a title announcement:

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `title.enabled` | boolean | true | Show title to all players |
| `title.title` | string | "&6&lMace Crafted!" | Main title text |
| `title.subtitle` | string | (see above) | Subtitle text |
| `title.fade-in` | integer | 10 | Fade in time (ticks) |
| `title.stay` | integer | 70 | Display time (ticks) |
| `title.fade-out` | integer | 20 | Fade out time (ticks) |

### Chat Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `chat.enabled` | boolean | true | Announce in chat |
| `chat.message` | string | (see above) | Chat message with placeholders |

**Placeholders:**

- `{player}` - Name of the player who crafted the mace
- `{current}` - Current mace count
- `{max}` - Maximum allowed maces

### Sound Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `sound.enabled` | boolean | true | Play sound to all players |
| `sound.sound` | string | ENTITY_ENDER_DRAGON_GROWL | Sound effect name |
| `sound.volume` | decimal | 1.0 | Volume (0.0 - 1.0) |
| `sound.pitch` | decimal | 1.0 | Pitch (0.5 - 2.0) |

## How It Works

1. **Tracking**: The plugin tracks how many maces have been crafted server-wide
2. **Announcement**: When someone crafts a mace, everyone is notified via title, chat, and sound
3. **Recipe Removal**: Once `max-maces` is reached, the mace crafting recipe is disabled
4. **Persistence**: The count is saved to config and persists through restarts

### What Happens When Limit is Reached

- The mace crafting recipe is removed from the game
- Players can no longer craft new maces at crafting tables
- Existing maces remain functional
- The limit can be increased by changing `max-maces` in config and reloading

## Using the GUI

1. Run `/smp` to open the main menu
2. Click on the Mace icon to access Mace Limiter settings
3. Left-click to toggle the feature on/off
4. Right-click to view current status and adjust settings

The GUI shows:

- Current mace count vs maximum
- Toggle for enabling/disabling the limiter
- Quick access to change the maximum limit

## Command Reference

| Command | Permission | Description |
|---------|------------|-------------|
| `/mace` | `vanillacore.command.mace` | View current mace limit status |

## Recommended Settings

### Rare Maces (Recommended for SMPs)

Only 1-3 maces on the entire server:

```yaml
mace-limiter:
  enabled: true
  max-maces: 3
```

This creates a valuable server commodity where mace ownership matters.

### Moderate Availability

Allow more maces but still keep them special:

```yaml
mace-limiter:
  enabled: true
  max-maces: 10
```

### No Limits

Disable the feature entirely to allow unlimited mace crafting:

```yaml
mace-limiter:
  enabled: false
```

## Resetting the Count

To reset the mace count (for a new season or event):

1. Open `config.yml`
2. Set `maces-crafted: 0`
3. Run `/smp reload`

The crafting recipe will be re-enabled and players can craft maces again up to the limit.

## Integration with Enchantment Limiter

For complete mace balance, combine this feature with the Enchantment Limiter to cap mace-specific enchantments:

```yaml
features:
  enchantment-limiter:
    enabled: true
    limits:
      density: 3
      breach: 2
      wind_burst: 1
  mace-limiter:
    enabled: true
    max-maces: 3
```

This ensures the few maces that exist are still balanced in combat.
