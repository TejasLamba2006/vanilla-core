# Changelog

All notable changes to Vanilla Core will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.3.0] - 2026-02-13

### Changed

- **🎨 Complete Rebrand: SMP Core → Vanilla Core**
  - **Plugin renamed** from "SMP Core" to "Vanilla Core" across all systems
  - **Command changed** from `/smp` to `/vanilla` (breaking change)
    - New aliases: `vc`, `vpt` (old `smp` alias removed)
    - All subcommands now use `/vanilla` prefix
  - **Permission nodes** remain unchanged (`vanillacore.*`)
  - **Documentation URLs updated**:
    - Docs: <https://vanillacore.tejaslamba.com>
    - Modrinth: <https://modrinth.com/plugin/vanillacorewastaken>
    - GitHub: <https://github.com/TejasLamba2006/vanilla-core>
  - **Automatic data migration**: Existing plugin data automatically migrated from old folder names
    - Supports migration from: `smp-core`, `SMP-Core`, `SMPCore`
    - New folder: `Vanilla Core`
    - Migration runs automatically on first load after update
  - **In-game branding**: All chat messages, GUIs, and prefixes updated
    - Chat prefix: `§8[§6Vanilla Core§8]§r`
    - Menu titles updated throughout
    - Help messages and command usage updated
  - **Configuration files**: All comments and examples updated to reflect new branding
  - **Complete documentation rewrite**: 50+ files updated across docs site
    - All feature guides updated
    - Command reference updated
    - Configuration guide updated
    - GUI reference updated
  - **Developer documentation**: Updated project instructions and video scripts

### Added

- **Backward compatibility**: Plugin automatically migrates old data folder on first run

### Notes

- **‼️ BREAKING CHANGE**: The main command has changed from `/smp` to `/vanilla`
  - Users must update any scripts, command blocks, or documentation
  - Server operators should announce this change to players
  - Old `/smp` command will no longer work
- **No data loss**: All existing configurations, limits, and settings are preserved
- **Permission compatibility**: All permission nodes remain unchanged (`vanillacore.*`)

## [1.2.1] - 2026-01-13

### Changed

- **Open Source Migration**: Project is now fully open source under MIT License
  - Changed from custom restrictive license to MIT License
  - Source code now publicly available on GitHub
  - Community contributions now welcome via pull requests
  - Added comprehensive contributing guidelines (CONTRIBUTING.md)
  - Added Code of Conduct (CODE_OF_CONDUCT.md)
  - Added Security Policy (SECURITY.md)
  - Added Support documentation (SUPPORT.md)
- **GitHub Workflows**: Added professional CI/CD automation
  - Build & test automation on every push/PR
  - CodeQL security scanning
  - Dependency vulnerability review
  - Automatic PR labeling
  - Stale issue/PR management
- **CodeRabbit Integration**: AI-powered code review configured
  - Custom rules for project coding standards
  - Path-based review instructions
  - Enforces "no comments" rule for Java code
- **Issue Templates**: Structured bug reports and feature requests
  - YAML-based bug report template
  - Feature request template with categories
  - Configuration redirects to Discord/documentation
- **Documentation Updates**: Comprehensive open source documentation
  - Updated README with badges, contribution section, build instructions
  - Updated MODRINTH.md to highlight open source nature
  - Created detailed launch checklist for maintainers
  - Enhanced .gitignore for open source development

## [1.2.0] - 2026-01-02

### Added

- **Server Restart Feature**: Complete server restart management with GUI configuration
  - Restart Now: Immediate server restart with confirmation (Shift+Click required)
  - Countdown System: Start restart countdown with configurable duration
  - Cancel Restart: Cancel any active restart countdown
  - Scheduled Restarts: Support for daily, weekly, and one-time scheduled restarts
    - Daily format: `HH:mm:ss` (e.g., "04:00:00" for 4 AM daily)
    - Weekly format: `MON HH:mm:ss` (e.g., "SUN 03:00:00" for Sunday 3 AM)
    - One-time format: `yyyy-MM-dd HH:mm:ss` (e.g., "2024-12-25 06:00:00")
  - Multiple Notification Types: Chat, ActionBar, BossBar, Title (configurable)
  - Customizable Countdown Announcements: Configure which seconds show warnings
  - BossBar Color: Cycle through RED, BLUE, GREEN, YELLOW, PURPLE, PINK, WHITE
  - Pre-Restart Commands: Execute commands before server restart (e.g., save-all)
  - Warning Sounds: Configurable sound on countdown announcements
  - Timezone Support: Set timezone for scheduled restarts
  - Full GUI Management: Configure all settings via right-click menu
  - Schedule Management GUI: Add/remove scheduled restart times
  - Chat Input System: Add custom schedules via chat input
  - Customizable Messages: All messages configurable with placeholders
  - Admin Join Notification: Admins notified when joining during active restart
- **Main Menu Redesign**: Improved GUI layout with quick links and donation support
  - Discord link with custom skull head (bottom row, slot 46)
  - Wiki/Documentation link with custom skull head (bottom row, slot 47)
  - Modrinth link with custom skull head (bottom row, slot 48)
  - Close Menu button (bottom row, slot 49)
  - Donate button with PayPal link (bottom row, slot 50)
  - "More Features Coming Soon" item shown after feature list when space available
  - Donate item shown after coming soon when space available
