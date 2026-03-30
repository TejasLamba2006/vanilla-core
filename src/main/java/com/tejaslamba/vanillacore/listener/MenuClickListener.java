package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.EnderChestItemLimiterFeature;
import com.tejaslamba.vanillacore.features.ItemLimiterFeature;
import com.tejaslamba.vanillacore.listener.menu.FeatureSettingsMenuClickHandler;
import com.tejaslamba.vanillacore.listener.menu.ItemLimiterMenuClickHandler;
import com.tejaslamba.vanillacore.listener.menu.MobManagerMenuClickHandler;
import com.tejaslamba.vanillacore.listener.menu.MobManagerWorldSelectMenuClickHandler;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import com.tejaslamba.vanillacore.menu.MainMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MenuClickListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final ItemLimiterMenuClickHandler itemLimiterHandler;
    private final MobManagerMenuClickHandler mobManagerHandler;
    private final MobManagerWorldSelectMenuClickHandler mobManagerWorldSelectHandler;
    private final FeatureSettingsMenuClickHandler featureSettingsHandler;
    private final Map<String, BiConsumer<InventoryClickEvent, Player>> guiHandlers;

    public MenuClickListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.itemLimiterHandler = new ItemLimiterMenuClickHandler(plugin);
        this.mobManagerHandler = new MobManagerMenuClickHandler(plugin);
        this.mobManagerWorldSelectHandler = new MobManagerWorldSelectMenuClickHandler(plugin);
        this.featureSettingsHandler = new FeatureSettingsMenuClickHandler(plugin);
        this.guiHandlers = createGuiHandlers();
    }

    private Map<String, BiConsumer<InventoryClickEvent, Player>> createGuiHandlers() {
        Map<String, BiConsumer<InventoryClickEvent, Player>> handlers = new HashMap<>();
        handlers.put("item-limiter-main", itemLimiterHandler::handleItemLimiterMain);
        handlers.put("item-limiter-add", itemLimiterHandler::handleItemLimiterAdd);
        handlers.put("item-limiter-view", itemLimiterHandler::handleItemLimiterView);
        handlers.put("item-limiter-banned", itemLimiterHandler::handleItemLimiterBanned);
        handlers.put("ender-chest-limiter-main", itemLimiterHandler::handleEnderChestMain);
        handlers.put("ender-chest-limiter-add", itemLimiterHandler::handleEnderChestAdd);
        handlers.put("ender-chest-limiter-view", itemLimiterHandler::handleEnderChestView);
        handlers.put("infinite-restock", featureSettingsHandler::handleInfiniteRestock);
        handlers.put("infinite-restock-blacklist", featureSettingsHandler::handleInfiniteRestockBlacklist);
        handlers.put("netherite-disabler", featureSettingsHandler::handleNetheriteDisabler);
        handlers.put("mob-manager-world-select", mobManagerWorldSelectHandler::handleMobManagerWorldSelect);
        handlers.put("mob-manager", mobManagerHandler::handleMobManagerMain);
        handlers.put("mob-manager-spawn-reasons", mobManagerHandler::handleMobManagerSpawnReasons);
        handlers.put("mob-manager-settings", mobManagerHandler::handleMobManagerSettings);
        handlers.put("shield-mechanics", featureSettingsHandler::handleShieldMechanics);
        handlers.put("potion-bans-settings", featureSettingsHandler::handlePotionBansSettings);
        handlers.put("ritual-settings", featureSettingsHandler::handleRitualSettings);
        handlers.put("item-cooldowns", featureSettingsHandler::handleItemCooldowns);
        return handlers;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof MainMenu mainMenu) {
            mainMenu.handleClick(event);
            return;
        }

        if (holder instanceof GuiHolder gui) {
            BiConsumer<InventoryClickEvent, Player> handler = guiHandlers.get(gui.getId());
            if (handler != null) {
                handler.accept(event, player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof GuiHolder gui && gui.getId().startsWith("item-limiter")) {
            ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
            if (feature != null) {
                feature.handleInventoryClose(player, gui.getId());
            }
        } else if (holder instanceof GuiHolder gui && gui.getId().startsWith("ender-chest-limiter")) {
            EnderChestItemLimiterFeature feature = plugin.getFeatureManager()
                    .getFeature(EnderChestItemLimiterFeature.class);
            if (feature != null) {
                feature.handleInventoryClose(player, gui.getId());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof GuiHolder || holder instanceof MainMenu) {
            event.setCancelled(true);
        }
    }
}
