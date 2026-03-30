package com.tejaslamba.vanillacore.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ConfigManager {

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
    }

    public FileConfiguration get() {
        return plugin.getConfig();
    }

    public void save() {
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

}
