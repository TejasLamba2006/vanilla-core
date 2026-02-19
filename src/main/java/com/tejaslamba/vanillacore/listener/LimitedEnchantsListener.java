package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.LimitedEnchantmentSlotsFeature;
import com.tejaslamba.vanillacore.limitedenchants.LimitedEnchantsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

public class LimitedEnchantsListener implements Listener {

    private final Main plugin;
    private final LimitedEnchantsManager manager;

    public LimitedEnchantsListener(Main plugin, LimitedEnchantsManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.equals(LimitedEnchantmentSlotsFeature.GUI_TITLE)
                || title.startsWith(LimitedEnchantmentSlotsFeature.CONFIG_GUI_TITLE)
                || title.startsWith(LimitedEnchantmentSlotsFeature.VIEW_GUI_TITLE)) {
            LimitedEnchantmentSlotsFeature feature = plugin.getFeatureManager()
                    .getFeature(LimitedEnchantmentSlotsFeature.class);
            if (feature != null) {
                feature.cleanupPlayer(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals(LimitedEnchantmentSlotsFeature.GUI_TITLE)) {
            event.setCancelled(true);
            LimitedEnchantmentSlotsFeature feature = plugin.getFeatureManager()
                    .getFeature(LimitedEnchantmentSlotsFeature.class);
            if (feature != null) {
                feature.handleMainGUIClick(event.getRawSlot(), player);
            }
            return;
        }

        if (title.startsWith(LimitedEnchantmentSlotsFeature.CONFIG_GUI_TITLE)) {
            event.setCancelled(true);
            LimitedEnchantmentSlotsFeature feature = plugin.getFeatureManager()
                    .getFeature(LimitedEnchantmentSlotsFeature.class);
            if (feature != null) {
                int page = feature.getPlayerPage(player);
                boolean isLeftClick = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT;
                boolean isRightClick = event.getClick() == ClickType.RIGHT
                        || event.getClick() == ClickType.SHIFT_RIGHT;
                boolean isShiftClick = event.isShiftClick();
                boolean isMiddleClick = event.getClick() == ClickType.MIDDLE;
                boolean isDropKey = event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP;

                feature.handleConfigGUIClick(event.getRawSlot(), page, isLeftClick, isRightClick, isShiftClick,
                        isMiddleClick, isDropKey, player);
            }
            return;
        }

        if (title.startsWith(LimitedEnchantmentSlotsFeature.VIEW_GUI_TITLE)) {
            event.setCancelled(true);
            LimitedEnchantmentSlotsFeature feature = plugin.getFeatureManager()
                    .getFeature(LimitedEnchantmentSlotsFeature.class);
            if (feature != null) {
                feature.handleViewRegistrationsClick(event.getRawSlot(), player);
            }
            return;
        }

        if (title.equals("§6Select Material to View")) {
            event.setCancelled(true);
            LimitedEnchantmentSlotsFeature feature = plugin.getFeatureManager()
                    .getFeature(LimitedEnchantmentSlotsFeature.class);
            if (feature != null) {
                feature.handleViewMaterialSelectionClick(event.getRawSlot(), player, event.getCurrentItem());
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("features.limited-enchantment-slots.enabled")) {
            return;
        }

        ItemStack item = event.getItem();
        Material material = item.getType();

        if (!manager.hasLimit(material)) {
            return;
        }

        if (!manager.canEnchant(material)) {
            event.setCancelled(true);
            Player player = event.getEnchanter();
            int limit = manager.getLimit(material);
            int registered = manager.getRegisteredCount(material);
            player.sendMessage("§c[Vanilla Core] §7Cannot enchant " + material.name() + "! Limit reached: §e"
                    + registered + "/" + limit);

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Limited Enchants - Blocked enchantment for " + player.getName()
                        + " on " + material.name() + " (limit: " + limit + ")");
            }
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (manager.registerItem(item, event.getEnchanter())) {
                int registered = manager.getRegisteredCount(material);
                int limit = manager.getLimit(material);
                event.getEnchanter().sendMessage("§a[Vanilla Core] §7Registered enchanted " + material.name()
                        + " (§e" + registered + "/" + limit + "§7)");
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("features.limited-enchantment-slots.enabled")) {
            return;
        }

        if (!(event.getInventory() instanceof GrindstoneInventory)) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();
        ItemStack topItem = grindstone.getItem(0);
        ItemStack bottomItem = grindstone.getItem(1);

        ItemStack sourceItem = null;
        if (topItem != null && topItem.getType() != Material.AIR && !topItem.getEnchantments().isEmpty()) {
            sourceItem = topItem;
        } else if (bottomItem != null && bottomItem.getType() != Material.AIR
                && !bottomItem.getEnchantments().isEmpty()) {
            sourceItem = bottomItem;
        }

        if (sourceItem == null) {
            return;
        }

        if (result.getEnchantments().isEmpty() && !sourceItem.getEnchantments().isEmpty()) {
            ItemStack finalSourceItem = sourceItem;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (manager.isItemRegistered(finalSourceItem)) {
                    manager.unregisterItem(finalSourceItem);
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        player.sendMessage("§e[Vanilla Core] §7Enchantment slot released for "
                                + finalSourceItem.getType().name());

                        if (plugin.isVerbose()) {
                            plugin.getLogger().info("[VERBOSE] Limited Enchants - Slot released for "
                                    + player.getName() + " via grindstone (" + finalSourceItem.getType().name() + ")");
                        }
                    }
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("features.limited-enchantment-slots.enabled")) {
            return;
        }

        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            manager.scanPlayerItems(player);
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("features.limited-enchantment-slots.enabled")) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("features.limited-enchantment-slots.enabled")) {
            return;
        }

        if (event.getKeepInventory()) {
            return;
        }

        Player player = event.getEntity();
        for (ItemStack item : event.getDrops()) {
            if (item != null && item.getType() != Material.AIR && manager.isItemRegistered(item)) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().info(
                            "[VERBOSE] Limited Enchants - Player " + player.getName() + " died with registered item: "
                                    + item.getType().name() + " (item will be unregistered if it despawns)");
                }
            }
        }
    }
}
