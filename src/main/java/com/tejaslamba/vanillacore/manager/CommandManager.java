package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import com.tejaslamba.vanillacore.commands.VanillaCommand;
import com.tejaslamba.vanillacore.command.DimensionCommand;

public class CommandManager {

    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDefaults() {
        registerCommand("vanilla", new VanillaCommand());

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
