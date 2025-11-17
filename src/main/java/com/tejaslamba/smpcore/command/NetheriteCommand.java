package com.tejaslamba.smpcore.command;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.NetheriteDisablerFeature;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NetheriteCommand implements CommandExecutor, TabCompleter, Listener {

    private final Main plugin;
    private final Map<String, Material> netheriteItems = new HashMap<>();
    private final Map<Integer, Material> slotMapping = new HashMap<>();

    public NetheriteCommand(Main plugin) {
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

        slotMapping.put(11, Material.NETHERITE_SWORD);
        slotMapping.put(12, Material.NETHERITE_AXE);
        slotMapping.put(13, Material.NETHERITE_PICKAXE);
        slotMapping.put(14, Material.NETHERITE_SHOVEL);
        slotMapping.put(15, Material.NETHERITE_HOE);
        slotMapping.put(20, Material.NETHERITE_HELMET);
        slotMapping.put(21, Material.NETHERITE_CHESTPLATE);
        slotMapping.put(23, Material.NETHERITE_LEGGINGS);
        slotMapping.put(24, Material.NETHERITE_BOOTS);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.netherite")) {
            sender.sendMessage(plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r")
                    + " §cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can open the GUI!");
                return true;
            }
            openGUI((Player) sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("gui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can open the GUI!");
                return true;
            }
            openGUI((Player) sender);
            return true;
        }

        if (args.length != 2) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        String item = args[1].toLowerCase();

        if (!netheriteItems.containsKey(item)) {
            sender.sendMessage(
                    "§cInvalid item! Valid items: sword, axe, pickaxe, shovel, hoe, helmet, chestplate, leggings, boots");
            return true;
        }

        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof NetheriteDisablerFeature)
                .findFirst()
                .orElse(null);

        if (feature == null) {
            sender.sendMessage("§cNetherite Disabler feature not found!");
            return true;
        }

        Material material = netheriteItems.get(item);
        boolean shouldDisable = action.equals("disable");

        feature.setDisabled(material, shouldDisable);

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger().info("[VERBOSE] Netherite Command - " + sender.getName() + " "
                    + (shouldDisable ? "disabled" : "enabled") + " " + material.name());
        }

        String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
        String displayItem = item.substring(0, 1).toUpperCase() + item.substring(1);
        sender.sendMessage(prefix + " " + (shouldDisable ? "§cDisabled" : "§aEnabled") + " §fNetherite " + displayItem);

        return true;
    }

    private void openGUI(Player player) {
        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof NetheriteDisablerFeature)
                .findFirst()
                .orElse(null);

        if (feature == null) {
            player.sendMessage("§cNetherite Disabler feature not found!");
            return;
        }

        Inventory gui = plugin.getServer().createInventory(null, 45, "§8Netherite Item Manager");

        for (Map.Entry<Integer, Material> entry : slotMapping.entrySet()) {
            int slot = entry.getKey();
            Material material = entry.getValue();
            boolean isDisabled = feature.isDisabled(material);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String itemName = material.name().toLowerCase().replace("netherite_", "");
                itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);

                meta.setDisplayName("§5Netherite " + itemName);

                List<String> lore = new ArrayList<>();
                lore.add("");
                if (isDisabled) {
                    lore.add("§cStatus: Disabled");
                } else {
                    lore.add("§aStatus: Enabled");
                }
                lore.add("");
                lore.add("§eClick to toggle!");
                meta.setLore(lore);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
        }

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName("§cClose");
            barrier.setItemMeta(barrierMeta);
        }
        gui.setItem(40, barrier);

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger().info("[VERBOSE] Netherite Command - Opened GUI for " + player.getName());
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Netherite Item Manager")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission("smpcore.netherite")) {
            player.sendMessage("§cYou don't have permission!");
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        Material clickedType = clicked.getType();
        if (!slotMapping.containsValue(clickedType)) {
            return;
        }

        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof NetheriteDisablerFeature)
                .findFirst()
                .orElse(null);

        if (feature == null) {
            return;
        }

        boolean currentState = feature.isDisabled(clickedType);
        feature.setDisabled(clickedType, !currentState);

        ItemMeta meta = clicked.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (!currentState) {
                lore.add("§cStatus: Disabled");
            } else {
                lore.add("§aStatus: Enabled");
            }
            lore.add("");
            lore.add("§eClick to toggle!");
            meta.setLore(lore);
            clicked.setItemMeta(meta);
        }

        String itemName = clickedType.name().toLowerCase().replace("netherite_", "");
        itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
        player.sendMessage((currentState ? "§aEnabled" : "§cDisabled") + " §fNetherite " + itemName);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Netherite Commands ===");
        sender.sendMessage("§e/netherite gui §7- Open netherite manager GUI");
        sender.sendMessage("§e/netherite disable <item> §7- Disable netherite item");
        sender.sendMessage("§e/netherite enable <item> §7- Enable netherite item");
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
