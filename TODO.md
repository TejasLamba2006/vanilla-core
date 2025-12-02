# SMP Core - Feature Implementation Checklist

## Core Combat & PvP Balance

- [x] Enchantment Limiters
  - [x] Protection limiter (max level enforcement)
  - [x] Sharpness limiter (max level enforcement)
- [x] Combat Item Bans
  - [x] Ban Mace
  - [x] Ban Anchors
  - [x] Ban Crystals
  - [x] Ban Pearls
  - [x] Ban Netherite items
- [ ] Combat Restrictions
  - [ ] Anti-restock in combat
  - [ ] Anti-elytra in combat
  - [ ] Visual Combat tag system (Just visual)
  - [ ] Ban breach swapping (hotbar swap prevention)
- [ ] Shield Mechanics
  - [ ] Mace stun shield on hit
  - [ ] Configurable shield disable duration
- [ ] Damage Caps
  - [ ] Mace damage cap
  - [ ] Explosion damage cap

## Bans & Restrictions

- [ ] Effect Ban System
  - [ ] Configurable potion effect blacklist
  - [ ] Effect removal on application
- [ ] Enchantment Ban System
  - [ ] Config-based enchantment blacklist
  - [ ] Item validation on craft/pickup
- [ ] Item-Specific Bans
  - [ ] Tipped arrow ban
  - [ ] Bed bombing prevention
  - [ ] TNT minecart restriction
  - [ ] Ban killing villagers
- [x] Dimension Management
  - [x] Toggle Nether access
  - [x] Toggle End access

## Custom Mechanics

- [ ] Rituals System
  - [ ] Ritual configuration system
  - [ ] Ritual trigger detection
  - [ ] Particle effect system
  - [ ] Ritual completion rewards
  - [ ] Config file for ritual definitions
- [ ] One-Craft Recipes
  - [ ] Track player craft history
  - [ ] Prevent duplicate crafting
  - [ ] Database/file storage
  - [ ] Config for one-craft items
- [ ] Warden Heart Drops
  - [ ] Custom item on warden kill
  - [ ] Integration with custom recipes
- [ ] Custom Shulker Box Recipes
  - [ ] Recipe definitions
  - [ ] Recipe registration
- [x] Invisibility QOL (Invisible Kills)
  - [x] Anonymous player names when invisible
  - [x] Hidden death messages for invisible killers

## Server Management

- [ ] SMP Start Command
  - [ ] `/smp start` command
  - [ ] Grace period system
  - [ ] Scheduled start time
  - [ ] Pre-start countdown
  - [ ] State persistence
- [ ] PvP Toggle System
  - [ ] Global PvP on/off
  - [ ] Command to toggle
  - [ ] Broadcast on change
- [ ] Player Protection
  - [ ] Anti-naked killing (no armor check)
  - [ ] Anti-AFK killing
- [x] Quality of Life
  - [x] One player sleep
  - [x] Infinite restock toggle
  - [ ] First join kit system
  - [ ] Stop item despawning
- [ ] Doomsday Features
  - [ ] Spectator mode on death toggle
  - [ ] Death event handling

## Anti-Cheat & Protection (ProtocolLib Required)

- [ ] Anti-Health Indicators
  - [ ] ProtocolLib integration
  - [ ] Health packet modification
- [ ] Anti-Seed Cracking
  - [ ] ProtocolLib integration
  - [ ] Seed data packet blocking
- [ ] Anti-Xaero Minimap
  - [ ] Minimap feature blocking

## Quality of Life Features

- [x] Item Limiter System
  - [x] Configurable item limits
  - [x] Per-item type limits
  - [x] Excess item handling (drop with pickup delay)
  - [x] Check method (on-hit / on-move)
- [x] Item Explosion Immunity
  - [x] Protect dropped items from explosions
- [ ] Custom Recipes
  - [ ] Recipe config system
  - [ ] Recipe registration
  - [ ] Shaped and shapeless support

## Commands

- [x] Main Command (`/smp`)
  - [x] Command registration
  - [x] Permission system
  - [x] Tab completion
- [x] Subcommands
  - [x] `/smp reload` - Reload configs
  - [ ] `/smp start` - Start SMP event
  - [ ] `/smp toggle <feature>` - Toggle features
  - [ ] `/smp ritual <action>` - Ritual management
  - [ ] `/smp kit` - Give first join kit

