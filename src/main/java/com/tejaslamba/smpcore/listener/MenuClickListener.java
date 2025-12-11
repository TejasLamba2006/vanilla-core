package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.MobSpawningFeature;
import com.tejaslamba.smpcore.features.NetheriteDisablerFeature;
import com.tejaslamba.smpcore.menu.MainMenu;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuClickListener implements Listener {

    private final Main plugin;

    public MenuClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals(NetheriteDisablerFeature.GUI_TITLE)) {
            event.setCancelled(true);
            handleNetheriteGUI(event, player);
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

    private void handleNetheriteGUI(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) {
            return;
        }

        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager()
                .getFeature("Netherite Disabler");
        if (feature == null || !feature.isEnabled()) {
            player.closeInventory();
            player.sendMessage("§cNetherite Disabler is disabled!");
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
            player.sendMessage("§cMob Spawning Manager is disabled!");
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

        if (clickedType == Material.OAK_DOOR) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MainMenu mainMenu = new MainMenu(plugin);
                mainMenu.open(player);
            });
            return true;
        }

        if (slot == 45 && clickedType == Material.ARROW) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, currentPage - 1));
            return true;
        }

        if (slot == 53 && clickedType == Material.ARROW) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, currentPage + 1));
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
        if (slot == 47 && clickedType == Material.LIME_DYE) {
            feature.setAllDisabled(false);
            player.sendMessage("§a[SMP] §7Enabled spawning for all mobs!");
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, currentPage));
            return;
        }

        if (slot == 51 && clickedType == Material.RED_DYE) {
            feature.setAllDisabled(true);
            player.sendMessage("§c[SMP] §7Disabled spawning for all mobs!");
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, currentPage));
            return;
        }

        if (slot < 45 && clickedType.name().endsWith("_SPAWN_EGG")) {
            EntityType entityType = feature.getEntityTypeAtSlot(currentPage, slot);
            if (entityType != null) {
                boolean newState = !feature.isDisabled(entityType);
                feature.setDisabled(entityType, newState);
                plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, currentPage));
            }
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
            player.sendMessage("§cMob Spawning Manager is disabled!");
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        int slot = event.getRawSlot();

        if (clickedType == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clickedType == Material.OAK_DOOR) {
            plugin.getServer().getScheduler().runTask(plugin, () -> feature.openMobGUI(player, 0));
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
