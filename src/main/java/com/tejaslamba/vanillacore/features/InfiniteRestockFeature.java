package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.InfiniteRestockListener;
import com.tejaslamba.vanillacore.infiniterestock.InfiniteRestockManager;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InfiniteRestockFeature extends BaseFeature {

    private InfiniteRestockListener listener;
    private InfiniteRestockManager manager;

    public static final String GUI_TITLE = "§6Infinite Restock Manager";
    public static final String BLACKLIST_GUI_TITLE = "§6Villager Blacklist";

    private static final Map<Villager.Profession, Material> PROFESSION_MATERIALS = new HashMap<>();

    static {
        PROFESSION_MATERIALS.put(Villager.Profession.ARMORER, Material.BLAST_FURNACE);
        PROFESSION_MATERIALS.put(Villager.Profession.BUTCHER, Material.SMOKER);
        PROFESSION_MATERIALS.put(Villager.Profession.CARTOGRAPHER, Material.CARTOGRAPHY_TABLE);
        PROFESSION_MATERIALS.put(Villager.Profession.CLERIC, Material.BREWING_STAND);
        PROFESSION_MATERIALS.put(Villager.Profession.FARMER, Material.COMPOSTER);
        PROFESSION_MATERIALS.put(Villager.Profession.FISHERMAN, Material.BARREL);
        PROFESSION_MATERIALS.put(Villager.Profession.FLETCHER, Material.FLETCHING_TABLE);
        PROFESSION_MATERIALS.put(Villager.Profession.LEATHERWORKER, Material.CAULDRON);
        PROFESSION_MATERIALS.put(Villager.Profession.LIBRARIAN, Material.LECTERN);
        PROFESSION_MATERIALS.put(Villager.Profession.MASON, Material.STONECUTTER);
        PROFESSION_MATERIALS.put(Villager.Profession.SHEPHERD, Material.LOOM);
        PROFESSION_MATERIALS.put(Villager.Profession.TOOLSMITH, Material.SMITHING_TABLE);
        PROFESSION_MATERIALS.put(Villager.Profession.WEAPONSMITH, Material.GRINDSTONE);
        PROFESSION_MATERIALS.put(Villager.Profession.NITWIT, Material.DEAD_BUSH);
        PROFESSION_MATERIALS.put(Villager.Profession.NONE, Material.VILLAGER_SPAWN_EGG);
    }

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new InfiniteRestockListener(plugin);
        manager = new InfiniteRestockManager(plugin);
        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Infinite Restock - Feature loaded");
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 30;
    }

    @Override
    public String getName() {
        return "Infinite Restock";
    }

    @Override
    public String getConfigPath() {
        return "features.infinite-restock";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.EMERALD, "<!italic><dark_purple>Infinite Restock",
                "<!italic><gray>Villagers never run out of trades");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Villagers will always have");
        lore.add("<gray>trades available and prices");
        lore.add("<gray>won't increase from demand");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Open Manager");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
        if (enabled) {
            player.sendMessage(MessageManager.parse("<green>[Vanilla Core] <gray>Infinite Restock enabled"));
        } else {
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Infinite Restock disabled"));
        }
    }

    @Override
    public void onRightClick(Player player) {
        openRestockGUI(player);
    }

    public void openRestockGUI(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        ItemStack maxTrades = createGuiItem(Material.ANVIL, "<!italic><yellow>Set Max Trades",
                "<gray>Current: <white>" + manager.getMaxTrades(),
                "<gray>0 = Unlimited",
                "",
                "<yellow>Click to change");

        ItemStack pricePenalty = createToggleItem(
                manager.isDisablePricePenalty(),
                "<!italic><yellow>Disable Price Penalty",
                new String[] { "<gray>Villager demand reset to 0" });

        ItemStack allowWT = createToggleItem(
                manager.isAllowWanderingTraders(),
                "<!italic><yellow>Allow Wandering Traders",
                new String[] { "<gray>Apply to travelling merchants" });

        ItemStack blacklist = createGuiItem(Material.BARRIER, "<!italic><yellow>Villager Blacklist",
                "<gray>Exclude specific professions",
                "<gray>from infinite restock",
                "",
                "<gray>Blacklisted: <red>" + manager.getBlacklistCount(),
                "",
                "<yellow>Click to manage");

        ItemStack uninstall = createToggleItem(
                manager.isUninstallMode(),
                "<!italic><red>Uninstall Mode",
                new String[] { "<gray>Restore original villager trades", "", "<red>\u26a0 Will reset all villagers!" });

        ItemStack back = createGuiItem(Material.ARROW, "<!italic><red>Back",
                "<gray>Return to main menu");

        gui.setItem(10, maxTrades);
        gui.setItem(12, pricePenalty);
        gui.setItem(14, allowWT);
        gui.setItem(16, blacklist);
        gui.setItem(22, uninstall);
        gui.setItem(31, back);

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openBlacklistGUI(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 45, BLACKLIST_GUI_TITLE);

        int slot = 10;
        for (Villager.Profession profession : Villager.Profession.values()) {
            if (profession == Villager.Profession.NONE || profession == Villager.Profession.NITWIT) {
                continue;
            }

            boolean isBlacklisted = manager.isProfessionBlacklisted(profession.name());
            Material material = PROFESSION_MATERIALS.getOrDefault(profession, Material.PLAYER_HEAD);

            String professionName = formatProfessionName(profession.name());
            String statusColor = isBlacklisted ? "<red>" : "<green>";
            String statusText = isBlacklisted ? "<red>Blacklisted" : "<green>Allowed";

            ItemStack item = new ItemStack(isBlacklisted ? Material.BARRIER : material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(
                        MessageManager.parse("<!italic>" + (isBlacklisted ? "<red>" : "<green>") + professionName));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<gray>Status: " + statusText));
                lore.add(Component.empty());
                if (isBlacklisted) {
                    lore.add(MessageManager.parse("<gray>This profession will NOT"));
                    lore.add(MessageManager.parse("<gray>receive infinite restock"));
                } else {
                    lore.add(MessageManager.parse("<gray>This profession will"));
                    lore.add(MessageManager.parse("<gray>receive infinite restock"));
                }
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<yellow>Click to toggle"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
            slot++;
            if (slot == 17)
                slot = 19;
            if (slot == 26)
                slot = 28;
            if (slot == 35)
                break;
        }

        gui.setItem(40, createGuiItem(Material.ARROW, "<!italic><red>Back", "<gray>Return to Restock Manager"));

        fillBorder(gui, Material.PURPLE_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    private String formatProfessionName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public void handleRestockGUIClick(InventoryClickEvent event, Player player) {
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        switch (raw) {
            case 10 -> {
                player.closeInventory();
                openChatInput(player);
            }
            case 12 -> {
                boolean v = !manager.isDisablePricePenalty();
                manager.setDisablePricePenalty(v);
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(player));
            }
            case 14 -> {
                boolean v = !manager.isAllowWanderingTraders();
                manager.setAllowWanderingTraders(v);
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(player));
            }
            case 16 -> {
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () -> openBlacklistGUI(player));
            }
            case 22 -> {
                boolean v = !manager.isUninstallMode();
                manager.setUninstallMode(v);
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(player));
            }
            case 31 -> {
                player.closeInventory();
                plugin.getMenuManager().openMainMenu(player);
            }
        }
    }

    public void handleBlacklistGUIClick(InventoryClickEvent event, Player player) {
        int raw = event.getRawSlot();
        if (raw >= event.getView().getTopInventory().getSize()) {
            return;
        }
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (raw == 40) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(player));
            return;
        }

        int[] validSlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };
        int index = -1;
        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == raw) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return;
        }

        List<Villager.Profession> professions = new ArrayList<>();
        for (Villager.Profession profession : Villager.Profession.values()) {
            if (profession != Villager.Profession.NONE && profession != Villager.Profession.NITWIT) {
                professions.add(profession);
            }
        }

        if (index >= professions.size()) {
            return;
        }

        Villager.Profession profession = professions.get(index);
        manager.toggleProfessionBlacklist(profession.name());
        Bukkit.getScheduler().runTask(plugin, () -> openBlacklistGUI(player));
    }

    private void openChatInput(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gold>Set Max Trades"));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<green>Enter the maximum trades per villager (0 = unlimited):"));
        player.sendMessage(MessageManager.parse("<gray>Type <red>'cancel' <gray>to cancel"));
        player.sendMessage(Component.empty());

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Input cancelled."));
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
                return;
            }

            try {
                int value = Integer.parseInt(input.trim());
                if (value < 0 || value > 64000) {
                    p.sendMessage(MessageManager
                            .parse("<red>[Vanilla Core] <gray>Invalid number! Must be between 0 and 64000"));
                    Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
                    return;
                }
                manager.setMaxTrades(value);
                p.sendMessage(MessageManager.parse("<green>[Vanilla Core] <gray>Max trades set to: " + value));
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
            } catch (NumberFormatException e) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Please enter a valid number!"));
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
            }
        });
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse(name));
            if (lore.length > 0) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(MessageManager.parse(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(boolean enabled, String name, String[] details) {
        Material mat = enabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        if (details != null) {
            for (String d : details)
                lore.add(d);
        }
        return createGuiItem(mat, name, lore.toArray(new String[0]));
    }

    private void fillBorder(Inventory inv, Material material) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            glass.setItemMeta(meta);
        }

        int size = inv.getSize();
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, glass);
            if (inv.getItem(size - 9 + i) == null)
                inv.setItem(size - 9 + i, glass);
        }
        for (int i = 9; i < size - 9; i += 9) {
            if (inv.getItem(i) == null)
                inv.setItem(i, glass);
            if (inv.getItem(i + 8) == null)
                inv.setItem(i + 8, glass);
        }
    }

    public InfiniteRestockManager getManager() {
        return manager;
    }
}
