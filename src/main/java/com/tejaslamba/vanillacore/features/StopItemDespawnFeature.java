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
        return createMenuItem(Material.CHEST, "<!italic><aqua>Stop Item Despawn",
                "<!italic><gray>Death drops stay on the ground forever");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Items dropped on death don't");
        lore.add("<gray>despawn. Ever.");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}
