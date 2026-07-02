package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.CrystalPvpBanFeature;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CrystalPvpBanListener implements Listener {

    private final VanillaCorePlugin plugin;

    public CrystalPvpBanListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        CrystalPvpBanFeature feature = plugin.getFeatureManager().getFeature(CrystalPvpBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (event.getItem() != null && event.getItem().getType() == Material.END_CRYSTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessageManager().get("crystal-pvp-ban.blocked"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        CrystalPvpBanFeature feature = plugin.getFeatureManager().getFeature(CrystalPvpBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getDamager() instanceof EnderCrystal || event.getEntity() instanceof EnderCrystal) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player player) {
                player.sendMessage(plugin.getMessageManager().get("crystal-pvp-ban.blocked"));
            }
            if (event.getEntity() instanceof Player player) {
                player.sendMessage(plugin.getMessageManager().get("crystal-pvp-ban.blocked"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        CrystalPvpBanFeature feature = plugin.getFeatureManager().getFeature(CrystalPvpBanFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getEntityType() == org.bukkit.entity.EntityType.END_CRYSTAL) {
            event.setCancelled(true);
        }
    }
}
