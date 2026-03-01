---
sidebar_position: 13
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['pvp', 'recommended']} />

# Breach swap

Blocks the "breach swap" exploit — switching between a Breach mace and a sword or axe to deal amplified damage after a mace hit.

## Overview

Breach reduces a target's armor effectiveness on mace hits. The exploit works by immediately swapping to a sword or axe after landing a mace hit, so the follow-up strikes connect while the armor debuff is still active. It's fast enough that most players can't react in time.

This feature cancels any hotbar switch or F-key hand swap that would go between a Breach mace and a sword/axe, in either direction. That's it — no cooldowns, no delays, just a hard stop on the swap itself.

## Configuration

```yaml
features:
  breach-swap:
    enabled: false
    # Action bar message shown when the swap is blocked.
    # Supports MiniMessage format.
    denied-message: "<red>Breach swapping is disabled."
```

### Configuration options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Toggle the feature on or off |
| `denied-message` | string | `<red>Breach swapping is disabled.` | Action bar message sent to the player when a swap is blocked. Supports [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting. |

## In-game GUI

`/vanilla menu` → **Breach Swap** → Left Click to toggle.

## How it works

Two events are watched:

- `PlayerItemHeldEvent` — fires on hotbar slot changes. Cancelled if swapping between a Breach mace and a sword or axe.
- `PlayerSwapHandItemsEvent` — fires when the player presses F. Cancelled under the same condition.

The player gets an action bar message whenever a swap is blocked.

A "Breach mace" means any `MACE` with the `BREACH` enchantment. Swords and axes are matched by material name suffix (`_SWORD`, `_AXE`), so custom-tier materials from datapacks are covered too.