- **CDN Integration (Plugin-Side)**: Server-side CDN integration for remote management
  - `CDNManager`: Fetches manifest and feature config from CDN with 5-minute cache
  - `UpdateNotificationListener`: Notifies admins/ops on join when updates available
  - Remote feature kill-switch capability via CDN config
  - Maintenance mode detection and warning system
  - Clickable update notifications with download and changelog links
  - Update indicator in main menu (Nether Star) when new version available
- **CDN-Based Config System**: Dynamic configuration system hosted via CDN
  - Features, enchantments, and presets fetched dynamically from CDN
  - Versioned configs at `/cdn/config/{version}/`
  - Manifest file for version discovery
  - Supports feature kill-switch via remote config
  - Future-proofing for remote GUI control
- **Dynamic Config Builder**: Completely rewritten config builder page
  - All data fetched from CDN (no hardcoded arrays)
  - Loading states and error handling
  - Enchantment configuration modal with category tabs
  - Version badge showing latest config version
  - Documentation links per feature
  - Purple theme update
  - Removed all emoji icons for cleaner UI
- **Config Builder URL Advertisement**: Added config builder URL to config.yml header
  - Users directed to <https://vanillacore.tejaslamba.com/config-builder>
- **bStats Integration**: Server metrics tracking via bStats
  - Plugin ID: 28654
  - Automatic server count and usage tracking
  - No configuration required, works out of the box
- **One Player Sleep Messages**: Configurable broadcast messages
  - `sleep-message`: Broadcast when a player starts sleeping (supports {player}, {displayname})
  - `skip-message`: Broadcast when night is skipped
  - Leave empty to disable either message
- **Item Limiter Notification Options**: Customizable feedback when items are removed
  - `notify-player`: Toggle player notifications (default: true)
  - `notify-message`: Customizable message with {item}, {amount}, {limit} placeholders
  - `drop-excess`: Choose to drop items (true) or delete them (false)
- **Minimap Control**: Control Xaero's Minimap/Worldmap features
  - DISABLED mode: Completely disables minimap
  - FAIR mode: Disables cave map and radar for competitive play
  - FULL mode: Allows all minimap features
  - Per-world mode configuration
  - Send settings on player join and world change
  - Full GUI configuration
- **Enchantment Limiter GUI**: Full GUI for configuring enchantment limits
  - Paginated view of all enchantments (vanilla + custom)
  - Left/right click to adjust limits, shift+click for max/ban
  - Middle click to remove limits
  - Supports custom/modded enchantments via Registry.ENCHANTMENT
  - Shows namespace for custom enchants (e.g., `customenchants:lifesteal`)
- **Infinite Restock Villager Blacklist GUI**: Configure excluded professions via GUI
  - Visual grid of all villager professions
  - Toggle professions on/off with click
  - Shows profession-specific icons
- **Mob Manager Spawn Reason Descriptions**: Each spawn reason now has explanatory lore
  - Descriptions explain when each spawn reason triggers
  - Helps admins understand what each setting affects

### Changed

- **DimensionLock Simplified**: `enabled` now directly controls lock state
  - Removed redundant `locked` variable
  - If feature enabled = dimension locked
  - If feature disabled = dimension open
- **MaceLimiter Recipe Fix**: `restoreMaceRecipes()` now only adds mace recipe
  - Previously used `Bukkit.resetRecipes()` which reset ALL recipes
  - Now manually adds only the mace recipe back
- **Netherite Disabler Documentation**: Clarified that it only blocks smithing table
  - Added detailed config comments explaining behavior
  - Notes about permission bypass (`vanillacore.netherite.craft.<item>`)
  - Recommendation to use Item Limiter for complete removal
- **Enchantment Limiter Config Simplified**: Removed unused options
  - Removed: `block-enchanting-table`, `block-anvil`, `check-on-pickup`
  - Removed: `check-on-inventory-click`, `notify-player`, `limit-message`
  - Only `enabled` and `limits` remain

### Removed

- **Custom Anvil Caps Feature**: Completely removed (redundant with Enchantment Limiter)
  - Deleted `CustomAnvilCapsFeature.java`
  - Deleted `CustomAnvilCapsListener.java`
  - Removed `custom-anvil-caps` config section

### Fixed

- **DimensionLockListener NPE**: Fixed potential null pointer exception
  - Null check for `event.getTo().getWorld()` now before verbose logging

## [1.1.0] - 2024-12-27

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
- **Mob Manager Feature Enhancements**:
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
  - Command: `/smp infiniterestock` opens the manager (permission `vanillacore.infiniterestock`)

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
- **Mob Manager GUI**: Main GUI now shows per-world mob configuration
  - Back button returns to world select instead of main menu
- **Mob Spawning → Mob Manager**: Renamed feature for clarity
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
- **Commands**: Added permission checks to `/smp` command - menu now requires `vanillacore.menu` permission
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
| 1.2.1 | 2026-01-13 | Open Source Migration, MIT License, CI/CD Workflows |
| 1.2.0 | 2026-01-02 | Server Restart, Main Menu Redesign, CDN Integration |
| 1.1.0 | 2024-12-27 | GUI overhaul, Mob Manager, Infinite Restock, Item Limiter rewrite |
| 1.0.0 | 2024-12-01 | Initial release |
