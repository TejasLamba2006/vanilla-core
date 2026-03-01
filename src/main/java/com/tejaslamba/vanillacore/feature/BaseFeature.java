package com.tejaslamba.vanillacore.feature;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFeature implements Feature {

    protected VanillaCorePlugin plugin;
    protected boolean enabled;
    private boolean listenerRegistered = false;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".enabled", false);
        registerListenerIfNeeded();
    }

    @Override
    public void onDisable() {
        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Disabling feature: " + getName());
        }
        unregisterListener();
        this.enabled = false;
    }

    @Override
    public void reload() {
        boolean wasEnabled = this.enabled;
        this.enabled = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".enabled", false);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Reloading feature: " + getName());
            plugin.getLogger().info("[VERBOSE]   - Was Enabled: " + wasEnabled);
            plugin.getLogger().info("[VERBOSE]   - Now Enabled: " + enabled);
        }
    }

    private void registerListenerIfNeeded() {
        if (!listenerRegistered && getListener() != null) {
            plugin.getServer().getPluginManager().registerEvents(getListener(), plugin);
            listenerRegistered = true;

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Registered listener for feature: " + getName());
                plugin.getLogger().info("[VERBOSE]   - Listener Class: " + getListener().getClass().getSimpleName());
            }
        }
    }

    private void unregisterListener() {
        if (listenerRegistered && getListener() != null) {
            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Unregistering listener for feature: " + getName());
            }

            HandlerList.unregisterAll(getListener());
            listenerRegistered = false;
        }
    }

    @Override
    public boolean isEnabled() {
        if (isRemotelyDisabled()) {
            return false;
        }
        return enabled;
    }

    public boolean isRemotelyDisabled() {
        if (plugin == null)
            return false;
        return plugin.getFeatureManager().isFeatureRemotelyDisabled(getFeatureId());
    }

    public boolean isLocallyEnabled() {
        return enabled;
    }

    protected String getFeatureId() {
        return getConfigPath().replace("features.", "").replace("-", "_");
    }

    @Override
    public Listener getListener() {
        return null;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        toggleDefault(player);
    }

    protected void toggleDefault(Player player) {
        if (isRemotelyDisabled()) {
            String message = "This feature has been remotely disabled.";
            if (plugin.getCDNManager() != null) {
                message = plugin.getCDNManager().getDisabledMessage();
            }
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>" + message));
            return;
        }

        if (plugin.getFeatureManager().isMaintenanceMode()) {
            String message = "Plugin is in maintenance mode.";
            if (plugin.getCDNManager() != null) {
                message = plugin.getCDNManager().getMaintenanceMessage();
            }
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>" + message));
            return;
        }

        boolean current = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".enabled", false);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Player " + player.getName() + " toggling feature: " + getName());
            plugin.getLogger().info("[VERBOSE]   - Current State: " + (current ? "Enabled" : "Disabled"));
            plugin.getLogger().info("[VERBOSE]   - New State: " + (!current ? "Enabled" : "Disabled"));
        }

        plugin.getConfigManager().get().set(getConfigPath() + ".enabled", !current);
        plugin.getConfigManager().save();
        plugin.getConfigManager().load();
        reload();

        String displayName = plugin.getMenuConfigManager().getDisplayNameForConfig(getConfigPath() + ".enabled");
        player.sendMessage(MessageManager.parse(
                "<gold>[Vanilla Core] <gray>Toggled " + displayName + " <gray>to "
                        + (!current ? "<green>Enabled" : "<red>Disabled")));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        if (isRemotelyDisabled()) {
            lore.add("<red><bold>REMOTELY DISABLED");
            lore.add("<gray>(Critical issue detected)");
        } else {
            lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        }
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Toggle");
        return lore;
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.BARRIER, "<!italic><red>Unnamed Feature");
    }

    protected ItemStack createMenuItem(Material material, String name, String... customLore) {
        Material displayMaterial = isRemotelyDisabled() ? Material.BARRIER : material;
        String displayNameStr;
        if (isRemotelyDisabled()) {
            String plainName = MiniMessage.miniMessage().stripTags(name);
            displayNameStr = "<!italic><red><strikethrough>" + plainName + " <red>[DISABLED]";
        } else {
            displayNameStr = name;
        }

        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse(displayNameStr));
            List<Component> loreComponents = new ArrayList<>();

            for (String line : customLore) {
                loreComponents.add(MessageManager.parse(line));
            }

            if (customLore.length > 0) {
                loreComponents.add(Component.empty());
            }

            for (String line : getMenuLore()) {
                loreComponents.add(MessageManager.parse(line));
            }
            loreComponents.add(Component.empty());
            loreComponents.add(MessageManager.parse("<dark_gray>Config: <gray>" + getConfigPath() + ".enabled"));

            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }
}
