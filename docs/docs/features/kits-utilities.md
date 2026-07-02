---
sidebar_position: 33
---

# Kits And Utilities

SMP Watchdog includes persistent kits and an admin utility command pack.

## Kits

- `/kit <name>`
- `/kits`

Kit configuration:

- `kits.enabled`
- `kits.first-join-kit`
- `kits.definitions.*`

Kit cooldown claims are persisted in `kits-data.yml`.

## First Join Kit

When a player joins for the first time, `kits.first-join-kit` is granted automatically if configured.

## Utility/Admin Commands

- `/fly [player]`
- `/god [player]`
- `/speed <1-10> [player]`
- `/repair [all]`
- `/giveitem <player> <material> [amount]`
- `/workbench`
- `/anvil`
- `/ec [player]`
- `/invsee <player>`
- `/clearinv [player]`

God mode is backed by a damage-cancel listener while enabled.
