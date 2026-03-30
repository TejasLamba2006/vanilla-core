package com.tejaslamba.vanillacore.listener.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MobManagerFeature;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MobManagerWorldSelectMenuClickHandler extends AbstractMenuClickHandler {

    private static final String MOB_MANAGER_DISABLED_KEY = "mob-manager.feature-disabled";

    public MobManagerWorldSelectMenuClickHandler(VanillaCorePlugin plugin) {
        super(plugin);
    }

    public void handleMobManagerWorldSelect(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        MobManagerFeature feature = getEnabledFeature(MobManagerFeature.class, player, MOB_MANAGER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        if (handleNavigationClick(event, player, feature)) {
            return;
        }

        handleWorldSelectionClick(event.getCurrentItem(), player, feature);
    }

    private boolean handleNavigationClick(InventoryClickEvent event, Player player, MobManagerFeature feature) {
        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();
        int inventorySize = event.getInventory().getSize();

        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return true;
        }

        if (clickedType == Material.ARROW && slot == inventorySize - 3) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMenuManager().openMainMenu(player));
            return true;
        }

        if (clickedType == Material.COMPARATOR && slot == inventorySize - 9) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
            return true;
        }

        if (clickedType == Material.NETHER_STAR && slot == inventorySize - 5) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, 0, null));
            return true;
        }

        return false;
    }

    private void handleWorldSelectionClick(ItemStack currentItem, Player player, MobManagerFeature feature) {
        Material clickedType = currentItem.getType();
        if (!isWorldSelectionMaterial(clickedType)) {
            return;
        }

        String worldName = currentItem.getItemMeta() != null
                ? currentItem.getItemMeta().getDisplayName().substring(2)
                : null;
        if (worldName == null) {
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, 0, worldName));
        } else if (plugin.isVerbose()) {
            plugin.getLogger().warning("World '" + worldName + "' no longer exists, ignoring selection");
        }
    }

    private boolean isWorldSelectionMaterial(Material material) {
        return material == Material.GRASS_BLOCK
                || material == Material.NETHERRACK
                || material == Material.END_STONE
                || material == Material.STONE;
    }
}
