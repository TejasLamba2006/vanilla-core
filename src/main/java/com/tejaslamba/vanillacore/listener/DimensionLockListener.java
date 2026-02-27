package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.EndLockFeature;
import com.tejaslamba.vanillacore.features.NetherLockFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class DimensionLockListener implements Listener {

    private final VanillaCorePlugin plugin;

    public DimensionLockListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }

        Player player = event.getPlayer();
        World.Environment toEnvironment = event.getTo().getWorld().getEnvironment();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Dimension Lock - " + player.getName()
                    + " is attempting to enter " + toEnvironment.name());
        }

        boolean hasBypass = player.hasPermission("vanillacore.dimension.bypass");

        if (toEnvironment == World.Environment.THE_END) {
            EndLockFeature endLock = plugin.getFeatureManager().getFeature(EndLockFeature.class);

            if (endLock != null && endLock.isLocked() && !hasBypass
                    && !player.hasPermission("vanillacore.dimension.bypass.end")) {
                event.setCancelled(true);
                String message = plugin.getConfigManager().get()
                        .getString("features.dimension-lock-end.locked-message", "<red>The End is currently locked!");
                player.sendMessage(MessageManager.parse(message));

                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Lock - Blocked " + player.getName() + " from entering The End");
                }
            }
        } else if (toEnvironment == World.Environment.NETHER) {
            NetherLockFeature netherLock = plugin.getFeatureManager().getFeature(NetherLockFeature.class);

            if (netherLock != null && netherLock.isLocked() && !hasBypass
                    && !player.hasPermission("vanillacore.dimension.bypass.nether")) {
                event.setCancelled(true);
                String message = plugin.getConfigManager().get()
                        .getString("features.dimension-lock-nether.locked-message",
                                "<red>The Nether is currently locked!");
                player.sendMessage(MessageManager.parse(message));

                if (plugin.isVerbose()) {
                    plugin.getLogger().info(
                            "[VERBOSE] Dimension Lock - Blocked " + player.getName() + " from entering The Nether");
                }
            }
        }
    }
}
