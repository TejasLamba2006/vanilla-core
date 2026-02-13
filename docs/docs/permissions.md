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
| `smpcore.admin` | Full access to all Vanilla Core features and GUI | OP |
| `smpcore.reload` | Ability to reload configuration files | OP |
| `smpcore.gui` | Ability to open the main configuration GUI | OP |

## Bypass Permissions

Bypass permissions allow players to ignore specific restrictions. Use these carefully, as they defeat the purpose of the features they bypass.

| Permission | Description | Default |
|------------|-------------|---------|
| `smpcore.bypass.itemlimit` | Carry unlimited items, ignoring limits | OP |
| `smpcore.bypass.enchantlimit` | Use items with any enchantment level | OP |
| `smpcore.bypass.mace` | Use maces without any restrictions | OP |
| `smpcore.bypass.netherite` | Obtain and use netherite items | OP |
| `smpcore.bypass.nether` | Enter the Nether when it is locked | OP |
| `smpcore.bypass.end` | Enter the End when it is locked | OP |

:::danger Bypass Warning
Giving players bypass permissions undermines your server's balance settings. A player with `smpcore.bypass.itemlimit` can carry unlimited totems while everyone else is limited to 2. Only grant bypass permissions when absolutely necessary.
:::

## Command Permissions

### Main Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smpcore.command.smp` | Use the `/smp` command | OP |
| `smpcore.command.reload` | Use the `/smp reload` command | OP |

### Dimension Lock Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smpcore.nether` | View Nether lock status | OP |
| `smpcore.nether.open` | Unlock the Nether dimension | OP |
| `smpcore.nether.close` | Lock the Nether dimension | OP |
| `smpcore.end` | View End lock status | OP |
| `smpcore.end.open` | Unlock the End dimension | OP |
| `smpcore.end.close` | Lock the End dimension | OP |

### Item Limiter Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smpcore.itemlimit` | Open the item limiter GUI | OP |
| `smpcore.itemlimit.set` | Create and modify item limits | OP |
| `smpcore.itemlimit.remove` | Remove existing item limits | OP |
| `smpcore.itemlimit.list` | View all configured item limits | OP |

### Enchantment Limiter Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smpcore.enchantlimit` | Open the enchantment limiter GUI | OP |
| `smpcore.enchantlimit.set` | Set enchantment level caps | OP |
| `smpcore.enchantlimit.remove` | Remove enchantment caps | OP |
| `smpcore.enchantlimit.list` | View all enchantment caps | OP |

## Wildcard Permissions

Wildcard permissions grant all child permissions in a category:

| Permission | Grants |
|------------|--------|
| `smpcore.*` | All Vanilla Core permissions |
| `smpcore.bypass.*` | All bypass permissions |
| `smpcore.nether.*` | All Nether-related permissions |
| `smpcore.end.*` | All End-related permissions |
| `smpcore.itemlimit.*` | All item limiter permissions |
| `smpcore.enchantlimit.*` | All enchantment limiter permissions |

## LuckPerms Configuration Examples

### Full Staff Access

Give staff members complete access to manage Vanilla Core:

```
/lp group staff permission set smpcore.admin true
/lp group staff permission set smpcore.reload true
```

Or use the wildcard:

```
/lp group staff permission set smpcore.* true
```

### Moderator Access (Limited)

Allow moderators to manage dimensions without bypassing limits:

```
/lp group moderator permission set smpcore.nether.* true
/lp group moderator permission set smpcore.end.* true
/lp group moderator permission set smpcore.itemlimit.list true
```

### VIP Bypass (Item Limits Only)

Allow VIP players to bypass item limits but still respect other restrictions:

```
/lp group vip permission set smpcore.bypass.itemlimit true
```

:::tip Permission Inheritance
If you're using LuckPerms with group inheritance (Moderator inherits from VIP, Staff inherits from Moderator), plan your permissions carefully to avoid unintended bypass access.
:::

### Remove OP Bypass

To prevent OP players from automatically having bypass permissions:

1. Set the permissions explicitly to false:

```
/lp group default permission set smpcore.bypass.* false
```

1. Then grant specific bypasses only to intended groups.

## Best Practices

### Separate Bypass from Management

Staff should be able to manage features without bypassing them. Consider this setup:

**Admin Group:**

- `smpcore.admin` - Can manage all features
- No bypass permissions - Subject to same limits as players

**Helper Group:**

- `smpcore.itemlimit.list` - Can view limits to help players
- No other permissions

### Audit Bypass Permissions

Regularly review who has bypass permissions:

```
/lp user <playername> permission info
```

Look for any `smpcore.bypass.*` entries that shouldn't be there.

### Use Groups, Not Individual Permissions

Always assign permissions through groups rather than directly to players. This makes management easier and more consistent.

---

Next: Explore [Features](./category/features) in detail.
