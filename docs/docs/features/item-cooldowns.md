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
```

## Notes

- Values are in seconds and support decimals.
- You can add additional material keys under `cooldowns`.
- The GUI gives quick controls for golden apples, enchanted golden apples, ender pearls, and wind charges.

## In-Game Usage

- Open `/smp` menu
- Find `Item Cooldowns`
- Left-click to enable or disable the feature
- Right-click to open settings GUI
- Left-click to increase cooldown, right-click to decrease, and use Shift for larger steps
