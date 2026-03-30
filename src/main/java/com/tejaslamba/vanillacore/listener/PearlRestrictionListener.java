package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.PearlRestrictionFeature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PearlRestrictionListener implements Listener {

    private final VanillaCorePlugin plugin;

    public PearlRestrictionListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        PearlRestrictionFeature feature = plugin.getFeatureManager().getFeature(PearlRestrictionFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessageManager().get("pearl-restriction.blocked"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PearlRestrictionFeature feature = plugin.getFeatureManager().getFeature(PearlRestrictionFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
            event.getPlayer()
                    .sendMessage(plugin.getMessageManager().get("pearl-restriction.blocked"));
        }
    }
}