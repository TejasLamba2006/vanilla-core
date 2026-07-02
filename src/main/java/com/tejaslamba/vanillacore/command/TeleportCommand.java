package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TeleportCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final TeleportManager teleportManager;

    public TeleportCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        return switch (name) {
            case "tpa" -> handleTpa(sender, args, TeleportManager.RequestType.TPA);
            case "tpahere" -> handleTpa(sender, args, TeleportManager.RequestType.TPA_HERE);
            case "tpaccept" -> handleAccept(sender);
            case "tpdeny" -> handleDeny(sender);
            case "spawn" -> handleSpawn(sender);
            case "setspawn" -> handleSetSpawn(sender);
            case "back" -> handleBack(sender);
            case "tp" -> handleTp(sender, args);
            case "tphere" -> handleTpHere(sender, args);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if ((name.equals("tpa") || name.equals("tpahere") || name.equals("tphere")) && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (name.equals("tp")) {
            if (args.length == 1 || args.length == 2) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase(Locale.ROOT)
                                .startsWith(args[args.length - 1].toLowerCase(Locale.ROOT)))
                        .toList();
            }
        }

        return Collections.emptyList();
    }

    private boolean handleTpa(CommandSender sender, String[] args, TeleportManager.RequestType type) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        if (type == TeleportManager.RequestType.TPA && !player.hasPermission("smp.tpa")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (type == TeleportManager.RequestType.TPA_HERE && !player.hasPermission("smp.tpahere")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player,
                    type == TeleportManager.RequestType.TPA ? "teleport.request.usage" : "teleport.request-here.usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.player-not-found");
            return true;
        }

        teleportManager.requestTeleport(player, target, type);
        return true;
    }

    private boolean handleAccept(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.tpaccept")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        teleportManager.acceptTeleport(player);
        return true;
    }

    private boolean handleDeny(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.tpdeny")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        teleportManager.denyTeleport(player);
        return true;
    }

    private boolean handleSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.spawn")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (teleportManager.teleportToSpawn(player)) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.spawn.teleported");
        }
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.setspawn")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        teleportManager.setSpawn(player.getLocation());
        plugin.getMessageManager().sendPrefixed(player, "teleport.spawn.set");
        return true;
    }

    private boolean handleBack(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.back")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        teleportManager.teleportBack(player);
        return true;
    }

    private boolean handleTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smp.tp")) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.no-permission");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.tp.usage");
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(player, "teleport.player-not-found");
                return true;
            }
            teleportManager.teleportPlayer(player, target.getLocation(), true);
            plugin.getMessageManager().sendPrefixed(player, "teleport.tp.self-success", "player", target.getName());
            return true;
        }

        Player source = Bukkit.getPlayerExact(args[0]);
        Player destination = Bukkit.getPlayerExact(args[1]);
        if (source == null || destination == null) {
            plugin.getMessageManager().sendPrefixed(sender, "teleport.player-not-found");
            return true;
        }
        teleportManager.teleportPlayer(source, destination.getLocation(), true);
        plugin.getMessageManager().sendPrefixed(sender, "teleport.tp.other-success",
                "source", source.getName(), "target", destination.getName());
        return true;
    }

    private boolean handleTpHere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.tphere")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.tphere.usage");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.player-not-found");
            return true;
        }
        Location destination = player.getLocation();
        teleportManager.teleportPlayer(target, destination, true);
        plugin.getMessageManager().sendPrefixed(player, "teleport.tphere.success", "player", target.getName());
        return true;
    }
}

