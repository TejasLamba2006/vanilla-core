package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.StopItemDespawnListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StopItemDespawnFeature extends BaseFeature {

    private StopItemDespawnListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new StopItemDespawnListener(plugin);
        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Stop Item Despawn - Feature loaded");
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 33;
    }

    @Override
    public String getName() {
        return "Stop Item Despawn";
    }

    @Override
    public String getConfigPath() {
        return "features.stop-item-despawn";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.CHEST,
                plugin.getMessageManager().getRaw("feature-menus.stop-item-despawn.name"),
                plugin.getMessageManager().getRaw("feature-menus.stop-item-despawn.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.stop-item-despawn.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.stop-item-despawn.lore-2"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}
