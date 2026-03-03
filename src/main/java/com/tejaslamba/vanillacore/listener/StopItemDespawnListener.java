package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.StopItemDespawnFeature;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StopItemDespawnListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final Map<UUID, Location> recentDeaths = new HashMap<>();
    private final Set<UUID> deathDrops = new HashSet<>();

    public StopItemDespawnListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isEnabled()) {
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        Location deathLoc = event.getPlayer().getLocation();
        recentDeaths.put(playerId, deathLoc);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentDeaths.remove(playerId), 60L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!isEnabled()) {
            return;
        }

        Location spawnLoc = event.getLocation();
        for (Location deathLoc : recentDeaths.values()) {
            if (deathLoc.getWorld() != null
                    && deathLoc.getWorld().equals(spawnLoc.getWorld())
                    && spawnLoc.distanceSquared(deathLoc) < 16) {
                deathDrops.add(event.getEntity().getUniqueId());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (deathDrops.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        if (deathDrops.contains(event.getEntity().getUniqueId())) {
            deathDrops.add(event.getTarget().getUniqueId());
            deathDrops.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        deathDrops.remove(event.getItem().getUniqueId());
    }

    private boolean isEnabled() {
        StopItemDespawnFeature feature = plugin.getFeatureManager().getFeature(StopItemDespawnFeature.class);
        return feature != null && feature.isEnabled();
    }
}
