package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.Main;
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

    private final Main plugin;
    private static final String WHITELIST_LORE = "§8[§6EnchantWhitelisted§8]";

    public EnchantCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.enchant")) {
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
            sender.sendMessage("§cUsage: /smp enchant limit <enchant> <maxLevel> [anvil|replacement|both]");
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage("§cInvalid enchantment: " + args[1]);
            return true;
        }

        int maxLevel;
        try {
            maxLevel = Integer.parseInt(args[2]);
            if (maxLevel < 0) {
                sender.sendMessage("§cMax level must be 0 or greater!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number: " + args[2]);
            return true;
        }

        String option = args.length == 4 ? args[3].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage("§cInvalid option! Use: anvil, replacement, or both");
            return true;
        }

        String enchantKey = enchant.getKey().getKey();
        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");

        if (option.equals("anvil") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.custom-anvil-caps.caps." + enchantKey, maxLevel);
        }
        if (option.equals("replacement") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.enchantment-replacement.caps." + enchantKey, maxLevel);
        }
        plugin.getConfigManager().save();

        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        if (maxLevel == 0) {
            sender.sendMessage(prefix + " §aBlocked enchantment: §f" + enchantKey + " §7(" + optionText + ")");
        } else {
            sender.sendMessage(prefix + " §aSet enchantment limit: §f" + enchantKey + " §7→ §e" + maxLevel + " §7("
                    + optionText + ")");
        }

        return true;
    }

    private boolean handleUnlimit(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("§cUsage: /smp enchant unlimit <enchant> [anvil|replacement|both]");
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage("§cInvalid enchantment: " + args[1]);
            return true;
        }

        String option = args.length == 3 ? args[2].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage("§cInvalid option! Use: anvil, replacement, or both");
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
            sender.sendMessage("§cNo anvil limit set for: " + enchantKey);
            return true;
        }
        if (option.equals("replacement") && !hasReplacementCap) {
            sender.sendMessage("§cNo replacement limit set for: " + enchantKey);
            return true;
        }
        if (option.equals("both") && !hasAnvilCap && !hasReplacementCap) {
            sender.sendMessage("§cNo limit set for: " + enchantKey);
            return true;
        }

        if (option.equals("anvil") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.custom-anvil-caps.caps." + enchantKey, null);
        }
        if (option.equals("replacement") || option.equals("both")) {
            plugin.getConfigManager().get().set("features.enchantment-replacement.caps." + enchantKey, null);
        }
        plugin.getConfigManager().save();

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        sender.sendMessage(prefix + " §aRemoved limit for: §f" + enchantKey + " §7(" + optionText + ")");

        return true;
    }

    private boolean handleList(CommandSender sender) {
        ConfigurationSection anvilCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.custom-anvil-caps.caps");
        ConfigurationSection replacementCaps = plugin.getConfigManager().get()
                .getConfigurationSection("features.enchantment-replacement.caps");

        sender.sendMessage("§6§l=== Enchantment Limits ===");
        sender.sendMessage("");

        sender.sendMessage("§e§lAnvil Caps:");
        if (anvilCaps == null || anvilCaps.getKeys(false).isEmpty()) {
            sender.sendMessage("  §7No anvil caps configured.");
        } else {
            Map<String, Object> limits = anvilCaps.getValues(false);
            limits.forEach((enchant, cap) -> {
                String capText = cap.equals(0) ? "§c§lBLOCKED" : "§e" + cap;
                sender.sendMessage("  §7• §f" + enchant + " §7→ " + capText);
            });
        }

        sender.sendMessage("");
        sender.sendMessage("§e§lReplacement Caps:");
        if (replacementCaps == null || replacementCaps.getKeys(false).isEmpty()) {
            sender.sendMessage("  §7No replacement caps configured.");
        } else {
            Map<String, Object> limits = replacementCaps.getValues(false);
            limits.forEach((enchant, cap) -> {
                String capText = cap.equals(0) ? "§c§lBLOCKED" : "§e" + cap;
                sender.sendMessage("  §7• §f" + enchant + " §7→ " + capText);
            });
        }

        return true;
    }

    private boolean handleBlock(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("§cUsage: /smp enchant block <enchant> [anvil|replacement|both]");
            return true;
        }

        Enchantment enchant = EnchantmentUtils.parseEnchantment(args[1]);
        if (enchant == null) {
            sender.sendMessage("§cInvalid enchantment: " + args[1]);
            return true;
        }

        String option = args.length == 3 ? args[2].toLowerCase() : "both";
        if (!option.equals("anvil") && !option.equals("replacement") && !option.equals("both")) {
            sender.sendMessage("§cInvalid option! Use: anvil, replacement, or both");
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

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        String optionText = option.equals("both") ? "both anvil & replacement" : option;
        sender.sendMessage(prefix + " §aBlocked enchantment: §f" + enchantKey + " §7(" + optionText + ")");

        return true;
    }

    private boolean handleUnblock(CommandSender sender, String[] args) {
        return handleUnlimit(sender, args);
    }

    private boolean handleScan(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
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
                                new org.bukkit.event.player.PlayerJoinEvent(player, null));

                        Map<Enchantment, Integer> after = EnchantmentUtils.getAllEnchantments(item);
                        if (!before.equals(after)) {
                            modified++;
                        }
                    }
                }
            }

            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            sender.sendMessage(prefix + " §aScanned §e" + scanned + " §aitems, modified §e" + modified);
        } else {
            sender.sendMessage("§cEnchantment Replacement feature is not enabled!");
        }

        return true;
    }

    private boolean handleWhitelist(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            sender.sendMessage("§cYou must be holding an item!");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        if (isWhitelisted(item)) {
            lore.remove(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);

            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            sender.sendMessage(prefix + " §cRemoved enchantment scan whitelist from item");
        } else {
            lore.add(WHITELIST_LORE);
            meta.setLore(lore);
            item.setItemMeta(meta);

            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            sender.sendMessage(prefix + " §aWhitelisted item from enchantment scans");
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
        sender.sendMessage("§6§l=== Enchantment Commands ===");
        sender.sendMessage("§e/smp enchant limit <enchant> <maxLevel> [option] §7- Set enchantment limit");
        sender.sendMessage("§e/smp enchant unlimit <enchant> [option] §7- Remove enchantment limit");
        sender.sendMessage("§e/smp enchant list §7- Show all limits");
        sender.sendMessage("§e/smp enchant block <enchant> [option] §7- Block enchantment completely");
        sender.sendMessage("§e/smp enchant unblock <enchant> [option] §7- Unblock enchantment");
        sender.sendMessage("§e/smp enchant scan §7- Manually scan your inventory");
        sender.sendMessage("§e/smp enchant whitelist §7- Toggle whitelist on held item");
        sender.sendMessage("§7Options: §fانvil §7| §freplacement §7| §fboth §7(default)");
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
