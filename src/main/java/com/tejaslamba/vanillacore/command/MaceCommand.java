package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MaceLimiterFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaceCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private static final String WHITELIST_LORE = "§8[§6MaceWhitelisted§8]";

    public MaceCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillacore.mace")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "whitelist":
                return handleWhitelist(sender);
            case "reset":
                return handleReset(sender);
            case "status":
                return handleStatus(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleWhitelist(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageManager.parse("<red>Only players can use this command!"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) {
            sender.sendMessage(MessageManager.parse("<red>You must be holding a Mace!"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return true;
        }

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        if (lore.contains(WHITELIST_LORE)) {
            lore.remove(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage(MessageManager
                    .parse("<dark_gray>[<gold>Vanilla Core<dark_gray>] <red>Removed mace limiter whitelist from item"));
        } else {
            lore.add(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage(MessageManager
                    .parse("<dark_gray>[<gold>Vanilla Core<dark_gray>] <green>Whitelisted mace from limiter"));
        }

        return true;
    }

    private boolean handleReset(CommandSender sender) {
        if (!sender.hasPermission("vanillacore.mace.reset")) {
            sender.sendMessage(MessageManager.parse("<red>You don't have permission to reset the mace limiter!"));
            return true;
        }

        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);

        if (feature == null) {
            sender.sendMessage(MessageManager.parse("<red>Mace Limiter feature not found!"));
            return true;
        }

        feature.resetCraftCount();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Command - " + sender.getName() + " reset the mace limiter");
        }

        sender.sendMessage(MessageManager.parse(
                "<dark_gray>[<gold>Vanilla Core<dark_gray>] <green>Reset mace craft count! Maces can now be crafted again."));

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);

        if (feature == null) {
            sender.sendMessage(MessageManager.parse("<red>Mace Limiter feature not found!"));
            return true;
        }

        boolean enabled = feature.isEnabled();
        int crafted = feature.getMacesCrafted();
        int max = feature.getMaxMaces();

        sender.sendMessage(MessageManager.parse("<gold><bold>=== Mace Limiter Status ==="));
        sender.sendMessage(MessageManager.parse("<yellow>Feature Enabled: " + (enabled ? "<green>Yes" : "<red>No")));
        sender.sendMessage(MessageManager.parse("<yellow>Maces Crafted: <green>" + crafted + "<yellow>/<green>" + max));
        sender.sendMessage(
                MessageManager.parse("<yellow>Can Craft More: " + (feature.canCraftMace() ? "<green>Yes" : "<red>No")));

        return true;
    }

    public static boolean isWhitelisted(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }

        List<String> lore = meta.getLore();
        return lore.contains(WHITELIST_LORE);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageManager.parse("<gold><bold>=== Mace Commands ==="));
        sender.sendMessage(
                MessageManager.parse("<yellow>/vanilla mace whitelist <gray>- Toggle whitelist on held mace"));
        sender.sendMessage(MessageManager.parse("<yellow>/vanilla mace reset <gray>- Reset mace limiter (admin)"));
        sender.sendMessage(MessageManager.parse("<yellow>/vanilla mace status <gray>- Check mace limiter status"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("whitelist", "reset", "status"));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
