package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.AnchorRestrictionListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AnchorRestrictionFeature extends BaseFeature {

    private AnchorRestrictionListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new AnchorRestrictionListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 21;
    }

    @Override
    public String getName() {
        return "Anchor Restriction";
    }

    @Override
    public String getConfigPath() {
        return "features.anchor-restriction";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.RESPAWN_ANCHOR,
                plugin.getMessageManager().getRaw("feature-menus.anchor-restriction.name"),
                plugin.getMessageManager().getRaw("feature-menus.anchor-restriction.description"));
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