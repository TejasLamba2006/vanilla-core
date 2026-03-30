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
        return createMenuItem(Material.ENDER_PEARL, "<!italic><aqua>Pearl Restriction",
                "<!italic><gray>Disable ender pearl usage and teleports");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}