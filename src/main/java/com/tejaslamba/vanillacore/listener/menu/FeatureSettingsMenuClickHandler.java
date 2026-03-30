package com.tejaslamba.vanillacore.listener.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.InfiniteRestockFeature;
import com.tejaslamba.vanillacore.features.ItemCooldownsFeature;
import com.tejaslamba.vanillacore.features.NetheriteDisablerFeature;
import com.tejaslamba.vanillacore.features.PotionBansFeature;
import com.tejaslamba.vanillacore.features.RitualFeature;
import com.tejaslamba.vanillacore.features.ShieldMechanicsFeature;
import com.tejaslamba.vanillacore.menu.MainMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FeatureSettingsMenuClickHandler extends AbstractMenuClickHandler {

    public FeatureSettingsMenuClickHandler(VanillaCorePlugin plugin) {
        super(plugin);
    }

    public void handleInfiniteRestock(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        InfiniteRestockFeature feature = getEnabledFeature(InfiniteRestockFeature.class, player,
                "infinite-restock.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleRestockGUIClick(event, player);
    }

    public void handleInfiniteRestockBlacklist(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        InfiniteRestockFeature feature = getEnabledFeature(InfiniteRestockFeature.class, player,
                "infinite-restock.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleBlacklistGUIClick(event, player);
    }

    public void handleNetheriteDisabler(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        NetheriteDisablerFeature feature = getEnabledFeature(NetheriteDisablerFeature.class, player,
                "netherite-disabler.feature-disabled");
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();

        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedType == Material.ARROW) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MainMenu mainMenu = new MainMenu(plugin);
                mainMenu.open(player);
            });
            return;
        }

        Material netheriteItem = feature.getSlotMapping().get(event.getRawSlot());
        if (netheriteItem != null) {
            boolean newState = !feature.isDisabled(netheriteItem);
            feature.setDisabled(netheriteItem, newState);
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openNetheriteGUI(player));
        }
    }

    public void handleShieldMechanics(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)
                || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ShieldMechanicsFeature feature = getEnabledFeature(ShieldMechanicsFeature.class, player,
                "shield-mechanics.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleSettingsGUIClick(event.getRawSlot(), event.isShiftClick(), player);
    }

    public void handleRitualSettings(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)
                || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        RitualFeature feature = getEnabledFeature(RitualFeature.class, player, "ritual.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleSettingsGUIClick(event.getRawSlot(), event.isShiftClick(), event.isRightClick(), player);
    }

    public void handlePotionBansSettings(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)
                || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        PotionBansFeature feature = getEnabledFeature(PotionBansFeature.class, player, "potion-bans.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleSettingsGUIClick(event.getRawSlot(), event.isRightClick(), player);
    }

    public void handleItemCooldowns(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event)
                || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemCooldownsFeature feature = getEnabledFeature(ItemCooldownsFeature.class, player,
                "item-cooldowns.feature-disabled");
        if (feature == null) {
            return;
        }

        feature.handleSettingsGUIClick(event.getRawSlot(), event.isShiftClick(), event.isRightClick(), player);
    }
}
