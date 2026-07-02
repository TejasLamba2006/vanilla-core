package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final TeleportManager teleportManager;

    public HomeCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        String name = command.getName().toLowerCase(Locale.ROOT);
        return switch (name) {
            case "sethome" -> handleSetHome(player, args);
            case "home" -> handleHome(player, args);
            case "delhome" -> handleDeleteHome(player, args);
            case "homes" -> handleHomes(player, args);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        String commandName = command.getName().toLowerCase(Locale.ROOT);
        if ((commandName.equals("home") || commandName.equals("delhome")) && args.length == 1) {
            return teleportManager.getHomes(player.getUniqueId()).keySet().stream()
                    .filter(n -> n.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (commandName.equals("homes") && args.length == 1 && player.hasPermission("smp.homes.target")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        return Collections.emptyList();
    }

    private boolean handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("smp.sethome")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.sethome-usage");
            return true;
        }

        String homeName = args[0].toLowerCase(Locale.ROOT);
        int maxHomes = Math.max(1, plugin.getConfigManager().get().getInt("teleport.homes.max-homes", 3));
        Map<String, Location> homes = teleportManager.getHomes(player.getUniqueId());
        boolean alreadyExists = homes.containsKey(homeName);
        if (!alreadyExists && homes.size() >= maxHomes) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.limit", "max", String.valueOf(maxHomes));
            return true;
        }

        teleportManager.setHome(player.getUniqueId(), homeName, player.getLocation());
        plugin.getMessageManager().sendPrefixed(player, "teleport.homes.set", "home", homeName);
        return true;
    }

    private boolean handleHome(Player player, String[] args) {
        if (!player.hasPermission("smp.home")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        if (args.length == 0) {
            teleportManager.openHomesGui(player, player.getUniqueId());
            return true;
        }

        String homeName = args[0].toLowerCase(Locale.ROOT);
        Location location = teleportManager.getHome(player.getUniqueId(), homeName);
        if (location == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.not-found", "home", homeName);
            return true;
        }

        teleportManager.teleportPlayer(player, location, true);
        plugin.getMessageManager().sendPrefixed(player, "teleport.homes.teleported", "home", homeName);
        return true;
    }

    private boolean handleDeleteHome(Player player, String[] args) {
        if (!player.hasPermission("smp.delhome")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.delhome-usage");
            return true;
        }

        String homeName = args[0].toLowerCase(Locale.ROOT);
        Location current = teleportManager.getHome(player.getUniqueId(), homeName);
        if (current == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.not-found", "home", homeName);
            return true;
        }

        teleportManager.deleteHome(player.getUniqueId(), homeName);
        plugin.getMessageManager().sendPrefixed(player, "teleport.homes.deleted", "home", homeName);
        return true;
    }

    private boolean handleHomes(Player player, String[] args) {
        if (!player.hasPermission("smp.homes")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        if (args.length == 0) {
            teleportManager.openHomesGui(player, player.getUniqueId());
            return true;
        }

        if (!player.hasPermission("smp.homes.target")) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.no-permission");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = target.getUniqueId();
        teleportManager.openHomesGui(player, targetId);
        return true;
    }
}

