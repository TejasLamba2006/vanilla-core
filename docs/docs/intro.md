---
sidebar_position: 1
slug: /
---

import { LatestVersion, ModrinthStats } from '@site/src/components/ModrinthVersions';

# Introduction

Vanilla Core is an all-in-one Minecraft plugin for SMP servers. It handles PvP balance, custom mechanics, and server management without needing a dozen separate plugins.

<ModrinthStats />

<div style={{textAlign: 'center', margin: '1.5rem 0'}}>
  <img src="/gifs/main-gui.gif" alt="Vanilla Core main GUI demo" style={{maxWidth: '720px', width: '100%', borderRadius: '12px', boxShadow: '0 10px 30px rgba(0,0,0,0.35)'}} />
</div>

## Latest Version

<LatestVersion />

## What Does It Do?

Vanilla Core tackles the common headaches of running an SMP:

- Players hoarding stacks of totems or golden apples
- Enchantments getting out of hand and ruining PvP
- Netherite making diamond gear pointless
- Mace doing way too much damage
- People rushing to the Nether or End before you're ready
- Needing a bunch of plugins just to manage basic stuff

## Features

### Combat Balance

- **Enchantment Limiter** - Cap enchantment levels (like Sharpness 3 max instead of 5)
- **Mace Limiter** - Keep mace damage under control
- **Netherite Disabler** - Block netherite crafting entirely if you want diamond meta back

### Item Control

- **Item Limiter** - Set max quantities per player for any item
- **Item Explosion Immunity** - Stop creepers and TNT from destroying dropped items

### Dimension Management

- **Nether Lock** - Lock or unlock the Nether whenever you want
- **End Lock** - Same deal for the End

:::tip Staged Progression
Use dimension locks to create progression for your server. Keep the Nether locked for week one, then open it up. Save the End for when everyone's ready for a group dragon fight.
:::

### World Management

- **Mob Manager** - Control which mobs spawn in which worlds, with spawn reason filtering
- **One Player Sleep** - Let one person skip the night for everyone

### Quality of Life

- **Infinite Restock** - Villagers never run out of trades
- **Invisible Kills** - Hide death messages when the killer is invisible
- **Minimap Control** - Block or limit minimap mod features

## Requirements

| Requirement | Version | Notes |
|-------------|---------|-------|
| Server | Paper 1.21.1+ | Spigot works but Paper is better |
| Java | 21+ | Required for 1.21.1+ |
| WorldGuard | 7.0+ | Optional - for Mob Manager region bypass |

:::warning Java Version
You need Java 21 or newer. If you're on an older version, upgrade before installing.
:::

## Quick Start

1. Download from [Modrinth](<https://modrinth.com/plugin/Vanilla> Core) or the [Downloads page](/downloads)
2. Drop the JAR in your `plugins` folder
3. Restart the server
4. Run `/smp` to open the settings GUI

Everything is off by default. Just enable what you need.

## Support

Vanilla Core is free to use. If it's helpful, consider [donating via PayPal](https://paypal.me/tejaslamba) to support development.

Need help? [Join the Discord](https://discord.gg/7fQPG4Grwt?utm_source=vanillacore.tejaslamba.com).

---

Ready to install? Head to the [Installation Guide](/docs/installation).
