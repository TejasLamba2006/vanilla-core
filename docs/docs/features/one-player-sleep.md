---
sidebar_position: 7
---

# One Player Sleep

Allow a single player to skip the night for the entire server.

## Overview

In vanilla Minecraft, all players must sleep simultaneously to skip the night. This becomes problematic on multiplayer servers where players are in different timezones, AFK, or simply busy building. One Player Sleep removes this requirement, allowing any single player to skip the night for everyone.

## Why Use One Player Sleep

Common problems solved by this feature:

- **Timezone differences**: Players from different regions are online at different times
- **AFK players**: One AFK player blocks everyone from sleeping
- **Base builders**: Players busy building underground or in the nether
- **Phantom prevention**: Makes it easier to reset phantom timers
- **Server flow**: No more waiting and asking "can everyone sleep?"

One Player Sleep is one of the most requested features on any SMP. It dramatically improves quality of life for all players.

## Configuration

```yaml
features:
  one-player-sleep:
    enabled: false
    sleep-message: "§e\{player\} §7is sleeping..."
    skip-message: "§a☀ Good morning!"
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |
| `sleep-message` | string | "§e\{player\} §7is sleeping..." | Message broadcast when a player starts sleeping |
| `skip-message` | string | "§a☀ Good morning!" | Message broadcast when night is skipped |

### Message Placeholders

The `sleep-message` supports these placeholders:

| Placeholder | Replaced With |
|-------------|---------------|
| `\{player\}` | Player's name |
| `\{displayname\}` | Player's display name (with nickname if set) |

Set either message to empty (`""`) to disable that message.

## How It Works

When enabled, Vanilla Core modifies Minecraft's `playersSleepingPercentage` gamerule:

- **Enabled**: Sets `playersSleepingPercentage` to `0` (only 1 player needed)
- **Disabled**: Resets to `100` (all players must sleep)

This uses the vanilla game mechanic introduced in Minecraft 1.17, ensuring full compatibility with all Minecraft features.

## Using the GUI

1. Run `/smp` to open the main menu
2. Locate One Player Sleep in the feature list
3. Left-click to toggle the feature on/off

## What Happens When Enabled

When a player enters a bed with this feature enabled:

1. The player starts sleeping normally
2. No waiting for other players required
3. Night skips after the standard sleep animation
4. All players advance to morning together

## Recommended Use

This feature works great on virtually any server type:

### Small Private SMP (2-5 players)

Enable for maximum convenience:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: "§e{player} §7is sleeping..."
  skip-message: "§a☀ Good morning!"
```

### Large Public Server (50+ players)

Still recommended - prevents phantom issues. You may want shorter messages:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: ""
  skip-message: "§a☀ Morning!"
```

### Silent Mode

If you don't want any sleep announcements:

```yaml
one-player-sleep:
  enabled: true
  sleep-message: ""
  skip-message: ""
```

## Technical Details

### Gamerule Approach

Using the `playersSleepingPercentage` gamerule provides several benefits:

- **Native Integration**: Works with vanilla sleep mechanics
- **No Performance Impact**: No event listeners or calculations needed
- **Full Compatibility**: Works with all bed types and sleep features
- **Persistent**: Survives server restarts when enabled

### Edge Cases

**Beds in Nether/End**: Still explode as normal - this only affects Overworld sleep.

**Phantom Spawning**: Phantoms are still based on individual player insomnia. Sleeping resets your counter regardless of how many others slept.

## Integration with Other Features

This is a standalone feature that works independently. No configuration needed with other Vanilla Core features.
