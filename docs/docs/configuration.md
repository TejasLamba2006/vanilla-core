---
sidebar_position: 10
---

# Configuration

Complete configuration reference for Vanilla Core.

## Config File Location

The main configuration file is located at:

```
plugins/Vanilla Core/config.yml
```

All plugin settings are stored in this single file. The file is automatically created with default values on first server start.

## Configuration Structure

The configuration is organized into logical sections:

| Section | Purpose |
|---------|---------|
| `plugin` | Global plugin settings (name, prefix, verbose logging) |
| `features` | All feature toggles and their individual settings |

## Plugin Settings

```yaml
plugin:
  name: "Vanilla Core"
  prefix: "<dark_gray>[<gold>SMP<dark_gray>]<reset>"
  verbose: false
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `name` | string | "Vanilla Core" | Plugin display name |
| `prefix` | string | `"<dark_gray>[<gold>SMP<dark_gray>]<reset>"` | Prefix for chat messages (MiniMessage format) |
| `verbose` | boolean | false | Enable verbose logging for debugging |

## Features Configuration

Each feature follows a consistent pattern:

```yaml
features:
  feature-name:
    enabled: true/false
    # Feature-specific options...
```

## Complete Configuration Reference

Below is the complete default configuration with all options:

```yaml
# ============================================
# Vanilla Core Configuration
# ============================================
config-version: 2

plugin:
  name: "Vanilla Core"
  prefix: "<dark_gray>[<gold>SMP<dark_gray>]<reset>"
  verbose: false

features:
  # ------------------------------------------
  # Enchantment Limiter
  # ------------------------------------------
  enchantment-limiter:
    enabled: false
    limits:
      sharpness: 4
      protection: 3
      fire_protection: 3
      blast_protection: 3
      projectile_protection: 3
      feather_falling: 3
      power: 4
      unbreaking: 2

  # ------------------------------------------
  # Mace Limiter
  # ------------------------------------------
  mace-limiter:
    enabled: false
    max-maces: 1
    maces-crafted: 0  # Managed by plugin
    title:
      enabled: true
      title: "<gold>⚔ MACE CRAFTED ⚔"
      subtitle: "<yellow>{player} <gray>has crafted mace <yellow>#<gold>{count}"
      fade-in: 10
      stay: 70
      fade-out: 20
    chat:
      enabled: true
      message: "<gold>{player} <yellow>has crafted mace #<gold>{count}<yellow>!"
    sound:
      enabled: true
      sound: "ENTITY_ENDER_DRAGON_GROWL"
      volume: 1.0
      pitch: 1.0

  # ------------------------------------------
  # Dimension Lock - End
  # ------------------------------------------
  dimension-lock-end:
    enabled: false
    locked: false
    locked-message: "<red>The End is currently locked!"

  # ------------------------------------------
  # Dimension Lock - Nether
  # ------------------------------------------
  dimension-lock-nether:
    enabled: false
    locked: false
    locked-message: "<red>The Nether is currently locked!"

  # ------------------------------------------
  # Netherite Disabler
  # ------------------------------------------
  netherite-disabler:
    enabled: false
    disabled-items:
      sword: true
      axe: true
      pickaxe: true
      shovel: true
      hoe: true
      helmet: true
      chestplate: true
      leggings: true
      boots: true

  # ------------------------------------------
  # Invisible Kills
  # ------------------------------------------
  invisible-kills:
    enabled: false
    death-message: "{victim} was killed by <obfuscated>?????????"

  # ------------------------------------------
  # Item Explosion Immunity
  # ------------------------------------------
  item-explosion-immunity:
    enabled: false

  # ------------------------------------------
  # Infinite Restock
  # ------------------------------------------
  infinite-restock:
    enabled: false
    max-trades: 0
    disable-price-penalty: true
    allow-wandering-traders: true
    uninstall-mode: false
    villager-blacklist: []

  # ------------------------------------------
  # Item Limiter
  # ------------------------------------------
  item-limiter:
    enabled: false
    notify-player: true
    notify-message: "<red>[Vanilla Core] <gray>Excess items removed: {item} x{amount} (limit: {limit})"
    drop-excess: true
    limits:
      GOLDEN_APPLE:
        material: GOLDEN_APPLE
        limit: 96
      COBWEB:
        material: COBWEB
        limit: 64
      TOTEM_OF_UNDYING:
        material: TOTEM_OF_UNDYING
        limit: 2
      EXPERIENCE_BOTTLE:
        material: EXPERIENCE_BOTTLE
        limit: 128
      WIND_CHARGE:
        material: WIND_CHARGE
        limit: 64
      BREEZE_ROD:
        material: BREEZE_ROD
        limit: 64
      SPLASH_POTION_STRONG_STRENGTH:
        material: SPLASH_POTION
        limit: 0
        potionType: STRONG_STRENGTH
      SPLASH_POTION_STRONG_SWIFTNESS:
        material: SPLASH_POTION
        limit: 0
        potionType: STRONG_SWIFTNESS

  # ------------------------------------------
  # One Player Sleep
  # ------------------------------------------
  one-player-sleep:
    enabled: false
    sleep-message: "<yellow>{player} <gray>is sleeping..."
    skip-message: "<green>☀ Good morning!"

  # ------------------------------------------
  # Mob Manager
  # ------------------------------------------
  mob-manager:
    enabled: false
    worlds: {}
    allowed-spawn-reasons:
      - SPAWNER_EGG
      - CUSTOM
      - COMMAND
    chunk-cleanup-enabled: false
    worldguard-bypass: true

  # ------------------------------------------
  # Minimap Control
  # ------------------------------------------
  minimap-control:
    enabled: false
    global-mode: FAIR
    nether-fair-mode: true
    send-on-join: true
    send-on-world-change: true
    worlds: {}

  # ------------------------------------------
  # Server Restart
  # ------------------------------------------
  server-restart:
    enabled: false
    countdown-time: 60
    countdown-announcements:
      - 300
      - 120
      - 60
      - 30
      - 10
      - 5
      - 4
      - 3
      - 2
      - 1
    notification-types:
      - chat
      - bossbar
    bossbar-color: RED
    timezone: ""
    scheduled-restarts-enabled: false
    scheduled-times:
      - "04:00:00"
    execute-pre-commands: true
    pre-restart-commands:
      - "save-all"
    sound:
      enabled: true
      sound: "BLOCK_NOTE_BLOCK_PLING"
      volume: 1.0
      pitch: 1.0
    messages:
      countdown-message: "<red>⚠ Server restarting in <yellow>{time} <red>seconds!"
      actionbar-message: "<red>Restarting in {time}s"
      bossbar-message: "<red>Server Restart: {time}s"
      title-message: "<red>⚠ Server Restart"
      subtitle-message: "<yellow>{time} seconds remaining"
      restart-now-message: "<red>⚠ Server restart initiated by {player}!"
      restart-done-message: "<red>⚠ Server is restarting now!"
      cancelled-message: "<green>✔ Server restart has been cancelled."
