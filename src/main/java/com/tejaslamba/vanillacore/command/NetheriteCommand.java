package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.features.NetheriteDisablerFeature;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class NetheriteCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final Map<String, Material> netheriteItems = new HashMap<>();

    public NetheriteCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        initializeItems();
    }

    private void initializeItems() {
        netheriteItems.put("sword", Material.NETHERITE_SWORD);
        netheriteItems.put("axe", Material.NETHERITE_AXE);
        netheriteItems.put("pickaxe", Material.NETHERITE_PICKAXE);
        netheriteItems.put("shovel", Material.NETHERITE_SHOVEL);
        netheriteItems.put("hoe", Material.NETHERITE_HOE);
        netheriteItems.put("helmet", Material.NETHERITE_HELMET);
        netheriteItems.put("chestplate", Material.NETHERITE_CHESTPLATE);
        netheriteItems.put("leggings", Material.NETHERITE_LEGGINGS);
        netheriteItems.put("boots", Material.NETHERITE_BOOTS);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillacore.netherite")) {
            sender.sendMessage(MessageManager.parse(
                    "<dark_gray>[<gold>Vanilla Core<dark_gray>] <red>You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MessageManager.parse("<red>Only players can open the GUI!"));
                return true;
            }
            openGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MessageManager.parse("<red>Only players can open the GUI!"));
                return true;
            }
            openGUI(player);
            return true;
        }

        if (args.length != 2) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        String item = args[1].toLowerCase();

        if (!netheriteItems.containsKey(item)) {
            sender.sendMessage(MessageManager.parse(
                    "<red>Invalid item! Valid items: sword, axe, pickaxe, shovel, hoe, helmet, chestplate, leggings, boots"));
            return true;
        }

        NetheriteDisablerFeature feature = getFeature();
        if (feature == null) {
            sender.sendMessage(MessageManager.parse("<red>Netherite Disabler feature not found!"));
            return true;
        }

        Material material = netheriteItems.get(item);
        boolean shouldDisable = action.equals("disable");

        feature.setDisabled(material, shouldDisable);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Netherite Command - " + sender.getName() + " "
                    + (shouldDisable ? "disabled" : "enabled") + " " + material.name());
        }

        String displayItem = item.substring(0, 1).toUpperCase() + item.substring(1);
        sender.sendMessage(MessageManager.parse("<dark_gray>[<gold>Vanilla Core<dark_gray>] "
                + (shouldDisable ? "<red>Disabled" : "<green>Enabled") + " <white>Netherite " + displayItem));

        return true;
    }

    private void openGUI(Player player) {
        NetheriteDisablerFeature feature = getFeature();
        if (feature == null) {
            player.sendMessage(MessageManager.parse("<red>Netherite Disabler feature not found!"));
            return;
        }

        feature.openNetheriteGUI(player);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Netherite Command - Opened GUI for " + player.getName());
        }
    }

    private NetheriteDisablerFeature getFeature() {
        return (NetheriteDisablerFeature) plugin.getFeatureManager().getFeature("Netherite Disabler");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageManager.parse("<gold><bold>=== Netherite Commands ==="));
        sender.sendMessage(MessageManager.parse("<yellow>/vanilla netherite gui <gray>- Open netherite manager GUI"));
        sender.sendMessage(
                MessageManager.parse("<yellow>/vanilla netherite disable <item> <gray>- Disable netherite item"));
        sender.sendMessage(
                MessageManager.parse("<yellow>/vanilla netherite enable <item> <gray>- Enable netherite item"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("gui", "disable", "enable"));
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("gui")) {
            completions.addAll(netheriteItems.keySet());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
