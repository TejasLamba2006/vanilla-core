package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.RitualFeature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RitualListener implements Listener {

    private final VanillaCorePlugin plugin;

    public RitualListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RitualFeature feature = plugin.getFeatureManager().getFeature(RitualFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!feature.getRitualManager().hasActiveRitual()) {
            return;
        }

        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> feature.getRitualManager().addBossBarViewer(player), 20L);
    }
}

