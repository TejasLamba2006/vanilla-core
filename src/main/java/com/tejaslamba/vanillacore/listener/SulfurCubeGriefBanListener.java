package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.SulfurCubeGriefBanFeature;
import org.bukkit.entity.SulfurCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SulfurCubeGriefBanListener implements Listener {

    private final VanillaCorePlugin plugin;

    public SulfurCubeGriefBanListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        SulfurCubeGriefBanFeature feature = plugin.getFeatureManager().getFeature(SulfurCubeGriefBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof SulfurCube)) {
            return;
        }

        event.blockList().clear();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Sulfur Cube Grief Ban - Cleared block damage from explosion at "
                    + event.getLocation());
        }
    }
}
