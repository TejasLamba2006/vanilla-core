package com.tejaslamba.smpcore.command;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.DimensionLockFeature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DimensionCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final String dimension;

    public DimensionCommand(Main plugin, String dimension) {
        this.plugin = plugin;
        this.dimension = dimension;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.dimension." + dimension)) {
            sender.sendMessage(plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r")
                    + " §cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /" + dimension + " <open|close|status>");
            return true;
        }

        String action = args[0].toLowerCase();

        DimensionLockFeature feature = (DimensionLockFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof DimensionLockFeature &&
                        ((DimensionLockFeature) f).getDimension().equals(dimension))
                .findFirst()
                .orElse(null);

        if (feature == null) {
            sender.sendMessage("§cDimension Lock feature not found!");
            return true;
        }

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);

        switch (action) {
            case "open":
                feature.setLocked(false);
                sender.sendMessage(prefix + " §aThe " + dimensionName + " has been opened!");
                if (verbose) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " opened " + dimensionName);
                }
                break;
            case "close":
                feature.setLocked(true);
                sender.sendMessage(prefix + " §aThe " + dimensionName + " has been closed!");
                if (verbose) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " closed " + dimensionName);
                }
                break;
            case "status":
                boolean isLocked = feature.isLocked();
                boolean isEnabled = feature.isEnabled();
                sender.sendMessage("§e§l=== " + dimensionName + " Status ===");
                sender.sendMessage("§eFeature: " + (isEnabled ? "§aEnabled" : "§cDisabled"));
                sender.sendMessage("§eAccess: " + (isLocked ? "§cLocked" : "§aOpen"));
                break;
            default:
                sender.sendMessage("§eUsage: /" + dimension + " <open|close|status>");
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
