---
sidebar_position: 5
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['pvp', 'recommended']} />

# Shield Mechanics

Customize how shields behave when a player is hit while blocking — specifically the cooldown (stun duration) applied after a mace or axe hit.

## Overview

In vanilla Minecraft, an axe hitting a blocking player always disables their shield for exactly 5 seconds (100 ticks). There is no built-in stun for maces. Shield Mechanics lets you configure a mace stun (not in vanilla), override the axe stun duration (vanilla forces 100 ticks), and toggle each weapon type separately.

All settings are configurable via the in-game GUI or `config.yml`.

## Configuration

```yaml
features:
  shield-mechanics:
    enabled: false
    mace-stun:
      enabled: true
      duration-ticks: 100
    axe-stun:
      enabled: true
      duration-ticks: 100
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |
| `mace-stun.enabled` | boolean | true | Apply stun when a mace hits a blocking player |
| `mace-stun.duration-ticks` | integer | 100 | Shield cooldown duration in ticks (20 ticks = 1 second) |
| `axe-stun.enabled` | boolean | true | Override axe shield stun duration |
| `axe-stun.duration-ticks` | integer | 100 | Shield cooldown duration in ticks |

## In-Game GUI

Open via `/vanilla menu` → **Shield Mechanics** → Right Click.

| Slot | Item | Action |
|------|------|--------|
| 4 | Mace | Toggle mace stun on/off |
| 3 | Red glass | Decrease mace stun duration (Left: −5, Shift: −20 ticks) |
| 5 | Lime glass | Increase mace stun duration (Left: +5, Shift: +20 ticks) |
| 13 | Diamond Axe | Toggle axe stun on/off |
| 12 | Red glass | Decrease axe stun duration |
| 14 | Lime glass | Increase axe stun duration |

Duration is clamped between **1** and **600 ticks** (0.05s – 30s).

## How It Works

- **Mace stun** — applied immediately on `EntityDamageByEntityEvent` when the victim is blocking and the attacker holds a mace.
- **Axe stun** — applied one tick after the hit event resolves. This is intentional: vanilla's axe shield-disable runs server-side after the event pipeline, so scheduling 1 tick later ensures our configured duration wins over vanilla's hardcoded 100-tick value.
- Applies to all axe tiers (wooden through netherite).
- Works against mobs and players wielding these weapons.

## Notes

- The mace has no built-in shield-disable in vanilla — this feature adds that behavior.
- Setting `axe-stun.duration-ticks: 100` with `axe-stun.enabled: true` matches vanilla behavior exactly.
- Setting `axe-stun.duration-ticks` lower than 100 makes shields recover faster; higher makes them locked for longer.
