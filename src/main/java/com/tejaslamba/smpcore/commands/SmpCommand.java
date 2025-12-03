package com.tejaslamba.smpcore.commands;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.command.EnchantCommand;
import com.tejaslamba.smpcore.command.MaceCommand;
import com.tejaslamba.smpcore.command.NetheriteCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmpCommand implements CommandExecutor, TabCompleter {

    private final EnchantCommand enchantCommand;
    private final MaceCommand maceCommand;
    private final NetheriteCommand netheriteCommand;

    public SmpCommand() {
        this.enchantCommand = new EnchantCommand(Main.getInstance());
        this.maceCommand = new MaceCommand(Main.getInstance());
        this.netheriteCommand = new NetheriteCommand(Main.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§c[SMP] Menu can only be opened by a player");
                return true;
            }
            Main.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("enchant")) {
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onCommand(sender, command, label, enchantArgs);
        }

        if (args[0].equalsIgnoreCase("mace")) {
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onCommand(sender, command, label, maceArgs);
        }

        if (args[0].equalsIgnoreCase("netherite")) {
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onCommand(sender, command, label, netheriteArgs);
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§c[SMP] Menu can only be opened by a player");
                return true;
            }
            Main.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("smpcore.reload")) {
                sender.sendMessage("§c[SMP] You don't have permission to reload");
                return true;
            }
            Main.getInstance().getConfigManager().load();
            Main.getInstance().getBanManager().load();
            Main.getInstance().getMenuConfigManager().load();
            sender.sendMessage("§a[SMP] Configuration reloaded successfully!");
            return true;
        }

        sender.sendMessage("§c[SMP] Unknown subcommand. Usage: /smp [menu|reload|enchant|mace|netherite]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("menu");
            completions.add("enchant");
            completions.add("mace");
            completions.add("netherite");
            if (sender.hasPermission("smpcore.reload")) {
                completions.add("reload");
            }
            return completions;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("enchant")) {
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onTabComplete(sender, command, alias, enchantArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("mace")) {
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onTabComplete(sender, command, alias, maceArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("netherite")) {
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onTabComplete(sender, command, alias, netheriteArgs);
        }

        return Collections.emptyList();
    }

}
