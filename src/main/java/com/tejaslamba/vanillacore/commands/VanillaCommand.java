package com.tejaslamba.vanillacore.commands;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.command.EnchantCommand;
import com.tejaslamba.vanillacore.command.MaceCommand;
import com.tejaslamba.vanillacore.command.NetheriteCommand;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VanillaCommand implements CommandExecutor, TabCompleter {

    private final EnchantCommand enchantCommand;
    private final MaceCommand maceCommand;
    private final NetheriteCommand netheriteCommand;
    private final com.tejaslamba.vanillacore.command.InfiniteRestockCommand infiniteRestockCommand;

    public VanillaCommand() {
        this.enchantCommand = new EnchantCommand(VanillaCorePlugin.getInstance());
        this.maceCommand = new MaceCommand(VanillaCorePlugin.getInstance());
        this.netheriteCommand = new NetheriteCommand(VanillaCorePlugin.getInstance());
        this.infiniteRestockCommand = new com.tejaslamba.vanillacore.command.InfiniteRestockCommand(
                VanillaCorePlugin.getInstance());
    }

    private MessageManager msg() {
        return VanillaCorePlugin.getInstance().getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                msg().sendPrefixed(sender, "general.player-only");
                return true;
            }
            if (!p.hasPermission("vanillacore.menu")) {
                msg().sendPrefixed(p, "commands.menu.no-permission");
                return true;
            }
            VanillaCorePlugin.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("enchant")) {
            if (!sender.hasPermission("vanillacore.enchant")) {
                msg().sendPrefixed(sender, "commands.enchant.no-permission");
                return true;
            }
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onCommand(sender, command, label, enchantArgs);
        }

        if (args[0].equalsIgnoreCase("mace")) {
            if (!sender.hasPermission("vanillacore.mace")) {
                msg().sendPrefixed(sender, "commands.mace.no-permission");
                return true;
            }
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onCommand(sender, command, label, maceArgs);
        }

        if (args[0].equalsIgnoreCase("netherite")) {
            if (!sender.hasPermission("vanillacore.netherite")) {
                msg().sendPrefixed(sender, "commands.netherite.no-permission");
                return true;
            }
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onCommand(sender, command, label, netheriteArgs);
        }

        if (args[0].equalsIgnoreCase("infiniterestock")) {
            if (!sender.hasPermission("vanillacore.infiniterestock")) {
                msg().sendPrefixed(sender, "commands.infinite-restock.no-permission");
                return true;
            }
            String[] irArgs = new String[args.length - 1];
            System.arraycopy(args, 1, irArgs, 0, args.length - 1);
            return infiniteRestockCommand.onCommand(sender, command, label, irArgs);
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player p)) {
                msg().sendPrefixed(sender, "general.player-only");
                return true;
            }
            if (!p.hasPermission("vanillacore.menu")) {
                msg().sendPrefixed(p, "commands.menu.no-permission");
                return true;
            }
            VanillaCorePlugin.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("vanillacore.reload")) {
                msg().sendPrefixed(sender, "commands.reload.no-permission");
                return true;
            }
            VanillaCorePlugin.getInstance().getConfigManager().load();
            VanillaCorePlugin.getInstance().getMessageManager().reload();
            VanillaCorePlugin.getInstance().getMenuConfigManager().load();
            VanillaCorePlugin.getInstance().refreshVerbose();
            VanillaCorePlugin.getInstance().getFeatureManager().getFeatures()
                    .forEach(com.tejaslamba.vanillacore.feature.Feature::reload);
            msg().sendPrefixed(sender, "commands.reload.success");
            return true;
        }

        msg().sendPrefixed(sender, "general.unknown-command");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("vanillacore.menu")) {
                completions.add("menu");
            }
            if (sender.hasPermission("vanillacore.enchant")) {
                completions.add("enchant");
            }
            if (sender.hasPermission("vanillacore.mace")) {
                completions.add("mace");
            }
            if (sender.hasPermission("vanillacore.netherite")) {
                completions.add("netherite");
            }
            if (sender.hasPermission("vanillacore.infiniterestock")) {
                completions.add("infiniterestock");
            }
            if (sender.hasPermission("vanillacore.reload")) {
                completions.add("reload");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("enchant") && sender.hasPermission("vanillacore.enchant")) {
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onTabComplete(sender, command, alias, enchantArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("mace") && sender.hasPermission("vanillacore.mace")) {
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onTabComplete(sender, command, alias, maceArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("netherite")
                && sender.hasPermission("vanillacore.netherite")) {
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onTabComplete(sender, command, alias, netheriteArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("infiniterestock")
                && sender.hasPermission("vanillacore.infiniterestock")) {
            String[] irArgs = new String[args.length - 1];
            System.arraycopy(args, 1, irArgs, 0, args.length - 1);
            return infiniteRestockCommand.onTabComplete(sender, command, alias, irArgs);
        }

        return Collections.emptyList();
    }

}
