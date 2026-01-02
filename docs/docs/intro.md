---
sidebar_position: 1
slug: /
---

import { LatestVersion, ModrinthStats } from '@site/src/components/ModrinthVersions';

# Introduction

SMP Core is a comprehensive Minecraft plugin designed for SMP (Survival Multiplayer) servers, focusing on PvP balance, custom mechanics, and server management features. Instead of installing dozens of plugins, SMP Core provides everything you need in a single, lightweight package.

<ModrinthStats />

<div style={{textAlign: 'center', margin: '1.5rem 0'}}>
  <img src="/gifs/main-gui.gif" alt="SMP Core main GUI demo" style={{maxWidth: '720px', width: '100%', borderRadius: '12px', boxShadow: '0 10px 30px rgba(0,0,0,0.35)'}} />
</div>

## Latest Version

<LatestVersion />

## What Does SMP Core Do?

SMP Core addresses common problems that SMP server owners face:

- Players hoarding too many totems or golden apples
- Overpowered enchantments making PvP unfair
- Netherite gear creating an unbalanced meta
- Mace weapons dealing excessive damage
- Players rushing to the Nether or End before the server is ready
- The need for multiple plugins to manage basic server functions

## Key Features

### Combat Balance

- **Enchantment Limiter** - Cap enchantment levels (for example, limit Sharpness to level 3 instead of 5)
- **Mace Limiter** - Control mace enchantments and damage output
- **Netherite Disabler** - Prevent netherite crafting and usage entirely

### Item Control

- **Item Limiter** - Set maximum quantities per player for any item
- **Item Explosion Immunity** - Protect specific items from being destroyed by explosions

### Dimension Management

- **Nether Lock** - Lock or unlock the Nether dimension with commands
- **End Lock** - Lock or unlock the End dimension with commands

:::tip Staged Progression
Use dimension locks to create a staged progression system. Keep the Nether locked for the first week, then unlock it. Keep the End locked until you're ready for a community dragon fight event.
:::

### World Management

- **Mob Manager** - Control mob spawning per world with spawn reason filtering
- **One Player Sleep** - Allow a single player to skip the night

### Quality of Life

- **Infinite Restock** - Villagers never run out of trades
- **Invisible Kills** - Hide death messages when the killer is invisible
- **Minimap Control** - Block or restrict certain minimap mod features

## Requirements

| Requirement | Version | Notes |
|-------------|---------|-------|
| Server | Paper 1.21.1+ | Spigot works but Paper is recommended |
| Java | 21+ | Required for 1.21.1+ servers |
| WorldGuard | 7.0+ | Optional - enables region bypass for Mob Manager |

:::warning Java Version
SMP Core requires Java 21 or newer. If you're running an older Java version, you must upgrade before installing this plugin.
:::

## Quick Start

1. Download SMP Core from [Modrinth](https://modrinth.com/plugin/smp-core)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Run `/smp` to open the configuration GUI

The plugin generates a default configuration on first run. All features can be enabled or disabled individually.

## Support Development

SMP Core is free to use. If you find it useful, consider [donating via PayPal](https://paypal.me/tejaslamba) to support continued development.

## Getting Help

- [Join our Discord for support](https://discord.gg/7fQPG4Grwt?utm_source=smpcore.tejaslamba.com)

---

Ready to get started? Continue to the [Installation Guide](/docs/installation).
