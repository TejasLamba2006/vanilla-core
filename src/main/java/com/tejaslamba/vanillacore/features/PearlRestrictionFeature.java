package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.PearlRestrictionListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PearlRestrictionFeature extends BaseFeature {

    private PearlRestrictionListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new PearlRestrictionListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 22;
    }

    @Override
    public String getName() {
        return "Pearl Restriction";
    }

    @Override
    public String getConfigPath() {
        return "features.pearl-restriction";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ENDER_PEARL,
                plugin.getMessageManager().getRaw("feature-menus.pearl-restriction.name"),
                plugin.getMessageManager().getRaw("feature-menus.pearl-restriction.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}
