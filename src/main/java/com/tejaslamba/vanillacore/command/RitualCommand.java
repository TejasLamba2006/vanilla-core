package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.RitualFeature;
import com.tejaslamba.vanillacore.ritual.RitualManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RitualCommand implements CommandExecutor, TabCompleter {

    private static final Pattern DURATION_PART_PATTERN = Pattern.compile("(\\d+)([smhd])");
    private static final List<String> TIME_SUGGESTIONS = List.of("30s", "1m", "5m", "1h", "1d");

    private final VanillaCorePlugin plugin;

    public RitualCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        RitualFeature feature = plugin.getFeatureManager().getFeature(RitualFeature.class);

        if (feature == null || !feature.isEnabled()) {
            plugin.getMessageManager().sendPrefixed(sender, "ritual.feature-disabled");
            return true;
        }

        if (args.length == 0) {
            return handleStart(sender, feature, new String[0]);
        }

        if (args[0].equalsIgnoreCase("start")) {
            String[] startArgs = new String[args.length - 1];
            System.arraycopy(args, 1, startArgs, 0, args.length - 1);
            return handleStart(sender, feature, startArgs);
        }

        if (args[0].equalsIgnoreCase("status")) {
            return handleStatus(sender, feature);
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            return handleCancel(sender, feature);
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleStart(CommandSender sender, RitualFeature feature, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("vanillacore.ritual")) {
            plugin.getMessageManager().sendPrefixed(player, "commands.ritual.no-permission");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            plugin.getMessageManager().sendPrefixed(player, "ritual.no-item-in-hand");
            return true;
        }

        if (args.length > 2) {
            sendHelp(player);
            return true;
        }

        int durationSeconds = Math.max(1, feature.getDurationMinutes()) * 60;
        if (args.length >= 1 && !args[0].isBlank()) {
            Integer parsedDuration = parseDurationSeconds(args[0]);
            if (parsedDuration == null) {
                plugin.getMessageManager().sendPrefixed(player, "ritual.invalid-time");
                return true;
            }
            durationSeconds = parsedDuration;
        }

        String colorName = feature.normalizeParticleColorName(feature.getParticleColorName());
        if (colorName == null) {
            colorName = "BLUE";
        }

        if (args.length == 2) {
            String parsedColor = feature.normalizeParticleColorName(args[1]);
            if (parsedColor == null) {
                plugin.getMessageManager().sendPrefixed(player, "ritual.invalid-color");
                return true;
            }
            colorName = parsedColor;
        }

        RitualManager manager = feature.getRitualManager();
        if (!manager.startRitual(player, feature, durationSeconds, colorName)) {
            plugin.getMessageManager().sendPrefixed(player, "ritual.already-active");
            return true;
        }

        plugin.getMessageManager().sendPrefixed(player, "ritual.started-self");
        return true;
    }

    private Integer parseDurationSeconds(String raw) {
        if (raw == null) {
            return null;
        }

        String token = raw.trim().toLowerCase(Locale.ROOT).replace(" ", "");
        if (token.isEmpty()) {
            return null;
        }

        if (token.chars().allMatch(Character::isDigit)) {
            try {
                int seconds = Integer.parseInt(token);
                return seconds > 0 ? seconds : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        Matcher matcher = DURATION_PART_PATTERN.matcher(token);
        int index = 0;
        long totalSeconds = 0L;

        while (matcher.find()) {
            if (matcher.start() != index) {
                return null;
            }

            long value;
            try {
                value = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }

            char unit = matcher.group(2).charAt(0);
            long multiplier = switch (unit) {
                case 's' -> 1L;
                case 'm' -> 60L;
                case 'h' -> 3600L;
                case 'd' -> 86400L;
                default -> -1L;
            };
            if (multiplier < 0L) {
                return null;
            }

            totalSeconds += value * multiplier;
            if (totalSeconds > Integer.MAX_VALUE) {
                return null;
            }

            index = matcher.end();
        }

        if (index != token.length() || totalSeconds <= 0L) {
            return null;
        }

        return (int) totalSeconds;
    }

    private boolean handleStatus(CommandSender sender, RitualFeature feature) {
        RitualManager manager = feature.getRitualManager();

        if (!manager.hasActiveRitual()) {
            plugin.getMessageManager().sendPrefixed(sender, "ritual.status-inactive");
            return true;
        }

        plugin.getMessageManager().sendPrefixed(sender, "ritual.status-active",
                "item", manager.getActiveItemName(),
                "player", manager.getActiveStarterName(),
                "remaining", String.valueOf(manager.getRemainingSeconds()));
        return true;
    }

    private boolean handleCancel(CommandSender sender, RitualFeature feature) {
        if (!sender.hasPermission("vanillacore.ritual.cancel")) {
            plugin.getMessageManager().sendPrefixed(sender, "ritual.no-cancel-permission");
            return true;
        }

        RitualManager manager = feature.getRitualManager();
        if (!manager.hasActiveRitual()) {
            plugin.getMessageManager().sendPrefixed(sender, "ritual.status-inactive");
            return true;
        }

        String cancelledBy = sender instanceof Player p ? p.getName() : "Console";
        manager.cancelActiveRitual(cancelledBy);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getPrefixed("ritual.help-1"));
        sender.sendMessage(plugin.getMessageManager().getPrefixed("ritual.help-2"));
        sender.sendMessage(plugin.getMessageManager().getPrefixed("ritual.help-3"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("start", "status"));
            if (sender.hasPermission("vanillacore.ritual.cancel")) {
                completions.add("cancel");
            }

            String typed = args[0].toLowerCase();
            return completions.stream().filter(s -> s.startsWith(typed)).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            String typed = args[1].toLowerCase(Locale.ROOT);
            return TIME_SUGGESTIONS.stream().filter(s -> s.startsWith(typed)).toList();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            RitualFeature feature = plugin.getFeatureManager().getFeature(RitualFeature.class);
            if (feature == null) {
                return Collections.emptyList();
            }

            String typed = args[2].toUpperCase(Locale.ROOT);
            return feature.getSupportedParticleColors().stream().filter(s -> s.startsWith(typed)).toList();
        }

        return Collections.emptyList();
    }
}
