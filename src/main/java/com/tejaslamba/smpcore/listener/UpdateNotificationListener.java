package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.manager.CDNManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UpdateNotificationListener implements Listener {

    private final Main plugin;
    private final Set<UUID> notifiedPlayers;

    public UpdateNotificationListener(Main plugin) {
        this.plugin = plugin;
        this.notifiedPlayers = new HashSet<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("smpcore.admin") && !player.isOp()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            CDNManager cdnManager = plugin.getCDNManager();
            if (cdnManager == null)
                return;

            cdnManager.refreshIfNeeded();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline())
                    return;

                if (cdnManager.isUpdateAvailable() && !notifiedPlayers.contains(player.getUniqueId())) {
                    sendUpdateNotification(player, cdnManager);
                    notifiedPlayers.add(player.getUniqueId());
                }

                Set<String> disabledFeatures = cdnManager.getDisabledFeatures();
                if (!disabledFeatures.isEmpty()) {
                    sendDisabledFeaturesWarning(player, disabledFeatures);
                }

                if (cdnManager.isMaintenanceMode()) {
                    sendMaintenanceWarning(player);
                }
            });
        }, 60L);
    }

    private void sendUpdateNotification(Player player, CDNManager cdnManager) {
        String currentVersion = cdnManager.getCurrentVersion();
        String latestVersion = cdnManager.getLatestVersion();

        player.sendMessage("");
        player.sendMessage("§8§l§m                                              ");
        player.sendMessage("§6§l  SMP Core §7- §eUpdate Available!");
        player.sendMessage("");
        player.sendMessage("§7  Current: §c" + currentVersion + " §7→ Latest: §a" + latestVersion);
        player.sendMessage("");

        TextComponent downloadMsg = new TextComponent("  ");
        TextComponent downloadBtn = new TextComponent("§a§l[DOWNLOAD]");
        downloadBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/smpcore"));
        downloadBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to open Modrinth")));

        TextComponent changelogBtn = new TextComponent(" §e§l[CHANGELOG]");
        changelogBtn.setClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL, cdnManager.getDocumentationUrl() + "/changelog"));
        changelogBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to view changelog")));

        downloadMsg.addExtra(downloadBtn);
        downloadMsg.addExtra(changelogBtn);
        player.spigot().sendMessage(downloadMsg);

        player.sendMessage("§8§l§m                                              ");
        player.sendMessage("");
    }

    private void sendDisabledFeaturesWarning(Player player, Set<String> disabledFeatures) {
        player.sendMessage("§c§l[SMP Core] §eWarning: §7Some features have been remotely disabled:");
        for (String feature : disabledFeatures) {
            player.sendMessage("§7  - §c" + feature);
        }
        player.sendMessage("§7This may be due to a critical bug. Check documentation for details.");
    }

    private void sendMaintenanceWarning(Player player) {
        player.sendMessage("§c§l[SMP Core] §eWarning: §7Plugin is in maintenance mode.");
        player.sendMessage("§7Some features may be limited. Check documentation for updates.");
    }

    public void clearNotifiedPlayer(UUID uuid) {
        notifiedPlayers.remove(uuid);
    }

    public void clearAllNotifications() {
        notifiedPlayers.clear();
    }
}
