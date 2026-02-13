package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.InfiniteRestockFeature;
import com.tejaslamba.vanillacore.infiniterestock.InfiniteRestockManager;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class InfiniteRestockListener implements Listener {

    private final Main plugin;

    public InfiniteRestockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMerchantOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);

        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) {
            return;
        }

        if (!(merchantInv.getHolder() instanceof AbstractVillager villager)) {
            return;
        }

        InfiniteRestockManager manager = ((InfiniteRestockFeature) feature).getManager();
        manager.applyOnMerchantOpen(villager);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Infinite Restock - Set infinite trades on merchant open");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTradeClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);

        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) {
            return;
        }

        if (!(merchantInv.getHolder() instanceof AbstractVillager villager)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        InfiniteRestockManager manager = ((InfiniteRestockFeature) feature).getManager();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            manager.applyOnMerchantOpen(villager);
            player.updateInventory();
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractVillager(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof AbstractVillager villager)) {
            return;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        InfiniteRestockManager manager = feature.getManager();
        manager.applyOnInteract(villager);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillagerAcquireTrade(org.bukkit.event.entity.VillagerAcquireTradeEvent event) {
        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }
        AbstractVillager villager = event.getEntity();
        InfiniteRestockManager manager = feature.getManager();
        manager.applyOnVillagerAcquireTrade(villager);
    }
}
