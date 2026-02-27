package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.InfiniteRestockFeature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfiniteRestockCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;

    public InfiniteRestockCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§c[Vanilla Core] Command can only be used by a player");
            return true;
        }

        if (!p.hasPermission("vanillacore.infiniterestock")) {
            p.sendMessage("§c[Vanilla Core] You don't have permission to manage Infinite Restock!");
            return true;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);
        if (feature == null) {
            p.sendMessage("§c[Vanilla Core] Infinite Restock feature not found");
            return true;
        }

        feature.openRestockGUI(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("vanillacore.infiniterestock")) {
            return Collections.emptyList();
        }
        return new ArrayList<>();
    }
}
