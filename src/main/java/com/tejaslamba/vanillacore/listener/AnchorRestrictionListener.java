package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.AnchorRestrictionFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class AnchorRestrictionListener implements Listener {

    private final VanillaCorePlugin plugin;

    public AnchorRestrictionListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        AnchorRestrictionFeature feature = plugin.getFeatureManager().getFeature(AnchorRestrictionFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType() != Material.GLOWSTONE) {
            return;
        }

        if (event.getPlayer().getWorld().getEnvironment() != World.Environment.NETHER) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessageManager().get("anchor-restriction.blocked"));
        }
    }
}