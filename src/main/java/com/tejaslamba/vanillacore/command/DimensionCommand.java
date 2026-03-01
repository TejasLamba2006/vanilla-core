package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.MessageManager;
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
        if (!sender.hasPermission("vanillacore.dimension." + dimension)) {
            sender.sendMessage(MessageManager.parse(
                    "<dark_gray>[<gold>Vanilla Core<dark_gray>] <red>You don't have permission to use this command!"));
            return true;
        }

        DimensionLockFeature feature = dimension.equals("end")
                ? plugin.getFeatureManager().getFeature(EndLockFeature.class)
                : plugin.getFeatureManager().getFeature(NetherLockFeature.class);

        if (feature == null) {
            sender.sendMessage(MessageManager.parse("<red>Dimension Lock feature not found!"));
            return true;
        }

        String dimensionName = dimension.substring(0, 1).toUpperCase() + dimension.substring(1);

        if (args.length == 0) {
            boolean isLocked = feature.isLocked();
            sender.sendMessage(MessageManager.parse("<dark_gray>[<gold>Vanilla Core<dark_gray>] <yellow>The "
                    + dimensionName + " is currently " + (isLocked ? "<red>Locked" : "<green>Open")));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "open":
                feature.setLocked(false);
                sender.sendMessage(MessageManager.parse("<dark_gray>[<gold>Vanilla Core<dark_gray>] <green>The "
                        + dimensionName + " has been opened!"));
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " opened " + dimensionName);
                }
                break;
            case "close":
                feature.setLocked(true);
                sender.sendMessage(MessageManager.parse(
                        "<dark_gray>[<gold>Vanilla Core<dark_gray>] <red>The " + dimensionName + " has been closed!"));
                if (plugin.isVerbose()) {
                    plugin.getLogger()
                            .info("[VERBOSE] Dimension Command - " + sender.getName() + " closed " + dimensionName);
                }
                break;
            case "status":
                boolean isLocked = feature.isLocked();
                sender.sendMessage(MessageManager.parse("<yellow><bold>=== " + dimensionName + " Status ==="));
                sender.sendMessage(
                        MessageManager.parse("<yellow>Access: " + (isLocked ? "<red>Locked" : "<green>Open")));
                break;
            default:
                sender.sendMessage(MessageManager.parse("<yellow>Usage: /" + dimension + " <open|close|status>"));
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
