package com.tejaslamba.smpcore.menu;

import com.tejaslamba.smpcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenu extends BaseMenu {

    private static final int[] VALID_SLOTS = calculateValidSlots();
    private static final int SLOTS_PER_PAGE = VALID_SLOTS.length;

    private int currentPage = 0;

    public MainMenu(Main plugin) {
        super(plugin, null);
    }

    private static int[] calculateValidSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < 5; row++) {
            for (int col = 1; col < 8; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, 54, "§6§lSMP Core Settings");
    }

    public void setupItems() {
        inventory.clear();
        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();

        int totalPages = getTotalPages(items.size());
        int startIndex = currentPage * SLOTS_PER_PAGE;

        int itemIndex = 0;
        for (int slot : VALID_SLOTS) {
            int actualIndex = startIndex + itemIndex;
            if (actualIndex >= items.size())
                break;
            inventory.setItem(slot, items.get(actualIndex));
            itemIndex++;
        }

        if (totalPages > 1) {
            if (currentPage > 0) {
                inventory.setItem(45, createNavItem(Material.ARROW, "§a« Previous Page", currentPage, totalPages));
            }

            inventory.setItem(49, createPageIndicator(currentPage + 1, totalPages));

            if (currentPage < totalPages - 1) {
                inventory.setItem(53, createNavItem(Material.ARROW, "§aNext Page »", currentPage + 2, totalPages));
            }
        } else {
            inventory.setItem(49, createMenuItem(Material.OAK_DOOR, "§c§lClose Menu", "§7Close this menu"));
        }
    }

    private ItemStack createNavItem(Material material, String name, int targetPage, int totalPages) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Page §e" + targetPage + "§7/§e" + totalPages);
            lore.add("§eClick to navigate");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPageIndicator(int current, int total) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Page §e" + current + "§6/§e" + total);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Use arrows to navigate");
            lore.add("");
            lore.add("§cClick to close menu");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getTotalPages(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / SLOTS_PER_PAGE));
    }

    @Override
    public void open(Player player) {
        setupItems();
        player.openInventory(inventory);
    }

    public void openPage(Player player, int page) {
        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();
        int totalPages = getTotalPages(items.size());
        currentPage = Math.max(0, Math.min(page, totalPages - 1));
        setupItems();
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        boolean isRightClick = event.isRightClick();

        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();
        int totalPages = getTotalPages(items.size());

        if (slot == 45 && currentPage > 0) {
            currentPage--;
            setupItems();
            return;
        }

        if (slot == 53 && currentPage < totalPages - 1) {
            currentPage++;
            setupItems();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.contains("§8Config: §7")) {
                        String configPath = line.replace("§8Config: §7", "").replaceAll("§.", "");
                        String featureConfigPath = configPath.replace(".enabled", "");

                        com.tejaslamba.smpcore.feature.Feature feature = plugin.getFeatureManager()
                                .getFeatureByConfigPath(featureConfigPath);
                        if (feature != null) {
                            if (isRightClick) {
                                feature.onRightClick(player);
                            } else {
                                feature.onLeftClick(player);
                            }
                            refresh(player);
                            setupItems();
                        }
                        return;
                    }
                }
            }
        }
    }

}
