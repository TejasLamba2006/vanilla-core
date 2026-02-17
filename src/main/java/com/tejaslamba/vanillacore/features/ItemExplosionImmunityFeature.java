package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.gui.feature.ItemExplosionImmunityConfigGUI;
import com.tejaslamba.vanillacore.listener.ItemExplosionImmunityListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemExplosionImmunityFeature extends BaseFeature {

    private ItemExplosionImmunityListener listener;

    @Override
    public void onEnable(Main plugin) {
        listener = new ItemExplosionImmunityListener(plugin);
        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Item Explosion Immunity - Feature loaded");
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 32;
    }

    @Override
    public String getName() {
        return "Item Explosion Immunity";
    }

    @Override
    public String getConfigPath() {
        return "features.item-explosion-immunity";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.TNT, "§5Item Explosion Immunity",
                "§7Protect dropped items from explosions");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7Dropped items will not be");
        lore.add("§7destroyed by explosions");
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Configure");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        ItemExplosionImmunityConfigGUI gui = new ItemExplosionImmunityConfigGUI(plugin, player);
        gui.open();
    }
}
