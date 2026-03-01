package com.tejaslamba.vanillacore.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private static final int CURRENT_CONFIG_VERSION = 2;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        migrateConfig();
    }

    private void migrateConfig() {
        FileConfiguration config = plugin.getConfig();
        int configVersion = config.getInt("config-version", 0);
        boolean verbose = config.getBoolean("plugin.verbose", false);

        if (configVersion == CURRENT_CONFIG_VERSION) {
            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Config is up to date (version " + CURRENT_CONFIG_VERSION + ")");
            }
            return;
        }

        plugin.getLogger().info("Migrating config from version " + configVersion + " to " + CURRENT_CONFIG_VERSION);

        Map<String, Object> defaults = getDefaultValues();
        boolean modified = false;

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                modified = true;
                if (verbose) {
                    plugin.getLogger()
                            .info("[VERBOSE] Added missing config key: " + entry.getKey() + " = " + entry.getValue());
                }
            }
        }

        config.set("config-version", CURRENT_CONFIG_VERSION);
        modified = true;

        if (modified) {
            save();
            plugin.getLogger().info("Config migration completed. Added " + defaults.size() + " missing entries.");
        }
    }

    private Map<String, Object> getDefaultValues() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("config-version", CURRENT_CONFIG_VERSION);
        defaults.put("plugin.name", "Vanilla Core");
        defaults.put("plugin.prefix", "<dark_gray>[<gold>Vanilla Core<dark_gray>]<reset>");
        defaults.put("plugin.verbose", false);

        String[] features = {

        };

        for (String feature : features) {
            defaults.put("features." + feature + ".enabled", false);
        }

        defaults.put("features.enchantment-limiter.enabled", false);

        defaults.put("features.mace-limiter.enabled", false);
        defaults.put("features.mace-limiter.mace-crafted", false);
        defaults.put("features.mace-limiter.broadcast-enabled", true);
        defaults.put("features.mace-limiter.broadcast-message",
                "<gold>{player} <yellow>has crafted the only Mace on the server!");

        defaults.put("features.dimension-lock-end.enabled", false);
        defaults.put("features.dimension-lock-end.locked", false);
        defaults.put("features.dimension-lock-end.locked-message", "<red>The End is currently locked!");

        defaults.put("features.dimension-lock-nether.enabled", false);
        defaults.put("features.dimension-lock-nether.locked", false);
        defaults.put("features.dimension-lock-nether.locked-message", "<red>The Nether is currently locked!");

        defaults.put("features.netherite-disabler.enabled", false);
        defaults.put("features.netherite-disabler.disabled-items.sword", true);
        defaults.put("features.netherite-disabler.disabled-items.axe", true);
        defaults.put("features.netherite-disabler.disabled-items.pickaxe", true);
        defaults.put("features.netherite-disabler.disabled-items.shovel", true);
        defaults.put("features.netherite-disabler.disabled-items.hoe", true);
        defaults.put("features.netherite-disabler.disabled-items.helmet", true);
        defaults.put("features.netherite-disabler.disabled-items.chestplate", true);
        defaults.put("features.netherite-disabler.disabled-items.leggings", true);
        defaults.put("features.netherite-disabler.disabled-items.boots", true);

        defaults.put("features.invisible-kills.enabled", false);
        defaults.put("features.invisible-kills.death-message", "{victim} was killed by <obfuscated>?????????");

        defaults.put("features.item-explosion-immunity.enabled", false);

        defaults.put("features.infinite-restock.enabled", false);
        defaults.put("features.infinite-restock.max-trades", 0);
        defaults.put("features.infinite-restock.disable-price-penalty", true);
        defaults.put("features.infinite-restock.allow-wandering-traders", true);
        defaults.put("features.infinite-restock.uninstall-mode", false);
        defaults.put("features.infinite-restock.villager-blacklist", new java.util.ArrayList<String>());

        defaults.put("features.item-limiter.enabled", false);
        defaults.put("features.item-limiter.check-method", "on-hit");
        defaults.put("features.item-limiter.limits.golden_apple", 4);
        defaults.put("features.item-limiter.limits.enchanted_golden_apple", 1);
        defaults.put("features.item-limiter.limits.totem_of_undying", 1);
        defaults.put("features.item-limiter.limits.end_crystal", 2);
        defaults.put("features.item-limiter.limits.ender_pearl", 8);

        defaults.put("features.one-player-sleep.enabled", false);

        return defaults;
    }

    public void save() {
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

    public FileConfiguration get() {
        return plugin.getConfig();
    }

}
