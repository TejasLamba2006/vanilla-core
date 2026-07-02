---
sidebar_position: 32
---

# Homes And Warps

SMP Watchdog supports persistent homes and warps with GUI access.

## Homes

- `/sethome <name>`
- `/home [name]`
- `/delhome <name>`
- `/homes [player]`

Homes are persisted in `homes.yml` and survive restarts.

### Home Limit

Configure max homes with:

- `teleport.homes.max-homes`

## Warps

- `/setwarp <name>`
- `/warp [name]`
- `/delwarp <name>`
- `/warps`

Warps are persisted in `warps.yml` and can be accessed through the warps GUI.

## Back Persistence

Back locations are persisted in `teleports.yml` and used by `/back`.
