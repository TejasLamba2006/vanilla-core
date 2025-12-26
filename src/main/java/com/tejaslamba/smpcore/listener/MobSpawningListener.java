package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.MobSpawningFeature;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class MobSpawningListener implements Listener {

    private static final String VERBOSE_PREFIX = "[VERBOSE] Mob Spawning - ";
    private static final long CHUNK_CLEANUP_DELAY = 5L;

    private final Main plugin;
    private final MobSpawningFeature feature;
    private final WorldGuardHook worldGuardHook;

    public MobSpawningListener(Main plugin, MobSpawningFeature feature) {
        this.plugin = plugin;
        this.feature = feature;
        this.worldGuardHook = new WorldGuardHook(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!feature.isEnabled()) {
            return;
        }

        EntityType entityType = event.getEntityType();
        World world = event.getLocation().getWorld();

        if (!feature.isDisabledInWorld(entityType, world)) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        if (feature.isSpawnReasonAllowed(reason)) {
            return;
        }

        if (feature.isWorldGuardBypass() && worldGuardHook.isInProtectedRegion(event.getLocation())) {
            if (plugin.isVerbose()) {
                plugin.getLogger().info(VERBOSE_PREFIX + "Allowed spawn of " + entityType.name()
                        + " in WorldGuard protected region");
            }
            return;
        }

        event.setCancelled(true);

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Blocked spawn of " + entityType.name()
                    + " in world " + world.getName() + " (reason: " + reason.name() + ")");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        feature.cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!feature.isEnabled() || !feature.isChunkCleanupEnabled()) {
            return;
        }

        World world = event.getWorld();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!plugin.isEnabled() || !feature.isEnabled() || !feature.isChunkCleanupEnabled()) {
                return;
            }

            if (!event.getChunk().isLoaded()) {
                return;
            }

            int removedCount = 0;
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof LivingEntity && shouldRemoveEntity(entity, world)) {
                    entity.remove();
                    removedCount++;
                }
            }

            if (removedCount > 0 && plugin.isVerbose()) {
                plugin.getLogger().info(VERBOSE_PREFIX + "Chunk cleanup removed " + removedCount
                        + " disabled mobs from chunk at " + event.getChunk().getX() + ", "
                        + event.getChunk().getZ() + " in world " + world.getName());
            }
        }, CHUNK_CLEANUP_DELAY);
    }

    private boolean shouldRemoveEntity(Entity entity, World world) {
        EntityType entityType = entity.getType();

        if (!feature.isDisabledInWorld(entityType, world)) {
            return false;
        }

        if (isProtectedEntity(entity)) {
            return false;
        }

        if (feature.isWorldGuardBypass() && worldGuardHook.isInProtectedRegion(entity.getLocation())) {
            return false;
        }

        return true;
    }

    private boolean isProtectedEntity(Entity entity) {
        if (entity.getCustomName() != null) {
            return true;
        }

        if (entity instanceof Tameable tameable && tameable.isTamed()) {
            return true;
        }

        if (entity.isPersistent()) {
            return true;
        }

        if (!entity.getPersistentDataContainer().isEmpty()) {
            return true;
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.isLeashed()) {
                return true;
            }
            var equipment = livingEntity.getEquipment();
            if (equipment != null) {
                for (var item : equipment.getArmorContents()) {
                    if (item != null && !item.getType().isAir()) {
                        return true;
                    }
                }
                var mainHand = equipment.getItemInMainHand();
                var offHand = equipment.getItemInOffHand();
                if ((mainHand != null && !mainHand.getType().isAir()) ||
                        (offHand != null && !offHand.getType().isAir())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static class WorldGuardHook {

        private final Main plugin;
        private final boolean worldGuardAvailable;
        private Object worldGuard;

        private java.lang.reflect.Method adaptWorldMethod;
        private java.lang.reflect.Method blockVectorAtMethod;
        private java.lang.reflect.Method getPlatformMethod;
        private java.lang.reflect.Method getRegionContainerMethod;
        private java.lang.reflect.Method getRegionManagerMethod;
        private java.lang.reflect.Method getApplicableRegionsMethod;
        private java.lang.reflect.Method sizeMethod;
        private Class<?> blockVector3Class;

        public WorldGuardHook(Main plugin) {
            this.plugin = plugin;
            this.worldGuardAvailable = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;

            if (worldGuardAvailable) {
                try {
                    Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                    worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);

                    Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                    blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                    Class<?> weWorldClass = Class.forName("com.sk89q.worldedit.world.World");

                    adaptWorldMethod = bukkitAdapterClass.getMethod("adapt", World.class);
                    blockVectorAtMethod = blockVector3Class.getMethod("at", int.class, int.class, int.class);
                    getPlatformMethod = worldGuard.getClass().getMethod("getPlatform");

                    Object platform = getPlatformMethod.invoke(worldGuard);
                    getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");

                    Object regionContainer = getRegionContainerMethod.invoke(platform);
                    getRegionManagerMethod = regionContainer.getClass().getMethod("get", weWorldClass);

                    plugin.getLogger().info("WorldGuard integration enabled for Mob Spawning");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to hook into WorldGuard: " + e.getMessage());
                    worldGuard = null;
                }
            }
        }

        public boolean isInProtectedRegion(org.bukkit.Location location) {
            if (!worldGuardAvailable || worldGuard == null) {
                return false;
            }

            try {
                Object weWorld = adaptWorldMethod.invoke(null, location.getWorld());
                Object blockVector = blockVectorAtMethod.invoke(null,
                        location.getBlockX(), location.getBlockY(), location.getBlockZ());

                Object platform = getPlatformMethod.invoke(worldGuard);
                Object regionContainer = getRegionContainerMethod.invoke(platform);
                Object regionManager = getRegionManagerMethod.invoke(regionContainer, weWorld);

                if (regionManager == null) {
                    return false;
                }

                if (getApplicableRegionsMethod == null) {
                    getApplicableRegionsMethod = regionManager.getClass()
                            .getMethod("getApplicableRegions", blockVector3Class);
                }

                Object applicableRegions = getApplicableRegionsMethod.invoke(regionManager, blockVector);

                if (sizeMethod == null) {
                    sizeMethod = applicableRegions.getClass().getMethod("size");
                }

                int size = (int) sizeMethod.invoke(applicableRegions);
                return size > 0;
            } catch (Exception e) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().warning("WorldGuard region check failed: " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                }
                return false;
            }
        }
    }
}
