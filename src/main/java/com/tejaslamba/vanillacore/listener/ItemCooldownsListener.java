package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.ItemCooldownsFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ItemCooldownsListener implements Listener {

    private final VanillaCorePlugin plugin;

    public ItemCooldownsListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemCooldownsFeature feature = getFeature();
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Material material = event.getItem().getType();
        int cooldownTicks = feature.getCooldownTicks(material);
        if (cooldownTicks <= 0) {
            return;
        }

        if (player.getCooldown(material) > 0) {
            event.setCancelled(true);
            sendCooldownMessage(player, material);
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> player.setCooldown(material, cooldownTicks), 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        ItemCooldownsFeature feature = getFeature();
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType().isAir()) {
            return;
        }

        Player player = event.getPlayer();
        Material material = event.getItem().getType();

        if (material.isEdible()) {
            return;
        }

        int cooldownTicks = feature.getCooldownTicks(material);
        if (cooldownTicks <= 0) {
            return;
        }

        if (player.getCooldown(material) > 0) {
            event.setCancelled(true);
            sendCooldownMessage(player, material);
            return;
        }

        player.setCooldown(material, cooldownTicks);
    }

    private void sendCooldownMessage(Player player, Material material) {
        double remaining = Math.ceil(player.getCooldown(material) / 20.0D * 10.0D) / 10.0D;
        player.sendMessage(MessageManager.parse(
                "<red>[SMP Watchdog] <gray>This item is on cooldown for <yellow>" + remaining + "s<gray>."));
    }

    private ItemCooldownsFeature getFeature() {
        return plugin.getFeatureManager().getFeature(ItemCooldownsFeature.class);
    }
}

