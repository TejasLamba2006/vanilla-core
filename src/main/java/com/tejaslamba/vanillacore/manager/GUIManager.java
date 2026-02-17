package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.gui.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {

    private final Main plugin;
    private final Map<UUID, InventoryHolder> activeGUIs = new HashMap<>();

    public GUIManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerGUI(Player player, InventoryHolder gui) {
        activeGUIs.put(player.getUniqueId(), gui);
    }

    public void unregisterGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }

    public InventoryHolder getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ConfigMenuGUI) {
            event.setCancelled(true);
            ((ConfigMenuGUI) holder).handleClick(event);
        } else if (holder instanceof BooleanToggleGUI) {
            event.setCancelled(true);
            ((BooleanToggleGUI) holder).handleClick(event);
        } else if (holder instanceof ListEditorGUI) {
            event.setCancelled(true);
            ((ListEditorGUI) holder).handleClick(event);
        } else if (holder instanceof ItemSelectorGUI) {
            event.setCancelled(true);
            ((ItemSelectorGUI) holder).handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ConfigMenuGUI) {
            ConfigMenuGUI gui = (ConfigMenuGUI) holder;
            if (gui.isDirty) {
                player.sendMessage("§e[Vanilla Core] You have unsaved changes. They were not saved.");
            }
        }
    }
}
