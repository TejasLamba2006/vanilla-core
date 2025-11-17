package com.tejaslamba.smpcore.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private static final int CURRENT_CONFIG_VERSION = 1;

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
        defaults.put("plugin.name", "SMP Core");
        defaults.put("plugin.prefix", "§8[§6SMP§8]§r");
        defaults.put("plugin.verbose", false);
        defaults.put("messages.reload", "§aConfiguration reloaded successfully!");

        String[] features = {
                "ban-anchors", "ban-crystals", "ban-pearls",
                "combat-tag", "anti-restock", "anti-elytra-combat", "anti-naked-killing",
                "anti-afk-killing", "one-player-sleep", "infinite-restock", "pvp-toggle",
                "first-join-kit", "spectator-on-death", "ban-tipped-arrows", "prevent-bed-bombing",
                "restrict-tnt-minecart", "prevent-villager-killing",
                "breach-swap-ban", "invisibility-qol", "item-limiter"
        };

        for (String feature : features) {
            boolean defaultEnabled = feature.equals("pvp-toggle");
            defaults.put("features." + feature + ".enabled", defaultEnabled);
        }

        defaults.put("features.custom-anvil-caps.enabled", false);
        defaults.put("features.custom-anvil-caps.block-enchanting-table", true);
        defaults.put("features.custom-anvil-caps.block-anvil", true);
        defaults.put("features.custom-anvil-caps.keep-only-best-protection", true);
        defaults.put("features.custom-anvil-caps.caps.sharpness", 4);
        defaults.put("features.custom-anvil-caps.caps.protection", 3);
        defaults.put("features.custom-anvil-caps.caps.fire_protection", 3);
        defaults.put("features.custom-anvil-caps.caps.blast_protection", 3);
        defaults.put("features.custom-anvil-caps.caps.projectile_protection", 3);
        defaults.put("features.custom-anvil-caps.caps.feather_falling", 3);
        defaults.put("features.custom-anvil-caps.caps.power", 4);
        defaults.put("features.custom-anvil-caps.caps.unbreaking", 2);

        defaults.put("features.enchantment-replacement.enabled", false);
        defaults.put("features.enchantment-replacement.scan-on-join", true);
        defaults.put("features.enchantment-replacement.scan-on-inventory-open", true);
        defaults.put("features.enchantment-replacement.scan-on-villager-trade", true);
        defaults.put("features.enchantment-replacement.scan-on-pickup", false);
        defaults.put("features.enchantment-replacement.scan-on-craft", true);
        defaults.put("features.enchantment-replacement.keep-only-best-protection", true);
        defaults.put("features.enchantment-replacement.caps.sharpness", 4);
        defaults.put("features.enchantment-replacement.caps.protection", 3);
        defaults.put("features.enchantment-replacement.caps.fire_protection", 3);
        defaults.put("features.enchantment-replacement.caps.blast_protection", 3);
        defaults.put("features.enchantment-replacement.caps.projectile_protection", 3);
        defaults.put("features.enchantment-replacement.caps.feather_falling", 3);
        defaults.put("features.enchantment-replacement.caps.power", 4);
        defaults.put("features.enchantment-replacement.caps.unbreaking", 2);

        defaults.put("features.mace-limiter.enabled", false);
        defaults.put("features.mace-limiter.mace-crafted", false);
        defaults.put("features.mace-limiter.broadcast-enabled", true);
        defaults.put("features.mace-limiter.broadcast-message",
                "§6{player} §ehas crafted the only Mace on the server!");

        defaults.put("features.dimension-lock-end.enabled", false);
        defaults.put("features.dimension-lock-end.locked", false);
        defaults.put("features.dimension-lock-end.locked-message", "§cThe End is currently locked!");

        defaults.put("features.dimension-lock-nether.enabled", false);
        defaults.put("features.dimension-lock-nether.locked", false);
        defaults.put("features.dimension-lock-nether.locked-message", "§cThe Nether is currently locked!");

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
