package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportDataManager {

    private final VanillaCorePlugin plugin;

    private File homesFile;
    private File warpsFile;
    private File teleportsFile;
    private File kitsDataFile;

    private FileConfiguration homesConfig;
    private FileConfiguration warpsConfig;
    private FileConfiguration teleportsConfig;
    private FileConfiguration kitsDataConfig;

    public TeleportDataManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        teleportsFile = new File(plugin.getDataFolder(), "teleports.yml");
        kitsDataFile = new File(plugin.getDataFolder(), "kits-data.yml");

        homesConfig = loadFile(homesFile);
        warpsConfig = loadFile(warpsFile);
        teleportsConfig = loadFile(teleportsFile);
        kitsDataConfig = loadFile(kitsDataFile);
    }

    public void saveAll() {
        saveFile(homesConfig, homesFile);
        saveFile(warpsConfig, warpsFile);
        saveFile(teleportsConfig, teleportsFile);
        saveFile(kitsDataConfig, kitsDataFile);
    }

    public Map<String, Location> getHomes(UUID playerId) {
        ConfigurationSection section = homesConfig.getConfigurationSection(playerId.toString());
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, Location> homes = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Location location = section.getLocation(key);
            if (location != null && location.getWorld() != null) {
                homes.put(key.toLowerCase(), location);
            }
        }
        return homes;
    }

    public Location getHome(UUID playerId, String homeName) {
        Location location = homesConfig.getLocation(playerId + "." + homeName.toLowerCase());
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location;
    }

    public void setHome(UUID playerId, String homeName, Location location) {
        homesConfig.set(playerId + "." + homeName.toLowerCase(), location);
        saveFile(homesConfig, homesFile);
    }

    public void deleteHome(UUID playerId, String homeName) {
        homesConfig.set(playerId + "." + homeName.toLowerCase(), null);
        saveFile(homesConfig, homesFile);
    }

    public Map<String, Location> getWarps() {
        ConfigurationSection section = warpsConfig.getConfigurationSection("warps");
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, Location> warps = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Location location = section.getLocation(key);
            if (location != null && location.getWorld() != null) {
                warps.put(key.toLowerCase(), location);
            }
        }
        return warps;
    }

    public Location getWarp(String warpName) {
        Location location = warpsConfig.getLocation("warps." + warpName.toLowerCase());
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location;
    }

    public void setWarp(String warpName, Location location) {
        warpsConfig.set("warps." + warpName.toLowerCase(), location);
        saveFile(warpsConfig, warpsFile);
    }

    public void deleteWarp(String warpName) {
        warpsConfig.set("warps." + warpName.toLowerCase(), null);
        saveFile(warpsConfig, warpsFile);
    }

    public Location getSpawn() {
        Location location = teleportsConfig.getLocation("spawn");
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location;
    }

    public void setSpawn(Location location) {
        teleportsConfig.set("spawn", location);
        saveFile(teleportsConfig, teleportsFile);
    }

    public Location getBack(UUID playerId) {
        Location location = teleportsConfig.getLocation("back." + playerId);
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location;
    }

    public void setBack(UUID playerId, Location location) {
        teleportsConfig.set("back." + playerId, location);
        saveFile(teleportsConfig, teleportsFile);
    }

    public long getKitLastClaim(UUID playerId, String kitName) {
        return kitsDataConfig.getLong("kits." + playerId + "." + kitName.toLowerCase(), 0L);
    }

    public void setKitLastClaim(UUID playerId, String kitName, long timestampMs) {
        kitsDataConfig.set("kits." + playerId + "." + kitName.toLowerCase(), timestampMs);
        saveFile(kitsDataConfig, kitsDataFile);
    }

    private FileConfiguration loadFile(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed creating data file " + file.getName() + ": " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed saving data file " + file.getName() + ": " + e.getMessage());
        }
    }
}

