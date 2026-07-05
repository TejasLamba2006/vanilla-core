# Vanilla Core

[![Build Status](https://github.com/TejasLamba2006/vanilla-core/actions/workflows/build.yml/badge.svg)](https://github.com/TejasLamba2006/vanilla-core/actions/workflows/build.yml)
[![CodeQL](https://github.com/TejasLamba2006/vanilla-core/actions/workflows/codeql.yml/badge.svg)](https://github.com/TejasLamba2006/vanilla-core/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Modrinth](https://img.shields.io/modrinth/dt/GH4H8ndx?logo=modrinth&label=downloads)](https://modrinth.com/plugin/vanillacorewastaken)
[![Discord](https://img.shields.io/discord/1380092469613432912?logo=discord&label=discord)](https://discord.gg/7fQPG4Grwt)
[![CodeFactor](https://www.codefactor.io/repository/github/tejaslamba2006/vanilla-core/badge)](https://www.codefactor.io/repository/github/tejaslamba2006/vanilla-core)

A Minecraft plugin for Vanilla servers focused on PvP balance, custom mechanics, and server management. Built for Paper/Spigot 26.2+.

> This project is now open source.

---

## 🌟 Features

Vanilla Core is packed with over 25 meticulously designed features to give you total control over your Vanilla survival server. Below is a complete breakdown of what you can tweak and enforce!

### Combat & PvP

- **Item Limiter**: Keep the economy and PvP fair by limiting how many of specific items players can carry (e.g., God Apples, Totems, Ender Pearls).
- **Ender Chest Limiter**: Extend item limits directly to Ender Chests, stopping players from stashing overpowered items.
- **Mace Limiter**: Server-wide limit on how many maces can be crafted, complete with epic title and chat broadcasts!
- **Breach Swap Fix**: Put an end to the "breach swap" exploit. This completely prevents players from instantly swapping between a Breach-enchanted mace and another weapon to bypass combat mechanics.
- **Netherite Disabler**: Want a classic diamond-tier server? Prevent players from upgrading gear to Netherite at the Smithing Table (configurable per item type).
- **Shield Mechanics**: Rebalance shields by adding a configurable stun duration when blocking mace or axe attacks. Customize and toggle stun mechanics for each weapon independently.

### Enchantments

- **Custom Anvil Caps**: Stop players from combining enchantments beyond your server's limits using the anvil or enchanting table.
- **Enchantment Replacement**: Automatically scan players' inventories and smoothly downgrade any over-leveled enchantments down to your configured max levels.
- **Item Whitelist**: Have special admin items? Mark specific items as completely exempt from any enchantment scans or downgrades.

### Bans & Restrictions

- **Item Bans**: Soft-ban items by completely preventing their usage. Works flawlessly for Maces, Respawn Anchors, End Crystals, Ender Pearls, and Netherite items.
- **Potion Effect Bans**: Use our elegant GUI to ban specific potion effects (like Strength II) entirely, or restrict just tier 2 variants.
- **Combat Restrictions**:
  - **Bed Bombing Ban**: Disable explosive bed placements in the Nether and End.
  - **Crystal PvP Ban**: Disable End Crystal placement and explosion damage.
  - **TNT Minecart Ban**: Neutralize TNT Minecarts to stop instant explosion griefing.
  - **Villager Killing Ban**: Stop players from accidentally (or intentionally!) killing villagers.
  - **Anchor Restriction**: Prevent Respawn Anchors from being charged outside of the Nether.
  - **Pearl Restriction**: Temporarily or permanently disable Ender Pearl teleportation.

### Dimensions & Environment

- **Dimension Locks**: Take absolute control over progression by independently locking access to **The Nether** and **The End**.
- **Mob Manager**: Fine-tune mob spawns dynamically per-world. Disable annoying natural spawns while preserving spawners, eggs, and specific spawn events (fully supports WorldGuard bypasses!).

### Custom Mechanics & Quality of Life

- **Ritual Events**: Start cinematic, timed server events (Rituals) using held items. Enjoy epic particle rings, global countdown bossbars, and dramatic lightning/firework finales.
- **Item Cooldowns**: Add GUI-configurable cooldowns for spammy items like Ender Pearls, Wind Charges, and Golden Apples.
- **Server Restarts**: Automate your server restarts with beautiful BossBars, ActionBars, and chat countdown warnings. Supports daily, weekly, or one-time schedules.
- **Minimap Control**: Ensure competitive fairness! Seamlessly integrate with Xaero's Minimap to force disable cave maps, radar, or completely disable the mod for players on your server.
- **Infinite Restock**: Tired of villagers locking their trades? Make villagers restock instantly and prevent prices from inflating due to high demand.
- **Stop Item Despawn**: Don't let your players lose their hard-earned gear! Force items dropped upon death to remain on the ground permanently until retrieved.
- **One Player Sleep**: Only one player needs to sleep to skip the night and bring on the morning.
- **Invisible Kills**: Perfect for assassins—hide the killer's name in death messages if they drink an Invisibility potion.
- **Item Explosion Immunity**: Protect all dropped items on the ground from being obliterated by creepers or TNT.
- **Faster Happy Ghasts**: Speed up newly spawned "happy" ghasts to match specific server mechanics.
- **Spectator On Death**: Give your hardcore or event server a twist by respawning dead players directly into spectator mode.

### Core & Essentials (AirCore Features)

We've integrated the best features from AirCore directly into Vanilla Core, completely eliminating the need for bulky third-party essentials plugins!

- **Teleportation**: Comprehensive TPA system (`/tpa`, `/tpahere`, `/tpaccept`, `/tpdeny`), along with `/spawn`, `/setspawn`, and `/back`.
- **Homes & Warps**: Let players set multiple homes (`/sethome`, `/home`, `/homes`) and admins set server-wide warps (`/setwarp`, `/warp`, `/warps`), complete with beautiful interactive GUIs!
- **Chat & Social**: Private messaging (`/msg`, `/reply`), an elegant `/socialspy` for admins, and the ability to block/unblock players. We also support interactive @mentions and clickable URL formatting!
- **Kits System**: GUI-based kits with configurable cooldowns (`/kit`, `/kits`)—perfect for starter gear or donor perks.
- **Virtual Utilities**: Grant players access to a portable crafting table (`/workbench`), anvil (`/anvil`), or ender chest (`/ec`).
- **Admin Tools**: Everything a moderator needs: `/fly`, `/god`, `/speed`, `/invsee`, `/clearinv`, `/repair`, and `/giveitem`.

### GUI System

- **Main Menu**: Type `/smp` to open a gorgeous, paginated GUI showcasing every single feature.
- **Dynamic Controls**: Left-click to instantly toggle features on or off. Right-click to dive into deep configuration menus.
- **Seamless Pagination**: With dozens of features, the menu automatically paginates so you never get lost.

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/smp` | Opens the main settings menu | - |
| `/smp menu` | Opens the main settings menu | - |
| `/smp reload` | Reloads all configurations | `smp.reload` |
| `/smp enchant <sub>` | Enchantment management | `smp.enchant` |
| `/smp mace <sub>` | Mace limiter commands | `smp.mace` |
| `/smp netherite <sub>` | Netherite disabler commands | `smp.netherite` |
| `/smp ritual <sub>` | Ritual management | `smp.ritual` |
| `/ritual <sub>` | Ritual management shortcut | `smp.ritual` |

### Enchantment Subcommands

- `/smp enchant limit <enchant> <level> [anvil|replacement|both]`
- `/smp enchant unlimit <enchant> [anvil|replacement|both]`
- `/smp enchant block <enchant> [anvil|replacement|both]`
- `/smp enchant unblock <enchant> [anvil|replacement|both]`
- `/smp enchant list`
- `/smp enchant scan`
- `/smp enchant whitelist`

---

## Installation

### For Server Owners

1. Download from [Modrinth](https://modrinth.com/plugin/vanillacorewastaken)
2. Place the JAR in your server's `plugins` folder
3. Restart or reload your server
4. Configure features in your plugin folder `config.yml`
5. Use `/smp reload` to apply changes or `/smp` to use the GUI

### For Developers

```bash
git clone https://github.com/TejasLamba2006/vanilla-core.git
cd vanilla-core

mvn clean package
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

---

## Configuration

All features are disabled by default and fully configurable via `config.yml`.

<details>
<summary>Example: Item Limiter Configuration</summary>

```yaml
features:
  item-limiter:
    enabled: false
    check-method: "on-hit"
    limits:
      golden_apple: 4
      enchanted_golden_apple: 1
      totem_of_undying: 1
      end_crystal: 2
      ender_pearl: 8
```

</details>

<details>
<summary>Example: Enchantment Caps Configuration</summary>

```yaml
features:
  custom-anvil-caps:
    enabled: false
    block-enchanting-table: true
    block-anvil: true
    keep-only-best-protection: true
    caps:
      sharpness: 4
      protection: 3
      power: 4
      unbreaking: 2
```

</details>

<details>
<summary>Example: Mace Limiter Configuration</summary>

```yaml
features:
  mace-limiter:
    enabled: false
    max-maces: 1
    title:
      enabled: true
      title: "§6⚔ MACE CRAFTED ⚔"
      subtitle: "§e{player} §7has crafted mace §e#§6{count}"
    chat:
      enabled: true
      message: "§6{player} §ehas crafted mace #§6{count}§e!"
    sound:
      enabled: true
      sound: "ENTITY_ENDER_DRAGON_GROWL"
```

</details>

---

## Requirements

- Server: Paper or Spigot 26.2+
- Java: 25 or higher

---

## Technical Features

- Thread-Safe: All managers use concurrent data structures
- Performance Optimized: O(1) feature lookups, cached configs, cooldown systems
- Memory Safe: Proper cleanup on player quit, no memory leaks
- Hot Reload: All configs can be reloaded without restart

---

## Contributing

You can help by:

- Reporting bugs using [issue templates](.github/ISSUE_TEMPLATE/bug_report.yml)
- Suggesting features using [feature requests](.github/ISSUE_TEMPLATE/feature_request.yml)
- Submitting pull requests following our [contributing guidelines](CONTRIBUTING.md)
- Improving documentation in the [docs folder](docs/)
- Starring the repository if you find it useful

### Development

```bash
git clone https://github.com/YOUR_USERNAME/vanilla-core.git

git checkout -b feature/amazing-feature

mvn clean package

git commit -m "Add amazing feature"
git push origin feature/amazing-feature
```

Read the full [Contributing Guide](CONTRIBUTING.md) for detailed information.

---

## Issues & Support

- Bug Reports: Use our [bug report template](.github/ISSUE_TEMPLATE/bug_report.yml)
- Feature Requests: Use our [feature request template](.github/ISSUE_TEMPLATE/feature_request.yml)
- Discord: Join our [Discord server](https://discord.gg/7fQPG4Grwt) for quick help
- Documentation: Check the [full documentation](https://vanillacore.tejaslamba.com)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

You are free to use, modify, and distribute this software. See [docs/license.md](docs/docs/license.md) for more information.

