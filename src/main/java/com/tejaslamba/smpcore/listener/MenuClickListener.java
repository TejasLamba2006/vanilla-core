package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.NetheriteDisablerFeature;
import com.tejaslamba.smpcore.menu.MainMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        if (title.equals("§8Netherite Item Manager")) {
            event.setCancelled(true);
            handleNetheriteGUI(event, player);
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
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MainMenu mainMenu = new MainMenu(plugin);
                player.openInventory(mainMenu.getInventory());
            });
            return;
        }

        Material netheriteItem = feature.getSlotMapping().get(event.getRawSlot());
        if (netheriteItem != null) {
            boolean newState = !feature.isDisabled(netheriteItem);
            feature.setDisabled(netheriteItem, newState);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                feature.openNetheriteGUI(player);
            });
        }
    }

}
