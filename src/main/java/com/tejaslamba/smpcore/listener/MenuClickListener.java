package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.ItemLimiterFeature;
import com.tejaslamba.smpcore.features.MobSpawningFeature;
import com.tejaslamba.smpcore.features.NetheriteDisablerFeature;
import com.tejaslamba.smpcore.features.InfiniteRestockFeature;
import com.tejaslamba.smpcore.menu.MainMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuClickListener implements Listener {

    private final Main plugin;

    public MenuClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals(ItemLimiterFeature.MAIN_GUI_TITLE)) {
            event.setCancelled(true);
            handleItemLimiterMainGUI(event, player);
            return;
        }

        if (title.equals(ItemLimiterFeature.ADD_GUI_TITLE)) {
            handleItemLimiterAddGUI(event, player);
            return;
        }

        if (title.equals(ItemLimiterFeature.VIEW_GUI_TITLE)) {
            event.setCancelled(true);
            handleItemLimiterViewGUI(event, player);
            return;
        }

        if (title.equals(ItemLimiterFeature.BANNED_GUI_TITLE)) {
            event.setCancelled(true);
            handleItemLimiterBannedGUI(event, player);
            return;
        }

        if (title.equals(InfiniteRestockFeature.GUI_TITLE)) {
            event.setCancelled(true);
            handleInfiniteRestockGUI(event, player);
            return;
        }

        if (title.equals(NetheriteDisablerFeature.GUI_TITLE)) {
            event.setCancelled(true);
            handleNetheriteGUI(event, player);
            return;
        }

        if (title.equals(MobSpawningFeature.WORLD_SELECT_GUI_TITLE)) {
            event.setCancelled(true);
            handleWorldSelectGUI(event, player);
            return;
        }

        if (title.equals(MobSpawningFeature.SETTINGS_GUI_TITLE)) {
            event.setCancelled(true);
            handleMobSpawningSettingsGUI(event, player);
            return;
        }

        if (title.startsWith(MobSpawningFeature.GUI_TITLE)) {
            event.setCancelled(true);
            handleMobSpawningGUI(event, player);
            return;
        }

        if (title.equals(MobSpawningFeature.SPAWN_REASONS_GUI_TITLE)) {
            event.setCancelled(true);
            handleSpawnReasonsGUI(event, player);
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MainMenu mainMenu) {
            mainMenu.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals(ItemLimiterFeature.MAIN_GUI_TITLE) ||
                title.equals(ItemLimiterFeature.ADD_GUI_TITLE) ||
                title.equals(ItemLimiterFeature.VIEW_GUI_TITLE) ||
                title.equals(ItemLimiterFeature.BANNED_GUI_TITLE)) {

            ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
            if (feature != null) {
                feature.handleInventoryClose(player, title);
            }
        }
    }

    private void handleItemLimiterMainGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null) {
            player.closeInventory();
            return;
        }

        feature.handleMainMenuClick(event.getSlot(), player);
    }

    private void handleItemLimiterAddGUI(InventoryClickEvent event, Player player) {
        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null) {
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        feature.handleAddItemClick(event, player);
    }

    private void handleItemLimiterViewGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null) {
            player.closeInventory();
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

    private void handleItemLimiterBannedGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemLimiterFeature feature = plugin.getFeatureManager().getFeature(ItemLimiterFeature.class);
        if (feature == null) {
            player.closeInventory();
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

    private void handleInfiniteRestockGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);
        if (feature == null) {
            player.closeInventory();
            return;
        }

        feature.handleRestockGUIClick(event, player);
    }

    private void handleNetheriteGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager()
                .getFeature("Netherite Disabler");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            plugin.getMessageManager().sendPrefixed(player, "netherite-disabler.feature-disabled");
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

    private void handleMobSpawningGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        MobSpawningFeature feature = (MobSpawningFeature) plugin.getFeatureManager()
                .getFeature("Mob Spawning");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.feature-disabled");
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();
        int currentPage = feature.getPlayerPage(player);

        if (handleMobSpawningNavigation(player, feature, clickedType, slot, currentPage)) {
            return;
        }

        handleMobSpawningToggle(player, feature, clickedType, slot, currentPage);
    }

    private boolean handleMobSpawningNavigation(Player player, MobSpawningFeature feature,
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

    private void handleMobSpawningToggle(Player player, MobSpawningFeature feature,
            Material clickedType, int slot, int currentPage) {
        String selectedWorld = feature.getPlayerSelectedWorld(player);

        if (slot == 47 && clickedType == Material.LIME_DYE) {
            feature.setAllDisabled(false, selectedWorld);
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.enabled-all");
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, currentPage, selectedWorld));
            return;
        }

        if (slot == 51 && clickedType == Material.RED_DYE) {
            feature.setAllDisabled(true, selectedWorld);
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.disabled-all");
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

    private void handleWorldSelectGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        MobSpawningFeature feature = (MobSpawningFeature) plugin.getFeatureManager()
                .getFeature("Mob Spawning");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.feature-disabled");
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();
        int inventorySize = event.getInventory().getSize();

        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedType == Material.COMPARATOR && slot == inventorySize - 9) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
            return;
        }

        if (clickedType == Material.NETHER_STAR && slot == inventorySize - 5) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, 0, null));
            return;
        }

        if (clickedType == Material.GRASS_BLOCK || clickedType == Material.NETHERRACK
                || clickedType == Material.END_STONE || clickedType == Material.STONE) {
            String worldName = event.getCurrentItem().getItemMeta() != null
                    ? event.getCurrentItem().getItemMeta().getDisplayName().substring(2)
                    : null;
            if (worldName != null) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> feature.openMobGUI(player, 0, worldName));
                } else if (plugin.isVerbose()) {
                    plugin.getLogger().warning("World '" + worldName + "' no longer exists, ignoring selection");
                }
            }
        }
    }

    private void handleMobSpawningSettingsGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        MobSpawningFeature feature = (MobSpawningFeature) plugin.getFeatureManager()
                .getFeature("Mob Spawning");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.feature-disabled");
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
                plugin.getMessageManager().sendPrefixed(player, "mob-spawning.chunk-cleanup-enabled");
            } else {
                plugin.getMessageManager().sendPrefixed(player, "mob-spawning.chunk-cleanup-disabled");
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
            return;
        }

        if (slot == 15) {
            boolean newState = !feature.isWorldGuardBypass();
            feature.setWorldGuardBypass(newState);
            if (newState) {
                plugin.getMessageManager().sendPrefixed(player, "mob-spawning.worldguard-bypass-enabled");
            } else {
                plugin.getMessageManager().sendPrefixed(player, "mob-spawning.worldguard-bypass-disabled");
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openGlobalSettingsGUI(player));
            return;
        }
    }

    private void handleSpawnReasonsGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        MobSpawningFeature feature = (MobSpawningFeature) plugin.getFeatureManager()
                .getFeature("Mob Spawning");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            plugin.getMessageManager().sendPrefixed(player, "mob-spawning.feature-disabled");
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();

        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedType == Material.OAK_DOOR) {
            String selectedWorld = feature.getPlayerSelectedWorld(player);
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> feature.openMobGUI(player, 0, selectedWorld));
            return;
        }

        if (clickedType == Material.BOOK) {
            return;
        }

        if (slot < 45 && (clickedType == Material.LIME_STAINED_GLASS_PANE
                || clickedType == Material.RED_STAINED_GLASS_PANE)) {
            CreatureSpawnEvent.SpawnReason reason = feature.getSpawnReasonAtSlot(slot);
            if (reason != null) {
                boolean newState = !feature.isSpawnReasonAllowed(reason);
                feature.setSpawnReasonAllowed(reason, newState);
                plugin.getServer().getScheduler().runTask(plugin, () -> feature.openSpawnReasonsGUI(player));
            }
        }
    }
}
