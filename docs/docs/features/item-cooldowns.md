---
sidebar_position: 8
---

# Item Cooldowns

Set per-item cooldowns for consumables and right-click items.

## Configuration

```yaml
features:
  item-cooldowns:
    enabled: false
    cooldowns:
      GOLDEN_APPLE: 0.0
      ENCHANTED_GOLDEN_APPLE: 0.0
      ENDER_PEARL: 0.0
      WIND_CHARGE: 0.5
      WOODEN_SPEAR: 0.0
      STONE_SPEAR: 0.0
      IRON_SPEAR: 0.0
      GOLDEN_SPEAR: 0.0
      DIAMOND_SPEAR: 0.0
      NETHERITE_SPEAR: 0.0
      COPPER_SPEAR: 0.0
```

## Notes

- Values are in seconds and support decimals.
- You can add additional material keys under `cooldowns`.
- The GUI gives quick controls for golden apples, enchanted golden apples, ender pearls, wind charges, and all seven Spear tiers (Wooden through Netherite, plus Copper) added in Mounts of Mayhem.

## In-Game Usage

- Open `/smp` menu
- Find `Item Cooldowns`
- Left-click to enable or disable the feature
- Right-click to open settings GUI
- Left-click to increase cooldown, right-click to decrease, and use Shift for larger steps
