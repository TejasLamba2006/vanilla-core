package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.VillagerKillingBanFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class VillagerKillingBanListener implements Listener {

    private final VanillaCorePlugin plugin;

    public VillagerKillingBanListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        VillagerKillingBanFeature feature = plugin.getFeatureManager().getFeature(VillagerKillingBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getEntity() instanceof Villager && event.getDamager() instanceof Player player) {
            event.setCancelled(true);
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Killing villagers is disabled."));
        }
    }
}