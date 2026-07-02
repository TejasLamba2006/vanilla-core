package com.tejaslamba.vanillacore.command;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.UtilityManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UtilityCommand implements CommandExecutor, TabCompleter {

    private final VanillaCorePlugin plugin;
    private final UtilityManager utilityManager;

    public UtilityCommand(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        this.utilityManager = plugin.getUtilityManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        return switch (name) {
            case "fly" -> handleFly(sender, args);
            case "god" -> handleGod(sender, args);
            case "speed" -> handleSpeed(sender, args);
            case "repair" -> handleRepair(sender, args);
            case "giveitem" -> handleGiveItem(sender, args);
            case "workbench" -> handleWorkbench(sender);
            case "anvil" -> handleAnvil(sender);
            case "ec" -> handleEc(sender, args);
            case "invsee" -> handleInvsee(sender, args);
            case "clearinv" -> handleClearInv(sender, args);
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if ((name.equals("fly") || name.equals("god") || name.equals("ec") || name.equals("clearinv"))
                && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (name.equals("invsee") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (name.equals("speed")) {
            if (args.length == 1) {
                return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10").stream()
                        .filter(v -> v.startsWith(args[0])).toList();
            }
            if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            }
        }

        if (name.equals("giveitem")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            if (args.length == 2) {
                return List.of(Material.values()).stream().map(Material::name)
                        .filter(m -> m.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).limit(30)
                        .toList();
            }
        }

        if (name.equals("repair") && args.length == 1) {
            return List.of("all").stream().filter(v -> v.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        return Collections.emptyList();
    }

    private boolean handleFly(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!sender.hasPermission("smp.fly.others")) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
                return true;
            }
        } else if (!sender.hasPermission("smp.fly")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        boolean enable = !target.getAllowFlight();
        target.setAllowFlight(
                enable || target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR);
        if (!enable && target.isFlying() && target.getGameMode() != GameMode.CREATIVE
                && target.getGameMode() != GameMode.SPECTATOR) {
            target.setFlying(false);
        }

        plugin.getMessageManager().sendPrefixed(sender, enable ? "utility.fly.enabled" : "utility.fly.disabled",
                "player",
                target.getName());
        if (!sender.getName().equalsIgnoreCase(target.getName())) {
            plugin.getMessageManager().sendPrefixed(target,
                    enable ? "utility.fly.enabled-self" : "utility.fly.disabled-self");
        }
        return true;
    }

    private boolean handleGod(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!sender.hasPermission("smp.god.others")) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
                return true;
            }
        } else if (!sender.hasPermission("smp.god")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        boolean enabled = utilityManager.toggleGod(target.getUniqueId());
        plugin.getMessageManager().sendPrefixed(sender, enabled ? "utility.god.enabled" : "utility.god.disabled",
                "player",
                target.getName());
        if (!sender.getName().equalsIgnoreCase(target.getName())) {
            plugin.getMessageManager().sendPrefixed(target,
                    enabled ? "utility.god.enabled-self" : "utility.god.disabled-self");
        }
        return true;
    }

    private boolean handleSpeed(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.speed.usage");
            return true;
        }

        if (!sender.hasPermission("smp.speed")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        int speedLevel;
        try {
            speedLevel = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendPrefixed(sender, "general.invalid-number");
            return true;
        }

        speedLevel = Math.min(10, Math.max(1, speedLevel));
        float speed = Math.min(1.0f, speedLevel / 10.0f);

        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("smp.speed.others")) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
                return true;
            }
            target = player;
        }

        target.setWalkSpeed(speed);
        target.setFlySpeed(speed);
        plugin.getMessageManager().sendPrefixed(sender, "utility.speed.set", "player", target.getName(), "speed",
                String.valueOf(speedLevel));
        return true;
    }

    private boolean handleRepair(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!sender.hasPermission("smp.repair")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        boolean all = args.length >= 1 && args[0].equalsIgnoreCase("all");
        if (all && !sender.hasPermission("smp.repair.all")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        if (all) {
            for (ItemStack item : player.getInventory().getContents()) {
                repairItem(item);
            }
            plugin.getMessageManager().sendPrefixed(player, "utility.repair.all-success");
            return true;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            plugin.getMessageManager().sendPrefixed(player, "utility.repair.no-item");
            return true;
        }
        if (!repairItem(inHand)) {
            plugin.getMessageManager().sendPrefixed(player, "utility.repair.invalid");
            return true;
        }

        plugin.getMessageManager().sendPrefixed(player, "utility.repair.success");
        return true;
    }

    private boolean handleGiveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smp.giveitem")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.give.usage");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
            return true;
        }

        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.give.invalid-item");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                plugin.getMessageManager().sendPrefixed(sender, "general.invalid-number");
                return true;
            }
        }

        ItemStack stack = new ItemStack(material, Math.min(material.getMaxStackSize(), amount));
        target.getInventory().addItem(stack);
        plugin.getMessageManager().sendPrefixed(sender, "utility.give.success", "player", target.getName(), "item",
                material.name(), "amount", String.valueOf(stack.getAmount()));
        return true;
    }

    private boolean handleWorkbench(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!sender.hasPermission("smp.workbench")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }
        player.openWorkbench(null, true);
        return true;
    }

    private boolean handleAnvil(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!sender.hasPermission("smp.anvil")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }
        player.openAnvil(null, true);
        return true;
    }

    private boolean handleEc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!sender.hasPermission("smp.ec.others")) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
                return true;
            }
        } else if (!sender.hasPermission("smp.ec")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        player.openInventory(target.getEnderChest());
        return true;
    }

    private boolean handleInvsee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }
        if (!sender.hasPermission("smp.invsee")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.invsee.usage");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
            return true;
        }
        Inventory targetInventory = target.getInventory();
        player.openInventory(targetInventory);
        return true;
    }

    private boolean handleClearInv(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().sendPrefixed(sender, "general.player-only");
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!sender.hasPermission("smp.clearinv.others")) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendPrefixed(sender, "utility.player-not-found");
                return true;
            }
        } else if (!sender.hasPermission("smp.clearinv")) {
            plugin.getMessageManager().sendPrefixed(sender, "utility.no-permission");
            return true;
        }

        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        target.getInventory().setItemInOffHand(null);
        plugin.getMessageManager().sendPrefixed(sender, "utility.clearinv.success", "player", target.getName());
        return true;
    }

    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }
        damageable.setDamage(0);
        item.setItemMeta(damageable);
        return true;
    }
}

