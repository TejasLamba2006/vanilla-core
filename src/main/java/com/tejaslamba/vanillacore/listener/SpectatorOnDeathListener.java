package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.SpectatorOnDeathFeature;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpectatorOnDeathListener implements Listener {

    private final VanillaCorePlugin plugin;

    public SpectatorOnDeathListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        SpectatorOnDeathFeature feature = plugin.getFeatureManager().getFeature(SpectatorOnDeathFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(plugin.getMessageManager().get("spectator-on-death.now-spectator"));
        }, 1L);
    }
}
