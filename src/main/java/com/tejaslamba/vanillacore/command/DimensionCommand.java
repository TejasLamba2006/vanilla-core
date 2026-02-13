package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.Main;
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

    private final Main plugin;
    private final String dimension;

    public DimensionCommand(Main plugin, String dimension) {
        this.plugin = plugin;
        this.dimension = dimension;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillacore.dimension." + dimension)) {
            sender.sendMessage(plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r")
                    + " §cYou don't have permission to use this command!");
            return true;
        }

        DimensionLockFeature feature = dimension.equals("end")
                ? plugin.getFeatureManager().getFeature(EndLockFeature.class)
                : plugin.getFeatureManager().getFeature(NetherLockFeature.class);

        if (feature == null) {
            sender.sendMessage("§cDimension Lock feature not found!");
            return true;
        }

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);

        if (args.length == 0) {
            boolean isLocked = feature.isLocked();
            sender.sendMessage(
                    prefix + " §eThe " + dimensionName + " is currently " + (isLocked ? "§cLocked" : "§aOpen"));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "open":
                feature.setLocked(false);
                sender.sendMessage(prefix + " §aThe " + dimensionName + " has been opened!");
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " opened " + dimensionName);
                }
                break;
            case "close":
                feature.setLocked(true);
                sender.sendMessage(prefix + " §cThe " + dimensionName + " has been closed!");
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " closed " + dimensionName);
                }
                break;
            case "status":
                boolean isLocked = feature.isLocked();
                sender.sendMessage("§e§l=== " + dimensionName + " Status ===");
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
