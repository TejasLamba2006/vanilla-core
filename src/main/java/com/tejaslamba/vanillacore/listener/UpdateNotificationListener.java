package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.CDNManager;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UpdateNotificationListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final Set<UUID> notifiedPlayers;

    public UpdateNotificationListener(VanillaCorePlugin plugin) {
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

        player.sendMessage(Component.empty());
        player.sendMessage(
                MessageManager.parse("<dark_gray><strikethrough>                                              "));
        player.sendMessage(MessageManager.parse("<gold><bold>  " + title + " <gray>- <yellow>Update Available!"));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>  " + message));
        player.sendMessage(Component.empty());

        Component downloadBtn = Component.text("[DOWNLOAD]")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/vanillacorewastaken"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open Modrinth").color(NamedTextColor.GRAY)));

        Component changelogBtn = Component.text(" [CHANGELOG]")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.openUrl(
                        cdnManager.getDocumentationUrl().replace("docs", "changelog")))
                .hoverEvent(HoverEvent.showText(Component.text("Click to view changelog").color(NamedTextColor.GRAY)));

        player.sendMessage(Component.text("  ").append(downloadBtn).append(changelogBtn));

        player.sendMessage(
                MessageManager.parse("<dark_gray><strikethrough>                                              "));
        player.sendMessage(Component.empty());
    }

    private void sendDisabledFeaturesWarning(Player player, Set<String> disabledFeatures, CDNManager cdnManager) {
        String disabledMessage = cdnManager.getDisabledMessage();
        player.sendMessage(MessageManager
                .parse("<red><bold>[Vanilla Core] <yellow>Warning: <gray>Some features have been remotely disabled:"));
        for (String feature : disabledFeatures) {
            player.sendMessage(MessageManager.parse("<gray>  - <red>" + feature));
        }
        player.sendMessage(MessageManager.parse("<gray>" + disabledMessage));
    }

    private void sendMaintenanceWarning(Player player, CDNManager cdnManager) {
        String maintenanceMessage = cdnManager.getMaintenanceMessage();
        player.sendMessage(MessageManager
                .parse("<red><bold>[Vanilla Core] <yellow>Warning: <gray>Plugin is in maintenance mode."));
        player.sendMessage(MessageManager.parse("<gray>" + maintenanceMessage));
    }

    public void clearNotifiedPlayer(UUID uuid) {
        notifiedPlayers.remove(uuid);
    }

    public void clearAllNotifications() {
        notifiedPlayers.clear();
    }
}
