package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.TntMinecartBanFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TntMinecartBanListener implements Listener {

    private final VanillaCorePlugin plugin;

    public TntMinecartBanListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        TntMinecartBanFeature feature = plugin.getFeatureManager().getFeature(TntMinecartBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getDamager().getType() == EntityType.TNT_MINECART) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        TntMinecartBanFeature feature = plugin.getFeatureManager().getFeature(TntMinecartBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getEntityType() == EntityType.TNT_MINECART) {
            event.setCancelled(true);
        }
    }
}
