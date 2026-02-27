package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MinimapControlFeature;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MinimapControlListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final MinimapControlFeature feature;

    public MinimapControlListener(VanillaCorePlugin plugin, MinimapControlFeature feature) {
        this.plugin = plugin;
        this.feature = feature;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!feature.isEnabled() || !feature.isSendOnJoin())
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                feature.sendMinimapSettings(event.getPlayer());
            }
        }, 40L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        feature.cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!feature.isEnabled() || !feature.isSendOnWorldChange())
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                feature.sendMinimapSettings(event.getPlayer());
            }
        }, 5L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        String title = event.getView().getTitle();

        if (title.equals(MinimapControlFeature.GUI_TITLE)) {
            event.setCancelled(true);
            handleMainGUIClick(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(MinimapControlFeature.WORLD_SETTINGS_GUI_TITLE)) {
            event.setCancelled(true);
            handleWorldSettingsClick(player, event.getSlot(), event.getCurrentItem(), event.isRightClick());
        }
    }

    private void handleMainGUIClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return;

        switch (slot) {
            case 13 -> {
                feature.cycleGlobalMode();
                player.sendMessage(plugin.getMessageManager().get("minimap-control.mode-changed",
                        "mode", feature.getGlobalMode().name()));
                feature.openMainGUI(player);
            }
            case 20 -> {
                feature.setNetherFairMode(!feature.isNetherFairMode());
                player.sendMessage(plugin.getMessageManager().get("minimap-control.nether-fair-toggled",
                        "state", feature.isNetherFairMode() ? "Enabled" : "Disabled"));
                feature.openMainGUI(player);
            }
            case 22 -> {
                feature.setSendOnJoin(!feature.isSendOnJoin());
                player.sendMessage(plugin.getMessageManager().get("minimap-control.send-on-join-toggled",
                        "state", feature.isSendOnJoin() ? "Enabled" : "Disabled"));
                feature.openMainGUI(player);
            }
            case 24 -> {
                feature.setSendOnWorldChange(!feature.isSendOnWorldChange());
                player.sendMessage(plugin.getMessageManager().get("minimap-control.send-on-world-change-toggled",
                        "state", feature.isSendOnWorldChange() ? "Enabled" : "Disabled"));
                feature.openMainGUI(player);
            }
            case 30 -> feature.openWorldSettingsGUI(player);
            case 32 -> {
                feature.sendToAllPlayers();
                player.sendMessage(plugin.getMessageManager().get("minimap-control.applied-to-all"));
            }
            case 35 -> player.closeInventory();
        }
    }

    private void handleWorldSettingsClick(Player player, int slot, ItemStack item, boolean rightClick) {
        if (item == null || item.getType() == Material.AIR)
            return;

        int inventorySize = player.getOpenInventory().getTopInventory().getSize();

        if (slot == inventorySize - 5) {
            feature.openMainGUI(player);
            return;
        }

        if (slot == inventorySize - 1) {
            player.closeInventory();
            return;
        }

        List<World> worlds = Bukkit.getWorlds();
        if (slot < worlds.size()) {
            World world = worlds.get(slot);

            if (rightClick) {
                feature.clearWorldMode(world.getName());
                player.sendMessage(plugin.getMessageManager().get("minimap-control.world-mode-reset",
                        "world", world.getName()));
            } else {
                feature.cycleWorldMode(world.getName());
                player.sendMessage(plugin.getMessageManager().get("minimap-control.world-mode-changed",
                        "world", world.getName(), "mode", feature.getWorldMode(world.getName()).name()));
            }

            feature.openWorldSettingsGUI(player);
        }
    }
}
