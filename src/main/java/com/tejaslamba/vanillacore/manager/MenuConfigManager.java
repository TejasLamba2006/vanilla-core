package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuConfigManager {

    private final Main plugin;
    private FileConfiguration menuConfig;
    private final Map<String, String> configToNameMap = new HashMap<>();

    public MenuConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        InputStream in = plugin.getResource("menus.yml");
        if (in != null) {
            menuConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
        } else {
            // Fallback: attempt to load from an external file if present, but do not create
            // or overwrite it.
            File menuFile = new File(plugin.getDataFolder(), "menus.yml");
            if (menuFile.exists()) {
                menuConfig = YamlConfiguration.loadConfiguration(menuFile);
            } else {
                menuConfig = new YamlConfiguration();
            }
        }
        loadConfigToNameMap();
    }

    private void loadConfigToNameMap() {
        configToNameMap.clear();
        for (String menuKey : menuConfig.getKeys(false)) {
            ConfigurationSection menuSection = menuConfig.getConfigurationSection(menuKey);
            if (menuSection == null)
                continue;

            ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
            if (itemsSection == null)
                continue;

            for (String slotKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(slotKey);
                if (itemSection == null)
                    continue;

                String configPath = itemSection.getString("config");
                String name = itemSection.getString("name");
                if (configPath != null && name != null) {
                    configToNameMap.put("features." + configPath + ".enabled", name);
                }
            }
        }
    }

    public String getMenuTitle(String menuKey) {
        return menuConfig.getString(menuKey + ".title", "Menu");
    }

    public int getMenuSize(String menuKey) {
        return menuConfig.getInt(menuKey + ".size", 54);
    }

    public ConfigurationSection getMenuItems(String menuKey) {
        return menuConfig.getConfigurationSection(menuKey + ".items");
    }

    public String getItemName(String menuKey, int slot) {
        return menuConfig.getString(menuKey + ".items." + slot + ".name", "");
    }

    public Material getItemMaterial(String menuKey, int slot) {
        String materialName = menuConfig.getString(menuKey + ".items." + slot + ".material", "STONE");
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    public List<String> getItemLore(String menuKey, int slot) {
        return menuConfig.getStringList(menuKey + ".items." + slot + ".lore");
    }

    public String getItemConfig(String menuKey, int slot) {
        return menuConfig.getString(menuKey + ".items." + slot + ".config");
    }

    public String getItemAction(String menuKey, int slot) {
        return menuConfig.getString(menuKey + ".items." + slot + ".action");
    }

    public String getDisplayNameForConfig(String configPath) {
        return configToNameMap.getOrDefault(configPath, configPath);
    }

}
