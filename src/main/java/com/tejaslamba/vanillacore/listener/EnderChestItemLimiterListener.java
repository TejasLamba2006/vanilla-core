package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.enderchestlimiter.EnderChestItemLimiterManager;
import com.tejaslamba.vanillacore.features.EnderChestItemLimiterFeature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class EnderChestItemLimiterListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final EnderChestItemLimiterManager manager;

    public EnderChestItemLimiterListener(VanillaCorePlugin plugin, EnderChestItemLimiterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        EnderChestItemLimiterFeature feature = plugin.getFeatureManager()
                .getFeature(EnderChestItemLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (player.hasPermission("smp.enderchestlimiter.bypass")) {
            return;
        }

        if (event.getView().getTopInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        ItemStack itemToInsert = getItemBeingInserted(event, player);
        if (itemToInsert == null || !manager.isBlocked(itemToInsert)) {
            return;
        }

        event.setCancelled(true);
        manager.sendBlockedMessage(player, itemToInsert);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        EnderChestItemLimiterFeature feature = plugin.getFeatureManager()
                .getFeature(EnderChestItemLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (player.hasPermission("smp.enderchestlimiter.bypass")) {
            return;
        }

        if (event.getView().getTopInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        boolean touchesEnderChest = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);

        if (!touchesEnderChest) {
            return;
        }

        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem == null || !manager.isBlocked(draggedItem)) {
            return;
        }

        event.setCancelled(true);
        manager.sendBlockedMessage(player, draggedItem);
    }

    private ItemStack getItemBeingInserted(InventoryClickEvent event, Player player) {
        int topSize = event.getView().getTopInventory().getSize();
        int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                return event.getCurrentItem();
            }
            return null;
        }

        if (rawSlot < 0 || rawSlot >= topSize) {
            return null;
        }

        if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbar = event.getHotbarButton();
                if (hotbar >= 0 && hotbar < 9) {
                    return player.getInventory().getItem(hotbar);
                }
                return null;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                return player.getInventory().getItemInOffHand();
            }
            return null;
        }

        if (action == InventoryAction.PLACE_ALL
                || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME
                || action == InventoryAction.SWAP_WITH_CURSOR) {
            return event.getCursor();
        }

        return null;
    }
}

