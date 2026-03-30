package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.enderchestlimiter.EnderChestItemLimiterManager;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimit;
import com.tejaslamba.vanillacore.listener.EnderChestItemLimiterListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderChestItemLimiterFeature extends BaseFeature {

    private EnderChestItemLimiterListener listener;
    private EnderChestItemLimiterManager manager;
    private final Map<UUID, GuiSession> sessions = new HashMap<>();
    private final Map<UUID, Boolean> keepSession = new HashMap<>();

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        manager = new EnderChestItemLimiterManager(plugin);
        listener = new EnderChestItemLimiterListener(plugin, manager);
        super.onEnable(plugin);
        manager.load();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Ender Chest Item Limiter - Loaded with "
                    + manager.getBlockedItemCount() + " blocked items");
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder gh
                    && gh.getId().startsWith("ender-chest-limiter")) {
                player.closeInventory();
            }
        }
        sessions.clear();
        keepSession.clear();
        super.onDisable();
    }

    @Override
    public void reload() {
        super.reload();
        if (manager != null) {
            manager.load();
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 12;
    }

    @Override
    public String getName() {
        return "Ender Chest Item Limiter";
    }

    @Override
    public String getConfigPath() {
        return "features.ender-chest-item-limiter";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ENDER_CHEST,
                plugin.getMessageManager().getRaw("feature-menus.ender-chest-item-limiter.name"),
                plugin.getMessageManager().getRaw("feature-menus.ender-chest-item-limiter.description-1"),
                plugin.getMessageManager().getRaw("feature-menus.ender-chest-item-limiter.description-2"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.ender-chest-item-limiter.count")
                .replace("<count>", String.valueOf(manager != null ? manager.getBlockedItemCount() : 0)));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-open-manager"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        openMainMenu(player);
    }

    public void openMainMenu(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(new GuiHolder("ender-chest-limiter-main"), 27,
                getGuiTitle("ender-chest-item-limiter.gui.main.title", "&5Ender Chest Item Limiter"));

        ItemStack viewBlocked = createGuiItemFromKeys(Material.BOOK,
                "ender-chest-item-limiter.gui.main.view-blocked.name",
                "ender-chest-item-limiter.gui.main.view-blocked.lore-1",
                "ender-chest-item-limiter.gui.main.view-blocked.lore-2");

        ItemStack addBlocked = createGuiItemFromKeys(Material.EMERALD,
                "ender-chest-item-limiter.gui.main.add-blocked.name",
                "ender-chest-item-limiter.gui.main.add-blocked.lore-1",
                "ender-chest-item-limiter.gui.main.add-blocked.lore-2");

        ItemStack back = createGuiItemFromKeys(Material.ARROW, "ender-chest-item-limiter.gui.shared.back.name",
                "ender-chest-item-limiter.gui.shared.back.lore-1");

        gui.setItem(11, viewBlocked);
        gui.setItem(13, addBlocked);
        gui.setItem(15, back);

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void openViewBlockedItems(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Map<String, ItemLimit> blockedItems = manager.getBlockedItems();
        int size = Math.min(54, Math.max(27, (blockedItems.size() / 9 + 1) * 9 + 9));

        Inventory gui = Bukkit.createInventory(new GuiHolder("ender-chest-limiter-view"), size,
                getGuiTitle("ender-chest-item-limiter.gui.view.title", "&5Blocked Ender Chest Items"));

        int slot = 0;
        for (Map.Entry<String, ItemLimit> entry : blockedItems.entrySet()) {
            if (slot >= size - 9) {
                break;
            }

            ItemLimit blocked = entry.getValue();
            ItemStack displayItem = blocked.createItemStack();
            ItemMeta meta = displayItem.getItemMeta();

            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(plugin.getMessageManager().get("ender-chest-item-limiter.gui.view.blocked-item.label"));

                if (blocked.getCustomModelData() != null) {
                    lore.add(plugin.getMessageManager().get(
                            "ender-chest-item-limiter.gui.view.blocked-item.custom-model-data",
                            "customModelData", String.valueOf(blocked.getCustomModelData())));
                }
                if (blocked.getDisplayName() != null) {
                    lore.add(plugin.getMessageManager().get(
                            "ender-chest-item-limiter.gui.view.blocked-item.display-name",
                            "displayName", blocked.getDisplayName()));
                }
                if (blocked.getPotionType() != null) {
                    lore.add(
                            plugin.getMessageManager().get("ender-chest-item-limiter.gui.view.blocked-item.potion-type",
                                    "potionType", blocked.getPotionType().name()));
                }

                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("ender-chest-item-limiter.gui.view.blocked-item.remove-hint"));

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }

            gui.setItem(slot, displayItem);
            slot++;
        }

        ItemStack back = createGuiItemFromKeys(Material.ARROW, "ender-chest-item-limiter.gui.shared.back.name",
                "ender-chest-item-limiter.gui.shared.back.lore-1");
        gui.setItem(size - 5, back);

        player.openInventory(gui);
    }

    public void openAddItemGui(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Inventory gui = Bukkit.createInventory(new GuiHolder("ender-chest-limiter-add"), 27,
                getGuiTitle("ender-chest-item-limiter.gui.add.title", "&5Block Ender Chest Item"));

        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            session = new GuiSession();
            sessions.put(player.getUniqueId(), session);
        }

        ItemStack itemSlot;
        if (session.item != null) {
            itemSlot = session.item.clone();
            ItemMeta meta = itemSlot.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("ender-chest-item-limiter.gui.add.item-slot.remove-hint"));
                meta.lore(lore);
                itemSlot.setItemMeta(meta);
            }
        } else {
            itemSlot = createGuiItemFromKeys(Material.BARRIER, "ender-chest-item-limiter.gui.add.item-slot.empty.name",
                    "ender-chest-item-limiter.gui.add.item-slot.empty.lore-1",
                    "ender-chest-item-limiter.gui.add.item-slot.empty.lore-2");
        }

        boolean canConfirm = session.item != null;
        String confirmName = canConfirm
                ? plugin.getMessageManager().getRaw("ender-chest-item-limiter.gui.add.confirm.ready.name")
                : plugin.getMessageManager().getRaw("ender-chest-item-limiter.gui.add.confirm.incomplete.name");
        Material confirmMaterial = canConfirm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

        List<String> confirmLore = new ArrayList<>();
        if (canConfirm) {
            confirmLore.add(plugin.getMessageManager().getRaw("ender-chest-item-limiter.gui.add.confirm.ready.lore-1"));
            confirmLore.add(plugin.getMessageManager().getRaw("ender-chest-item-limiter.gui.add.confirm.ready.lore-2"));
        } else {
            confirmLore
                    .add(plugin.getMessageManager()
                            .getRaw("ender-chest-item-limiter.gui.add.confirm.incomplete.lore-1"));
        }

        ItemStack confirm = createGuiItem(confirmMaterial, confirmName, confirmLore.toArray(new String[0]));
        ItemStack back = createGuiItemFromKeys(Material.ARROW, "ender-chest-item-limiter.gui.shared.back.name",
                "ender-chest-item-limiter.gui.shared.back.lore-1");

        gui.setItem(13, itemSlot);
        gui.setItem(16, confirm);
        gui.setItem(22, back);

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }

    public void handleMainMenuClick(int slot, Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        switch (slot) {
            case 11 -> openViewBlockedItems(player);
            case 13 -> openAddItemGui(player);
            case 15 -> {
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
            case 16 -> {
                if (session.item != null) {
                    manager.addBlockedItem(session.item);
                    plugin.getMessageManager().sendPrefixed(player, "ender-chest-item-limiter.messages.item-blocked");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    sessions.remove(player.getUniqueId());
                    player.closeInventory();
                } else {
                    plugin.getMessageManager().sendPrefixed(player,
                            "ender-chest-item-limiter.messages.please-set-item");
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
            plugin.getMessageManager().sendPrefixed(player, "ender-chest-item-limiter.messages.item-selected");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5F, 1.0F);
        } else if (currentItem != null && currentItem.getType() != Material.AIR
                && currentItem.getType() != Material.BARRIER) {
            if (cursor == null || cursor.getType() == Material.AIR) {
                event.setCancelled(true);
                event.setCursor(session.item != null ? session.item.clone() : currentItem.clone());
                session.item = null;
                keepSession.put(player.getUniqueId(), true);
                openAddItemGui(player);
                plugin.getMessageManager().sendPrefixed(player, "ender-chest-item-limiter.messages.item-removed");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5F, 1.0F);
            }
        } else {
            event.setCancelled(true);
        }
    }

    public void handleViewBlockedItemsClick(int slot, int inventorySize, boolean isShiftClick, boolean isLeftClick,
            Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

        if (slot == inventorySize - 5) {
            openMainMenu(player);
            return;
        }

        List<String> keys = new ArrayList<>(manager.getBlockedItems().keySet());

        if (slot >= keys.size()) {
            return;
        }

        if (isShiftClick && isLeftClick) {
            manager.removeBlockedItem(keys.get(slot));
            plugin.getMessageManager().sendPrefixed(player,
                    "ender-chest-item-limiter.messages.blocked-item-removed");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            openViewBlockedItems(player);
        }
    }

    public void handleInventoryClose(Player player, String guiId) {
        if (guiId.equals("ender-chest-limiter-add")) {
            if (keepSession.remove(player.getUniqueId()) == null) {
                sessions.remove(player.getUniqueId());
            }
        }
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

    private ItemStack createGuiItemFromKeys(Material material, String namePath, String... lorePaths) {
        String name = plugin.getMessageManager().getRaw(namePath);
        String[] lore = Arrays.stream(lorePaths)
                .map(path -> plugin.getMessageManager().getRaw(path))
                .toArray(String[]::new);
        return createGuiItem(material, name, lore);
    }

    private String getGuiTitle(String path, String fallback) {
        String raw = plugin.getMessageManager().getRaw(path);
        if (raw.equals(path)) {
            raw = fallback;
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
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
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
            if (inv.getItem(size - 9 + i) == null) {
                inv.setItem(size - 9 + i, glass);
            }
        }

        for (int i = 9; i < size - 9; i += 9) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
            if (inv.getItem(i + 8) == null) {
                inv.setItem(i + 8, glass);
            }
        }
    }

    public EnderChestItemLimiterManager getManager() {
        return manager;
    }

    public static class GuiSession {
        public ItemStack item;
    }
}
