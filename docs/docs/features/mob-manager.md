---
sidebar_position: 6
---

import {FeatureBadgeGroup} from '@site/src/components/FeatureBadge';

<FeatureBadgeGroup badges={['popular']} />

# Mob Manager

Control mob spawning on a per-world basis with spawn reason filtering.

## Overview

The Mob Manager provides control over which mobs can spawn in each world on your server. You can block specific mob types from spawning naturally while still allowing them from spawn eggs, spawners, or commands. This gives you granular control over mob population without completely removing them.

## Configuration

```yaml
features:
  mob-manager:
    enabled: false
    worlds:
      world:
        # List of disabled mob types
        - PHANTOM
        - CREEPER
      world_nether:
        - GHAST
      world_the_end:
        - ENDERMAN
    allowed-spawn-reasons:
      - SPAWNER_EGG
      - CUSTOM
      - COMMAND
    chunk-cleanup-enabled: false
    worldguard-bypass: true
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the mob manager |
| `worlds` | map | empty | Per-world lists of disabled mob types |
| `allowed-spawn-reasons` | list | varies | Spawn reasons that bypass mob blocking |
| `chunk-cleanup-enabled` | boolean | false | Remove blocked mobs when chunks load |
| `worldguard-bypass` | boolean | true | Allow mobs in WorldGuard regions (optional) |

### Allowed Spawn Reasons

Even when a mob type is disabled, it can still spawn through these allowed methods:

| Spawn Reason | Description |
|--------------|-------------|
| `SPAWNER_EGG` | Placed using a spawn egg |
| `CUSTOM` | Spawned by plugins |
| `COMMAND` | Spawned via /summon or similar |
| `SPAWNER` | From mob spawners (add if you want spawners to work) |

To also block spawn eggs, remove `SPAWNER_EGG` from the list:

```yaml
allowed-spawn-reasons:
  - CUSTOM
  - COMMAND
```

### WorldGuard Integration

When WorldGuard is installed and `worldguard-bypass: true`:

- Mobs can spawn normally within WorldGuard regions
- Only natural spawning outside regions is affected
- Useful for allowing spawner farms while blocking wild mobs

WorldGuard is optional. Mob Manager works without it.

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on Mob Manager
3. **Left Click**: Toggle the feature on/off
4. **Right Click**: Open the Mob Manager settings

The Mob Manager GUI allows you to:

- Toggle the feature
- View current per-world settings
- Configure blocked mobs for each world

## Per-World Configuration

Each world can have its own list of blocked mobs:

```yaml
worlds:
  world:
    - PHANTOM
    - CREEPER
    - ZOMBIE
  world_nether:
    - GHAST
    - PIGLIN_BRUTE
  world_the_end:
    - ENDERMAN
  creative_world:
    - ZOMBIE
    - SKELETON
    - CREEPER
    - SPIDER
    - PHANTOM
```

Use the exact world folder name (case-sensitive).

## Mob Type Reference

Use the exact Minecraft entity type names in UPPERCASE:

### Common Hostile Mobs

| Type | Description |
|------|-------------|
| `ZOMBIE` | Zombie |
| `SKELETON` | Skeleton |
| `CREEPER` | Creeper |
| `SPIDER` | Spider |
| `PHANTOM` | Phantom (most commonly disabled) |
| `ENDERMAN` | Enderman |
| `WITCH` | Witch |
| `DROWNED` | Drowned |

### Nether Mobs

| Type | Description |
|------|-------------|
| `GHAST` | Ghast |
| `BLAZE` | Blaze |
| `WITHER_SKELETON` | Wither Skeleton |
| `PIGLIN` | Piglin |
| `PIGLIN_BRUTE` | Piglin Brute |
| `HOGLIN` | Hoglin |
| `MAGMA_CUBE` | Magma Cube |

### End Mobs

| Type | Description |
|------|-------------|
| `ENDERMAN` | Enderman |
| `SHULKER` | Shulker |

## Presets

### No Phantoms Server-Wide

```yaml
mob-manager:
  enabled: true
  worlds:
    world:
      - PHANTOM
    world_nether:
      - PHANTOM
    world_the_end:
      - PHANTOM
```

### Peaceful Overworld

Remove most hostile mobs from the main world:

```yaml
worlds:
  world:
    - ZOMBIE
    - SKELETON
    - CREEPER
    - SPIDER
    - PHANTOM
    - WITCH
    - DROWNED
```

### Safe Spawn Area (with WorldGuard)

Keep spawn safe while allowing mobs elsewhere. Create a WorldGuard region around spawn, then:

```yaml
mob-manager:
  enabled: true
  worldguard-bypass: false  # Mobs blocked everywhere, including regions
```

Or use WorldGuard's own mob flags for more control.

## Chunk Cleanup

When `chunk-cleanup-enabled: true`:

- Blocked mob types are removed when chunks are loaded
- Clears existing mobs that were spawned before the restriction
- Useful when enabling mob blocking mid-game

When `chunk-cleanup-enabled: false`:

- Only prevents NEW spawns
- Existing mobs remain until they despawn naturally

## Troubleshooting

### Mobs still spawning

1. Verify `enabled: true` in config
2. Check the world name matches exactly (case-sensitive)
3. Verify the mob type is spelled correctly (UPPERCASE)
4. Run `/vanilla reload` after config changes
5. Check if the spawn reason is in `allowed-spawn-reasons`

### Spawners not working

By default, spawner-spawned mobs are blocked too. Add `SPAWNER` to allowed reasons:

```yaml
allowed-spawn-reasons:
  - SPAWNER_EGG
  - SPAWNER
  - CUSTOM
  - COMMAND
```

### Mob type not recognized

1. Use uppercase: `ZOMBIE` not `zombie`
2. Use underscores: `WITHER_SKELETON` not `WITHER SKELETON`
3. Check console for warnings about invalid mob types
