package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import com.tejaslamba.vanillacore.command.DimensionCommand;
import com.tejaslamba.vanillacore.command.HomeCommand;
import com.tejaslamba.vanillacore.command.KitCommand;
import com.tejaslamba.vanillacore.command.RitualCommand;
import com.tejaslamba.vanillacore.command.TeleportCommand;
import com.tejaslamba.vanillacore.command.UtilityCommand;
import com.tejaslamba.vanillacore.command.VanillaCommand;
import com.tejaslamba.vanillacore.command.WarpCommand;

public class CommandManager {

    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDefaults() {
        VanillaCommand vanillaCommand = new VanillaCommand();
        registerCommand("smp", vanillaCommand);
        registerCommand("msg", vanillaCommand);
        registerCommand("reply", vanillaCommand);
        registerCommand("socialspy", vanillaCommand);
        registerCommand("ritual", new RitualCommand((VanillaCorePlugin) plugin));

        TeleportCommand teleportCommand = new TeleportCommand((VanillaCorePlugin) plugin);
        registerCommand("tpa", teleportCommand);
        registerCommand("tpahere", teleportCommand);
        registerCommand("tpaccept", teleportCommand);
        registerCommand("tpdeny", teleportCommand);
        registerCommand("spawn", teleportCommand);
        registerCommand("setspawn", teleportCommand);
        registerCommand("back", teleportCommand);
        registerCommand("tp", teleportCommand);
        registerCommand("tphere", teleportCommand);

        HomeCommand homeCommand = new HomeCommand((VanillaCorePlugin) plugin);
        registerCommand("home", homeCommand);
        registerCommand("sethome", homeCommand);
        registerCommand("delhome", homeCommand);
        registerCommand("homes", homeCommand);

        WarpCommand warpCommand = new WarpCommand((VanillaCorePlugin) plugin);
        registerCommand("warp", warpCommand);
        registerCommand("setwarp", warpCommand);
        registerCommand("delwarp", warpCommand);
        registerCommand("warps", warpCommand);

        KitCommand kitCommand = new KitCommand((VanillaCorePlugin) plugin);
        registerCommand("kit", kitCommand);
        registerCommand("kits", kitCommand);

        UtilityCommand utilityCommand = new UtilityCommand((VanillaCorePlugin) plugin);
        registerCommand("fly", utilityCommand);
        registerCommand("god", utilityCommand);
        registerCommand("speed", utilityCommand);
        registerCommand("repair", utilityCommand);
        registerCommand("giveitem", utilityCommand);
        registerCommand("workbench", utilityCommand);
        registerCommand("anvil", utilityCommand);
        registerCommand("ec", utilityCommand);
        registerCommand("invsee", utilityCommand);
        registerCommand("clearinv", utilityCommand);

        registerCommand("nether", new DimensionCommand((VanillaCorePlugin) plugin, "nether"));
        registerCommand("end", new DimensionCommand((VanillaCorePlugin) plugin, "end"));
    }

    public void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
                cmd.setTabCompleter(tabCompleter);
            }
        }
    }

}

