package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.BedBombingBanFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BedBombingBanListener implements Listener {

    private final VanillaCorePlugin plugin;

    public BedBombingBanListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        BedBombingBanFeature feature = plugin.getFeatureManager().getFeature(BedBombingBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (!Tag.BEDS.isTagged(event.getClickedBlock().getType())) {
            return;
        }

        World.Environment environment = event.getPlayer().getWorld().getEnvironment();
        if (environment == World.Environment.NETHER || environment == World.Environment.THE_END) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Bed bombing is disabled."));
        }
    }
}