```

## Feature Quick Reference

| Feature | Config Key | Description |
|---------|------------|-------------|
| [Enchantment Limiter](/docs/features/enchantment-limiter) | `enchantment-limiter` | Limit enchantment levels |
| [Item Limiter](/docs/features/item-limiter) | `item-limiter` | Limit item quantities |
| [Mace Limiter](/docs/features/mace-limiter) | `mace-limiter` | Limit total maces craftable |
| [Netherite Disabler](/docs/features/netherite-disabler) | `netherite-disabler` | Block netherite item crafting |
| [Dimension Locks](/docs/features/dimension-locks) | `dimension-lock-end`, `dimension-lock-nether` | Lock dimensions |
| [One Player Sleep](/docs/features/one-player-sleep) | `one-player-sleep` | One player sleep to skip night |
| [Mob Manager](/docs/features/mob-manager) | `mob-manager` | Control mob spawning |
| [Infinite Restock](/docs/features/infinite-restock) | `infinite-restock` | Unlimited villager trades |
| [Invisible Kills](/docs/features/invisible-kills) | `invisible-kills` | Hide killer name when invisible |
| [Item Explosion Immunity](/docs/features/item-explosion-immunity) | `item-explosion-immunity` | Protect items from explosions |
| [Minimap Control](/docs/features/minimap-control) | `minimap-control` | Control minimap mod features |
| [Server Restart](/docs/features/server-restart) | `server-restart` | Scheduled restarts with countdown |

## Color Tags (MiniMessage)

All messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html) format. Wrap color names in `< >`:

| Tag | Color | Tag | Color |
|-----|-------|-----|-------|
| `<black>` | Black | `<dark_gray>` | Dark Gray |
| `<dark_blue>` | Dark Blue | `<blue>` | Blue |
| `<dark_green>` | Dark Green | `<green>` | Green |
| `<dark_aqua>` | Dark Aqua | `<aqua>` | Aqua |
| `<dark_red>` | Dark Red | `<red>` | Red |
| `<dark_purple>` | Dark Purple | `<light_purple>` | Light Purple |
| `<gold>` | Gold | `<yellow>` | Yellow |
| `<gray>` | Gray | `<white>` | White |

### Format Tags

| Tag | Effect |
|-----|--------|
| `<bold>` | Bold |
| `<italic>` | Italic |
| `<underlined>` | Underline |
| `<strikethrough>` | Strikethrough |
| `<obfuscated>` | Obfuscated |
| `<reset>` | Reset |
| `<!italic>` | Cancel default italic (useful in item lore) |

## Reloading Configuration

After editing `config.yml`, apply changes with:

```bash
/vanilla reload
```

## Backup Recommendations

Before making significant changes:

### Manual Backup

```bash
cp plugins/Vanilla Core/config.yml plugins/Vanilla Core/config.yml.backup
```

## Troubleshooting Config Issues

### Config not loading

1. Check for YAML syntax errors (online validators help)
2. Look for console errors on startup
3. Delete config.yml and let plugin regenerate defaults

### Changes not taking effect

1. Run `/vanilla reload` after editing
2. Check you edited the correct file (not a backup)
3. Use the GUI for most changes - it handles validation automatically

### Feature not working

1. Verify `enabled: true` for the feature
2. Check for permission requirements
3. Review feature-specific documentation
