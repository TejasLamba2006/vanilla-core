# Vanilla Core - Feature Implementation Checklist

Reconciled against actual source (`src/main/java/.../features`, `config.yml`) and the
reference "Cool Menu" showcase screenshot (`1772923464196.webp`). Old entries that
turned out to already be implemented were corrected.

## Version Compatibility

`pom.xml` now targets Paper API `26.2.build.48-alpha` (latest published; no stable
build exists yet for 26.2) — bumped from `1.21.1`, no legacy version support kept.
Required JDK 25 (compiler + shade-plugin 3.6.2 + ProGuard 7.9.1, all bumped to read
Java 25 class files). Two vanilla game drops motivated this and add content this
plugin needs to balance/ban:

- **Mounts of Mayhem** — Java 1.21.11 (2025-12-09). Adds the **Spear** weapon (jab +
  charge attacks, longer reach, per-tier cooldown, craftable/smithing-upgradable to
  netherite), the spear-exclusive **Lunge** enchantment, Nautilus mount + armor,
  netherite horse armor, camel husk, zombie horses spawning naturally.
  [minecraft.net](https://www.minecraft.net/en-us/article/mounts-of-mayhem-official-release-date)
- **Chaos Cubed** — Java 26.2 (2026-06-16). Adds **Sulfur Caves** biome, the
  **Sulfur Cube** mob (slime-like, absorbs dropped blocks into 8 behavior
  archetypes — explosive w/ TNT, damaging w/ magma, etc.), **Potent Sulfur**
  block (underwater gas/nausea), geysers, native Java friends list, closes the
  Hardcore LAN permadeath exploit, experimental Vulkan renderer.
  [minecraft.net](https://www.minecraft.net/en-us/article/play-chaos-cubed-today)

- [x] Bump `minecraft.version` / Paper API dependency to 26.2 (covers both drops) — verified with `mvn clean package` and `mvn clean package -Pobfuscate`
- [x] Fixed one real API break: `Attribute.GENERIC_FLYING_SPEED` was renamed to `Attribute.FLYING_SPEED` (`FasterHappyGhastsListener`) — removed the old reflection fallback since we no longer support pre-rename versions
- [x] Re-check `NetheriteDisablerFeature` covers the new netherite horse armor item — also found and fixed the same gap for Netherite Spear and Netherite Nautilus Armor
- [x] Confirm `MobManagerFeature`'s per-world mob toggle picks up Sulfur Cube, Nautilus, Camel Husk, Zombie Horse automatically — confirmed, it's spawn-egg-enum-driven so no code changes were needed
- [x] Decide whether Sulfur Cube block-absorption archetypes need any anti-exploit handling — added `SulfurCubeGriefBanFeature` to clear block damage from Sulfur Cube explosions (no dedicated archetype-change Bukkit event exists yet, so scoped to the verifiable explosion case)

## Core Features (Must Implement)

### Combat & PvP Balance

- [ ] Mace/Spear Damage Limiter (max-damage cap, e.g. `10/INFINITE`, LMB +1/RMB -1) — distinct from `mace-limiter` (craft-count limiter, already done). Spear now has two attack types (jab, charge) so the limiter needs to cap both, not just base attack damage
- [ ] Lunge enchantment awareness (spear-exclusive, 3 levels) — decide whether it needs a slot in `enchantment-limiter`/enchant-ban like other enchants
- [ ] Global PvP Toggle + custom attack damage/speed override ("Combat System: Enabled", set attack dmg/speed when item in main hand)
- [x] Enchantment Limiters (Protection + Sharpness max-level enforcement)
- [x] Combat Item Bans (Mace, Anchors, Crystals, Pearls, Netherite)
- [x] Ban breach swapping (hotbar swap prevention)
- [x] Shield Mechanics (mace stun on hit, disable duration, sound fix, tick-delay fix, skip vanilla damage tick)
- [ ] Anti-restock in combat
- [ ] Anti-elytra in combat

### Cooldowns

- [x] Generic Item Cooldown system (`features.item-cooldowns`) — config-driven material→seconds map, covers Gapple/Enchanted Gapple/Pearl/Wind Charge/all 7 Spear tiers
- [x] Spear cooldown — added all 7 Spear tiers (Wooden through Netherite, plus Copper) to `item-cooldowns`, reusing the existing generic material-cooldown GUI/config

### Bans & Restrictions

- [x] Effect Ban System (`PotionBansFeature` — config blacklist + removal on application)
- [x] Enchantment Ban System
- [x] Bed Bombing Prevention (`BedBombingBanFeature`)
- [x] TNT Minecart Restriction (`TntMinecartBanFeature`)
- [x] Dimension Management (Nether/End toggle)
- [ ] Tipped Arrow Ban (ban all, or ban specific potion-effect arrows individually)

### Mob Control

- [x] Mob Manager (per-world spawning, spawn-egg bypass, WorldGuard integration, chunk cleanup, GUI)
- [x] Ban Killing Villagers (`VillagerKillingBanFeature`)

### Custom Mechanics

- [x] Rituals System (`RitualFeature` + `RitualManager` + `RitualCommand` — craftable trigger items, color, duration, radius, particles)
- [ ] Custom Recipes via Config (add/remove arbitrary recipes through the GUI)
- [ ] One-Craft Recipes (craft history tracking, prevent duplicate crafts)
- [ ] Golden Heads / Warden Heart Drops (mob-kill reward items, right-click to obtain)
- [ ] Potion Keg / Enchant Keg / Tipped Arrow Keg (bulk brewing/enchanting containers)
- [ ] Flasks (new custom consumable item type)
- [x] Invisibility QOL (`InvisibleKillsFeature` — anonymous names + hidden death messages)

### Server Management

- [ ] SMP Start Command (`/start`, grace period, scheduled start, countdown, state persistence)

---

## Secondary Features

### Quality of Life

- [x] One Player Sleep
- [x] Item Limiter System + `/itemlimit`, `/banitem`
- [x] Limited Enchantment Slots
- [x] Item Explosion Immunity
- [x] Infinite Restock Toggle
- [x] One Mace (craft-count limiter)
- [x] Stop Item Despawning (also covers "immortal death drops")
- [x] First Join Kit System (`KitManager`/`KitCommand`/`KitJoinListener`, `kits.*` config)
- [x] `/invsee <type> <player>` (already in `UtilityCommand`)
- [ ] Easy Recipes (Easy Gaps / Easy Cobwebs toggles)
- [ ] Simple XP Clumps (instant XP orb pickup, no hover delay)
- [ ] String Duper fix toggle (config-gated bug revival switch)

### Protection Features

- [ ] Anti-Naked Killing (+armor bonus when unarmored)
- [ ] Anti-AFK Killing
- [x] Ban Killing Villagers

### Doomsday Features

- [x] Spectator Mode on Death (`SpectatorOnDeathFeature`)

### Anti-Cheat & Protection

- [x] Minimap Control (Xaero's — disable, fair mode, per-world, GUI)
- [ ] Anti-Health Indicators (ProtocolLib required)
- [ ] Built-in Health Indicators (ProtocolLib required)
- [ ] Anti-Seed Cracking (ProtocolLib required)
- [ ] Anti X-Ray + engine mode toggle
- [ ] Anti Admin Abuse (`/setwebhook <url>` — Discord alert on admin action)
- [ ] Anti-Draining (ban draining water buckets mid-fight)
- [ ] Glowing item marker toggle

---

## Implemented Features Summary

| Feature | Status | Config Path |
|---------|--------|-------------|
| Enchantment Limiter | Done | features.enchantment-limiter |
| Mace Limiter (craft count) | Done | features.mace-limiter |
| Nether Lock | Done | features.dimension-lock-nether |
| End Lock | Done | features.dimension-lock-end |
| Netherite Disabler | Done | features.netherite-disabler |
| Invisible Kills | Done | features.invisible-kills |
| Item Explosion Immunity | Done | features.item-explosion-immunity |
| Infinite Restock | Done | features.infinite-restock |
| Item Limiter | Done | features.item-limiter |
| Ender Chest Item Limiter | Done | features.ender-chest-item-limiter |
| One Player Sleep | Done | features.one-player-sleep |
| Mob Manager | Done | features.mob-manager |
| Minimap Control | Done | features.minimap-control |
| Item Bans | Done | bans.items |
| Shield Mechanics | Done | features.shield-mechanics |
| Stop Item Despawn | Done | features.stop-item-despawn |
| Ritual System | Done | features.ritual |
| Breach Swap Ban | Done | features.breach-swap |
| Potion (Effect) Bans | Done | features.potion-bans |
| Bed Bombing Ban | Done | features.bed-bombing-ban |
| TNT Minecart Ban | Done | features.tnt-minecart-ban |
| Villager Killing Ban | Done | features.villager-killing-ban |
| Crystal PvP Ban | Done | features.crystal-pvp-ban |
| Anchor Restriction | Done | features.anchor-restriction |
| Pearl Restriction | Done | features.pearl-restriction |
| Generic Item Cooldowns | Done | features.item-cooldowns |
| Spectator On Death | Done | features.spectator-on-death |
| Faster Happy Ghasts | Done | features.faster-happy-ghasts |
| First Join Kit | Done | kits.* |
| Sulfur Cube Grief Ban | Done | features.sulfur-cube-grief-ban |

---

## Manager Classes

- [x] BanManager - Item/Effect ban enforcement
- [x] CooldownManager - Unified cooldown storage
- [x] FeatureManager - Dynamic feature loading
- [x] ConfigManager - Config handling
- [x] MenuManager / MenuConfigManager - GUI system
- [x] ChatInputManager - Chat input handling
- [x] CommandManager - Command registration
- [x] RitualManager - Ritual system
- [x] KitManager - First-join kit system
- [x] DatabaseManager - SQLite backing store (social)
- [x] CDNManager - Remote kill-switch / maintenance mode
- [ ] RecipeManager - Custom recipes / one-craft items (NOT IMPLEMENTED)
- [ ] SMPStateManager - SMP start state (NOT IMPLEMENTED)
