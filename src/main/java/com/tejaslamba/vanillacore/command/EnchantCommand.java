package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.utils.EnchantmentUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private static final String WHITELIST_LORE = "§8[§6EnchantWhitelisted§8]";

    public EnchantCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillacore.enchant")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "limit":
                return handleLimit(sender, args);
            case "unlimit":
                return handleUnlimit(sender, args);
            case "list":
                return handleList(sender);
            case "block":
                return handleBlock(sender, args);
            case "unblock":
                return handleUnblock(sender, args);
            case "scan":
                return handleScan(sender);
            case "whitelist":
                return handleWhitelist(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleLimit(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.limit.usage"));
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.invalid-enchantment", "enchant", args[1]));
            return true;
        }

        int maxLevel;
        try {
            maxLevel = Integer.parseInt(args[2]);
            if (maxLevel < 0) {
                sender.sendMessage(plugin.getMessageManager().get("commands.enchant.limit.max-level-min"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.invalid-number", "value", args[2]));
            return true;
        }

        String option = args.length == 4 ? args[3].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.invalid-option"));
            return true;
        }

        String enchantKey = enchant.getKey().getKey();

        if (option.equals("anvil") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.custom-anvil-caps.caps." + enchantKey, maxLevel);
        }
        if (option.equals("replacement") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.enchantment-replacement.caps." + enchantKey, maxLevel);
        }
        plugin.getConfigManager().save();

        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        if (maxLevel == 0) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.blocked", "enchant", enchantKey,
                    "option", optionText));
        } else {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.limit.set", "enchant", enchantKey,
                    "maxLevel", maxLevel, "option", optionText));
        }

        return true;
    }

    private boolean handleUnlimit(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.unlimit.usage"));
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.invalid-enchantment", "enchant", args[1]));
            return true;
        }

        String option = args.length == 3 ? args[2].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.invalid-option"));
            return true;
        }

        String enchantKey = enchant.getKey().getKey();
        ConfigurationSection anvilCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.custom-anvil-caps.caps");
        ConfigurationSection replacementCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.enchantment-replacement.caps");

        boolean hasAnvilCap = anvilCaps != null && anvilCaps.contains(enchantKey);
        boolean hasReplacementCap = replacementCaps != null && replacementCaps.contains(enchantKey);

        if (option.equals("anvil") && !hasAnvilCap) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.unlimit.no-anvil", "enchant", enchantKey));
            return true;
        }
        if (option.equals("replacement") && !hasReplacementCap) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.unlimit.no-replacement", "enchant", enchantKey));
            return true;
        }
        if (option.equals("both") && !hasAnvilCap && !hasReplacementCap) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.unlimit.no-limit", "enchant", enchantKey));
            return true;
        }

        if (option.equals("anvil") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.custom-anvil-caps.caps." + enchantKey, null);
        }
        if (option.equals("replacement") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.enchantment-replacement.caps." + enchantKey, null);
        }
        plugin.getConfigManager().save();

        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.unlimit.removed", "enchant", enchantKey,
                "option", optionText));

        return true;
    }

    private boolean handleList(CommandSender sender) {
        ConfigurationSection anvilCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.custom-anvil-caps.caps");
        ConfigurationSection replacementCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.enchantment-replacement.caps");

        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.title"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.spacer"));

        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.anvil-title"));
        if (anvilCaps == null || anvilCaps.getKeys(false).isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.anvil-empty"));
        } else {
            Map<String, Object> limits = anvilCaps.getValues(false);
            limits.forEach((enchant, cap) -> {
                if (cap.equals(0)) {
                    sender.sendMessage(
                            plugin.getMessageManager().get("commands.enchant.list.entry-blocked", "enchant", enchant));
                } else {
                    sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.entry-limit",
                            "enchant", enchant,
                            "limit", cap));
                }
            });
        }

        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.spacer"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.replacement-title"));
        if (replacementCaps == null || replacementCaps.getKeys(false).isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.replacement-empty"));
        } else {
            Map<String, Object> limits = replacementCaps.getValues(false);
            limits.forEach((enchant, cap) -> {
                if (cap.equals(0)) {
                    sender.sendMessage(
                            plugin.getMessageManager().get("commands.enchant.list.entry-blocked", "enchant", enchant));
                } else {
                    sender.sendMessage(plugin.getMessageManager().get("commands.enchant.list.entry-limit",
                            "enchant", enchant,
                            "limit", cap));
                }
            });
        }

        return true;
    }

    private boolean handleBlock(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.block.usage"));
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage(
                    plugin.getMessageManager().get("commands.enchant.invalid-enchantment", "enchant", args[1]));
            return true;
        }

        String option = args.length == 3 ? args[2].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.invalid-option"));
            return true;
        }

        String enchantKey = enchant.getKey().getKey();

        if (option.equals("anvil") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.custom-anvil-caps.caps." + enchantKey, 0);
        }
        if (option.equals("replacement") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.enchantment-replacement.caps." + enchantKey, 0);
        }
        plugin.getConfigManager().save();

        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.blocked", "enchant", enchantKey,
                "option", optionText));

        return true;
    }

    private boolean handleUnblock(CommandSender sender, String[] args) {
        return handleUnlimit(sender, args);
    }

    private boolean handleScan(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().get("general.player-only"));
            return true;
        }

        if (plugin.getFeatureManager().getFeatures().stream()
                .anyMatch(f -> f.getConfigPath().equals("features.enchantment-replacement") && f.isEnabled())) {

            int scanned = 0;
            int modified = 0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    if (isWhitelisted(item)) {
                        continue;
                    }

                    Map<Enchantment, Integer> before = EnchantmentUtils.getAllEnchantments(item);
                    if (!before.isEmpty()) {
                        scanned++;
                        plugin.getServer().getPluginManager().callEvent(
                                new org.bukkit.event.player.PlayerJoinEvent(player,
                                        (net.kyori.adventure.text.Component) null));

                        Map<Enchantment, Integer> after = EnchantmentUtils.getAllEnchantments(item);
                        if (!before.equals(after)) {
                            modified++;
                        }
                    }
                }
            }

            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.scan.result", "scanned", scanned,
                    "modified", modified));
        } else {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.scan.replacement-disabled"));
        }

        return true;
    }

    private boolean handleWhitelist(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().get("general.player-only"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.whitelist.must-hold-item"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        if (isWhitelisted(item)) {
            lore.remove(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.whitelist.removed"));
        } else {
            lore.add(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage(plugin.getMessageManager().get("commands.enchant.whitelist.added"));
        }

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
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.title"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.limit"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.unlimit"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.list"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.block"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.unblock"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.scan"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.whitelist"));
        sender.sendMessage(plugin.getMessageManager().get("commands.enchant.help.options"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("limit", "unlimit", "list", "block", "unblock",
                    "scan", "whitelist"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("limit") ||
                args[0].equalsIgnoreCase("unlimit") || args[0].equalsIgnoreCase("block") ||
                args[0].equalsIgnoreCase("unblock"))) {
            Registry.ENCHANTMENT.iterator().forEachRemaining(enchant -> completions.add(enchant.getKey().getKey()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("limit")) {
            completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "10"));
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("unlimit") ||
                args[0].equalsIgnoreCase("block") || args[0].equalsIgnoreCase("unblock"))) {
            completions.addAll(Arrays.asList("anvil", "replacement", "both"));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("limit")) {
            completions.addAll(Arrays.asList("anvil", "replacement", "both"));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
