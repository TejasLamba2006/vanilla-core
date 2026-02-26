---
sidebar_position: 11
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['new']} />

# Minimap Control

Control minimap mod features using Xaero's minimap protocol.

## Overview

Minimap Control restricts minimap mod features server-wide. It communicates with compatible mods (like Xaero's Minimap) to set rules: disable the minimap entirely, allow navigation only, or allow everything including radar.

## Configuration

```yaml
features:
  minimap-control:
    enabled: false
    global-mode: FAIR
    nether-fair-mode: true
    send-on-join: true
    send-on-world-change: true
    worlds: {}
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for minimap control |
| `global-mode` | string | FAIR | Default mode for all worlds |
| `nether-fair-mode` | boolean | true | Force FAIR mode in nether regardless of other settings |
| `send-on-join` | boolean | true | Send minimap rules when player joins |
| `send-on-world-change` | boolean | true | Send minimap rules when player changes world |
| `worlds` | map | empty | Per-world minimap mode overrides |

### Minimap Modes

| Mode | Description |
|------|-------------|
| `DISABLED` | Minimap completely disabled - no map display |
| `FAIR` | Minimap allowed but radar features blocked (no entity/player radar) |
| `FULL` | All minimap features allowed including radar |

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on Minimap Control
3. **Left Click**: Toggle the feature on/off
4. **Right Click**: Open settings menu

## Settings

### Fair PvP Server

Allow basic minimap but block radar features:

```yaml
minimap-control:
  enabled: true
  global-mode: FAIR
  nether-fair-mode: true
  send-on-join: true
  send-on-world-change: true
```

This setup:

- Players can use minimap for navigation
- Radar features (player/entity tracking) are blocked
- Nether always uses FAIR mode (no finding players through walls)

### No Minimaps Server

Completely disable minimaps:

```yaml
minimap-control:
  enabled: true
  global-mode: DISABLED
  nether-fair-mode: true
  send-on-join: true
  send-on-world-change: true
```

### Per-World Configuration

Different rules for different worlds:

```yaml
minimap-control:
  enabled: true
  global-mode: FAIR
  nether-fair-mode: true
  send-on-join: true
  send-on-world-change: true
  worlds:
    creative_world: FULL
    pvp_world: DISABLED
    survival_world: FAIR
```

## Why Use FAIR Mode?

FAIR mode blocks radar features which provide unfair advantages:

| Blocked Feature | Why It's Unfair |
|-----------------|-----------------|
| Player radar | Reveals player positions through walls, negates invisibility |
| Entity radar | Trivializes finding spawners and mob farms |

FAIR mode still allows:

- Basic map display
- Waypoints
- Map exploration

FAIR mode is ideal for most SMP servers.

## Nether Fair Mode

When `nether-fair-mode: true`:

- Nether always uses FAIR mode regardless of `global-mode` or `worlds` settings
- Prevents radar abuse in the nether where players often travel
- Useful even on servers that allow full minimap features elsewhere

## Compatible Mods

Minimap Control uses the Xaero's minimap protocol. Compatible mods include:

| Mod | Compatibility |
|-----|---------------|
| Xaero's Minimap | Full support |
| Xaero's World Map | Full support |

Other minimap mods may or may not respond to these server-sent restrictions. Players using incompatible mods or ignoring server restrictions won't be kicked automatically.

## How It Works

1. When enabled, the server sends minimap restriction packets
2. Compatible mods receive these packets and adjust their features
3. Packets are sent on join and/or world change (configurable)
4. Per-world settings allow different rules in different dimensions

## Troubleshooting

### Players report minimap still works fully

1. Check if the player is using a compatible mod (Xaero's)
2. Verify `enabled: true` in config
3. Ensure `send-on-join: true`
4. Player may need to reconnect after config changes

### Settings not applying on world change

Ensure `send-on-world-change: true` is set.

### Want to allow full features in creative world

Add the world to the `worlds` map with `FULL` mode:

```yaml
worlds:
  creative_world: FULL
```
