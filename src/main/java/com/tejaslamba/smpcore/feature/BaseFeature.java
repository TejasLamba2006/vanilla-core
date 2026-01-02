package com.tejaslamba.smpcore.feature;

import com.tejaslamba.smpcore.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFeature implements Feature {

    protected Main plugin;
    protected boolean enabled;
    private boolean listenerRegistered = false;

    @Override
    public void onEnable(Main plugin) {
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
            player.sendMessage("§c[SMP Core] §7" + message);
            return;
        }

        if (plugin.getFeatureManager().isMaintenanceMode()) {
            String message = "Plugin is in maintenance mode.";
            if (plugin.getCDNManager() != null) {
                message = plugin.getCDNManager().getMaintenanceMessage();
            }
            player.sendMessage("§c[SMP Core] §7" + message);
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
        player.sendMessage("§6[SMP] §7Toggled " + displayName + " §7to §" + (!current ? "aEnabled" : "cDisabled"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        if (isRemotelyDisabled()) {
            lore.add("§c§lREMOTELY DISABLED");
            lore.add("§7(Critical issue detected)");
        } else {
            lore.add(enabled ? "§aEnabled" : "§cDisabled");
        }
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Toggle");
        return lore;
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.BARRIER, "§cUnnamed Feature");
    }

    protected ItemStack createMenuItem(Material material, String name, String... customLore) {
        Material displayMaterial = isRemotelyDisabled() ? Material.BARRIER : material;
        String displayName = isRemotelyDisabled() ? "§c§m" + stripColor(name) + " §c[DISABLED]" : name;

        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> loreList = new ArrayList<>();

            for (String line : customLore) {
                loreList.add(line);
            }

            if (customLore.length > 0) {
                loreList.add("");
            }

            loreList.addAll(getMenuLore());
            loreList.add("");
            loreList.add("§8Config: §7" + getConfigPath() + ".enabled");

            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}
