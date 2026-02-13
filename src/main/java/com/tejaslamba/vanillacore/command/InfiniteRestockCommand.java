package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.Main;
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

    private final Main plugin;

    public InfiniteRestockCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§c[SMP] Command can only be used by a player");
            return true;
        }

        if (!p.hasPermission("smpcore.infiniterestock")) {
            p.sendMessage("§c[SMP] You don't have permission to manage Infinite Restock!");
            return true;
        }

        InfiniteRestockFeature feature = plugin.getFeatureManager().getFeature(InfiniteRestockFeature.class);
        if (feature == null) {
            p.sendMessage("§c[SMP] Infinite Restock feature not found");
            return true;
        }

        feature.openRestockGUI(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("smpcore.infiniterestock")) {
            return Collections.emptyList();
        }
        return new ArrayList<>();
    }
}
