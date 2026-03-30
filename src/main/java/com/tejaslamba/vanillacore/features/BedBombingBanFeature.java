package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.BedBombingBanListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BedBombingBanFeature extends BaseFeature {

    private BedBombingBanListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new BedBombingBanListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 17;
    }

    @Override
    public String getName() {
        return "Bed Bombing Ban";
    }

    @Override
    public String getConfigPath() {
        return "features.bed-bombing-ban";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.RED_BED, "<!italic><red>Bed Bombing Ban",
                "<!italic><gray>Disable bed explosions in Nether and End");
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