package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class SocialCommand {

    private static final String PLAYER_ONLY = "general.player-only";
    private static final String CMD_FIELD = "command";
    private static final String CMD_DISABLED = "social.command-disabled";
    private static final String PM_NO_PERMISSION = "social.pm.no-permission";
    private static final String BLOCK_NO_PERMISSION = "social.block.no-permission";
    private static final String SUB_REPLY = "reply";
    private static final String SUB_TOGGLE_CHAT = "togglechat";
    private static final String SUB_TOGGLE_PM = "togglepm";
    private static final String SUB_TOGGLE_MENTIONS = "togglementions";
    private static final String SUB_BLOCK = "block";
    private static final String SUB_UNBLOCK = "unblock";
    private static final String SUB_ANNOUNCEMENTS = "announcements";

    private final VanillaCorePlugin plugin;

    public SocialCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "msg", "pm", "tell" -> handleMsg(sender, args);
            case SUB_REPLY, "r" -> handleReply(sender, args);
            case "socialspy" -> handleSocialSpy(sender);
            case SUB_TOGGLE_CHAT -> handleToggle(sender, SUB_TOGGLE_CHAT);
            case SUB_TOGGLE_PM -> handleToggle(sender, SUB_TOGGLE_PM);
            case SUB_TOGGLE_MENTIONS -> handleToggle(sender, SUB_TOGGLE_MENTIONS);
            case SUB_BLOCK -> handleBlock(sender, args);
            case SUB_UNBLOCK -> handleUnblock(sender, args);
            case "blocked" -> handleBlocked(sender);
            case SUB_ANNOUNCEMENTS -> handleAnnouncements(sender, args);
            default -> false;
        };
    }

    public List<String> onTabComplete(String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>();
            base.addAll(Arrays.asList("msg", SUB_REPLY, "socialspy", SUB_TOGGLE_CHAT, SUB_TOGGLE_PM,
                    SUB_TOGGLE_MENTIONS, SUB_BLOCK, SUB_UNBLOCK, "blocked", SUB_ANNOUNCEMENTS));
            return base.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("msg") || args[0].equalsIgnoreCase(SUB_BLOCK)
                || args[0].equalsIgnoreCase(SUB_UNBLOCK))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase(SUB_ANNOUNCEMENTS)) {
            return List.of("reload").stream()
                    .filter(n -> n.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return List.of();
    }

    private boolean handleMsg(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.msg")) {
            plugin.getMessageManager().sendPrefixed(player, PM_NO_PERMISSION);
            return true;
        }
        if (!plugin.getSocialManager().isCommandEnabled("msg")) {
            plugin.getMessageManager().sendPrefixed(player, CMD_DISABLED, CMD_FIELD, "msg");
            return true;
        }
        long remaining = plugin.getSocialManager().applyCooldown(player, "msg");
        if (remaining > 0) {
            plugin.getMessageManager().sendPrefixed(player, "social.cooldown", "seconds", String.valueOf(remaining));
            return true;
        }
        if (args.length < 3) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.target-offline");
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.self");
            return true;
        }
        if (!plugin.getSocialManager().isPmEnabled(target)) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.target-disabled");
            return true;
        }
        if (plugin.getSocialManager().isBlockedEitherWay(player.getUniqueId(), target.getUniqueId())) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.blocked");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getSocialManager().sendPrivateMessage(player, target, message);
        return true;
    }

    private boolean handleReply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.reply")) {
            plugin.getMessageManager().sendPrefixed(player, PM_NO_PERMISSION);
            return true;
        }
        if (!plugin.getSocialManager().isCommandEnabled(SUB_REPLY)) {
            plugin.getMessageManager().sendPrefixed(player, CMD_DISABLED, CMD_FIELD, SUB_REPLY);
            return true;
        }
        long remaining = plugin.getSocialManager().applyCooldown(player, SUB_REPLY);
        if (remaining > 0) {
            plugin.getMessageManager().sendPrefixed(player, "social.cooldown", "seconds", String.valueOf(remaining));
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageManager().sendPrefixed(player, "social.reply.usage");
            return true;
        }

        Player target = plugin.getSocialManager().getLastReplyTarget(player);
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendPrefixed(player, "social.reply.no-target");
            return true;
        }
        if (!plugin.getSocialManager().isPmEnabled(target)) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.target-disabled");
            return true;
        }
        if (plugin.getSocialManager().isBlockedEitherWay(player.getUniqueId(), target.getUniqueId())) {
            plugin.getMessageManager().sendPrefixed(player, "social.pm.blocked");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getSocialManager().sendPrivateMessage(player, target, message);
        return true;
    }

    private boolean handleSocialSpy(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.socialspy")) {
            plugin.getMessageManager().sendPrefixed(player, "social.socialspy.no-permission");
            return true;
        }
        plugin.getSocialManager().toggleSocialSpy(player);
        plugin.getMessageManager().sendPrefixed(player,
                plugin.getSocialManager().isSocialSpyEnabled(player) ? "social.socialspy.enabled"
                        : "social.socialspy.disabled");
        return true;
    }

    private boolean handleToggle(CommandSender sender, String mode) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }

        return switch (mode) {
            case SUB_TOGGLE_CHAT -> {
                if (!player.hasPermission("smp.toggle.chat")) {
                    plugin.getMessageManager().sendPrefixed(player, "social.chat.no-permission");
                    yield true;
                }
                plugin.getSocialManager().toggleChat(player);
                plugin.getMessageManager().sendPrefixed(player,
                        plugin.getSocialManager().isChatEnabled(player) ? "social.chat.toggled-on"
                                : "social.chat.toggled-off");
                yield true;
            }
            case SUB_TOGGLE_PM -> {
                if (!player.hasPermission("smp.toggle.pm")) {
                    plugin.getMessageManager().sendPrefixed(player, PM_NO_PERMISSION);
                    yield true;
                }
                plugin.getSocialManager().togglePm(player);
                plugin.getMessageManager().sendPrefixed(player,
                        plugin.getSocialManager().isPmEnabled(player) ? "social.pm.toggled-on"
                                : "social.pm.toggled-off");
                yield true;
            }
            case SUB_TOGGLE_MENTIONS -> {
                if (!player.hasPermission("smp.toggle.mentions")) {
                    plugin.getMessageManager().sendPrefixed(player, "social.mentions.no-permission");
                    yield true;
                }
                plugin.getSocialManager().toggleMentions(player);
                plugin.getMessageManager().sendPrefixed(player,
                        plugin.getSocialManager().isMentionsEnabled(player) ? "social.mentions.toggled-on"
                                : "social.mentions.toggled-off");
                yield true;
            }
            default -> false;
        };
    }

    private boolean handleBlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.block")) {
            plugin.getMessageManager().sendPrefixed(player, BLOCK_NO_PERMISSION);
            return true;
        }
        if (!plugin.getSocialManager().isCommandEnabled(SUB_BLOCK)) {
            plugin.getMessageManager().sendPrefixed(player, CMD_DISABLED, CMD_FIELD, SUB_BLOCK);
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageManager().sendPrefixed(player, "social.block.usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "social.block.target-offline");
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendPrefixed(player, "social.block.self");
            return true;
        }

        plugin.getSocialManager().block(player, target);
        plugin.getMessageManager().sendPrefixed(player, "social.block.added", "player", target.getName());
        return true;
    }

    private boolean handleUnblock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.unblock")) {
            plugin.getMessageManager().sendPrefixed(player, BLOCK_NO_PERMISSION);
            return true;
        }
        if (!plugin.getSocialManager().isCommandEnabled(SUB_UNBLOCK)) {
            plugin.getMessageManager().sendPrefixed(player, CMD_DISABLED, CMD_FIELD, SUB_UNBLOCK);
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageManager().sendPrefixed(player, "social.unblock.usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "social.block.target-offline");
            return true;
        }

        plugin.getSocialManager().unblock(player, target);
        plugin.getMessageManager().sendPrefixed(player, "social.unblock.removed", "player", target.getName());
        return true;
    }

    private boolean handleBlocked(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission("smp.blocklist")) {
            plugin.getMessageManager().sendPrefixed(player, BLOCK_NO_PERMISSION);
            return true;
        }

        Set<UUID> blocked = plugin.getSocialManager().getBlockedPlayers(player.getUniqueId());
        if (blocked.isEmpty()) {
            plugin.getMessageManager().sendPrefixed(player, "social.block.none");
            return true;
        }

        List<String> names = blocked.stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .filter(n -> n != null && !n.isBlank())
                .sorted()
                .toList();

        plugin.getMessageManager().sendPrefixed(player, "social.block.list", "players", String.join(", ", names));
        return true;
    }

    private boolean handleAnnouncements(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smp.announcements")) {
            plugin.getMessageManager().sendPrefixed(sender, "social.announcements.no-permission");
            return true;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("reload")) {
            plugin.getMessageManager().sendPrefixed(sender, "social.announcements.usage");
            return true;
        }

        plugin.getAnnouncementsManager().reload();
        plugin.getMessageManager().sendPrefixed(sender, "social.announcements.reloaded");
        return true;
    }
}

