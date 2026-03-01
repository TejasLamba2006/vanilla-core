package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimit;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimiterManager;
import com.tejaslamba.vanillacore.listener.ItemLimiterListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemLimiterFeature extends BaseFeature {

    public static final String MAIN_GUI_TITLE = "§6Item Cap Manager";
    public static final String ADD_GUI_TITLE = "§6Add Item Limit";
    public static final String VIEW_GUI_TITLE = "§6Configured Item Limits";
    public static final String BANNED_GUI_TITLE = "§cBanned Items";

    private ItemLimiterListener listener;
    private ItemLimiterManager manager;
    private final Map<UUID, GuiSession> sessions = new HashMap<>();
    private final Map<UUID, String> editingSessions = new HashMap<>();
    private final Map<UUID, Boolean> keepSession = new HashMap<>();

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        manager = new ItemLimiterManager(plugin);
        listener = new ItemLimiterListener(plugin, manager);
        super.onEnable(plugin);
        manager.load();

        if (enabled) {
            manager.startChecker();
        }

        if (plugin.isVerbose()) {
            plugin.getLogger()
                    .info("[VERBOSE] Item Limiter - Loaded with " + manager.getLimitsCount() + " item limits");
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String title = player.getOpenInventory().getTitle();
            if (title.equals(MAIN_GUI_TITLE) || title.equals(ADD_GUI_TITLE) ||
                    title.equals(VIEW_GUI_TITLE) || title.equals(BANNED_GUI_TITLE)) {
                player.closeInventory();
            }
        }
        sessions.clear();
        editingSessions.clear();
        keepSession.clear();

        if (manager != null) {
            manager.shutdown();
        }
        super.onDisable();
    }

    @Override
    public void reload() {
        boolean wasEnabled = this.enabled;
        super.reload();

        if (manager != null) {
            manager.load();

            if (wasEnabled && !enabled) {
                manager.stopChecker();
            } else if (!wasEnabled && enabled) {
                manager.startChecker();
            }
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 11;
    }

    @Override
    public String getName() {
        return "Item Limiter";
    }

    @Override
    public String getConfigPath() {
        return "features.item-limiter";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.HOPPER, "<!italic><red>Item Limiter",
                "<gray>Limits item quantities in inventories");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Limits how many of certain items");
        lore.add("<gray>players can carry at once");
        lore.add("");
        lore.add("<gray>Limited Items: <yellow>" + (manager != null ? manager.getLimitsCount() : 0));
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Open Manager");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);

        if (manager != null) {
            if (enabled) {
                manager.startChecker();
            } else {
                manager.stopChecker();
            }
        }
    }

    @Override
    public void onRightClick(Player player) {
        openMainMenu(player);
    }

    public void openMainMenu(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, MAIN_GUI_TITLE);

        ItemStack viewLimits = createGuiItem(Material.BOOK, "<!italic><green>View Item Limits",
                "<gray>Click to view all",
                "<gray>configured item limits");

        ItemStack bannedItems = createGuiItem(Material.BARRIER, "<!italic><red>Banned Items",
                "<gray>Click to view items",
                "<gray>that are banned (limit: 0)");

        ItemStack addLimit = createGuiItem(Material.EMERALD, "<!italic><aqua>Add New Limit",
                "<gray>Click to add a new",
                "<gray>item limit or ban");

        ItemStack back = createGuiItem(Material.ARROW, "<!italic><red>Back",
                "<gray>Return to main menu");

        gui.setItem(10, viewLimits);
        gui.setItem(12, bannedItems);
        gui.setItem(14, addLimit);
        gui.setItem(16, back);

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(gui);
    }

    public void openViewLimits(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Map<String, ItemLimit> allLimits = manager.getItemLimits();
        Map<String, ItemLimit> limits = new HashMap<>();
        for (Map.Entry<String, ItemLimit> entry : allLimits.entrySet()) {
            if (entry.getValue().getLimit() > 0) {
                limits.put(entry.getKey(), entry.getValue());
            }
        }

        int size = Math.min(54, Math.max(27, (limits.size() / 9 + 1) * 9 + 9));

        Inventory gui = Bukkit.createInventory(null, size, VIEW_GUI_TITLE);

        int slot = 0;
        for (Map.Entry<String, ItemLimit> entry : limits.entrySet()) {
            if (slot >= size - 9) {
                break;
            }

            ItemLimit limit = entry.getValue();
            ItemStack displayItem = limit.createItemStack();
            ItemMeta meta = displayItem.getItemMeta();

            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(MessageManager.parse("<gray>Limit: <yellow>" + limit.getLimit()));

                if (limit.getCustomModelData() != null) {
                    lore.add(MessageManager.parse("<gray>Custom Model Data: <aqua>" + limit.getCustomModelData()));
                }
                if (limit.getDisplayName() != null) {
                    lore.add(MessageManager.parse("<gray>Display Name: <white>" + limit.getDisplayName()));
                }
                if (limit.getPotionType() != null) {
                    lore.add(MessageManager.parse("<gray>Potion Type: <light_purple>" + limit.getPotionType().name()));
                }

                lore.add(Component.empty());
                lore.add(MessageManager.parse("<green>Left Click <gray>to edit limit"));
                lore.add(MessageManager.parse("<red>Shift + Left Click <gray>to delete"));

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }

            gui.setItem(slot, displayItem);
            slot++;
        }

        ItemStack back = createGuiItem(Material.ARROW, "<!italic><red>Back",
                "<gray>Return to main menu");
        gui.setItem(size - 5, back);

        player.openInventory(gui);
    }

    public void openBannedItems(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Map<String, ItemLimit> allLimits = manager.getItemLimits();
        Map<String, ItemLimit> bannedItems = new HashMap<>();
        for (Map.Entry<String, ItemLimit> entry : allLimits.entrySet()) {
            if (entry.getValue().getLimit() == 0) {
                bannedItems.put(entry.getKey(), entry.getValue());
            }
        }

        int size = Math.min(54, Math.max(27, (bannedItems.size() / 9 + 1) * 9 + 9));

        Inventory gui = Bukkit.createInventory(null, size, BANNED_GUI_TITLE);

        int slot = 0;
        for (Map.Entry<String, ItemLimit> entry : bannedItems.entrySet()) {
            if (slot >= size - 9) {
                break;
            }

            ItemLimit limit = entry.getValue();
            ItemStack displayItem = limit.createItemStack();
            ItemMeta meta = displayItem.getItemMeta();

            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(MessageManager.parse("<red><bold>BANNED"));

                if (limit.getCustomModelData() != null) {
                    lore.add(MessageManager.parse("<gray>Custom Model Data: <aqua>" + limit.getCustomModelData()));
                }
                if (limit.getDisplayName() != null) {
                    lore.add(MessageManager.parse("<gray>Display Name: <white>" + limit.getDisplayName()));
                }
                if (limit.getPotionType() != null) {
                    lore.add(MessageManager.parse("<gray>Potion Type: <light_purple>" + limit.getPotionType().name()));
                }

                lore.add(Component.empty());
                lore.add(MessageManager.parse("<green>Left Click <gray>to set a limit (unban)"));
                lore.add(MessageManager.parse("<red>Shift + Left Click <gray>to delete"));

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }

            gui.setItem(slot, displayItem);
            slot++;
        }

        ItemStack back = createGuiItem(Material.ARROW, "<!italic><red>Back",
                "<gray>Return to main menu");
        gui.setItem(size - 5, back);

        player.openInventory(gui);
    }

    public void openAddItemGui(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, ADD_GUI_TITLE);

        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            session = new GuiSession();
            sessions.put(player.getUniqueId(), session);
        }

        ItemStack anvil;
        if (session.banMode) {
            anvil = createGuiItem(Material.ANVIL, "<!italic><dark_gray>Set Limit Amount",
                    "<gray>Current: <red>0 (BANNED)",
                    "<dark_gray>Disabled - Ban mode active");
        } else {
            anvil = createGuiItem(Material.ANVIL, "<!italic><yellow>Set Limit Amount",
                    "<gray>Current: <white>" + session.limit,
                    "<gray>Click to set amount");
        }

        ItemStack banToggle;
        if (session.banMode) {
            banToggle = createGuiItem(Material.BARRIER, "<!italic><red><bold>BAN MODE: ON",
                    "<gray>Item will be <red>banned <gray>(limit: 0)",
                    "",
                    "<yellow>Click to switch to limit mode");
        } else {
            banToggle = createGuiItem(Material.LIME_DYE, "<!italic><green><bold>BAN MODE: OFF",
                    "<gray>Item will have a normal limit",
                    "",
                    "<yellow>Click to ban this item instead");
        }

        ItemStack barrier;
        if (session.item != null) {
            barrier = session.item.clone();
            ItemMeta meta = barrier.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<gray>Click to remove"));
                meta.lore(lore);
                barrier.setItemMeta(meta);
            }
        } else {
            barrier = createGuiItem(Material.BARRIER, "<!italic><red>Drag Item Here",
                    "<gray>Place your item here",
                    "<gray>to set its limit or ban it");
        }

        boolean canConfirm = session.item != null && (session.banMode || session.limit >= 0);
        String confirmName = canConfirm ? "<!italic><green>Confirm" : "<!italic><red>Incomplete";
        Material confirmMaterial = canConfirm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

        List<String> confirmLore = new ArrayList<>();
        if (canConfirm) {
            if (session.banMode) {
                confirmLore.add("<gray>Click to <red>ban <gray>this item");
            } else {
                confirmLore.add("<gray>Click to add this");
                confirmLore.add("<gray>item to the config");
            }
        } else {
            confirmLore.add("<gray>Set an item first");
        }

        ItemStack confirm = createGuiItem(confirmMaterial, confirmName, confirmLore.toArray(new String[0]));
        ItemStack back = createGuiItem(Material.ARROW, "<!italic><red>Back", "<gray>Return to main menu");

        gui.setItem(10, anvil);
        gui.setItem(12, banToggle);
        gui.setItem(13, barrier);
        gui.setItem(16, confirm);
        gui.setItem(22, back);

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(gui);
    }

    public void handleMainMenuClick(int slot, Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        switch (slot) {
            case 10 -> openViewLimits(player);
            case 12 -> openBannedItems(player);
            case 14 -> openAddItemGui(player);
            case 16 -> {
                player.closeInventory();
                plugin.getMenuManager().openMainMenu(player);
            }
        }
    }

    public void handleAddItemClick(InventoryClickEvent event, Player player) {
        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();

        if (slot == 13) {
            handleItemPlacement(event, player, session);
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        switch (slot) {
            case 10 -> {
                if (!session.banMode) {
                    keepSession.put(player.getUniqueId(), true);
                    player.closeInventory();
                    openChatInput(player, session);
                }
            }
            case 12 -> {
                session.banMode = !session.banMode;
                if (session.banMode) {
                    session.limit = 0;
                }
                keepSession.put(player.getUniqueId(), true);
                openAddItemGui(player);
            }
            case 16 -> {
                if (session.item != null && (session.banMode || session.limit >= 0)) {
                    int limitToUse = session.banMode ? 0 : session.limit;
                    manager.addItemLimit(session.item, limitToUse);
                    if (session.banMode) {
                        player.sendMessage(
                                MessageManager.parse("<green>[Vanilla Core] <gray>Item banned successfully!"));
                    } else {
                        player.sendMessage(
                                MessageManager.parse("<green>[Vanilla Core] <gray>Item limit added successfully!"));
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    sessions.remove(player.getUniqueId());
                    player.closeInventory();
                } else {
                    player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Please set an item first!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                }
            }
            case 22 -> {
                sessions.remove(player.getUniqueId());
                openMainMenu(player);
            }
        }
    }

    private void handleItemPlacement(InventoryClickEvent event, Player player, GuiSession session) {
        ItemStack cursor = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();

        if (cursor != null && cursor.getType() != Material.AIR) {
            event.setCancelled(true);
            session.item = cursor.clone();
            event.setCursor(null);
            keepSession.put(player.getUniqueId(), true);
            openAddItemGui(player);
            if (session.banMode) {
                player.sendMessage(
                        MessageManager.parse("<green>[Vanilla Core] <gray>Item placed! Click confirm to ban it."));
            } else {
                player.sendMessage(
                        MessageManager.parse("<green>[Vanilla Core] <gray>Item placed! Now set the limit amount."));
            }
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5F, 1.0F);
        } else if (currentItem != null && currentItem.getType() != Material.AIR
                && currentItem.getType() != Material.BARRIER) {
            if (cursor == null || cursor.getType() == Material.AIR) {
                event.setCancelled(true);
                event.setCursor(session.item != null ? session.item.clone() : currentItem.clone());
                session.item = null;
                keepSession.put(player.getUniqueId(), true);
                openAddItemGui(player);
                player.sendMessage(MessageManager.parse("<yellow>[Vanilla Core] <gray>Item removed."));
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5F, 1.0F);
            }
        } else {
            event.setCancelled(true);
        }
    }

    public void handleViewLimitsClick(int slot, int inventorySize, boolean isShiftClick, boolean isLeftClick,
            Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        if (slot == inventorySize - 5) {
            openMainMenu(player);
            return;
        }

        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, ItemLimit> entry : manager.getItemLimits().entrySet()) {
            if (entry.getValue().getLimit() > 0) {
                keys.add(entry.getKey());
            }
        }

        if (slot >= keys.size()) {
            return;
        }

        String key = keys.get(slot);

        if (isShiftClick && isLeftClick) {
            manager.removeItemLimit(key);
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Item limit removed!"));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            openViewLimits(player);
        } else if (isLeftClick) {
            editingSessions.put(player.getUniqueId(), key);
            player.closeInventory();
            openEditChatInput(player, key);
        }
    }

    public void handleBannedItemsClick(int slot, int inventorySize, boolean isShiftClick, boolean isLeftClick,
            Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        if (slot == inventorySize - 5) {
            openMainMenu(player);
            return;
        }

        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, ItemLimit> entry : manager.getItemLimits().entrySet()) {
            if (entry.getValue().getLimit() == 0) {
                keys.add(entry.getKey());
            }
        }

        if (slot >= keys.size()) {
            return;
        }

        String key = keys.get(slot);

        if (isShiftClick && isLeftClick) {
            manager.removeItemLimit(key);
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Banned item removed!"));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            openBannedItems(player);
        } else if (isLeftClick) {
            editingSessions.put(player.getUniqueId(), key);
            player.closeInventory();
            openEditBannedChatInput(player, key);
        }
    }

    private void openEditBannedChatInput(Player player, String key) {
        ItemLimit limit = manager.getItemLimit(key);
        if (limit == null) {
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Banned item not found!"));
            openBannedItems(player);
            return;
        }

        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gold>Unban Item - Set Limit"));
        player.sendMessage(MessageManager.parse("<gray>Current: <red>BANNED (0)"));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<green>Enter a limit to unban (1-64000):"));
        player.sendMessage(MessageManager.parse("<gray>Type <red>'cancel' <gray>to cancel"));
        player.sendMessage(Component.empty());

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            editingSessions.remove(p.getUniqueId());

            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Input cancelled."));
                Bukkit.getScheduler().runTask(plugin, () -> openBannedItems(p));
                return;
            }

            try {
                int newLimit = Integer.parseInt(input.trim());
                if (newLimit < 0 || newLimit > 64000) {
                    p.sendMessage(MessageManager
                            .parse("<red>[Vanilla Core] <gray>Invalid number! Must be between 0 and 64000"));
                    Bukkit.getScheduler().runTask(plugin, () -> openBannedItems(p));
                    return;
                }

                manager.updateItemLimit(key, newLimit);
                if (newLimit == 0) {
                    p.sendMessage(MessageManager.parse("<green>[Vanilla Core] <gray>Item remains banned"));
                } else {
                    p.sendMessage(
                            MessageManager.parse("<green>[Vanilla Core] <gray>Item unbanned with limit: " + newLimit));
                }
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                Bukkit.getScheduler().runTask(plugin, () -> openBannedItems(p));
            } catch (NumberFormatException e) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Please enter a valid number!"));
                Bukkit.getScheduler().runTask(plugin, () -> openBannedItems(p));
            }
        });
    }

    private void openChatInput(Player player, GuiSession session) {
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gold>Set Item Limit"));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<green>Enter the maximum amount (0-64000):"));
        player.sendMessage(MessageManager.parse("<gray>Type <red>'cancel' <gray>to cancel"));
        player.sendMessage(Component.empty());

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Input cancelled."));
                Bukkit.getScheduler().runTask(plugin, () -> openAddItemGui(p));
                return;
            }

            try {
                int limit = Integer.parseInt(input.trim());
                if (limit < 0 || limit > 64000) {
                    p.sendMessage(MessageManager
                            .parse("<red>[Vanilla Core] <gray>Invalid number! Must be between 0 and 64000"));
                    Bukkit.getScheduler().runTask(plugin, () -> openAddItemGui(p));
                    return;
                }

                GuiSession currentSession = sessions.get(p.getUniqueId());
                if (currentSession == null) {
                    currentSession = new GuiSession();
                    if (session.item != null) {
                        currentSession.item = session.item;
                    }
                    sessions.put(p.getUniqueId(), currentSession);
                }
                currentSession.limit = limit;

                p.sendMessage(MessageManager.parse("<green>[Vanilla Core] <gray>Limit set to: " + limit));
                Bukkit.getScheduler().runTask(plugin, () -> openAddItemGui(p));
            } catch (NumberFormatException e) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Please enter a valid number!"));
                Bukkit.getScheduler().runTask(plugin, () -> openAddItemGui(p));
            }
        });
    }

    private void openEditChatInput(Player player, String key) {
        ItemLimit limit = manager.getItemLimit(key);
        if (limit == null) {
            player.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Item limit not found!"));
            openViewLimits(player);
            return;
        }

        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gold>Edit Limit Amount"));
        player.sendMessage(MessageManager.parse("<gray>Current: <white>" + limit.getLimit()));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<green>Enter the new limit amount (0-64000):"));
        player.sendMessage(MessageManager.parse("<gray>Type <red>'cancel' <gray>to cancel"));
        player.sendMessage(Component.empty());

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            editingSessions.remove(p.getUniqueId());

            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Input cancelled."));
                Bukkit.getScheduler().runTask(plugin, () -> openViewLimits(p));
                return;
            }

            try {
                int newLimit = Integer.parseInt(input.trim());
                if (newLimit < 0 || newLimit > 64000) {
                    p.sendMessage(MessageManager
                            .parse("<red>[Vanilla Core] <gray>Invalid number! Must be between 0 and 64000"));
                    Bukkit.getScheduler().runTask(plugin, () -> openViewLimits(p));
                    return;
                }

                manager.updateItemLimit(key, newLimit);
                p.sendMessage(MessageManager.parse("<green>[Vanilla Core] <gray>Limit updated to " + newLimit));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                Bukkit.getScheduler().runTask(plugin, () -> openViewLimits(p));
            } catch (NumberFormatException e) {
                p.sendMessage(MessageManager.parse("<red>[Vanilla Core] <gray>Please enter a valid number!"));
                Bukkit.getScheduler().runTask(plugin, () -> openViewLimits(p));
            }
        });
    }

    public void handleInventoryClose(Player player, String title) {
        if (title.equals(ADD_GUI_TITLE)) {
            if (keepSession.remove(player.getUniqueId()) == null) {
                sessions.remove(player.getUniqueId());
            }
        }
        editingSessions.remove(player.getUniqueId());
    }

    private ItemStack createGuiItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(MessageManager.parse(name));
            if (loreLines.length > 0) {
                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(line.isEmpty() ? Component.empty() : MessageManager.parse(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
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

    public ItemLimiterManager getManager() {
        return manager;
    }

    public boolean hasSession(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    public static class GuiSession {
        public ItemStack item;
        public int limit = 0;
        public boolean banMode = false;
    }
}
