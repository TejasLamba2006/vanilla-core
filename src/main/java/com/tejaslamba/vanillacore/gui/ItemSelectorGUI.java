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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemSelectorGUI implements InventoryHolder {

    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    private final Consumer<Material> onSelect;
    private final Runnable onCancel;
    private int currentPage = 0;
    private List<Material> filteredMaterials;
    private String searchTerm = "";
    private Category currentCategory = Category.ALL;

    private static final int ITEMS_PER_PAGE = 36;
    
    public enum Category {
        ALL("All Items", null),
        BLOCKS("Blocks", m -> m.isBlock()),
        ITEMS("Items", m -> m.isItem() && !m.isBlock()),
        COMBAT("Combat", m -> isCombatItem(m)),
        TOOLS("Tools", m -> isToolItem(m)),
        FOOD("Food", m -> m.isEdible()),
        REDSTONE("Redstone", m -> isRedstoneItem(m));

        private final String displayName;
        private final java.util.function.Predicate<Material> filter;

        Category(String displayName, java.util.function.Predicate<Material> filter) {
            this.displayName = displayName;
            this.filter = filter;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean matches(Material material) {
            return filter == null || filter.test(material);
        }
    }

    private static boolean isCombatItem(Material m) {
        String name = m.name();
        return name.contains("SWORD") || name.contains("BOW") || name.contains("CROSSBOW") ||
               name.contains("TRIDENT") || name.contains("HELMET") || name.contains("CHESTPLATE") ||
               name.contains("LEGGINGS") || name.contains("BOOTS") || name.equals("SHIELD") ||
               name.contains("ARROW") || name.equals("MACE");
    }

    private static boolean isToolItem(Material m) {
        String name = m.name();
        return name.contains("PICKAXE") || name.contains("AXE") || name.contains("SHOVEL") ||
               name.contains("HOE") || name.equals("SHEARS") || name.equals("FISHING_ROD") ||
               name.equals("FLINT_AND_STEEL");
    }

    private static boolean isRedstoneItem(Material m) {
        String name = m.name();
        return name.contains("REDSTONE") || name.contains("PISTON") || name.contains("DISPENSER") ||
               name.contains("DROPPER") || name.contains("HOPPER") || name.contains("OBSERVER") ||
               name.contains("COMPARATOR") || name.contains("REPEATER") || name.contains("LEVER") ||
               name.contains("BUTTON") || name.contains("PRESSURE_PLATE") || name.contains("RAIL");
    }

    public ItemSelectorGUI(Main plugin, Player player, String title, 
                          Consumer<Material> onSelect, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.onSelect = onSelect;
        this.onCancel = onCancel;
        this.inventory = Bukkit.createInventory(this, 54, "§6" + title);
        updateFilteredMaterials();
        setupItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void updateFilteredMaterials() {
        filteredMaterials = Arrays.stream(Material.values())
            .filter(m -> !m.isLegacy())
            .filter(m -> m.isItem())
            .filter(currentCategory::matches)
            .filter(m -> searchTerm.isEmpty() || 
                   m.name().toLowerCase().contains(searchTerm.toLowerCase()))
            .sorted(Comparator.comparing(Material::name))
            .collect(Collectors.toList());
    }

    private void setupItems() {
        inventory.clear();
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredMaterials.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = filteredMaterials.get(i);
            inventory.setItem(slot, createMaterialItem(material));
            slot++;
        }
        
        inventory.setItem(45, createCategoryItem());
        inventory.setItem(46, createSearchItem());
        
        int totalPages = (int) Math.ceil((double) filteredMaterials.size() / ITEMS_PER_PAGE);
        
        if (currentPage > 0) {
            inventory.setItem(48, createPreviousPageItem());
        }
        
        inventory.setItem(49, createPageInfoItem(totalPages));
        
        if (currentPage < totalPages - 1) {
            inventory.setItem(50, createNextPageItem());
        }
        
        inventory.setItem(53, createCancelItem());
    }

    private ItemStack createMaterialItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + formatName(material.name()));
            List<String> lore = new ArrayList<>();
            lore.add("§7Material: §f" + material.name());
            lore.add("");
            lore.add("§eClick to select");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCategoryItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aCategory: §e" + currentCategory.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Current category:");
            lore.add("§e" + currentCategory.getDisplayName());
            lore.add("");
            lore.add("§7Click to cycle categories");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSearchItem() {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aSearch");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (searchTerm.isEmpty()) {
                lore.add("§7No active search");
            } else {
                lore.add("§7Searching for: §e" + searchTerm);
                lore.add("§7Results: §e" + filteredMaterials.size());
            }
            lore.add("");
            lore.add("§eLeft-click: Search");
            lore.add("§eRight-click: Clear search");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPageInfoItem(int totalPages) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Page " + (currentPage + 1) + " / " + totalPages);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Total items: §e" + filteredMaterials.size());
            lore.add("§7Category: §e" + currentCategory.getDisplayName());
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
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNextPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aNext Page »");
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
            lore.add("§7Return without selecting");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < 36) {
            int index = currentPage * ITEMS_PER_PAGE + slot;
            if (index < filteredMaterials.size()) {
                player.closeInventory();
                Material selected = filteredMaterials.get(index);
                onSelect.accept(selected);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        } else if (slot == 45) {
            Category[] categories = Category.values();
            int nextIndex = (currentCategory.ordinal() + 1) % categories.length;
            currentCategory = categories[nextIndex];
            currentPage = 0;
            updateFilteredMaterials();
            setupItems();
        } else if (slot == 46) {
            if (event.isLeftClick()) {
                player.closeInventory();
                new AnvilInputGUI.Builder(plugin, player)
                    .prompt("Enter search term:")
                    .defaultValue(searchTerm)
                    .onComplete((p, input) -> {
                        searchTerm = input;
                        currentPage = 0;
                        updateFilteredMaterials();
                        setupItems();
                        open();
                    })
                    .onCancel(this::open)
                    .build()
                    .open();
            } else if (event.isRightClick()) {
                searchTerm = "";
                currentPage = 0;
                updateFilteredMaterials();
                setupItems();
            }
        } else if (slot == 48 && currentPage > 0) {
            currentPage--;
            setupItems();
        } else if (slot == 50) {
            int totalPages = (int) Math.ceil((double) filteredMaterials.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                setupItems();
            }
        } else if (slot == 53) {
            player.closeInventory();
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }

    public static class Builder {
        private Main plugin;
        private Player player;
        private String title = "Select Item";
        private Consumer<Material> onSelect;
        private Runnable onCancel;

        public Builder(Main plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder onSelect(Consumer<Material> onSelect) {
            this.onSelect = onSelect;
            return this;
        }

        public Builder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public ItemSelectorGUI build() {
            return new ItemSelectorGUI(plugin, player, title, onSelect, onCancel);
        }
    }
}
