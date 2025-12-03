package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class ItemBanListener implements Listener {

    private final Main plugin;

    public ItemBanListener(Main plugin) {
        this.plugin = plugin;
    }

    private String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("§", "&"));
    }

    private void sendBanMessage(Player player) {
        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        player.sendMessage(formatMessage(prefix + " §cThis item is banned!"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (plugin.getBanManager().isItemBanned(item.getType())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.getInventory().remove(item);
            player.updateInventory();
            sendBanMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            return;
        }

        if (plugin.getBanManager().isItemBanned(item.getType())) {
            event.setCancelled(true);
            player.getInventory().remove(item);
            player.updateInventory();
            sendBanMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Material itemType = event.getItem().getItemStack().getType();

        if (plugin.getBanManager().isItemBanned(itemType)) {
            event.setCancelled(true);
            event.getItem().remove();
            sendBanMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (plugin.getBanManager().isItemBanned(item.getType())) {
            event.setCancelled(true);
            player.getInventory().remove(item);
            player.updateInventory();
            sendBanMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (plugin.getBanManager().isItemBanned(item.getType())) {
            event.setCancelled(true);
            player.getInventory().remove(item);
            player.updateInventory();
            sendBanMessage(player);
        }
    }

}
