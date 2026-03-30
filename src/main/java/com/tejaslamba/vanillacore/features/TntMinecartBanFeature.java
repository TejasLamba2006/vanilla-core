package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.TntMinecartBanListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TntMinecartBanFeature extends BaseFeature {

    private TntMinecartBanListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new TntMinecartBanListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 18;
    }

    @Override
    public String getName() {
        return "TNT Minecart Ban";
    }

    @Override
    public String getConfigPath() {
        return "features.tnt-minecart-ban";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.TNT_MINECART, "<!italic><red>TNT Minecart Ban",
                "<!italic><gray>Disable TNT minecart explosions and damage");
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