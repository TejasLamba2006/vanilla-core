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

public class BooleanToggleGUI implements InventoryHolder {

    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    private final String description;
    private final boolean currentValue;
    private final Consumer<Boolean> onSelect;
    private final Runnable onCancel;

    public BooleanToggleGUI(Main plugin, Player player, String title, String description, 
                           boolean currentValue, Consumer<Boolean> onSelect, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.description = description;
        this.currentValue = currentValue;
        this.onSelect = onSelect;
        this.onCancel = onCancel;
        this.inventory = Bukkit.createInventory(this, 27, "§6" + title);
        setupItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void setupItems() {
        inventory.setItem(11, createEnableItem());
        inventory.setItem(15, createDisableItem());
        inventory.setItem(22, createCancelItem());
    }

    private ItemStack createEnableItem() {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a✓ Enable");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (description != null && !description.isEmpty()) {
                lore.add("§7" + description);
                lore.add("");
            }
            if (currentValue) {
                lore.add("§a§l► CURRENTLY SELECTED");
            } else {
                lore.add("§7Click to enable");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDisableItem() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c✗ Disable");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (description != null && !description.isEmpty()) {
                lore.add("§7" + description);
                lore.add("");
            }
            if (!currentValue) {
                lore.add("§c§l► CURRENTLY SELECTED");
            } else {
                lore.add("§7Click to disable");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cCancel");
            List<String> lore = new ArrayList<>();
            lore.add("§7Return without changing");
            meta.setLore(lore);
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

        if (slot == 11) {
            player.closeInventory();
            onSelect.accept(true);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        } else if (slot == 15) {
            player.closeInventory();
            onSelect.accept(false);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else if (slot == 22) {
            player.closeInventory();
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }

    public static class Builder {
        private Main plugin;
        private Player player;
        private String title = "Confirm";
        private String description = "";
        private boolean currentValue = false;
        private Consumer<Boolean> onSelect;
        private Runnable onCancel;

        public Builder(Main plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder currentValue(boolean currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public Builder onSelect(Consumer<Boolean> onSelect) {
            this.onSelect = onSelect;
            return this;
        }

        public Builder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public BooleanToggleGUI build() {
            return new BooleanToggleGUI(plugin, player, title, description, currentValue, onSelect, onCancel);
        }
    }
}
