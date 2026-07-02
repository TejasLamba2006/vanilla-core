package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class KitJoinListener implements Listener {

    private final VanillaCorePlugin plugin;

    public KitJoinListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("kits.enabled", true)) {
            return;
        }
        if (!event.getPlayer().hasPlayedBefore()) {
            plugin.getKitManager().giveFirstJoinKit(event.getPlayer());
        }
    }
}

