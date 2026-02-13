package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.ItemLimiterFeature;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimit;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimiterManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemLimiterListener implements Listener {

    private final Main plugin;
    private final ItemLimiterManager manager;

    public ItemLimiterListener(Main plugin, ItemLimiterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (player.hasPermission("smpcore.itemlimiter.bypass")) {
            return;
        }

        ItemStack pickedUpItem = event.getItem().getItemStack();
        String itemKey = manager.findMatchingItemKey(pickedUpItem);

        if (itemKey == null) {
            return;
        }

        ItemLimit itemLimit = manager.getItemLimit(itemKey);
        if (itemLimit == null) {
            return;
        }

        int limit = itemLimit.getLimit();

        if (limit == 0) {
            event.setCancelled(true);
            manager.sendCooldownMessage(player, "§c[Vanilla Core] §7This item is completely banned");
            return;
        }

        int currentCount = manager.getPlayerItemCount(player, pickedUpItem);
        int totalAfterPickup = currentCount + pickedUpItem.getAmount();

        if (totalAfterPickup > limit) {
            int canPickup = Math.max(0, limit - currentCount);

            if (canPickup <= 0) {
                event.setCancelled(true);
                manager.sendCooldownMessage(player,
                        "§c[Vanilla Core] §7You have reached the limit of " + limit + " for this item");
            } else if (canPickup < pickedUpItem.getAmount()) {
                event.setCancelled(true);

                ItemStack partialItem = pickedUpItem.clone();
                partialItem.setAmount(canPickup);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(partialItem);

                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }

                ItemStack originalItem = event.getItem().getItemStack();
                int newAmount = originalItem.getAmount() - canPickup;

                if (newAmount <= 0) {
                    event.getItem().remove();
                } else {
                    originalItem.setAmount(newAmount);
                    event.getItem().setItemStack(originalItem);
                }

                manager.sendCooldownMessage(player, "§e[Vanilla Core] §7You can only pick up " + canPickup
                        + " more of this item (limit: " + limit + ")");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (player.hasPermission("smpcore.itemlimiter.bypass")) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.equals(ItemLimiterFeature.MAIN_GUI_TITLE) ||
                inventoryTitle.equals(ItemLimiterFeature.ADD_GUI_TITLE) ||
                inventoryTitle.equals(ItemLimiterFeature.VIEW_GUI_TITLE)) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.PLAYER || event.getCurrentItem() == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        String itemKey = manager.findMatchingItemKey(clickedItem);
        if (itemKey == null) {
            return;
        }

        ItemLimit itemLimit = manager.getItemLimit(itemKey);
        if (itemLimit == null) {
            return;
        }

        int limit = itemLimit.getLimit();

        if (limit == 0) {
            event.setCancelled(true);
            manager.sendCooldownMessage(player, "§c[Vanilla Core] §7This item is completely banned");
            return;
        }

        int currentCount = manager.getPlayerItemCount(player, clickedItem);

        if (event.isShiftClick()) {
            if (currentCount + clickedItem.getAmount() > limit) {
                event.setCancelled(true);
                manager.sendCooldownMessage(player,
                        "§c[Vanilla Core] §7This would exceed your limit of " + limit + " for this item");
            }
        } else {
            int amountToAdd = event.isLeftClick() ? clickedItem.getAmount() : (clickedItem.getAmount() + 1) / 2;
            if (currentCount + amountToAdd > limit) {
                event.setCancelled(true);
                manager.sendCooldownMessage(player,
                        "§c[Vanilla Core] §7This would exceed your limit of " + limit + " for this item");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (player.hasPermission("smpcore.itemlimiter.bypass")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> manager.checkAndEnforceLimits(player), 20L);
    }
}
