package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.world.TimeSkipEvent;

public class OnePlayerSleepListener implements Listener {

    private final VanillaCorePlugin plugin;

    public OnePlayerSleepListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        if (!plugin.getConfigManager().get().getBoolean("features.one-player-sleep.enabled", false)) {
            return;
        }

        String sleepMessage = plugin.getConfigManager().get().getString("features.one-player-sleep.sleep-message", "");
        if (sleepMessage == null || sleepMessage.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        String formattedMessage = sleepMessage
                .replace("{player}", player.getName())
                .replace("{displayname}", player.getDisplayName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(formattedMessage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() != TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            return;
        }

        if (!plugin.getConfigManager().get().getBoolean("features.one-player-sleep.enabled", false)) {
            return;
        }

        String skipMessage = plugin.getConfigManager().get().getString("features.one-player-sleep.skip-message", "");
        if (skipMessage == null || skipMessage.isEmpty()) {
            return;
        }

        World world = event.getWorld();
        for (Player player : world.getPlayers()) {
            player.sendMessage(skipMessage);
        }
    }
}
