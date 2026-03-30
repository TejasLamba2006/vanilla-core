package com.tejaslamba.vanillacore.listener.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class AbstractMenuClickHandler {

    protected final VanillaCorePlugin plugin;

    protected AbstractMenuClickHandler(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    protected boolean isClickInTopInventory(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        return slot >= 0 && slot < event.getView().getTopInventory().getSize();
    }

    protected <T extends BaseFeature> T getEnabledFeature(Class<T> clazz, Player player, String messageKey) {
        T feature = plugin.getFeatureManager().getFeature(clazz);
        if (feature == null || !feature.isEnabled()) {
            plugin.getMessageManager().sendPrefixed(player, messageKey);
            plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            return null;
        }
        return feature;
    }
}
