package com.tejaslamba.smpcore.manager;

import com.tejaslamba.smpcore.Main;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import com.tejaslamba.smpcore.commands.SmpCommand;
import com.tejaslamba.smpcore.command.DimensionCommand;

public class CommandManager {

    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDefaults() {
        registerCommand("smp", new SmpCommand());

        registerCommand("nether", new DimensionCommand((Main) plugin, "nether"));
        registerCommand("end", new DimensionCommand((Main) plugin, "end"));
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
