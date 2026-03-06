package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.DimensionLockListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class DimensionLockFeature extends BaseFeature {

    private final String dimension;
    private static DimensionLockListener sharedListener;
    private static boolean sharedListenerRegistered = false;

    public DimensionLockFeature(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".enabled", false);

        if (sharedListener == null) {
            sharedListener = new DimensionLockListener(plugin);
        }

        if (!sharedListenerRegistered) {
            plugin.getServer().getPluginManager().registerEvents(sharedListener, plugin);
            sharedListenerRegistered = true;
        }

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Dimension Lock (" + dimension + ") - Enabled: " + enabled);
        }
    }

    @Override
    public void onDisable() {
        if (plugin != null && plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Disabling feature: " + getName());
        }
        this.enabled = false;
        if (sharedListenerRegistered) {
            HandlerList.unregisterAll(sharedListener);
            sharedListener = null;
            sharedListenerRegistered = false;
        }
    }

    @Override
    public Listener getListener() {
        return sharedListener;
    }

    @Override
    public String getName() {
        return dimension.substring(0, 1).toUpperCase() + dimension.substring(1) + " Lock";
    }

    @Override
    public String getConfigPath() {
        return "features.dimension-lock-" + dimension;
    }

    @Override
    public ItemStack getMenuItem() {
        Material material = dimension.equals("end") ? Material.END_PORTAL_FRAME : Material.NETHERRACK;
        String name = "<!italic><dark_purple>" + getName();
        return createMenuItem(material, name,
                "<!italic><gray>Control access to the " + dimension);
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<red>Currently: Locked" : "<green>Currently: Open");
        lore.add("");
        lore.add("<yellow>Click to " + (enabled ? "Unlock" : "Lock"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggle(player);
    }

    @Override
    public void onRightClick(Player player) {
        toggle(player);
    }

    private void toggle(Player player) {
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

        enabled = !enabled;
        plugin.getConfigManager().get().set(getConfigPath() + ".enabled", enabled);
        plugin.getConfigManager().save();

        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);
        player.sendMessage(MessageManager.parse("<gold>[Vanilla Core] <gray>The " + dimensionName + " is now "
                + (enabled ? "<red>Locked" : "<green>Open")));

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Dimension Lock (" + dimension + ") - " + player.getName()
                    + " toggled to: enabled=" + enabled);
        }
    }

    public boolean isLocked() {
        return enabled;
    }

    public void setLocked(boolean locked) {
        this.enabled = locked;
        plugin.getConfigManager().get().set(getConfigPath() + ".enabled", locked);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Dimension Lock (" + dimension + ") - Set locked=" + locked);
        }
    }

    public String getDimension() {
        return dimension;
    }
}
