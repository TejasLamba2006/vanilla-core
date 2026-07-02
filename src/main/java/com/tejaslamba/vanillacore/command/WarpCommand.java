package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.TeleportManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final TeleportManager teleportManager;

    public WarpCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        return switch (name) {
            case "setwarp" -> handleSetWarp(sender, args);
            case "warp" -> handleWarp(sender, args);
            case "delwarp" -> handleDeleteWarp(sender, args);
            case "warps" -> handleWarps(sender);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String commandName = command.getName().toLowerCase(Locale.ROOT);
        if ((commandName.equals("warp") || commandName.equals("delwarp")) && args.length == 1) {
            return teleportManager.getWarps().keySet().stream()
                    .filter(n -> n.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return Collections.emptyList();
    }

    private boolean handleSetWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.setwarp")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.warps.setwarp-usage");
            return true;
        }

        String warpName = args[0].toLowerCase(Locale.ROOT);
        teleportManager.setWarp(warpName, player.getLocation());
        plugin.getMessageManager().sendPrefixed(player, "teleport.warps.set", "warp", warpName);
        return true;
    }

    private boolean handleWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.warp")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        if (args.length < 1) {
            teleportManager.openWarpsGui(player);
            return true;
        }

        String warpName = args[0].toLowerCase(Locale.ROOT);
        Location location = teleportManager.getWarp(warpName);
        if (location == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.warps.not-found", "warp", warpName);
            return true;
        }

        teleportManager.teleportPlayer(player, location, true);
        plugin.getMessageManager().sendPrefixed(player, "teleport.warps.teleported", "warp", warpName);
        return true;
    }

    private boolean handleDeleteWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smp.delwarp")) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.warps.delwarp-usage");
            return true;
        }

        String warpName = args[0].toLowerCase(Locale.ROOT);
        Map<String, Location> warps = teleportManager.getWarps();
        if (!warps.containsKey(warpName)) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.warps.not-found", "warp", warpName);
            return true;
        }

        teleportManager.deleteWarp(warpName);
        plugin.getMessageManager().sendPrefixed(sender, "teleport.warps.deleted", "warp", warpName);
        return true;
    }

    private boolean handleWarps(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.warps")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        teleportManager.openWarpsGui(player);
        return true;
    }
}

