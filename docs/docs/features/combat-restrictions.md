---
sidebar_position: 7
---

# Combat Restrictions

Combat restrictions are split into individual toggles, so you can enable only the ones your server needs.

## Configuration

```yaml
features:
  bed-bombing-ban:
    enabled: false
  tnt-minecart-ban:
    enabled: false
  villager-killing-ban:
    enabled: false
  crystal-pvp-ban:
    enabled: false
  anchor-restriction:
    enabled: false
  pearl-restriction:
    enabled: false
```

## Options

| Feature | Config Path | Description |
|---------|-------------|-------------|
| Bed Bombing Ban | `features.bed-bombing-ban.enabled` | Blocks bed explosions in Nether/End |
| TNT Minecart Ban | `features.tnt-minecart-ban.enabled` | Cancels TNT minecart explosion damage/explosions |
| Villager Killing Ban | `features.villager-killing-ban.enabled` | Prevents players from killing villagers |
| Crystal PvP Ban | `features.crystal-pvp-ban.enabled` | Blocks crystal placement/damage/explosions |
| Anchor Restriction | `features.anchor-restriction.enabled` | Restricts respawn anchor charging outside Nether |
| Pearl Restriction | `features.pearl-restriction.enabled` | Blocks pearl usage and pearl teleporting |

## In-Game Usage

- Open `/vanilla` menu
- Toggle each combat restriction on its own
