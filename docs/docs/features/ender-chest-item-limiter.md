---
sidebar_position: 3
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['new']} />

# Ender Chest Item Limiter

Block selected items from being stored inside ender chests.

## Overview

This feature uses the same item-matching model as Item Limiter, but the restriction applies specifically to ender chest storage. It supports:

- Basic material matching
- Custom model data matching
- Display name matching
- Potion type matching

If a blocked item is moved into an ender chest, the move is cancelled.

## Configuration

```yaml
features:
  ender-chest-item-limiter:
    enabled: false
    notify-player: true
    notify-message: "<red>[Vanilla Core] <gray>You cannot put <yellow>{item}<gray> in an ender chest"
    blocked-items:
      TOTEM_OF_UNDYING:
        material: TOTEM_OF_UNDYING
      custom_token:
        material: PAPER
        customModelData: 4501
        displayName: "§6Server Token"
      SPLASH_POTION_STRONG_STRENGTH:
        material: SPLASH_POTION
        potionType: STRONG_STRENGTH
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for ender chest restrictions |
| `notify-player` | boolean | true | Whether players should be notified when a blocked insert is denied |
| `notify-message` | string | (see above) | Message shown when a blocked item is prevented |
| `blocked-items` | map | `{}` | Map of blocked item match definitions |

### Message Placeholders

| Placeholder | Replaced With |
|-------------|---------------|
| `{item}` | The blocked item name |

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Find **Ender Chest Item Limiter**
3. **Left Click** to toggle the feature
4. **Right Click** to open the manager GUI

### Add a Blocked Item

1. Open **Ender Chest Item Limiter** manager
2. Click **Add Blocked Item**
3. Drag the target item into the item slot
4. Click **Confirm**

### Remove a Blocked Item

1. Open **View Blocked Items**
2. **Shift + Left Click** an entry to remove it from the blocklist

## What Is Blocked

The feature blocks insertion attempts into ender chest slots, including:

- Cursor placement (left/right click)
- Shift-click from player inventory into ender chest
- Number-key hotbar swap into ender chest
- Drag operations that target ender chest slots

## Notes

- To bypass restrictions for specific staff or roles, grant `vanillacore.enderchestlimiter.bypass`.
- Matching is exact for custom item fields. If you include `displayName` or `customModelData`, only items with that exact metadata match.
