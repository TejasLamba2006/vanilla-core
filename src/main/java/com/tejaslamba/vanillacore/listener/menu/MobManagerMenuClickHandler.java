package com.tejaslamba.vanillacore.listener.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MobManagerFeature;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MobManagerMenuClickHandler extends AbstractMenuClickHandler {

    private static final String MOB_MANAGER_DISABLED_KEY = "mob-manager.feature-disabled";

    public MobManagerMenuClickHandler(VanillaCorePlugin plugin) {
        super(plugin);
    }

    public void handleMobManagerMain(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        MobManagerFeature feature = getEnabledFeature(MobManagerFeature.class, player, MOB_MANAGER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();
        int currentPage = feature.getPlayerPage(player);

        if (handleMobManagerNavigation(player, feature, clickedType, slot, currentPage)) {
            return;
        }

        handleMobManagerToggle(player, feature, clickedType, slot, currentPage);
    }

    public void handleMobManagerSpawnReasons(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        MobManagerFeature feature = getEnabledFeature(MobManagerFeature.class, player, MOB_MANAGER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        if (handleSpawnReasonsNavigation(player, feature, clickedType)) {
            return;
        }

        handleSpawnReasonToggle(event.getRawSlot(), clickedType, player, feature);
    }

    public void handleMobManagerSettings(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (!isClickInTopInventory(event) || event.getCurrentItem() == null) {
            return;
        }

        MobManagerFeature feature = getEnabledFeature(MobManagerFeature.class, player, MOB_MANAGER_DISABLED_KEY);
        if (feature == null) {
            return;
        }

        int slot = event.getRawSlot();
        Material clickedType = event.getCurrentItem().getType();

        if (clickedType == Material.OAK_DOOR) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openWorldSelectGUI(player));
            return;
        }

        if (slot == 11) {
            boolean newState = !feature.isChunkCleanupEnabled();
            feature.setChunkCleanupEnabled(newState);
            if (newState) {
                plugin.getMessageManager().sendPrefixed(player, "mob-manager.chunk-cleanup-enabled");
            } else {
                plugin.getMessageManager().sendPrefixed(player, "mob-manager.chunk-cleanup-disabled");
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
            return;
        }

        if (slot == 15) {
            boolean newState = !feature.isWorldGuardBypass();
            feature.setWorldGuardBypass(newState);
            if (newState) {
                plugin.getMessageManager().sendPrefixed(player, "mob-manager.worldguard-bypass-enabled");
            } else {
                plugin.getMessageManager().sendPrefixed(player, "mob-manager.worldguard-bypass-disabled");
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
        }
    }

    private boolean handleMobManagerNavigation(Player player, MobManagerFeature feature,
            Material clickedType, int slot, int currentPage) {
        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return true;
        }

        String selectedWorld = feature.getPlayerSelectedWorld(player);

        if (clickedType == Material.OAK_DOOR) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openWorldSelectGUI(player));
            return true;
        }

        if (slot == 45 && clickedType == Material.ARROW) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, currentPage - 1, selectedWorld));
            return true;
        }

        if (slot == 53 && clickedType == Material.ARROW) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, currentPage + 1, selectedWorld));
            return true;
        }

        if (slot == 48 && clickedType == Material.SPAWNER) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openSpawnReasonsGUI(player));
            return true;
        }

        return false;
    }

    private void handleMobManagerToggle(Player player, MobManagerFeature feature,
            Material clickedType, int slot, int currentPage) {
        String selectedWorld = feature.getPlayerSelectedWorld(player);

        if (slot == 47 && clickedType == Material.LIME_DYE) {
            feature.setAllDisabled(false, selectedWorld);
            plugin.getMessageManager().sendPrefixed(player, "mob-manager.enabled-all");
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, currentPage, selectedWorld));
            return;
        }

        if (slot == 51 && clickedType == Material.RED_DYE) {
            feature.setAllDisabled(true, selectedWorld);
            plugin.getMessageManager().sendPrefixed(player, "mob-manager.disabled-all");
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, currentPage, selectedWorld));
            return;
        }

        if (slot < 45) {
            EntityType entityType = feature.getEntityTypeAtSlot(currentPage, slot);
            if (entityType != null) {
                boolean newState = !feature.isDisabled(entityType, selectedWorld);
                feature.setDisabled(entityType, newState, selectedWorld);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> feature.openMobGUI(player, currentPage, selectedWorld));
            }
        }
    }

    private boolean handleSpawnReasonsNavigation(Player player, MobManagerFeature feature, Material clickedType) {
        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return true;
        }

        if (clickedType == Material.OAK_DOOR) {
            String selectedWorld = feature.getPlayerSelectedWorld(player);
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, 0, selectedWorld));
            return true;
        }

        return clickedType == Material.BOOK;
    }

    private void handleSpawnReasonToggle(int slot, Material clickedType, Player player, MobManagerFeature feature) {
        if (slot >= 45 || !isSpawnReasonTogglePane(clickedType)) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason = feature.getSpawnReasonAtSlot(slot);
        if (reason == null) {
            return;
        }

        boolean newState = !feature.isSpawnReasonAllowed(reason);
        feature.setSpawnReasonAllowed(reason, newState);
        plugin.getServer().getScheduler().runTask(plugin, () -> feature.openSpawnReasonsGUI(player));
    }

    private boolean isSpawnReasonTogglePane(Material clickedType) {
        return clickedType == Material.LIME_STAINED_GLASS_PANE || clickedType == Material.RED_STAINED_GLASS_PANE;
    }
}
