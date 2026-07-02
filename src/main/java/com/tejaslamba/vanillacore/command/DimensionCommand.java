package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.DimensionLockFeature;
import com.tejaslamba.vanillacore.features.EndLockFeature;
import com.tejaslamba.vanillacore.features.NetherLockFeature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DimensionCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final String dimension;

    public DimensionCommand(VanillaCorePlugin plugin, String dimension) {
        this.plugin = plugin;
        this.dimension = dimension;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smp.dimension." + dimension)) {
            sender.sendMessage(plugin.getMessageManager().get("commands.dimension.no-permission"));
            return true;
        }

        DimensionLockFeature feature = dimension.equals("end")
                ? plugin.getFeatureManager().getFeature(EndLockFeature.class)
                : plugin.getFeatureManager().getFeature(NetherLockFeature.class);

        if (feature == null) {
            sender.sendMessage(plugin.getMessageManager().get("commands.dimension.feature-not-found"));
            return true;
        }

        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);

        if (args.length == 0) {
            boolean isLocked = feature.isLocked();
            sender.sendMessage(plugin.getMessageManager().get(
                    isLocked ? "commands.dimension.current-locked" : "commands.dimension.current-open",
                    "dimension", dimensionName));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "open":
                feature.setLocked(false);
                sender.sendMessage(
                        plugin.getMessageManager().get("commands.dimension.opened", "dimension", dimensionName));
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " opened " + dimensionName);
                }
                break;
            case "close":
                feature.setLocked(true);
                sender.sendMessage(
                        plugin.getMessageManager().get("commands.dimension.closed", "dimension", dimensionName));
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " closed " + dimensionName);
                }
                break;
            case "status":
                boolean isLocked = feature.isLocked();
                sender.sendMessage(
                        plugin.getMessageManager().get("commands.dimension.status.title", "dimension", dimensionName));
                sender.sendMessage(plugin.getMessageManager().get(
                        isLocked ? "commands.dimension.status.access-locked"
                                : "commands.dimension.status.access-open"));
                break;
            default:
                sender.sendMessage(plugin.getMessageManager().get("commands.dimension.usage", "dimension", dimension));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("open", "close", "status"));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}

