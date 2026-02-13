package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.feature.BaseFeature;
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
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage("§6§l=== Item Explosion Immunity ===");
        player.sendMessage("");
        player.sendMessage("§7Status: " + (isEnabled() ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("");
        player.sendMessage("§7When enabled, dropped items on");
        player.sendMessage("§7the ground will be protected from");
        player.sendMessage("§7TNT, creepers, crystals, and all");
        player.sendMessage("§7other explosion damage.");
        player.sendMessage("");
        player.sendMessage("§7Useful for preventing item loss");
        player.sendMessage("§7during PvP or griefing attempts.");
    }
}
