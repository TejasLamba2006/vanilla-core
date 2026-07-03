package com.tejaslamba.vanillacore.social;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnouncementsManager {

    private final VanillaCorePlugin plugin;
    private BukkitTask task;

    public AnnouncementsManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getConfigManager().get().getBoolean("social.announcements.enabled", false)) {
            return;
        }

        List<String> announcements = plugin.getConfigManager().get().getStringList("social.announcements.messages");
        if (announcements == null || announcements.isEmpty()) {
            return;
        }

        long periodSeconds = Math.max(10L,
                plugin.getConfigManager().get().getLong("social.announcements.interval-seconds", 300L));
        long periodTicks = periodSeconds * 20L;
        AtomicInteger index = new AtomicInteger(0);
        List<String> snapshot = new ArrayList<>(announcements);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int current = Math.floorMod(index.getAndIncrement(), snapshot.size());
            String message = snapshot.get(current);
            Bukkit.getServer()
                    .broadcast(plugin.getMessageManager().getPrefixed("social.announcement.line", "message", message));
        }, periodTicks, periodTicks);
    }

    public void reload() {
        start();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

