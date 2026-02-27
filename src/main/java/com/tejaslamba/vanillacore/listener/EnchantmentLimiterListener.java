package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.enchantlimiter.EnchantmentLimiterManager;
import com.tejaslamba.vanillacore.features.EnchantmentLimiterFeature;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class EnchantmentLimiterListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final EnchantmentLimiterManager manager;

    public EnchantmentLimiterListener(VanillaCorePlugin plugin, EnchantmentLimiterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
        if (feature != null) {
            feature.cleanupPlayer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.startsWith(EnchantmentLimiterFeature.CONFIG_GUI_TITLE)) {
            EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
            if (feature != null) {
                feature.cleanupPlayer(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onConfigGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.startsWith(EnchantmentLimiterFeature.CONFIG_GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) {
            return;
        }

        int page = feature.getPlayerPage(player);
        boolean isLeftClick = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT;
        boolean isRightClick = event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT;
        boolean isShiftClick = event.isShiftClick();
        boolean isMiddleClick = event.getClick() == ClickType.MIDDLE;

        feature.handleConfigGUIClick(slot, page, isLeftClick, isRightClick, isShiftClick, isMiddleClick, player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            manager.checkItemEnchantments(event.getEnchanter(), event.getItem());
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!item.hasItemMeta() || item.getEnchantments().isEmpty()) {
            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta bookMeta) {
                if (bookMeta.getStoredEnchants().isEmpty()) {
                    return;
                }
            } else {
                return;
            }
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        boolean modified = manager.checkItemEnchantments(player, item);

        if (modified && event.getInventory() instanceof AnvilInventory && event.getRawSlot() == 2) {
            player.updateInventory();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPickup(EntityPickupItemEvent event) {
        EnchantmentLimiterFeature feature = plugin.getFeatureManager().getFeature(EnchantmentLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        if (itemEntity == null || !itemEntity.isValid()) {
            return;
        }

        ItemStack item = itemEntity.getItemStack();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!item.hasItemMeta()) {
            return;
        }

        boolean hasEnchants = !item.getEnchantments().isEmpty();
        if (!hasEnchants && item.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta bookMeta) {
            hasEnchants = !bookMeta.getStoredEnchants().isEmpty();
        }

        if (!hasEnchants) {
            return;
        }

        boolean modified = manager.checkItemEnchantments(player, item);
        if (modified) {
            itemEntity.setItemStack(item);
        }
    }
}
