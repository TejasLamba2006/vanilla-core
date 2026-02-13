package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.manager.CDNManager;
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

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            CDNManager cdnManager = plugin.getCDNManager();
            if (cdnManager == null)
                return;

            cdnManager.refreshIfNeeded();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline())
                    return;

                String permission = cdnManager.getUpdateNotificationPermission();
                if (!player.hasPermission(permission) && !player.isOp()) {
                    return;
                }

                if (cdnManager.isUpdateNotificationEnabled() && cdnManager.isUpdateAvailable()
                        && !notifiedPlayers.contains(player.getUniqueId())) {
                    sendUpdateNotification(player, cdnManager);
                    notifiedPlayers.add(player.getUniqueId());
                }

                Set<String> disabledFeatures = cdnManager.getDisabledFeatures();
                if (!disabledFeatures.isEmpty()) {
                    sendDisabledFeaturesWarning(player, disabledFeatures, cdnManager);
                }

                if (cdnManager.isMaintenanceMode()) {
                    sendMaintenanceWarning(player, cdnManager);
                }
            });
        }, 60L);
    }

    private void sendUpdateNotification(Player player, CDNManager cdnManager) {
        String currentVersion = cdnManager.getCurrentVersion();
        String latestVersion = cdnManager.getLatestVersion();
        String title = cdnManager.getUpdateNotificationTitle();
        String message = cdnManager.getUpdateNotificationMessage()
                .replace("{current}", currentVersion)
                .replace("{latest}", latestVersion);
        String actionMessage = cdnManager.getUpdateNotificationActionMessage()
                .replace("{url}", "https://modrinth.com/plugin/vanillacorewastaken");

        player.sendMessage("");
        player.sendMessage("§8§l§m                                              ");
        player.sendMessage("§6§l  " + title + " §7- §eUpdate Available!");
        player.sendMessage("");
        player.sendMessage("§7  " + message);
        player.sendMessage("");

        TextComponent downloadMsg = new TextComponent("  ");
        TextComponent downloadBtn = new TextComponent("§a§l[DOWNLOAD]");
        downloadBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/vanillacorewastaken"));
        downloadBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to open Modrinth")));

        TextComponent changelogBtn = new TextComponent(" §e§l[CHANGELOG]");
        changelogBtn.setClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL,
                        cdnManager.getDocumentationUrl().replace("docs", "changelog")));
        changelogBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to view changelog")));

        downloadMsg.addExtra(downloadBtn);
        downloadMsg.addExtra(changelogBtn);
        player.spigot().sendMessage(downloadMsg);

        player.sendMessage("§8§l§m                                              ");
        player.sendMessage("");
    }

    private void sendDisabledFeaturesWarning(Player player, Set<String> disabledFeatures, CDNManager cdnManager) {
        String disabledMessage = cdnManager.getDisabledMessage();
        player.sendMessage("§c§l[Vanilla Core] §eWarning: §7Some features have been remotely disabled:");
        for (String feature : disabledFeatures) {
            player.sendMessage("§7  - §c" + feature);
        }
        player.sendMessage("§7" + disabledMessage);
    }

    private void sendMaintenanceWarning(Player player, CDNManager cdnManager) {
        String maintenanceMessage = cdnManager.getMaintenanceMessage();
        player.sendMessage("§c§l[Vanilla Core] §eWarning: §7Plugin is in maintenance mode.");
        player.sendMessage("§7" + maintenanceMessage);
    }

    public void clearNotifiedPlayer(UUID uuid) {
        notifiedPlayers.remove(uuid);
    }

    public void clearAllNotifications() {
        notifiedPlayers.clear();
    }
}
