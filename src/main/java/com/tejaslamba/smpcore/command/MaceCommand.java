package com.tejaslamba.smpcore.command;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.MaceLimiterFeature;
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

    private final Main plugin;
    private static final String WHITELIST_LORE = "§8[§6MaceWhitelisted§8]";

    public MaceCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.mace")) {
            sender.sendMessage(plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r")
                    + " §cYou don't have permission to use this command!");
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
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) {
            sender.sendMessage("§cYou must be holding a Mace!");
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

            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            sender.sendMessage(prefix + " §cRemoved mace limiter whitelist from item");
        } else {
            lore.add(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);

            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            sender.sendMessage(prefix + " §aWhitelisted mace from limiter");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender) {
        if (!sender.hasPermission("smpcore.mace.reset")) {
            sender.sendMessage("§cYou don't have permission to reset the mace limiter!");
            return true;
        }

        MaceLimiterFeature feature = (MaceLimiterFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof MaceLimiterFeature)
                .findFirst()
                .orElse(null);

        if (feature == null) {
            sender.sendMessage("§cMace Limiter feature not found!");
            return true;
        }

        feature.setMaceCrafted(false);

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger().info("[VERBOSE] Mace Command - " + sender.getName() + " reset the mace limiter");
        }

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        sender.sendMessage(prefix + " §aReset mace limiter! Mace can now be crafted again.");

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        MaceLimiterFeature feature = (MaceLimiterFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof MaceLimiterFeature)
                .findFirst()
                .orElse(null);

        if (feature == null) {
            sender.sendMessage("§cMace Limiter feature not found!");
            return true;
        }

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        boolean enabled = feature.isEnabled();
        boolean crafted = feature.isMaceCrafted();

        sender.sendMessage("§6§l=== Mace Limiter Status ===");
        sender.sendMessage("§eFeature Enabled: " + (enabled ? "§aYes" : "§cNo"));
        sender.sendMessage("§eMace Crafted: " + (crafted ? "§aYes" : "§cNo"));

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
        sender.sendMessage("§6§l=== Mace Commands ===");
        sender.sendMessage("§e/mace whitelist §7- Toggle whitelist on held mace");
        sender.sendMessage("§e/mace reset §7- Reset mace limiter (admin)");
        sender.sendMessage("§e/mace status §7- Check mace limiter status");
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
