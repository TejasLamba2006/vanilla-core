package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ItemExplosionImmunityListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemExplosionImmunityFeature extends BaseFeature {

    private ItemExplosionImmunityListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
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
        return createMenuItem(Material.TNT,
                plugin.getMessageManager().getRaw("feature-menus.item-explosion-immunity.name"),
                plugin.getMessageManager().getRaw("feature-menus.item-explosion-immunity.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.item-explosion-immunity.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.item-explosion-immunity.lore-2"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.title"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get(
                isEnabled() ? "item-explosion-immunity.info.status-enabled"
                        : "item-explosion-immunity.info.status-disabled"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-1"));
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-2"));
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-3"));
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-4"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-5"));
        player.sendMessage(plugin.getMessageManager().get("item-explosion-immunity.info.line-6"));
    }
}

