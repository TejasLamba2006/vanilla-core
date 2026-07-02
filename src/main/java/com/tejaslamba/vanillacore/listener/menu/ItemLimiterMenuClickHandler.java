package com.tejaslamba.vanillacore.listener.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.EnderChestItemLimiterFeature;
import com.tejaslamba.vanillacore.features.ItemLimiterFeature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ItemLimiterMenuClickHandler extends AbstractMenuClickHandler {

    private static final String ITEM_LIMITER_DISABLED_KEY = "item-limiter.feature-disabled";
    private static final String ENDER_CHEST_LIMITER_DISABLED_KEY = "ender-chest-item-limiter.feature-disabled";

    public ItemLimiterMenuClickHandler(VanillaCorePlugin plugin) {
        super(plugin);
    }

    public void handleItemLimiterMain(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = getEnabledFeature(ItemLimiterFeature.class, player,
                ITEM_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        feature.handleMainMenuClick(event.getSlot(), player);
    }

    public void handleItemLimiterAdd(InventoryClickEvent event, Player player) {
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        ItemLimiterFeature feature = getEnabledFeature(ItemLimiterFeature.class, player,
                ITEM_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        feature.handleAddItemClick(event, player);
    }

    public void handleItemLimiterView(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = getEnabledFeature(ItemLimiterFeature.class, player,
                ITEM_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        feature.handleViewLimitsClick(
                event.getSlot(),
                event.getInventory().getSize(),
                event.isShiftClick(),
                event.isLeftClick(),
                player);
    }

    public void handleItemLimiterBanned(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = getEnabledFeature(ItemLimiterFeature.class, player,
                ITEM_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        feature.handleBannedItemsClick(
                event.getSlot(),
                event.getInventory().getSize(),
                event.isShiftClick(),
                event.isLeftClick(),
                player);
    }

    public void handleEnderChestMain(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        EnderChestItemLimiterFeature feature = getEnabledFeature(EnderChestItemLimiterFeature.class, player,
                ENDER_CHEST_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        feature.handleMainMenuClick(event.getSlot(), player);
    }

    public void handleEnderChestAdd(InventoryClickEvent event, Player player) {
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        EnderChestItemLimiterFeature feature = getEnabledFeature(EnderChestItemLimiterFeature.class, player,
                ENDER_CHEST_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        feature.handleAddItemClick(event, player);
    }

    public void handleEnderChestView(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        EnderChestItemLimiterFeature feature = getEnabledFeature(EnderChestItemLimiterFeature.class, player,
                ENDER_CHEST_LIMITER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        feature.handleViewBlockedItemsClick(
                event.getSlot(),
                event.getInventory().getSize(),
                event.isShiftClick(),
                event.isLeftClick(),
                player);
    }
}

