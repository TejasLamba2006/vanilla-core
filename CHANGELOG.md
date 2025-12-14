# Changelog

All notable changes to SMP Core will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Mob Spawning Feature**: Control mob spawning with configurable mob type blacklist
- **Mace Limiter GUI**: New settings GUI accessible via right-click in main menu
  - Increase/decrease max maces with click and shift-click
  - Reset craft count button
  - Visual display of current limit status
- **Verbose Logging**: Optional debug logging for all features (`plugin.verbose` in config)
- **Dynamic Recipe Management**: Mace recipes automatically removed when limit reached

### Fixed

- **Mace Limiter**: Recipes are now properly restored when the feature is disabled
- **Mace Limiter**: Recipes are now automatically disabled when the craft limit is reached
- **Mace Limiter**: Adjusting max maces now properly updates recipe availability
- **Commands**: Added permission checks to `/smp` command - menu now requires `smpcore.menu` permission
- **Commands**: Tab completion now only shows subcommands the player has permission for

### Changed

- Version bump to 1.1.0
- Improved feature toggle system with better state management
- Enhanced reload functionality across all features

---

## [1.0.0] - Initial Release

### Features

#### Combat & PvP

- **Item Limiter**: Limit how many of specific items players can carry (gaps, totems, pearls, etc.)
  - Configurable limits per item type
  - Check methods: on-hit or on-move
- **Mace Limiter**: Server-wide mace crafting limit with broadcast announcements
  - Configurable max maces
  - Title, chat, and sound announcements on craft
- **Netherite Disabler**: Prevent use of netherite gear
  - Configurable per item type (armor, tools, weapons)

#### Enchantments

- **Custom Anvil Caps**: Prevent enchantments from exceeding set levels
  - Block enchanting table overleveling
  - Block anvil overleveling
  - Keep only best protection option
  - Configurable caps per enchantment
- **Enchantment Replacement**: Automatically scan and reduce overpowered enchantments
  - Configurable target levels
  - Blocked enchantments list
  - Item whitelist support

#### Bans System

- **Item Bans**: Ban specific items from being used
  - Mace, anchors, crystals, pearls, netherite items
- **Effect Bans**: Config-driven list of banned potion effects
  - Effect removal on application

#### Dimensions

- **Nether Lock**: Toggle access to The Nether
- **End Lock**: Toggle access to The End

#### Custom Mechanics

- **Invisible Kills**: Hide killer name in death messages when invisible
  - Anonymous player names
  - Hidden death messages
- **Item Explosion Immunity**: Protect dropped items from explosions
- **Infinite Restock**: Villagers never run out of trades
- **One Player Sleep**: Only one player needs to sleep to skip night

#### GUI System

- **Main Menu**: `/smp` opens a paginated GUI showing all features
- **Dynamic Feature Items**: Toggle features with left-click, configure with right-click
- **Menu Pagination**: Automatically handles many features across multiple pages

#### Commands

- `/smp` - Opens the main settings menu
- `/smp menu` - Opens the main settings menu
- `/smp reload` - Reloads all configurations
- `/smp enchant <sub>` - Enchantment management commands
- `/smp mace <sub>` - Mace limiter commands
- `/smp netherite <sub>` - Netherite disabler commands
- `/smp dimension <sub>` - Dimension lock commands

#### Core Systems

- **Feature Manager**: Dynamic feature loading and lifecycle management
- **Ban Manager**: Item/effect ban enforcement
- **Cooldown Manager**: Unified cooldown storage
- **Config Manager**: Config file handling with hot-reload support
- **Menu Manager**: GUI system management
- **Menu Config Manager**: Menu configuration
- **Chat Input Manager**: Chat input handling for configuration
- **Command Manager**: Command registration and handling

#### Technical

- Thread-safe managers using concurrent data structures
- Performance optimized with O(1) feature lookups
- Memory safe with proper cleanup on player quit
- Hot reload support for all configurations
- Verbose logging mode for debugging

---

## Version History

| Version | Release Date | Highlights |
|---------|--------------|------------|
| 1.0.0 | Initial | Full feature set release |
