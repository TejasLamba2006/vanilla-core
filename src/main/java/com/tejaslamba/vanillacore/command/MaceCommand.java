package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MaceLimiterFeature;
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
        if (!sender.hasPermission("smp.mace")) {
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
            sender.sendMessage(plugin.getMessageManager().get("general.player-only"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) {
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.must-hold-mace"));
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
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.whitelist.removed"));
        } else {
            lore.add(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.whitelist.added"));
        }

        return true;
    }

    private boolean handleReset(CommandSender sender) {
        if (!sender.hasPermission("smp.mace.reset")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.reset-no-permission"));
            return true;
        }

        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);

        if (feature == null) {
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.feature-not-found"));
            return true;
        }

        feature.resetCraftCount();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Command - " + sender.getName() + " reset the mace limiter");
        }

        sender.sendMessage(plugin.getMessageManager().get("commands.mace.reset-success"));

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);

        if (feature == null) {
            sender.sendMessage(plugin.getMessageManager().get("commands.mace.feature-not-found"));
            return true;
        }

        boolean enabled = feature.isEnabled();
        int crafted = feature.getMacesCrafted();
        int max = feature.getMaxMaces();

        sender.sendMessage(plugin.getMessageManager().get("commands.mace.status.title"));
        sender.sendMessage(plugin.getMessageManager().get(
                enabled ? "commands.mace.status.enabled-yes" : "commands.mace.status.enabled-no"));
        sender.sendMessage(
                plugin.getMessageManager().get("commands.mace.status.crafted", "crafted", crafted, "max", max));
        sender.sendMessage(plugin.getMessageManager().get(
                feature.canCraftMace() ? "commands.mace.status.can-craft-yes" : "commands.mace.status.can-craft-no"));

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
        sender.sendMessage(plugin.getMessageManager().get("commands.mace.help.title"));
        sender.sendMessage(plugin.getMessageManager().get("commands.mace.help.whitelist"));
        sender.sendMessage(plugin.getMessageManager().get("commands.mace.help.reset"));
        sender.sendMessage(plugin.getMessageManager().get("commands.mace.help.status"));
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
