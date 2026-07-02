package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class TeleportGuiListener implements Listener {

    private final VanillaCorePlugin plugin;

    public TeleportGuiListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof GuiHolder guiHolder)) {
            return;
        }

        String id = guiHolder.getId();
        if (id.startsWith("homes:")) {
            event.setCancelled(true);
            plugin.getTeleportManager().handleHomesGuiClick(player, event.getRawSlot());
        } else if (id.equals("warps")) {
            event.setCancelled(true);
            plugin.getTeleportManager().handleWarpsGuiClick(player, event.getRawSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof GuiHolder guiHolder) {
            String id = guiHolder.getId();
            if (id.startsWith("homes:") || id.equals("warps")) {
                plugin.getTeleportManager().clearGuiState(player.getUniqueId());
            }
        }
    }
}

