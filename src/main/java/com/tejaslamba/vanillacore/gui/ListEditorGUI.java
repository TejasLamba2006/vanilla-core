package com.tejaslamba.vanillacore.gui;

import com.tejaslamba.vanillacore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListEditorGUI implements InventoryHolder {

    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    private final List<String> items;
    private final Consumer<List<String>> onSave;
    private final Runnable onCancel;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 28;

    public ListEditorGUI(Main plugin, Player player, String title, List<String> items, 
                        Consumer<List<String>> onSave, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.items = new ArrayList<>(items);
        this.onSave = onSave;
        this.onCancel = onCancel;
        this.inventory = Bukkit.createInventory(this, 54, "§6" + title);
        setupItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void setupItems() {
        inventory.clear();
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex && slot < 28; i++) {
            String item = items.get(i);
            inventory.setItem(slot, createListItemStack(item, i));
            slot++;
        }
        
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        
        if (currentPage > 0) {
            inventory.setItem(45, createPreviousPageItem());
        }
        
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createNextPageItem());
        }
        
        inventory.setItem(47, createAddItem());
        inventory.setItem(49, createSaveItem());
        inventory.setItem(51, createCancelItem());
        
        if (!items.isEmpty()) {
            inventory.setItem(48, createClearAllItem());
        }
    }

    private ItemStack createListItemStack(String itemName, int index) {
        Material material = Material.PAPER;
        try {
            Material mat = Material.valueOf(itemName.toUpperCase());
            material = mat;
        } catch (IllegalArgumentException e) {
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + itemName);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Left-click: Remove from list");
            lore.add("§7Right-click: Move up");
            lore.add("§7Shift-right-click: Move down");
            lore.add("");
            lore.add("§8Index: " + (index + 1));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAddItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a+ Add Item");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to add a new item");
            lore.add("§7to the list");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createClearAllItem() {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lClear All");
            List<String> lore = new ArrayList<>();
            lore.add("§7Remove all items from the list");
            lore.add("");
            lore.add("§c§lWARNING: This cannot be undone!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSaveItem() {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a✓ Save Changes");
            List<String> lore = new ArrayList<>();
            lore.add("§7Save and return");
            lore.add("");
            lore.add("§7Total items: §e" + items.size());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c✗ Cancel");
            List<String> lore = new ArrayList<>();
            lore.add("§7Discard changes and return");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPreviousPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a« Previous Page");
            meta.setLore(List.of("§7Page " + currentPage + " of " + ((items.size() / ITEMS_PER_PAGE) + 1)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNextPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aNext Page »");
            meta.setLore(List.of("§7Page " + (currentPage + 2) + " of " + ((items.size() / ITEMS_PER_PAGE) + 1)));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < 28) {
            int index = currentPage * ITEMS_PER_PAGE + slot;
            if (index < items.size()) {
                if (event.isLeftClick()) {
                    items.remove(index);
                    setupItems();
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                } else if (event.isRightClick() && !event.isShiftClick() && index > 0) {
                    String temp = items.get(index);
                    items.set(index, items.get(index - 1));
                    items.set(index - 1, temp);
                    setupItems();
                } else if (event.isShiftClick() && event.isRightClick() && index < items.size() - 1) {
                    String temp = items.get(index);
                    items.set(index, items.get(index + 1));
                    items.set(index + 1, temp);
                    setupItems();
                }
            }
        } else if (slot == 45 && currentPage > 0) {
            currentPage--;
            setupItems();
        } else if (slot == 53) {
            int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                setupItems();
            }
        } else if (slot == 47) {
            player.closeInventory();
            new AnvilInputGUI.Builder(plugin, player)
                .prompt("Enter the item name to add:")
                .onComplete((p, input) -> {
                    items.add(input);
                    p.sendMessage("§a[Vanilla Core] Added '" + input + "' to the list.");
                    open();
                })
                .onCancel(this::open)
                .build()
                .open();
        } else if (slot == 48 && !items.isEmpty()) {
            items.clear();
            setupItems();
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        } else if (slot == 49) {
            player.closeInventory();
            onSave.accept(items);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else if (slot == 51) {
            player.closeInventory();
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }

    public static class Builder {
        private Main plugin;
        private Player player;
        private String title = "List Editor";
        private List<String> items = new ArrayList<>();
        private Consumer<List<String>> onSave;
        private Runnable onCancel;

        public Builder(Main plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder items(List<String> items) {
            this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
            return this;
        }

        public Builder onSave(Consumer<List<String>> onSave) {
            this.onSave = onSave;
            return this;
        }

        public Builder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public ListEditorGUI build() {
            return new ListEditorGUI(plugin, player, title, items, onSave, onCancel);
        }
    }
}
