---
sidebar_position: 4
---

# Permissions

Vanilla Core uses a hierarchical permission system. Most management and bypass permissions default to OP, while selected social/player-preference permissions default to true.

Primary namespace:

- `smp.*`

## Permission Defaults

By default, Vanilla Core uses these permission levels:

- **OP Players** - Full access to all commands and bypass permissions
- **Regular Players** - Access to permissions that default to `true` (for example `smp.msg`, `smp.reply`, and social toggle/block commands)

To change this behavior, use a permissions plugin like LuckPerms.

## Admin Permissions

These permissions control access to the main plugin functions:

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.admin` | Full access to all Vanilla Core features and GUI | OP |
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
| `smp.msg` | Send private messages | true |
| `smp.reply` | Reply to private messages | true |
| `smp.socialspy` | Use social spy | OP |
| `smp.announcements` | Manage announcements engine | OP |
| `smp.tpa` | Request teleport to players | true |
| `smp.tpahere` | Request players teleport to you | true |
| `smp.tpaccept` | Accept teleport requests | true |
| `smp.tpdeny` | Deny teleport requests | true |
| `smp.spawn` | Teleport to spawn | true |
| `smp.setspawn` | Set spawn location | OP |
| `smp.back` | Teleport back to previous location | true |
| `smp.tp` | Use direct teleport command | OP |
| `smp.tphere` | Teleport player to your location | OP |
| `smp.home` | Use home command | true |
| `smp.sethome` | Set homes | true |
| `smp.delhome` | Delete homes | true |
| `smp.homes` | Open homes GUI | true |
| `smp.homes.target` | Open another player's homes GUI | OP |
| `smp.warp` | Use warp command | true |
| `smp.setwarp` | Set warps | OP |
| `smp.delwarp` | Delete warps | OP |
| `smp.warps` | Open warps GUI | true |
| `smp.kit` | Claim kits | true |
| `smp.kits` | List kits | true |
| `smp.fly` | Toggle own flight | OP |
| `smp.fly.others` | Toggle others' flight | OP |
| `smp.god` | Toggle own god mode | OP |
| `smp.god.others` | Toggle others' god mode | OP |
| `smp.speed` | Set own speed | OP |
| `smp.speed.others` | Set others' speed | OP |
| `smp.repair` | Repair held item | OP |
| `smp.repair.all` | Repair all inventory items | OP |
| `smp.giveitem` | Give item utility command | OP |
| `smp.workbench` | Open virtual workbench | true |
| `smp.anvil` | Open virtual anvil | true |
| `smp.ec` | Open own ender chest | OP |
| `smp.ec.others` | Open others' ender chest | OP |
| `smp.invsee` | Open target inventory | OP |
| `smp.clearinv` | Clear own inventory | OP |
| `smp.clearinv.others` | Clear others' inventory | OP |

## Command Permissions

### Main Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.menu` | Use `/smp` and `/smp menu` | OP |
| `smp.reload` | Use `/smp reload` and `/smp reload <module>` | OP |
| `smp.version` | Use `/smp version` | OP |
| `smp.ritual` | Use `/ritual` and `/smp ritual` | OP |
| `smp.ritual.cancel` | Cancel active ritual events | OP |
| `smp.msg` | Use `/smp msg` or `/msg` | true |
| `smp.reply` | Use `/smp reply` or `/reply` | true |
| `smp.socialspy` | Use `/smp socialspy` or `/socialspy` | OP |
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

### Teleport Commands

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.tpa` | Use `/tpa` | true |
| `smp.tpahere` | Use `/tpahere` | true |
| `smp.tpaccept` | Use `/tpaccept` | true |
| `smp.tpdeny` | Use `/tpdeny` | true |
| `smp.spawn` | Use `/spawn` | true |
| `smp.setspawn` | Use `/setspawn` | OP |
| `smp.back` | Use `/back` | true |
| `smp.tp` | Use `/tp` | OP |
| `smp.tphere` | Use `/tphere` | OP |

### Homes And Warps

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.home` | Use `/home` | true |
| `smp.sethome` | Use `/sethome` | true |
| `smp.delhome` | Use `/delhome` | true |
| `smp.homes` | Use `/homes` | true |
| `smp.homes.target` | Use `/homes <player>` | OP |
| `smp.warp` | Use `/warp` | true |
| `smp.setwarp` | Use `/setwarp` | OP |
| `smp.delwarp` | Use `/delwarp` | OP |
| `smp.warps` | Use `/warps` | true |

### Kits And Utility

| Permission | Description | Default |
|------------|-------------|---------|
| `smp.kit` | Use `/kit` | true |
| `smp.kits` | Use `/kits` | true |
| `smp.fly` | Use `/fly` | OP |
| `smp.fly.others` | Use `/fly <player>` | OP |
| `smp.god` | Use `/god` | OP |
| `smp.god.others` | Use `/god <player>` | OP |
| `smp.speed` | Use `/speed` | OP |
| `smp.speed.others` | Use `/speed <value> <player>` | OP |
| `smp.repair` | Use `/repair` | OP |
| `smp.repair.all` | Use `/repair all` | OP |
| `smp.giveitem` | Use `/giveitem` | OP |
| `smp.workbench` | Use `/workbench` | true |
| `smp.anvil` | Use `/anvil` | true |
| `smp.ec` | Use `/ec` | OP |
| `smp.ec.others` | Use `/ec <player>` | OP |
| `smp.invsee` | Use `/invsee` | OP |
| `smp.clearinv` | Use `/clearinv` | OP |
| `smp.clearinv.others` | Use `/clearinv <player>` | OP |

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
| `smp.*` | All Vanilla Core permissions |
| `smp.netherite.craft.*` | Bypass all netherite craft restrictions |

## LuckPerms Configuration Examples

### Full Staff Access

Give staff members complete access to manage Vanilla Core:

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
