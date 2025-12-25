# Changelog

All notable changes to SMP Core will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **MessageManager & messages.yml**: Centralized message system for all plugin strings
  - All user-facing messages now configurable in `messages.yml`
  - Supports color codes with `&` prefix
  - Placeholder support with `{placeholder}` format
  - Messages are cached for performance
  - Hot-reload support with `/smp reload`
- **Enchantment Limiter**: Completely rewritten from EnchantLimiter plugin approach
  - Efficient enchantment caching with cross-version name support
  - Handles both item enchantments and enchanted book stored enchants
  - Triggers: enchanting table, anvil, inventory click, item pickup
  - Enchantment name mapping for compatibility (sharpness ↔ damage_all, etc.)
  - Set limit to 0 to completely ban an enchantment
- **ProGuard Obfuscation**: Build-time code obfuscation for distribution protection
  - Run `mvn package -Pobfuscate` to create obfuscated JAR
  - Produces `smp-core-x.x.x-obfuscated.jar` in target folder
  - Mapping file saved to `target/proguard_map.txt` for debugging
- **Item Limiter GUI System**: Complete GUI-based item limit management
  - Main menu with view, add, banned items, and reload options
  - Add item limits by dragging items and setting quantities
  - **Ban Mode toggle**: One-click option to ban items (sets limit to 0)
  - **Banned Items section**: Separate view for items with limit = 0
  - View and edit existing limits with visual item display
  - Delete limits with shift+click
  - Supports custom model data, display names, and potion types
- **Advanced Item Matching**: Item limits now support:
  - Custom model data matching for modded/custom items
  - Display name matching for named items
  - Potion type matching for specific potion variants
- **Real-time Item Enforcement**: Continuous background task checks player inventories
- **Pickup Prevention**: Prevents picking up items that would exceed limits
- **Inventory Click Prevention**: Blocks taking items from containers if it exceeds limits
- **Mob Spawning Feature Enhancements**:
  - **Per-world configuration**: Disable mobs in specific worlds only
  - **World Selection GUI**: New GUI to select which world to configure
  - **Non-spawn-egg entities**: Support for Iron Golem, Snow Golem, Wither, Ender Dragon
  - **Global Settings GUI**: Configure chunk cleanup and WorldGuard bypass
  - **Chunk Cleanup**: Optionally remove disabled mobs when chunks load (WARNING: Destructive!)
  - **WorldGuard Integration**: Optional bypass for mobs in WorldGuard protected regions
- **Mace Limiter GUI**: New settings GUI accessible via right-click in main menu
  - Increase/decrease max maces with click and shift-click
  - Reset craft count button
  - Visual display of current limit status
- **Verbose Logging**: Optional debug logging for all features (`plugin.verbose` in config)
- **Dynamic Recipe Management**: Mace recipes automatically removed when limit reached
- **Infinite Restock GUI**: Full GUI-based control for villager trading
  - Set max trades per villager (0 = unlimited)
  - Toggle price penalty removal (demand = 0)
  - Toggle applying to wandering traders
  - Uninstall mode to restore original trade limits
  - Command: `/smp infiniterestock` opens the manager (permission `smpcore.infiniterestock`)

### Changed

- **Enchantment Replacement → Enchantment Limiter**: Renamed and completely rewritten
  - Now uses efficient caching system from EnchantLimiter plugin
  - Cross-version enchantment name mapping
  - Simplified event handling with direct item modification
  - Separate config from Custom Anvil Caps
- **Custom Anvil Caps Listener**: Simplified implementation
  - Removed dependency on EnchantmentUtils for merging
  - Direct enchantment capping at anvil/enchanting table
  - Better protection enchantment filtering
- **Item Limiter Rewrite**: Completely rewritten item limiting system
  - New `ItemLimiterManager` for centralized limit management
  - New `ItemLimit` class for advanced item matching
  - Removed check-method config (now uses continuous background checking)
  - Added proper listener unregistration when feature is disabled
- **Config Default**: Verbose logging now defaults to `false`
- Version bump to 1.1.0
- Improved feature toggle system with better state management
- Enhanced reload functionality across all features
- **Infinite Restock Rewrite**: Ported from InstantRestock with persistence
  - Back up original trade limits in PDC and restore on uninstall
  - Apply on interact, merchant open, and trade acquisition
- **Mob Spawning GUI**: Main GUI now shows per-world mob configuration
  - Back button returns to world select instead of main menu
- **Dependencies**: Removed unused ProtocolLib soft-dependency, added WorldGuard soft-dependency

### Removed

- **Enchantment Replacement Feature**: Replaced by Enchantment Limiter
  - Removed `EnchantmentReplacementFeature.java`
  - Removed `EnchantmentReplacementListener.java`
- **Item Bans Feature**: Removed separate item ban system (use Item Limiter with limit=0 instead)
- **Effect Bans Feature**: Removed potion effect ban system (to be reimplemented later)
- **BanManager**: Removed as item/effect bans are now handled by Item Limiter
- **ProtocolLib Dependency**: Removed unused soft-dependency on ProtocolLib

### Fixed

- **Feature Unload**: Listeners are now properly unregistered when features are disabled
- **Memory Management**: Proper cleanup of sessions and caches on feature disable
- **Mace Limiter**: Recipes are now properly restored when the feature is disabled
- **Mace Limiter**: Recipes are now automatically disabled when the craft limit is reached
- **Mace Limiter**: Adjusting max maces now properly updates recipe availability
- **Commands**: Added permission checks to `/smp` command - menu now requires `smpcore.menu` permission
- **Commands**: Tab completion now only shows subcommands the player has permission for
- **Villager Trades**: Consistent application after trade clicks and profession upgrades

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
