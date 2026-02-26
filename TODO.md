# Vanilla Core - Feature Implementation Checklist

## Core Features (Must Implement)

### Combat & PvP Balance

- [ ] Damage Modification System
  - [ ] Weapon damage balancing
  - [ ] Armor damage reduction tuning
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
  - [ ] Ban breach swapping (hotbar swap prevention)
- [x] Shield Mechanics
  - [x] Mace stun shield on hit
  - [x] Modify Shield disable duration

### Cooldowns

- [ ] Mace Cooldown
- [ ] Wind Charge Cooldown
- [ ] Riptide Cooldown
- [ ] Gap Cooldown

### Bans & Restrictions

- [x] Effect Ban System
  - [x] Configurable potion effect blacklist
  - [x] Effect removal on application
- [ ] Enchantment Ban System
  - [ ] Config-based enchantment blacklist
  - [ ] Item validation on craft/pickup
- [ ] Tipped Arrow Ban
- [ ] Bed Bombing Prevention
- [ ] TNT Minecart Restriction
- [x] Dimension Management
  - [x] Toggle Nether access
  - [x] Toggle End access

### Mob Control

- [x] Mob Manager
  - [x] Per-world mob spawning control
  - [x] Toggle any mob type on/off
  - [x] Spawn egg bypass for admins
  - [x] WorldGuard region integration
  - [x] Chunk cleanup for disabled mobs
  - [x] GUI configuration

### Custom Mechanics

- [ ] Rituals System
  - [ ] Ritual configuration system
  - [ ] Ritual trigger detection
  - [ ] Particle effect system
  - [ ] Ritual completion rewards
- [ ] One-Craft Recipes
  - [ ] Track player craft history
  - [ ] Prevent duplicate crafting
  - [ ] Database/file storage
- [ ] Warden Heart Drops
  - [ ] Custom item on warden kill
  - [ ] Integration with custom recipes
- [x] Invisibility QOL (Invisible Kills)
  - [x] Anonymous player names when invisible
  - [x] Hidden death messages for invisible killers

### Server Management

- [ ] SMP Start Command
  - [ ] /vanilla start command
  - [ ] Grace period system
  - [ ] Scheduled start time
  - [ ] Pre-start countdown
  - [ ] State persistence

---

## Secondary Features (Other Plugin Alternatives Exist)

### Quality of Life

- [x] One Player Sleep
- [x] Item Limiter System
- [x] Limited Enchantment Slots (first-come-first-serve per-material enchant limits)
- [x] Item Explosion Immunity (Stop items from despawning due to explosions)
- [x] Infinite Restock Toggle
- [x] One Mace (Mace Limiter)
- [ ] Stop Item Despawning (timer-based)
- [ ] First Join Kit System
- [ ] Custom Recipes via Config
- [ ] Pearl Cooldown

### Protection Features

- [ ] Anti-Naked Killing
- [ ] Anti-AFK Killing
- [ ] Ban Killing Villagers

### PvP Toggle

- [ ] Global PvP on/off
- [ ] Command to toggle

### Doomsday Features

- [ ] Spectator Mode on Death
- [ ] Death event handling

### Anti-Cheat & Protection

- [x] Minimap Control (Xaero's)
  - [x] Disable minimap entirely
  - [x] Fair mode (no cave map, no radar)
  - [x] Per-world settings
  - [x] Send on join/world change
  - [x] GUI configuration
- [ ] Anti-Health Indicators (ProtocolLib Required)
- [ ] Built-in Health Indicators (ProtocolLib Required)
- [ ] Anti-Seed Cracking (ProtocolLib Required)

---

## Implemented Features Summary

| Feature | Status | Config Path |
|---------|--------|-------------|
| Custom Anvil Caps | Done | features.custom-anvil-caps |
| Enchantment Replacement | Done | features.enchantment-replacement |
| Enchantment Limiter | Done | features.enchantment-limiter |
| Mace Limiter | Done | features.mace-limiter |
| Nether Lock | Done | features.dimension-lock-nether |
| End Lock | Done | features.dimension-lock-end |
| Netherite Disabler | Done | features.netherite-disabler |
| Invisible Kills | Done | features.invisible-kills |
| Item Explosion Immunity | Done | features.item-explosion-immunity |
| Infinite Restock | Done | features.infinite-restock |
| Item Limiter | Done | features.item-limiter |
| One Player Sleep | Done | features.one-player-sleep |
| Mob Manager | Done | features.mob-manager |
| Minimap Control | Done | features.minimap-control |
| Item Bans | Done | bans.items |
| Effect Bans | Done | bans.effects |

---

## Manager Classes

- [x] BanManager - Item/Effect ban enforcement
- [x] CooldownManager - Unified cooldown storage
- [x] FeatureManager - Dynamic feature loading
- [x] ConfigManager - Config handling
- [x] MenuManager - GUI system
- [x] MenuConfigManager - Menu config
- [x] ChatInputManager - Chat input handling
- [x] CommandManager - Command registration
- [ ] RitualManager - Ritual system (NOT IMPLEMENTED)
- [ ] RecipeManager - Custom recipes (NOT IMPLEMENTED)
- [ ] SMPStateManager - SMP start state (NOT IMPLEMENTED)
