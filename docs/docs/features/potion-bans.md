---
sidebar_position: 6
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['pvp', 'recommended']} />

# Potion Bans

Potion Bans lets you decide how strict potion usage should be:

- Ban Tier 1 and Tier 2 separately per effect
- Toggle everything on at once with "Ban All Potions"

## Configuration

```yaml
features:
  potion-bans:
    enabled: false
    # Format: effect:tier1:tier2
    # Example: strength:true:false
    blocked-effects: []
```

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| enabled | boolean | false | Master toggle for Potion Bans |
| blocked-effects | list[string] | [] | Per-effect tier flags in `effect:tier1:tier2` format |

## Blocked Effects Format

Use lowercase effect keys (recommended). Each entry should be:

- `effect:tier1:tier2`

Examples:

- `strength:true:false` (Tier 1 blocked, Tier 2 allowed)
- `speed:false:true` (Tier 1 allowed, Tier 2 blocked)
- `regeneration:true:true` (both blocked)

Aliases like swiftness, healing, harming, and jump are also accepted.

## How It Works

When enabled, the feature blocks potion effects at application time and covers:

- Potion drinking/splash/lingering effect application
- Tier 1 and Tier 2 checks per effect
- Brewing stand glowstone usage when all Tier 2 effects are disabled

## In-Game Usage

- Open `/smp` menu
- Find `Potion Bans`
- Left-click to toggle feature
- Right-click to open Potion Bans settings GUI
- In GUI:
  - Toggle `Ban All Potions` (writes all effects as blocked in config)
  - Left-click an effect to toggle Tier 2
  - Right-click an effect to toggle Tier 1
  - Clear all blocked effects in one click

You can still edit `blocked-effects` directly in config when needed.
