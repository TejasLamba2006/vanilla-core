package com.tejaslamba.vanillacore.manager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final JavaPlugin plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messageCache = new HashMap<>();

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messageCache.clear();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultConfig);
        }

        if (com.tejaslamba.vanillacore.VanillaCorePlugin.getInstance().isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Messages loaded from messages.yml");
        }
    }

    public void save() {
        try {
            messagesConfig.save(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save messages.yml: " + e.getMessage());
        }
    }

    public String getRaw(String path) {
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }

        String message = messagesConfig.getString(path);
        if (message == null) {
            message = path;
        }

        messageCache.put(path, message);
        return message;
    }

    public String get(String path) {
        return colorize(getRaw(path));
    }

    public String get(String path, Object... replacements) {
        String message = getRaw(path);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace(placeholder, value);
        }

        return colorize(message);
    }

    public String getPrefix() {
        return get("general.prefix");
    }

    public String getPrefixed(String path) {
        return getPrefix() + " " + get(path);
    }

    public String getPrefixed(String path, Object... replacements) {
        return getPrefix() + " " + get(path, replacements);
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    public void send(CommandSender sender, String path, Object... replacements) {
        sender.sendMessage(get(path, replacements));
    }

    public void sendPrefixed(CommandSender sender, String path) {
        sender.sendMessage(getPrefixed(path));
    }

    public void sendPrefixed(CommandSender sender, String path, Object... replacements) {
        sender.sendMessage(getPrefixed(path, replacements));
    }

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }

    public void reload() {
        load();
    }
}