## Manager Classes

- [x] CombatManager
  - [x] Combat tag tracking
- [ ] RitualManager
  - [ ] Ritual state tracking
  - [ ] Ritual progression
  - [ ] Ritual config loading
- [x] BanManager
  - [x] Item ban enforcement
  - [ ] Enchantment ban enforcement
  - [ ] Effect ban enforcement
  - [x] Cache banned items/effects/enchants
- [x] CooldownManager
  - [x] Unified cooldown storage
  - [x] Cooldown checking
  - [x] Cooldown expiry
  - [x] Per-player cooldown maps
- [ ] RecipeManager
  - [ ] Custom recipe registration
  - [ ] One-craft recipe tracking
  - [ ] Recipe validation
- [ ] SMPStateManager
  - [ ] SMP start state
  - [ ] Grace period tracking
  - [ ] Event scheduling
- [x] FeatureManager
  - [x] Dynamic feature loading
  - [x] Feature toggle system
  - [x] Feature reload support

## Configuration Files

- [x] config.yml
  - [x] Feature toggles
  - [x] Combat settings
  - [x] Cooldown values
  - [x] Ban lists
  - [x] Comprehensive comments
- [ ] rituals.yml
  - [ ] Ritual definitions
  - [ ] Particle configurations
  - [ ] Rewards configuration
- [ ] recipes.yml
  - [ ] Custom recipe definitions
  - [ ] One-craft recipe list
- [ ] messages.yml
  - [ ] All player messages
  - [ ] Color code support
  - [ ] Placeholder support

## Event Listeners

- [ ] CombatListener
  - [ ] Combat tag on hit
  - [ ] Shield mechanics
- [x] ItemListener
  - [x] Item ban enforcement
  - [x] Item pickup validation
  - [x] Item craft validation
- [ ] PlayerListener
  - [ ] Join events (first kit)
  - [ ] Quit events (cleanup)
  - [ ] Death events (spectator)
- [x] InfiniteRestockListener
  - [x] Unlimited villager trades
  - [x] Price normalization
- [x] InvisibleKillsListener
  - [x] Hidden killer name in death messages
- [x] ItemExplosionImmunityListener
  - [x] Protect items from explosions
- [x] ItemLimiterListener
  - [x] Item quantity enforcement
  - [x] Excess item dropping
- [ ] RitualListener
  - [ ] Ritual trigger detection
  - [ ] Ritual progression
  - [ ] Ritual completion
- [ ] RestockListener
  - [ ] Anti-restock in combat
  - [ ] Inventory change tracking
- [ ] ProtectionListener
  - [ ] Naked killing prevention
  - [ ] AFK killing prevention
  - [ ] Villager killing prevention
- [x] DimensionListener
  - [x] Portal usage blocking
  - [x] Dimension teleport cancellation

## GUI System

- [x] Main Menu
  - [x] Feature toggle buttons
  - [x] Pagination support (28 items per page)
  - [x] Dynamic slot layout
- [x] Feature-specific GUIs
  - [x] Mace Limiter settings GUI

## Database/Storage

- [ ] Player Data Storage
  - [ ] One-craft recipe tracking
  - [ ] First join tracking
  - [ ] Cooldown persistence (optional)

## Dependencies & Integration

- [ ] ProtocolLib Soft Dependency
  - [ ] Optional loading
  - [ ] Feature detection
  - [ ] Graceful degradation

## Testing & Polish

- [ ] Config Validation
  - [ ] Invalid value handling
  - [ ] Default value fallbacks
- [ ] Performance Testing
  - [ ] Combat system load testing
  - [ ] Cooldown system stress testing
  - [ ] Memory leak checks
- [x] Documentation
  - [x] Config comments
  - [ ] Command usage
  - [ ] Permission list
  - [ ] Feature descriptions

## Build & Release

- [x] Maven Configuration
  - [x] Proper shading
  - [x] Resource filtering
  - [x] Version management
- [x] plugin.yml
  - [x] All commands registered
  - [x] All permissions defined
  - [x] Dependency declarations
- [ ] Final Testing
  - [ ] All features functional
  - [ ] No console errors
  - [ ] Reload works correctly
