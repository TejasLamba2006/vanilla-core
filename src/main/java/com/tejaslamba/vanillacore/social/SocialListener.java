package com.tejaslamba.vanillacore.social;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SocialListener implements Listener {

    private final VanillaCorePlugin plugin;

    public SocialListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getSocialManager() != null) {
            plugin.getSocialManager().preload(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getSocialManager() != null) {
            plugin.getSocialManager().unload(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (plugin.getConfigManager().get().getBoolean("social.lifecycle.auto-spawn-on-respawn", false)
                && plugin.getConfigManager().get().contains("social.spawn.world")) {
            String world = plugin.getConfigManager().get().getString("social.spawn.world", "world");
            double x = plugin.getConfigManager().get().getDouble("social.spawn.x", 0.0);
            double y = plugin.getConfigManager().get().getDouble("social.spawn.y", 64.0);
            double z = plugin.getConfigManager().get().getDouble("social.spawn.z", 0.0);
            float yaw = (float) plugin.getConfigManager().get().getDouble("social.spawn.yaw", 0.0);
            float pitch = (float) plugin.getConfigManager().get().getDouble("social.spawn.pitch", 0.0);
            if (Bukkit.getWorld(world) != null) {
                event.setRespawnLocation(new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("social.chat.enabled", true)) {
            return;
        }

        Player sender = event.getPlayer();
        if (!plugin.getSocialManager().isChatEnabled(sender)) {
            event.setCancelled(true);
            sender.sendMessage(plugin.getMessageManager().getPrefixed("social.chat.disabled"));
            return;
        }

        event.viewers().removeIf(viewer -> {
            if (viewer instanceof Player playerViewer) {
                if (!plugin.getSocialManager().isChatEnabled(playerViewer)) {
                    return true;
                }
                if (plugin.getSocialManager().isBlockedEitherWay(sender.getUniqueId(), playerViewer.getUniqueId())) {
                    return true;
                }
            }
            return false;
        });

        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.message());

        plugin.getSocialManager().handleMentions(sender, plain);
        event.renderer(
                (source, sourceDisplayName, message, viewer) -> plugin.getSocialManager().formatChat(sender, plain));
    }
}

