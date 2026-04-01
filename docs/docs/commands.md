---
sidebar_position: 3
---

# Commands

This page documents all commands available in SMP Watchdog.

## Primary Command

`/smp` is the primary command for plugin management.

## Main Subcommands

| Command | Description | Permission |
|---------|-------------|------------|
| `/smp` | Opens the main configuration GUI | `smp.menu` |
| `/smp menu` | Opens the main configuration GUI | `smp.menu` |
| `/smp reload` | Reloads all modules (`config`, `messages`, `menus`, `features`) | `smp.reload` |
| `/smp reload <module>` | Reloads one module: `all`, `config`, `messages`, `menus`, or `features` | `smp.reload` |
| `/smp version` | Shows the running plugin version | `smp.version` |
| `/smp enchant <subcommand>` | Enchantment limiter management | `smp.enchant` |
| `/smp mace <subcommand>` | Mace limiter management | `smp.mace` |
| `/smp netherite <subcommand>` | Netherite disabler management | `smp.netherite` |
| `/smp infiniterestock` | Opens Infinite Restock GUI | `smp.infiniterestock` |
| `/smp ritual <subcommand>` | Ritual management | `smp.ritual` |
| `/smp msg <player> <message>` | Send private message | `smp.msg` |
| `/smp reply <message>` | Reply to last private message | `smp.reply` |
| `/smp socialspy` | Toggle social spy mode | `smp.socialspy` |
| `/smp togglechat` | Toggle own chat visibility | `smp.toggle.chat` |
| `/smp togglepm` | Toggle own private messages | `smp.toggle.pm` |
| `/smp togglementions` | Toggle mention notifications | `smp.toggle.mentions` |
| `/smp block <player>` | Block a player | `smp.block` |
| `/smp unblock <player>` | Unblock a player | `smp.unblock` |
| `/smp blocked` | List blocked players | `smp.blocklist` |
| `/smp announcements reload` | Reload announcements engine | `smp.announcements` |

## Ritual Command

| Command | Description | Permission |
|---------|-------------|------------|
| `/ritual start [time] [color]` | Starts a ritual with optional duration and particle color overrides | `smp.ritual` |
| `/ritual status` | Shows currently active ritual status | `smp.ritual` |
| `/ritual cancel` | Cancels the active ritual | `smp.ritual.cancel` |

## Dimension Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/nether [open|close|status]` | Manage Nether lock state | `smp.dimension.nether` |
| `/end [open|close|status]` | Manage End lock state | `smp.dimension.end` |

## Reload Behavior

When you run `/smp reload`:

1. `config` is reloaded and verbose logging is refreshed.
2. `messages.yml` is reloaded.
3. Menu config and menu instances are rebuilt.
4. Every loaded feature receives `reload()`.
5. A summary is shown with success/failure counts and elapsed time.

When you run `/smp reload <module>`:

1. Only that module is reloaded.
2. Per-module timing feedback is shown.
3. Failures are reported for that module.

## Notes

- Primary permission namespace is `smp.*`.

---

Next: Learn about [Permissions](./permissions).
