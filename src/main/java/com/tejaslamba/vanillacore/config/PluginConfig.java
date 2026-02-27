package com.tejaslamba.vanillacore.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public record PluginConfig(
        boolean verbose,
        String prefix,
        FeaturesConfig features) {

    public record FeaturesConfig(
            boolean enchantmentLimiterEnabled,
            MaceLimiterConfig maceLimiter,
            DimensionLockConfig endLock,
            DimensionLockConfig netherLock,
            NetheriteDisablerConfig netheriteDisabler,
            InvisibleKillsConfig invisibleKills,
            boolean itemExplosionImmunityEnabled,
            InfiniteRestockConfig infiniteRestock,
            ItemLimiterConfig itemLimiter,
            OnePlayerSleepConfig onePlayerSleep,
            MobManagerConfig mobManager,
            MinimapControlConfig minimapControl,
            ServerRestartConfig serverRestart) {
    }

    public record MaceLimiterConfig(boolean enabled, int maxMaces, int macesCrafted) {
    }

    public record DimensionLockConfig(boolean enabled, boolean locked, String lockedMessage) {
    }

    public record NetheriteDisablerConfig(boolean enabled) {
    }

    public record InvisibleKillsConfig(boolean enabled, String deathMessage) {
    }

    public record InfiniteRestockConfig(
            boolean enabled,
            int maxTrades,
            boolean disablePricePenalty,
            boolean allowWanderingTraders) {
    }

    public record ItemLimiterConfig(
            boolean enabled,
            boolean notifyPlayer,
            String notifyMessage,
            boolean dropExcess,
            String checkMethod) {
    }

    public record OnePlayerSleepConfig(boolean enabled, String sleepMessage, String skipMessage) {
    }

    public record MobManagerConfig(
            boolean enabled,
            boolean chunkCleanupEnabled,
            boolean worldGuardBypass,
            List<String> allowedSpawnReasons) {
    }

    public record MinimapControlConfig(
            boolean enabled,
            String globalMode,
            boolean netherFairMode,
            boolean sendOnJoin,
            boolean sendOnWorldChange) {
    }

    public record ServerRestartConfig(boolean enabled, int countdownTime) {
    }

    public static PluginConfig from(FileConfiguration config) {
        return new PluginConfig(
                config.getBoolean("plugin.verbose", false),
                config.getString("plugin.prefix", "<dark_gray>[<gold>Vanilla Core<dark_gray>]<reset>"),
                loadFeatures(config));
    }

    private static FeaturesConfig loadFeatures(FileConfiguration config) {
        return new FeaturesConfig(
                config.getBoolean("features.enchantment-limiter.enabled", false),
                new MaceLimiterConfig(
                        config.getBoolean("features.mace-limiter.enabled", false),
                        config.getInt("features.mace-limiter.max-maces", 1),
                        config.getInt("features.mace-limiter.maces-crafted", 0)),
                new DimensionLockConfig(
                        config.getBoolean("features.dimension-lock-end.enabled", false),
                        config.getBoolean("features.dimension-lock-end.locked", false),
                        config.getString("features.dimension-lock-end.locked-message", "The End is currently locked!")),
                new DimensionLockConfig(
                        config.getBoolean("features.dimension-lock-nether.enabled", false),
                        config.getBoolean("features.dimension-lock-nether.locked", false),
                        config.getString("features.dimension-lock-nether.locked-message",
                                "The Nether is currently locked!")),
                new NetheriteDisablerConfig(
                        config.getBoolean("features.netherite-disabler.enabled", false)),
                new InvisibleKillsConfig(
                        config.getBoolean("features.invisible-kills.enabled", false),
                        config.getString("features.invisible-kills.death-message", "{victim} was killed by ?????????")),
                config.getBoolean("features.item-explosion-immunity.enabled", false),
                new InfiniteRestockConfig(
                        config.getBoolean("features.infinite-restock.enabled", false),
                        config.getInt("features.infinite-restock.max-trades", 0),
                        config.getBoolean("features.infinite-restock.disable-price-penalty", true),
                        config.getBoolean("features.infinite-restock.allow-wandering-traders", true)),
                new ItemLimiterConfig(
                        config.getBoolean("features.item-limiter.enabled", false),
                        config.getBoolean("features.item-limiter.notify-player", true),
                        config.getString("features.item-limiter.notify-message",
                                "<red>[Vanilla Core] <gray>Excess items removed: <yellow>{item} x{amount} <gray>(limit: {limit})"),
                        config.getBoolean("features.item-limiter.drop-excess", true),
                        config.getString("features.item-limiter.check-method", "on-hit")),
                new OnePlayerSleepConfig(
                        config.getBoolean("features.one-player-sleep.enabled", false),
                        config.getString("features.one-player-sleep.sleep-message", ""),
                        config.getString("features.one-player-sleep.skip-message", "")),
                new MobManagerConfig(
                        config.getBoolean("features.mob-manager.enabled", false),
                        config.getBoolean("features.mob-manager.chunk-cleanup-enabled", false),
                        config.getBoolean("features.mob-manager.worldguard-bypass", true),
                        config.getStringList("features.mob-manager.allowed-spawn-reasons")),
                new MinimapControlConfig(
                        config.getBoolean("features.minimap-control.enabled", false),
                        config.getString("features.minimap-control.global-mode", "FAIR"),
                        config.getBoolean("features.minimap-control.nether-fair-mode", true),
                        config.getBoolean("features.minimap-control.send-on-join", true),
                        config.getBoolean("features.minimap-control.send-on-world-change", true)),
                new ServerRestartConfig(
                        config.getBoolean("features.server-restart.enabled", false),
                        config.getInt("features.server-restart.countdown-time", 60)));
    }
}
