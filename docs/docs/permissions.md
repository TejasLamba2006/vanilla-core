---
sidebar_position: 4
---

# Permissions

SMP Watchdog uses a hierarchical permission system. All permissions default to OP only, meaning regular players cannot access any features without explicit permission grants.

Primary namespace:

- `smp.*`

## Permission Defaults

By default, SMP Watchdog uses these permission levels:

- **OP Players** - Full access to all commands and bypass permissions
- **Regular Players** - No access to any SMP Watchdog commands or bypasses

To change this behavior, use a permissions plugin like LuckPerms.

## Admin Permissions

These permissions control access to the main plugin functions:

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.admin` | Full access to all SMP Watchdog features and GUI | OP |
| `smp.reload` | Ability to reload configuration files | OP |
| `smp.version` | Ability to view the running plugin version | OP |
| `smp.menu` | Ability to open the main configuration GUI | OP |
| `smp.enchant` | Manage enchantment limiter commands | OP |
| `smp.mace` | Manage mace limiter commands | OP |
| `smp.mace.reset` | Reset mace limiter counts | OP |
| `smp.netherite` | Manage netherite disabler commands | OP |
| `smp.infiniterestock` | Open/manage Infinite Restock | OP |
| `smp.ritual` | Start/view ritual events | OP |
| `smp.ritual.cancel` | Cancel active ritual events | OP |
| `smp.msg` | Send private messages | OP |
| `smp.reply` | Reply to private messages | OP |
| `smp.socialspy` | Use social spy | OP |
| `smp.announcements` | Manage announcements engine | OP |

## Command Permissions

### Main Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.menu` | Use `/smp` and `/smp menu` | OP |
| `smp.reload` | Use `/smp reload` and `/smp reload <module>` | OP |
| `smp.version` | Use `/smp version` | OP |
| `smp.ritual` | Use `/ritual` and `/smp ritual` | OP |
| `smp.ritual.cancel` | Cancel active ritual events | OP |
| `smp.msg` | Use `/smp msg` | OP |
| `smp.reply` | Use `/smp reply` | OP |
| `smp.socialspy` | Use `/smp socialspy` | OP |
| `smp.toggle.chat` | Use `/smp togglechat` | true |
| `smp.toggle.pm` | Use `/smp togglepm` | true |
| `smp.toggle.mentions` | Use `/smp togglementions` | true |
| `smp.block` | Use `/smp block` | true |
| `smp.unblock` | Use `/smp unblock` | true |
| `smp.blocklist` | Use `/smp blocked` | true |
| `smp.announcements` | Use `/smp announcements reload` | OP |

### Dimension Lock Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.dimension.nether` | Manage Nether lock status/open/close | OP |
| `smp.dimension.end` | Manage End lock status/open/close | OP |

### Bypass and Exception Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.dimension.bypass` | Bypass all dimension locks | OP |
| `smp.dimension.bypass.end` | Bypass End lock only | false |
| `smp.dimension.bypass.nether` | Bypass Nether lock only | false |
| `smp.itemlimiter.bypass` | Bypass Item Limiter restrictions | OP |
| `smp.enderchestlimiter.bypass` | Bypass Ender Chest Item Limiter restrictions | OP |
| `smp.netherite.craft.*` | Bypass netherite crafting restrictions | false |

## Wildcard Permissions

Wildcard permissions grant all child permissions in a category:

| Permission | Grants |
|------------|--------|
| `smp.*` | All SMP Watchdog permissions |
| `smp.netherite.craft.*` | Bypass all netherite craft restrictions |

## LuckPerms Configuration Examples

### Full Staff Access

Give staff members complete access to manage SMP Watchdog:

```
/lp group staff permission set smp.admin true
/lp group staff permission set smp.reload true
```

Or use the wildcard:

```
/lp group staff permission set smp.* true
```

### Moderator Access (Limited)

Allow moderators to manage dimensions without bypassing limits:

```
/lp group moderator permission set smp.dimension.nether true
/lp group moderator permission set smp.dimension.end true
/lp group moderator permission set smp.menu true
```

### VIP Bypass (Item Limits Only)

Allow VIP players to bypass item limits but still respect other restrictions:

```
/lp group vip permission set smp.itemlimiter.bypass true
```

Note: Plan your permissions carefully if using group inheritance.

### Remove OP Bypass

To prevent OP players from automatically having bypass permissions:

1. Set the permissions explicitly to false:

```
/lp group default permission set smp.itemlimiter.bypass false
/lp group default permission set smp.enderchestlimiter.bypass false
```

1. Then grant specific bypasses only to intended groups.

## Best Practices

### Separate Bypass from Management

Staff should be able to manage features without bypassing them. Consider this setup:

**Admin Group:**

- `smp.admin` - Can manage all features
- No bypass permissions - Subject to same limits as players

**Helper Group:**

- `smp.menu` - Can open the settings menu and inspect feature states
- No other permissions

### Audit Bypass Permissions

Regularly review who has bypass permissions:

```
/lp user <playername> permission info
```

Look for any bypass entries that shouldn't be there, such as:

- `smp.itemlimiter.bypass`
- `smp.enderchestlimiter.bypass`
- `smp.dimension.bypass`

### Use Groups, Not Individual Permissions

Always assign permissions through groups rather than directly to players. This makes management easier and more consistent.

---

Next: Explore [Features](./category/features) in detail.
