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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigMenuGUI implements InventoryHolder {

    protected final Main plugin;
    protected final Inventory inventory;
    protected final Player player;
    protected final String configPath;
    protected boolean isDirty = false;
    protected Map<String, Object> pendingChanges = new HashMap<>();

    public ConfigMenuGUI(Main plugin, Player player, String configPath, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.configPath = configPath;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        setupItems();
        player.openInventory(inventory);
    }

    protected abstract void setupItems();

    public abstract void handleClick(InventoryClickEvent event);

    protected void setPendingChange(String key, Object value) {
        pendingChanges.put(key, value);
        isDirty = true;
        refresh();
    }

    protected Object getPendingChange(String key) {
        return pendingChanges.getOrDefault(key, getConfigValue(key));
    }

    protected Object getConfigValue(String key) {
        String fullPath = configPath + "." + key;
        return plugin.getConfigManager().get().get(fullPath);
    }

    protected void saveChanges() {
        for (Map.Entry<String, Object> entry : pendingChanges.entrySet()) {
            String fullPath = configPath + "." + entry.getKey();
            plugin.getConfigManager().get().set(fullPath, entry.getValue());
        }
        plugin.getConfigManager().save();
        plugin.getConfigManager().load();
        
        plugin.getFeatureManager().getFeatures().forEach(feature -> {
            if (feature.getConfigPath().equals(configPath)) {
                feature.reload();
            }
        });
        
        isDirty = false;
        pendingChanges.clear();
        player.sendMessage("§a[Vanilla Core] Configuration saved successfully!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    protected void discardChanges() {
        if (isDirty) {
            player.sendMessage("§c[Vanilla Core] Changes discarded.");
        }
        isDirty = false;
        pendingChanges.clear();
    }

    protected void refresh() {
        inventory.clear();
        setupItems();
    }

    protected void openMainMenu() {
        discardChanges();
        plugin.getMenuManager().openMainMenu(player);
    }

    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createToggleItem(Material material, String name, boolean enabled, String... description) {
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(line);
        }
        lore.add("");
        lore.add(enabled ? "§aCurrently: Enabled" : "§cCurrently: Disabled");
        lore.add("");
        lore.add("§eClick to toggle");
        
        return createItem(material, name, lore.toArray(new String[0]));
    }

    protected ItemStack createValueItem(Material material, String name, String currentValue, String... description) {
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(line);
        }
        lore.add("");
        lore.add("§7Current: §e" + currentValue);
        lore.add("");
        lore.add("§eClick to edit");
        
        return createItem(material, name, lore.toArray(new String[0]));
    }

    protected ItemStack createListItem(Material material, String name, int count, String... description) {
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(line);
        }
        lore.add("");
        lore.add("§7Items in list: §e" + count);
        lore.add("");
        lore.add("§eClick to manage");
        
        return createItem(material, name, lore.toArray(new String[0]));
    }

    protected ItemStack createBackButton() {
        return createItem(Material.ARROW, "§c« Back to Main Menu", 
            "§7Return to the main menu",
            "",
            isDirty ? "§e⚠ You have unsaved changes!" : "");
    }

    protected ItemStack createSaveButton() {
        if (!isDirty) {
            return createItem(Material.GRAY_DYE, "§8Save Changes", 
                "§7No changes to save");
        }
        return createItem(Material.EMERALD, "§a✓ Save Changes", 
            "§7Click to save all changes",
            "",
            "§e" + pendingChanges.size() + " change(s) pending");
    }

    protected ItemStack createCancelButton() {
        if (!isDirty) {
            return createItem(Material.BARRIER, "§cDiscard Changes", 
                "§7No changes to discard");
        }
        return createItem(Material.BARRIER, "§c✗ Discard Changes", 
            "§7Click to discard all changes",
            "",
            "§c" + pendingChanges.size() + " change(s) will be lost");
    }

    protected ItemStack createHelpItem() {
        return createItem(Material.BOOK, "§e? Help", 
            "§7Click items to configure them",
            "§7Green = Enabled | Red = Disabled",
            "",
            "§7Don't forget to save your changes!",
            "",
            "§eDocumentation: §b/vanilla wiki");
    }
}
