package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.FasterHappyGhastsFeature;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class FasterHappyGhastsListener implements Listener {

    private final VanillaCorePlugin plugin;

    public FasterHappyGhastsListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!"HAPPY_GHAST".equalsIgnoreCase(event.getEntityType().name())) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        FasterHappyGhastsFeature feature = plugin.getFeatureManager().getFeature(FasterHappyGhastsFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> applyFlyingSpeed(livingEntity, feature), 1L);
    }

    private void applyFlyingSpeed(LivingEntity entity, FasterHappyGhastsFeature feature) {
        if (!entity.isValid()) {
            return;
        }

        AttributeInstance attributeInstance = entity.getAttribute(Attribute.FLYING_SPEED);
        if (attributeInstance == null) {
            return;
        }

        attributeInstance.setBaseValue(feature.getFlyingSpeed());

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Faster Happy Ghasts - Applied flying speed "
                    + feature.getFlyingSpeed() + " to entity " + entity.getUniqueId());
        }
    }
}

