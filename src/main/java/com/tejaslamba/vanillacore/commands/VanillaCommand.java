package com.tejaslamba.vanillacore.commands;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.command.EnchantCommand;
import com.tejaslamba.vanillacore.command.MaceCommand;
import com.tejaslamba.vanillacore.command.NetheriteCommand;
import com.tejaslamba.vanillacore.command.RitualCommand;
import com.tejaslamba.vanillacore.feature.Feature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VanillaCommand implements CommandExecutor, TabCompleter {

    private static final String SUBCOMMAND_RELOAD = "reload";
    private static final String SUBCOMMAND_VERSION = "version";
    private static final String PERMISSION_RELOAD = "smp.reload";
    private static final String PERMISSION_VERSION = "smp.version";
    private static final String PLACEHOLDER_MODULE = "module";
    private static final String MODULE_ALL = "all";
    private static final String MODULE_CONFIG = "config";
    private static final String MODULE_MESSAGES = "messages";
    private static final String MODULE_MENUS = "menus";
    private static final String MODULE_FEATURES = "features";
    private static final List<String> RELOAD_MODULES = List.of(MODULE_ALL, MODULE_CONFIG, MODULE_MESSAGES,
            MODULE_MENUS,
            MODULE_FEATURES);

    private final EnchantCommand enchantCommand;
    private final MaceCommand maceCommand;
    private final NetheriteCommand netheriteCommand;
    private final com.tejaslamba.vanillacore.command.InfiniteRestockCommand infiniteRestockCommand;
    private final RitualCommand ritualCommand;
    private final SocialCommand socialCommand;

    public VanillaCommand() {
        this.enchantCommand = new EnchantCommand(VanillaCorePlugin.getInstance());
        this.maceCommand = new MaceCommand(VanillaCorePlugin.getInstance());
        this.netheriteCommand = new NetheriteCommand(VanillaCorePlugin.getInstance());
        this.infiniteRestockCommand = new com.tejaslamba.vanillacore.command.InfiniteRestockCommand(
                VanillaCorePlugin.getInstance());
        this.ritualCommand = new RitualCommand(VanillaCorePlugin.getInstance());
        this.socialCommand = new SocialCommand(VanillaCorePlugin.getInstance());
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
            if (!p.hasPermission("smp.menu")) {
                msg().sendPrefixed(p, "commands.menu.no-permission");
                return true;
            }
            VanillaCorePlugin.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("enchant")) {
            if (!sender.hasPermission("smp.enchant")) {
                msg().sendPrefixed(sender, "commands.enchant.no-permission");
                return true;
            }
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onCommand(sender, command, label, enchantArgs);
        }

        if (args[0].equalsIgnoreCase("mace")) {
            if (!sender.hasPermission("smp.mace")) {
                msg().sendPrefixed(sender, "commands.mace.no-permission");
                return true;
            }
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onCommand(sender, command, label, maceArgs);
        }

        if (args[0].equalsIgnoreCase("netherite")) {
            if (!sender.hasPermission("smp.netherite")) {
                msg().sendPrefixed(sender, "commands.netherite.no-permission");
                return true;
            }
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onCommand(sender, command, label, netheriteArgs);
        }

        if (args[0].equalsIgnoreCase("infiniterestock")) {
            if (!sender.hasPermission("smp.infiniterestock")) {
                msg().sendPrefixed(sender, "commands.infinite-restock.no-permission");
                return true;
            }
            String[] irArgs = new String[args.length - 1];
            System.arraycopy(args, 1, irArgs, 0, args.length - 1);
            return infiniteRestockCommand.onCommand(sender, command, label, irArgs);
        }

        if (args[0].equalsIgnoreCase("ritual")) {
            if (!sender.hasPermission("smp.ritual")) {
                msg().sendPrefixed(sender, "commands.ritual.no-permission");
                return true;
            }
            String[] ritualArgs = new String[args.length - 1];
            System.arraycopy(args, 1, ritualArgs, 0, args.length - 1);
            return ritualCommand.onCommand(sender, command, label, ritualArgs);
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player p)) {
                msg().sendPrefixed(sender, "general.player-only");
                return true;
            }
            if (!p.hasPermission("smp.menu")) {
                msg().sendPrefixed(p, "commands.menu.no-permission");
                return true;
            }
            VanillaCorePlugin.getInstance().getMenuManager().openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase(SUBCOMMAND_RELOAD)) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                msg().sendPrefixed(sender, "commands.reload.no-permission");
                return true;
            }
            handleReload(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase(SUBCOMMAND_VERSION)) {
            if (!sender.hasPermission(PERMISSION_VERSION)) {
                msg().sendPrefixed(sender, "commands.version.no-permission");
                return true;
            }
            String version = VanillaCorePlugin.getInstance().getPluginMeta().getVersion();
            msg().sendPrefixed(sender, "commands.version.current", SUBCOMMAND_VERSION, version);
            return true;
        }

        if (socialCommand.onCommand(sender, args)) {
            return true;
        }

        msg().sendPrefixed(sender, "general.unknown-command");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("smp.menu")) {
                completions.add("menu");
            }
            if (sender.hasPermission("smp.enchant")) {
                completions.add("enchant");
            }
            if (sender.hasPermission("smp.mace")) {
                completions.add("mace");
            }
            if (sender.hasPermission("smp.netherite")) {
                completions.add("netherite");
            }
            if (sender.hasPermission("smp.infiniterestock")) {
                completions.add("infiniterestock");
            }
            if (sender.hasPermission("smp.ritual")) {
                completions.add("ritual");
            }
            if (sender.hasPermission(PERMISSION_RELOAD)) {
                completions.add(SUBCOMMAND_RELOAD);
            }
            if (sender.hasPermission(PERMISSION_VERSION)) {
                completions.add(SUBCOMMAND_VERSION);
            }
            if (sender.hasPermission("smp.msg")) {
                completions.add("msg");
            }
            if (sender.hasPermission("smp.reply")) {
                completions.add("reply");
            }
            if (sender.hasPermission("smp.socialspy")) {
                completions.add("socialspy");
            }
            if (sender.hasPermission("smp.toggle.chat")) {
                completions.add("togglechat");
            }
            if (sender.hasPermission("smp.toggle.pm")) {
                completions.add("togglepm");
            }
            if (sender.hasPermission("smp.toggle.mentions")) {
                completions.add("togglementions");
            }
            if (sender.hasPermission("smp.block")) {
                completions.add("block");
            }
            if (sender.hasPermission("smp.unblock")) {
                completions.add("unblock");
            }
            if (sender.hasPermission("smp.blocklist")) {
                completions.add("blocked");
            }
            if (sender.hasPermission("smp.announcements")) {
                completions.add("announcements");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase(SUBCOMMAND_RELOAD)
                && sender.hasPermission(PERMISSION_RELOAD)) {
            return RELOAD_MODULES.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("enchant") && sender.hasPermission("smp.enchant")) {
            String[] enchantArgs = new String[args.length - 1];
            System.arraycopy(args, 1, enchantArgs, 0, args.length - 1);
            return enchantCommand.onTabComplete(sender, command, alias, enchantArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("mace") && sender.hasPermission("smp.mace")) {
            String[] maceArgs = new String[args.length - 1];
            System.arraycopy(args, 1, maceArgs, 0, args.length - 1);
            return maceCommand.onTabComplete(sender, command, alias, maceArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("netherite")
                && sender.hasPermission("smp.netherite")) {
            String[] netheriteArgs = new String[args.length - 1];
            System.arraycopy(args, 1, netheriteArgs, 0, args.length - 1);
            return netheriteCommand.onTabComplete(sender, command, alias, netheriteArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("infiniterestock")
                && sender.hasPermission("smp.infiniterestock")) {
            String[] irArgs = new String[args.length - 1];
            System.arraycopy(args, 1, irArgs, 0, args.length - 1);
            return infiniteRestockCommand.onTabComplete(sender, command, alias, irArgs);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("ritual")
                && sender.hasPermission("smp.ritual")) {
            String[] ritualArgs = new String[args.length - 1];
            System.arraycopy(args, 1, ritualArgs, 0, args.length - 1);
            return ritualCommand.onTabComplete(sender, command, alias, ritualArgs);
        }

        List<String> socialCompletions = socialCommand.onTabComplete(args);
        if (!socialCompletions.isEmpty()) {
            return socialCompletions;
        }

        return Collections.emptyList();
    }

    private void handleReload(CommandSender sender, String[] args) {
        String target = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : MODULE_ALL;
        if (!RELOAD_MODULES.contains(target)) {
            msg().sendPrefixed(sender, "commands.reload.invalid-module", PLACEHOLDER_MODULE, target);
            msg().sendPrefixed(sender, "commands.reload.usage");
            return;
        }

        long startNanos = System.nanoTime();
        List<String> successfulModules = new ArrayList<>();
        List<String> failedModules = new ArrayList<>();

        switch (target) {
            case MODULE_CONFIG -> reloadConfigModule(successfulModules, failedModules);
            case MODULE_MESSAGES -> reloadMessagesModule(successfulModules, failedModules);
            case MODULE_MENUS -> reloadMenusModule(successfulModules, failedModules);
            case MODULE_FEATURES -> reloadFeaturesModule(successfulModules, failedModules);
            case MODULE_ALL -> {
                reloadConfigModule(successfulModules, failedModules);
                reloadMessagesModule(successfulModules, failedModules);
                reloadMenusModule(successfulModules, failedModules);
                reloadFeaturesModule(successfulModules, failedModules);
            }
            default -> {
                msg().sendPrefixed(sender, "commands.reload.usage");
                return;
            }
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        if (MODULE_ALL.equals(target)) {
            msg().sendPrefixed(sender, "commands.reload.summary",
                    "successful", String.valueOf(successfulModules.size()),
                    "failed", String.valueOf(failedModules.size()),
                    "time", String.valueOf(elapsedMs));
        } else if (failedModules.isEmpty()) {
            msg().sendPrefixed(sender, "commands.reload.module-success",
                    PLACEHOLDER_MODULE, target,
                    "time", String.valueOf(elapsedMs));
        } else {
            msg().sendPrefixed(sender, "commands.reload.module-failed",
                    PLACEHOLDER_MODULE, target,
                    "time", String.valueOf(elapsedMs));
        }

        if (!failedModules.isEmpty()) {
            msg().sendPrefixed(sender, "commands.reload.failed-list", "modules", String.join(", ", failedModules));
        }
    }

    private void reloadConfigModule(List<String> successfulModules, List<String> failedModules) {
        runReloadModule(MODULE_CONFIG, successfulModules, failedModules, () -> {
            VanillaCorePlugin.getInstance().getConfigManager().load();
            VanillaCorePlugin.getInstance().refreshVerbose();
        });
    }

    private void reloadMessagesModule(List<String> successfulModules, List<String> failedModules) {
        runReloadModule(MODULE_MESSAGES, successfulModules, failedModules,
                () -> VanillaCorePlugin.getInstance().getMessageManager().reload());
    }

    private void reloadMenusModule(List<String> successfulModules, List<String> failedModules) {
        runReloadModule(MODULE_MENUS, successfulModules, failedModules, () -> {
            VanillaCorePlugin.getInstance().getMenuConfigManager().load();
            VanillaCorePlugin.getInstance().getMenuManager().load();
        });
    }

    private void reloadFeaturesModule(List<String> successfulModules, List<String> failedModules) {
        runReloadModule(MODULE_FEATURES, successfulModules, failedModules, this::reloadFeatures);
    }

    private void runReloadModule(String module, List<String> successfulModules, List<String> failedModules,
            Runnable action) {
        try {
            action.run();
            successfulModules.add(module);
        } catch (Exception e) {
            failedModules.add(module);
            VanillaCorePlugin.getInstance().getLogger().warning(
                    "Failed to reload module '" + module + "': " + e.getMessage());
        }
    }

    private void reloadFeatures() {
        Collection<Feature> features = VanillaCorePlugin.getInstance().getFeatureManager().getFeatures();
        if (features == null || features.isEmpty()) {
            return;
        }

        List<String> failedFeatures = new ArrayList<>();
        for (Feature feature : features) {
            try {
                feature.reload();
            } catch (Exception e) {
                failedFeatures.add(feature.getClass().getSimpleName());
                VanillaCorePlugin.getInstance().getLogger().warning(
                        "Failed to reload feature " + feature.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        if (!failedFeatures.isEmpty()) {
            throw new IllegalStateException("Failed features: " + String.join(", ", failedFeatures));
        }
    }

}
