---
sidebar_position: 4
---

# Permissions

Vanilla Core uses a hierarchical permission system. All permissions default to OP only, meaning regular players cannot access any features without explicit permission grants.

## Permission Defaults

By default, Vanilla Core uses these permission levels:

- **OP Players** - Full access to all commands and bypass permissions
- **Regular Players** - No access to any Vanilla Core commands or bypasses

To change this behavior, use a permissions plugin like LuckPerms.

## Admin Permissions

These permissions control access to the main plugin functions:

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.admin` | Full access to all Vanilla Core features and GUI | OP |
| `vanillacore.reload` | Ability to reload configuration files | OP |
| `vanillacore.gui` | Ability to open the main configuration GUI | OP |

## Bypass Permissions

Bypass permissions allow players to ignore specific restrictions. Use these carefully, as they defeat the purpose of the features they bypass.

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.bypass.itemlimit` | Carry unlimited items, ignoring limits | OP |
| `vanillacore.bypass.enchantlimit` | Use items with any enchantment level | OP |
| `vanillacore.bypass.mace` | Use maces without any restrictions | OP |
| `vanillacore.bypass.netherite` | Obtain and use netherite items | OP |
| `vanillacore.bypass.nether` | Enter the Nether when it is locked | OP |
| `vanillacore.bypass.end` | Enter the End when it is locked | OP |

Warning: Giving players bypass permissions undermines your server's balance settings.

## Command Permissions

### Main Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.command.smp` | Use the `/vanilla` command | OP |
| `vanillacore.command.reload` | Use the `/vanilla reload` command | OP |

### Dimension Lock Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.nether` | View Nether lock status | OP |
| `vanillacore.nether.open` | Unlock the Nether dimension | OP |
| `vanillacore.nether.close` | Lock the Nether dimension | OP |
| `vanillacore.end` | View End lock status | OP |
| `vanillacore.end.open` | Unlock the End dimension | OP |
| `vanillacore.end.close` | Lock the End dimension | OP |

### Item Limiter Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.itemlimit` | Open the item limiter GUI | OP |
| `vanillacore.itemlimit.set` | Create and modify item limits | OP |
| `vanillacore.itemlimit.remove` | Remove existing item limits | OP |
| `vanillacore.itemlimit.list` | View all configured item limits | OP |

### Enchantment Limiter Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.enchantlimit` | Open the enchantment limiter GUI | OP |
| `vanillacore.enchantlimit.set` | Set enchantment level caps | OP |
| `vanillacore.enchantlimit.remove` | Remove enchantment caps | OP |
| `vanillacore.enchantlimit.list` | View all enchantment caps | OP |

## Wildcard Permissions

Wildcard permissions grant all child permissions in a category:

| Permission | Grants |
|------------|--------|
| `vanillacore.*` | All Vanilla Core permissions |
| `vanillacore.bypass.*` | All bypass permissions |
| `vanillacore.nether.*` | All Nether-related permissions |
| `vanillacore.end.*` | All End-related permissions |
| `vanillacore.itemlimit.*` | All item limiter permissions |
| `vanillacore.enchantlimit.*` | All enchantment limiter permissions |

## LuckPerms Configuration Examples

### Full Staff Access

Give staff members complete access to manage Vanilla Core:

```
/lp group staff permission set vanillacore.admin true
/lp group staff permission set vanillacore.reload true
```

Or use the wildcard:

```
/lp group staff permission set vanillacore.* true
```

### Moderator Access (Limited)

Allow moderators to manage dimensions without bypassing limits:

```
/lp group moderator permission set vanillacore.nether.* true
/lp group moderator permission set vanillacore.end.* true
/lp group moderator permission set vanillacore.itemlimit.list true
```

### VIP Bypass (Item Limits Only)

Allow VIP players to bypass item limits but still respect other restrictions:

```
/lp group vip permission set vanillacore.bypass.itemlimit true
```

Note: Plan your permissions carefully if using group inheritance.

### Remove OP Bypass

To prevent OP players from automatically having bypass permissions:

1. Set the permissions explicitly to false:

```
/lp group default permission set vanillacore.bypass.* false
```

1. Then grant specific bypasses only to intended groups.

## Best Practices

### Separate Bypass from Management

Staff should be able to manage features without bypassing them. Consider this setup:

**Admin Group:**

- `vanillacore.admin` - Can manage all features
- No bypass permissions - Subject to same limits as players

**Helper Group:**

- `vanillacore.itemlimit.list` - Can view limits to help players
- No other permissions

### Audit Bypass Permissions

Regularly review who has bypass permissions:

```
/lp user <playername> permission info
```

Look for any `vanillacore.bypass.*` entries that shouldn't be there.

### Use Groups, Not Individual Permissions

Always assign permissions through groups rather than directly to players. This makes management easier and more consistent.

---

Next: Explore [Features](./category/features) in detail.

