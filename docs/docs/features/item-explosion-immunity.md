---
sidebar_position: 10
---

# Item Explosion Immunity

Protect dropped items from being destroyed by explosions.

## Overview

When explosions occur in Minecraft (TNT, creepers, ghasts, etc.), nearby dropped items can be destroyed. Item Explosion Immunity protects all dropped items from explosion damage.

## Why use item explosion immunity?

The worst PvP moments often aren't dying — they're losing your gear because a creeper snuck up right after. This protects dropped items from all explosion types: TNT traps, creepers, ghast fireballs, crystals during the dragon fight. Your drops stay on the ground until picked up or naturally despawned.

## Configuration

```yaml
features:
  item-explosion-immunity:
    enabled: false
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |

When enabled, ALL dropped item entities are protected from explosion damage. There is no item-by-item selection - it's an all-or-nothing protection.

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on Item Explosion Immunity (TNT icon)
3. **Left Click**: Toggle the feature on/off
4. **Right Click**: View information about the feature

## How It Works

When an explosion occurs:

1. Plugin intercepts the explosion event
2. All dropped item entities within the explosion radius are protected
3. The explosion completes normally for other entities
4. Protected items survive intact

### What Gets Protected

Protected items survive:

- Direct explosion damage
- All explosion types (TNT, creepers, ghasts, crystals, etc.)

Protected items do NOT survive:

- Fire damage after the explosion (fire resistance is separate)
- Being blown into lava or void
- Natural despawn timer (5 minutes)
- Other damage sources

## Explosion Types Covered

This feature protects against all explosion sources:

| Explosion Source | Protected |
|------------------|-----------|
| TNT | Yes |
| TNT Minecart | Yes |
| Creeper | Yes |
| Charged Creeper | Yes |
| Ghast Fireball | Yes |
| Wither | Yes |
| Wither Skull | Yes |
| End Crystal | Yes |
| Respawn Anchor (wrong dimension) | Yes |
| Bed (wrong dimension) | Yes |
| Wind Charge | Yes |

## Use Cases

### Anti-Grief Servers

```yaml
item-explosion-immunity:
  enabled: true
```

### Death Drop Protection

```yaml
item-explosion-immunity:
  enabled: true
```

## Integration with Other Features

### With Keep Inventory

If keep inventory is enabled:

- Players do not drop items on death
- This feature only matters for manually dropped items or items from containers

### With TNT Mining

If players use TNT for mining:

- Protected items will not be destroyed by TNT blasts
- Consider whether this affects your server's mining meta

## Troubleshooting

### Items still being destroyed

1. Verify `enabled: true` in config
2. Run `/vanilla reload` after config changes
3. Check for other plugins that may override entity damage

### Items being destroyed by fire after explosion

This feature only protects from explosion damage itself. Fire damage is separate:

- Items can still burn from fire started by explosions
- Consider fire spread settings if this is an issue
