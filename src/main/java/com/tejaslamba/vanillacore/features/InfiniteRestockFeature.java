package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.InfiniteRestockListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
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
import org.bukkit.Registry;

import java.util.*;

public class InfiniteRestockFeature extends BaseFeature {

    private InfiniteRestockListener listener;
    private InfiniteRestockManager manager;

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
        return createMenuItem(Material.EMERALD,
                plugin.getMessageManager().getRaw("feature-menus.infinite-restock.name"),
                plugin.getMessageManager().getRaw("feature-menus.infinite-restock.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.infinite-restock.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.infinite-restock.lore-2"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.infinite-restock.lore-3"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-open-manager"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
        if (enabled) {
            player.sendMessage(plugin.getMessageManager().get("infinite-restock.enabled"));
        } else {
            player.sendMessage(plugin.getMessageManager().get("infinite-restock.disabled"));
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

        Inventory gui = Bukkit.createInventory(new GuiHolder("infinite-restock"), 36,
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.title"));

        ItemStack maxTrades = createGuiItem(Material.ANVIL,
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.max-trades.name"),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.max-trades.current")
                        .replace("<value>", String.valueOf(manager.getMaxTrades())),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.max-trades.unlimited"),
                "",
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.max-trades.action"));

        ItemStack pricePenalty = createToggleItem(
                manager.isDisablePricePenalty(),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.price-penalty.name"),
                new String[] { plugin.getMessageManager().getRaw("infinite-restock.gui.main.price-penalty.lore-1") });

        ItemStack allowWT = createToggleItem(
                manager.isAllowWanderingTraders(),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.allow-wt.name"),
                new String[] { plugin.getMessageManager().getRaw("infinite-restock.gui.main.allow-wt.lore-1") });

        ItemStack blacklist = createGuiItem(Material.BARRIER,
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.blacklist.name"),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.blacklist.lore-1"),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.blacklist.lore-2"),
                "",
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.blacklist.count")
                        .replace("<count>", String.valueOf(manager.getBlacklistCount())),
                "",
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.blacklist.action"));

        ItemStack uninstall = createToggleItem(
                manager.isUninstallMode(),
                plugin.getMessageManager().getRaw("infinite-restock.gui.main.uninstall.name"),
                new String[] {
                        plugin.getMessageManager().getRaw("infinite-restock.gui.main.uninstall.lore-1"),
                        "",
                        plugin.getMessageManager().getRaw("infinite-restock.gui.main.uninstall.warning") });

        ItemStack back = createGuiItem(Material.ARROW,
                plugin.getMessageManager().getRaw("infinite-restock.gui.shared.back.name"),
                plugin.getMessageManager().getRaw("infinite-restock.gui.shared.back.main-lore"));

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

        Inventory gui = Bukkit.createInventory(new GuiHolder("infinite-restock-blacklist"), 45,
                plugin.getMessageManager().getRaw("infinite-restock.gui.blacklist.title"));

        int slot = 10;
        for (Villager.Profession profession : Registry.VILLAGER_PROFESSION) {
            String keyName = profession.getKey().getKey().toUpperCase(Locale.ROOT);
            if (keyName.equals("NONE") || keyName.equals("NITWIT")) {
                continue;
            }

            boolean isBlacklisted = manager.isProfessionBlacklisted(keyName);
            Material material = PROFESSION_MATERIALS.getOrDefault(profession, Material.PLAYER_HEAD);

            String professionName = formatProfessionName(keyName);

            ItemStack item = new ItemStack(isBlacklisted ? Material.BARRIER : material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getMessageManager().get(
                        isBlacklisted ? "infinite-restock.blacklist.item.name-blocked"
                                : "infinite-restock.blacklist.item.name-allowed",
                        "profession", professionName));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get(
                        isBlacklisted ? "infinite-restock.blacklist.item.status-blocked"
                                : "infinite-restock.blacklist.item.status-allowed"));
                lore.add(Component.empty());
                if (isBlacklisted) {
                    lore.add(plugin.getMessageManager().get("infinite-restock.blacklist.item.blocked-line-1"));
                    lore.add(plugin.getMessageManager().get("infinite-restock.blacklist.item.common-line-2"));
                } else {
                    lore.add(plugin.getMessageManager().get("infinite-restock.blacklist.item.allowed-line-1"));
                    lore.add(plugin.getMessageManager().get("infinite-restock.blacklist.item.common-line-2"));
                }
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("infinite-restock.blacklist.item.action"));
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

        gui.setItem(40, createGuiItem(Material.ARROW,
                plugin.getMessageManager().getRaw("infinite-restock.gui.shared.back.name"),
                plugin.getMessageManager().getRaw("infinite-restock.gui.shared.back.blacklist-lore")));

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
        for (Villager.Profession profession : Registry.VILLAGER_PROFESSION) {
            String keyName = profession.getKey().getKey().toUpperCase(Locale.ROOT);
            if (!keyName.equals("NONE") && !keyName.equals("NITWIT")) {
                professions.add(profession);
            }
        }

        if (index >= professions.size()) {
            return;
        }

        Villager.Profession profession = professions.get(index);
        manager.toggleProfessionBlacklist(profession.getKey().getKey().toUpperCase(Locale.ROOT));
        Bukkit.getScheduler().runTask(plugin, () -> openBlacklistGUI(player));
    }

    private void openChatInput(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("infinite-restock.input.title"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("infinite-restock.input.prompt"));
        player.sendMessage(plugin.getMessageManager().get("infinite-restock.input.cancel-hint"));
        player.sendMessage(Component.empty());

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(plugin.getMessageManager().get("infinite-restock.input.cancelled"));
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
                return;
            }

            try {
                int value = Integer.parseInt(input.trim());
                if (value < 0 || value > 64000) {
                    p.sendMessage(MessageManager
                            .parse("<red>[SMP Watchdog] <gray>Invalid number! Must be between 0 and 64000"));
                    Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
                    return;
                }
                manager.setMaxTrades(value);
                p.sendMessage(plugin.getMessageManager().get("infinite-restock.max-trades-set", "value", value));
                Bukkit.getScheduler().runTask(plugin, () -> openRestockGUI(p));
            } catch (NumberFormatException e) {
                p.sendMessage(plugin.getMessageManager().get("general.invalid-number"));
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

