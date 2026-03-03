---
sidebar_position: 11
---

# Stop item despawn

Keep death drops on the ground until someone picks them up.

## Overview

In vanilla Minecraft, dropped items despawn after 5 minutes (6000 ticks). This feature tracks items that spawn near a player's death location and blocks the despawn timer on them — they stay until collected.

## Why use this?

Five minutes isn't long when the fight happened across the map, the chunk reloaded twice, and your teammates are offline. Death loot just sits there waiting for someone to grab it, not on a timer.

## Configuration

```yaml
features:
  stop-item-despawn:
    enabled: false
```

### Configuration options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click Stop Item Despawn (Chest icon)
3. **Left Click**: Toggle on/off

## How it works

1. When a player dies, the death location is recorded.
2. Any item that spawns within 4 blocks of that spot in the next 3 seconds gets tagged as a death drop.
3. The tagged item ignores the despawn timer.
4. If two items merge, the merged stack keeps the tag.
5. Tags are removed when someone picks the item up.

### What's protected

- All items that drop from a player death at or near the death spot.

### What's not protected

- Items thrown or discarded in the same area that aren't near a death (though timing overlap is possible).
- Mob drops.
- Items destroyed by explosions — use [Item Explosion Immunity](item-explosion-immunity.md) for that.
- Items that fall into lava or the void.
