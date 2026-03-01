package com.tejaslamba.vanillacore.manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

    private static final MiniMessage MM = MiniMessage.miniMessage();

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

    public Component get(String path) {
        return MM.deserialize(getRaw(path));
    }

    public Component get(String path, Object... replacements) {
        if ((replacements.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "Replacements must be key/value pairs but got " + replacements.length + " args for path: " + path);
        }
        String raw = getRaw(path);
        TagResolver.Builder resolver = TagResolver.builder();
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            if (replacements[i] == null) {
                throw new IllegalArgumentException(
                        "Replacement key cannot be null at index " + i + " for path: " + path);
            }
            String key = String.valueOf(replacements[i]).replace("{", "").replace("}", "");
            String value = String.valueOf(replacements[i + 1]);
            resolver.resolver(Placeholder.unparsed(key, value));
        }
        return MM.deserialize(raw, resolver.build());
    }

    public Component getPrefix() {
        return get("general.prefix");
    }

    public Component getPrefixed(String path) {
        return getPrefix().append(Component.text(" ")).append(get(path));
    }

    public Component getPrefixed(String path, Object... replacements) {
        return getPrefix().append(Component.text(" ")).append(get(path, replacements));
    }

    public void send(Audience audience, String path) {
        audience.sendMessage(get(path));
    }

    public void send(Audience audience, String path, Object... replacements) {
        audience.sendMessage(get(path, replacements));
    }

    public void sendPrefixed(Audience audience, String path) {
        audience.sendMessage(getPrefixed(path));
    }

    public void sendPrefixed(Audience audience, String path, Object... replacements) {
        audience.sendMessage(getPrefixed(path, replacements));
    }

    public static Component parse(String miniMessage) {
        if (miniMessage == null)
            return Component.empty();
        return MM.deserializeOrNull(miniMessage);
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }

    public void reload() {
        load();
    }
}
