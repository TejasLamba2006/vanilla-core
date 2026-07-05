# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vanilla Core is a Minecraft Paper/Spigot plugin (26.2+, Java 25+, no legacy version support) for Vanilla survival servers, providing PvP balance tweaks, custom mechanics, and built-in essentials commands (teleport, homes, warps, kits, social) so servers don't need extra plugins. Modrinth slug: `smpwatchdog` (project ID `GH4H8ndx`).

## Build & Run

Requires **JDK 25** on `PATH`/`JAVA_HOME` — the compiler, shade plugin, and ProGuard all need it to read Paper 26.2's class files (major version 69). If the default JDK is older, override for the build:

```bash
JAVA_HOME="/c/Program Files/Java/jdk-25.0.2" PATH="/c/Program Files/Java/jdk-25.0.2/bin:$PATH" mvn clean package              # standard build -> target/vanilla-core-<version>.jar
JAVA_HOME="/c/Program Files/Java/jdk-25.0.2" PATH="/c/Program Files/Java/jdk-25.0.2/bin:$PATH" mvn clean package -Pobfuscate  # release build with ProGuard obfuscation (final jar has -final suffix)
```

There are no automated tests in this repo (no `src/test` sources) — validation is done by loading the jar on a real Paper server. A `paper-test-server/` directory exists locally for manual testing.

## Architecture

### Feature auto-discovery

Every gameplay feature implements `feature.Feature` (usually by extending `feature.BaseFeature`) and lives in the flat package `com.tejaslamba.vanillacore.features`. `manager.FeatureManager.loadFeatures()` scans that package via reflection/jar-entry walking at startup, instantiates every concrete `Feature`, and calls `onEnable`. **To add a new feature, drop a class directly in `features/` implementing `Feature` — no manual registration is needed.** Each feature owns:
- `getConfigPath()` → its `features.<key>` section in `config.yml`, checked for `.enabled`
- `getListener()` → an optional Bukkit `Listener` registered/unregistered automatically on enable/disable/toggle
- `getMenuItem()`/`getMenuLore()`/`onLeftClick`/`onRightClick` → its tile in the `/smp` GUI (default behavior in `BaseFeature` just toggles `.enabled` in config and reloads)

`BaseFeature` also checks `isRemotelyDisabled()` (via `CDNManager`, a kill-switch fetched remotely) and maintenance mode before allowing toggles.

### Listeners vs. Features

Bukkit event listeners live in `listener/` (one `[Feature]Listener` per feature/system) and are either returned by a `Feature.getListener()` or registered directly in `VanillaCorePlugin.onEnable()` for non-feature systems (teleport, kits, social, chat input, menu clicks, update notifications).

### Managers own state and config

Cross-cutting systems are managers under `manager/` (plus a few in their own packages: `database/`, `social/`, `itemlimiter/`, `enchantlimiter/`, `enderchestlimiter/`, `infiniterestock/`, `ritual/`), all instantiated once in `VanillaCorePlugin.onEnable()` and exposed via getters on the plugin instance (`plugin.getXManager()`). Key ones:
- `ConfigManager` — loads/saves `config.yml`; feature enabled-state and all tunables read through `plugin.getConfigManager().get().getBoolean/getString/...`
- `MessageManager` — loads `messages.yml`, parses MiniMessage strings (`MessageManager.parse(...)`) for all player-facing text
- `MenuConfigManager` / `MenuManager` — drive the paginated `/smp` GUI and its click handlers (`listener/menu/`)
- `CommandManager` — registers all commands (in `command/` and `commands/`) and their tab completion
- `CooldownManager` — shared cooldown map with periodic async cleanup
- `DatabaseManager` — SQLite (via `sqlite-jdbc`) backing store, primarily for social features
- `CDNManager` — polls a remote endpoint for per-feature kill-switch / maintenance-mode flags

### Config-driven, hot-reloadable

Every feature is disabled by default and toggled purely through `config.yml`. `/smp reload` re-reads config and calls `reload()` on every feature/manager without a server restart — when adding config keys, wire them through `ConfigManager` rather than caching values at startup only.

## Conventions (from `.github/copilot-instructions.md`)

- **No comments in production code** — use clean, self-documenting names instead.
- New features go in `com.tejaslamba.vanillacore.features`; listeners in `com.tejaslamba.vanillacore.listener`, named `[Feature]Listener`.
- All features must be config-toggleable; document new config keys with comments in `config.yml` itself (comments belong in config, not code).
- Use `ConcurrentHashMap`/thread-safe collections for any shared state; use async scheduling for heavy work.
- **Always update `CHANGELOG.md`** (Keep a Changelog format, `[Unreleased]` section) when making changes.
- **Always update the docs site** (`docs/docs/`, Docusaurus) when adding/changing a feature — it's the single source of truth for details; `MODRINTH.md` should stay minimal and link to the wiki.
- Never hallucinate features in docs/changelog — cross-reference actual source in `src/main/java/com/tejaslamba/vanillacore/`.
