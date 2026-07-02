package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final KitManager kitManager;

    public KitCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        return switch (name) {
            case "kit" -> handleKit(sender, args);
            case "kits" -> handleKits(sender);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("kit") && args.length == 1) {
            return kitManager.getKitNames().stream()
                    .filter(kitName -> kitName.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return Collections.emptyList();
    }

    private boolean handleKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.kit")) {
            plugin.getMessageManager().sendPrefixed(player, "kits.no-permission-global");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(player, "kits.usage");
            return true;
        }

        String kitName = args[0].toLowerCase(Locale.ROOT);
        kitManager.giveKit(player, kitName, false, false);
        return true;
    }

    private boolean handleKits(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("smp.kits")) {
            plugin.getMessageManager().sendPrefixed(player, "kits.no-permission-global");
            return true;
        }

        Set<String> kits = kitManager.getKitNames();
        if (kits.isEmpty()) {
            plugin.getMessageManager().sendPrefixed(player, "kits.none");
            return true;
        }

        plugin.getMessageManager().sendPrefixed(player, "kits.list", "kits", String.join(", ", kits));
        return true;
    }
}

