package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.DimensionLockListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class DimensionLockFeature extends BaseFeature {

    private final String dimension;
    private DimensionLockListener sharedListener;

    public DimensionLockFeature(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public void onEnable(Main plugin) {
        if (sharedListener == null) {
            sharedListener = new DimensionLockListener(plugin);
        }

        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Dimension Lock (" + dimension + ") - Enabled: " + enabled);
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
        String name = "§5" + getName();
        return createMenuItem(material, name,
                "§7Control access to the " + dimension);
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§cCurrently: Locked" : "§aCurrently: Open");
        lore.add("");
        lore.add("§eClick to " + (enabled ? "Unlock" : "Lock"));
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
        enabled = !enabled;
        plugin.getConfigManager().get().set(getConfigPath() + ".enabled", enabled);
        plugin.getConfigManager().save();

        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);
        player.sendMessage("§6[SMP] §7The " + dimensionName + " is now " + (enabled ? "§cLocked" : "§aOpen"));

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
