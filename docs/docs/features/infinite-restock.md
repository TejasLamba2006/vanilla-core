---
sidebar_position: 8
---

# Infinite Restock

Make villagers restock their trades instantly and infinitely.

## Overview

In vanilla Minecraft, villagers have limited stock for each trade and only restock twice per day at their workstation. Infinite Restock removes these limitations.

## Why use infinite restock?

The two-trades-per-day limit makes sense in singleplayer. On a server with trading halls and players all trying to use the same librarians, it's mostly just annoying. Found the perfect Mending librarian? Trade as much as you want. Need 64 stacks of emeralds? No waiting.

This feature is popular on servers that run public trading halls.

## Configuration

```yaml
features:
  infinite-restock:
    enabled: false
    max-trades: 0
    disable-price-penalty: true
    allow-wandering-traders: true
    uninstall-mode: false
    villager-blacklist: []
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |
| `max-trades` | integer | 0 | Maximum trades per villager (0 = unlimited) |
| `disable-price-penalty` | boolean | true | Prevents demand-based price increases |
| `allow-wandering-traders` | boolean | true | Apply infinite restock to wandering traders too |
| `uninstall-mode` | boolean | false | Restore original villager trades when disabling |
| `villager-blacklist` | list | [] | Professions excluded from infinite restock |

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on Infinite Restock (Emerald icon)
3. **Left Click**: Toggle the feature on/off
4. **Right Click**: Open the Infinite Restock Manager

### Infinite Restock Manager

The manager GUI provides detailed controls:

- **Set Max Trades**: Configure the trade limit per villager
- **Disable Price Penalty**: Toggle demand-based pricing
- **Allow Wandering Traders**: Include/exclude wandering traders
- **Villager Blacklist**: Exclude specific professions
- **Uninstall Mode**: Restore villagers when disabling

### Villager Blacklist

Click the Blacklist button to manage which professions are excluded:

- **Green**: Profession receives infinite restock
- **Red**: Profession is blacklisted (normal behavior)

Available professions:

- Armorer, Butcher, Cartographer, Cleric, Farmer
- Fisherman, Fletcher, Leatherworker, Librarian
- Mason, Shepherd, Toolsmith, Weaponsmith

## How It Works

### Instant Restock Mechanism

When enabled:

1. Player completes a trade with a villager
2. Plugin intercepts the trade completion event
3. The villager's stock is immediately restored
4. Player can repeat the trade without delay

The villager never actually runs out of stock from the player's perspective.

### Price Penalty System

Vanilla Minecraft increases prices for popular trades. When `disable-price-penalty: true`:

- Demand is reset to 0 for all trades
- Prices remain at base value regardless of trade frequency
- No more penalty for bulk trading
- Consistent, predictable pricing

### Max Trades Limit

The `max-trades` option allows you to set a limit:

- **0**: Unlimited trades (default)
- **1-64000**: Maximum trades per villager

This is useful if you want to allow restock but still have some scarcity.

## Presets

### Public Trading Hall

All villagers available to everyone with no limits:

```yaml
infinite-restock:
  enabled: true
  max-trades: 0
  disable-price-penalty: true
  allow-wandering-traders: true
  villager-blacklist: []
```

### Librarians Only

Keep other trades limited for economy balance:

```yaml
infinite-restock:
  enabled: true
  max-trades: 0
  disable-price-penalty: true
  villager-blacklist:
    - ARMORER
    - BUTCHER
    - CARTOGRAPHER
    - CLERIC
    - FARMER
    - FISHERMAN
    - FLETCHER
    - LEATHERWORKER
    - MASON
    - SHEPHERD
    - TOOLSMITH
    - WEAPONSMITH
```

### Economy Balance

Restock but keep some limits:

```yaml
infinite-restock:
  enabled: true
  max-trades: 100
  disable-price-penalty: false
  allow-wandering-traders: false
```

## Uninstall Mode

When you want to completely disable Infinite Restock and restore normal villager behavior:

1. Enable `uninstall-mode: true`
2. This will reset all affected villagers
3. The feature will be disabled
4. Villagers return to vanilla restock behavior

Note: Uninstall mode will reset villager trades.

## Integration with Other Features

### With Item Limiter

Combine to allow infinite trading but limit hoarding:

```yaml
# In item-limiter config, limit how many items players can carry
```

Players can buy unlimited items but only carry the configured limit.

### With Enchantment Limiter

Even with infinite book trades, enchantment caps still apply. Books above the cap will be reduced when applied to items.
