package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.MobSpawningFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawningListener implements Listener {

    private final Main plugin;
    private final MobSpawningFeature feature;

    public MobSpawningListener(Main plugin, MobSpawningFeature feature) {
        this.plugin = plugin;
        this.feature = feature;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!feature.isEnabled()) {
            return;
        }

        EntityType entityType = event.getEntityType();

        if (feature.isDisabled(entityType)) {
            CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

            if (feature.isSpawnReasonAllowed(reason)) {
                return;
            }

            event.setCancelled(true);

            boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Mob Spawning - Blocked spawn of " + entityType.name()
                        + " (reason: " + reason.name() + ")");
            }
        }
    }
}
