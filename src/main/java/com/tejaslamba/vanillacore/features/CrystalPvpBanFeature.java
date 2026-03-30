package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.CrystalPvpBanListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrystalPvpBanFeature extends BaseFeature {

    private CrystalPvpBanListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new CrystalPvpBanListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 20;
    }

    @Override
    public String getName() {
        return "Crystal PvP Ban";
    }

    @Override
    public String getConfigPath() {
        return "features.crystal-pvp-ban";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.END_CRYSTAL,
                plugin.getMessageManager().getRaw("feature-menus.crystal-pvp-ban.name"),
                plugin.getMessageManager().getRaw("feature-menus.crystal-pvp-ban.description"));
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