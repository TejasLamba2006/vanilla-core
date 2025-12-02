package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatInputListener implements Listener {

    private final Main plugin;

    public ChatInputListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getChatInputManager().hasAwaitingInput(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> plugin.getChatInputManager().handleInput(player, message));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getChatInputManager().cancelInput(event.getPlayer());
    }

}
