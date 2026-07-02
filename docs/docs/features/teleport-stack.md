---
sidebar_position: 31
---

# Teleport Stack

Vanilla Core now includes a full teleport workflow with request-based teleports, direct teleports, spawn, and back support.

## Commands

- `/tpa <player>`
- `/tpahere <player>`
- `/tpaccept`
- `/tpdeny`
- `/spawn`
- `/setspawn`
- `/back`
- `/tp <player> [target]`
- `/tphere <player>`

## Safety

Teleport safety is controlled by:

- `teleport.safety.enabled`

When enabled, teleports attempt to place players in a safe location and fall back to safer coordinates if needed.

## Request Timeout

Teleport request timeout is configured by:

- `teleport.request-timeout-seconds`
