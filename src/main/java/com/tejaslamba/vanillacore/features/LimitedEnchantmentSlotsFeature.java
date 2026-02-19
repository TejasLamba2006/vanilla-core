package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.limitedenchants.EnchantedItemRegistry;
import com.tejaslamba.vanillacore.limitedenchants.LimitedEnchantsManager;
import com.tejaslamba.vanillacore.listener.LimitedEnchantsListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LimitedEnchantmentSlotsFeature extends BaseFeature {

    public static final String GUI_TITLE = "§6Limited Enchantment Slots";
    public static final String CONFIG_GUI_TITLE = "§6Configure Material Limits";
    public static final String VIEW_GUI_TITLE = "§6Registered Items: ";
    private static final int[] CONTENT_SLOTS = new int[45];

    static {
        for (int i = 0; i < 45; i++) {
            CONTENT_SLOTS[i] = i;
        }
    }

    private LimitedEnchantsListener listener;
    private LimitedEnchantsManager manager;
    private final List<Material> allMaterials = new ArrayList<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, Material> viewingMaterial = new HashMap<>();
    private final Map<UUID, String> previousGUI = new HashMap<>();

    @Override
    public String getName() {
        return "Limited Enchantment Slots";
    }

    @Override
    public String getConfigPath() {
        return "features.limited-enchantment-slots";
    }

    @Override
    public int getDisplayOrder() {
        return 11;
    }

    @Override
    public void onEnable(Main plugin) {
        manager = new LimitedEnchantsManager(plugin);
        listener = new LimitedEnchantsListener(plugin, manager);
        super.onEnable(plugin);
        manager.load();
        loadAllEnchantableMaterials();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Limited Enchantment Slots - Enabled with "
                    + manager.getAllLimits().size() + " material limits");
            plugin.getLogger().info("[VERBOSE] Limited Enchantment Slots - Total registrations: "
                    + manager.getTotalRegistrations());
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            manager.scanAllOnlinePlayers();
        }, 40L);
    }

    private void loadAllEnchantableMaterials() {
        allMaterials.clear();
        for (Material material : Material.values()) {
            if (isEnchantable(material)) {
                allMaterials.add(material);
            }
        }
        allMaterials.sort(Comparator.comparing(Material::name));
    }

    private boolean isEnchantable(Material material) {
        if (!material.isItem() || material.isAir()) {
            return false;
        }

        String name = material.name();

        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                name.endsWith("_SWORD") || name.endsWith("_AXE") ||
                name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
                name.endsWith("_HOE") || name.equals("BOW") ||
                name.equals("CROSSBOW") || name.equals("TRIDENT") ||
                name.equals("FISHING_ROD") || name.equals("SHEARS") ||
                name.equals("SHIELD") || name.equals("ELYTRA") ||
                name.equals("BOOK") || name.equals("CARVED_PUMPKIN") ||
                name.equals("MACE") || name.equals("FLINT_AND_STEEL") ||
                name.equals("CARROT_ON_A_STICK") || name.equals("WARPED_FUNGUS_ON_A_STICK") ||
                name.equals("BRUSH");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String title = player.getOpenInventory().getTitle();
            if (title.equals(GUI_TITLE) || title.startsWith(CONFIG_GUI_TITLE)
                    || title.startsWith(VIEW_GUI_TITLE)) {
                player.closeInventory();
            }
        }

        playerPages.clear();
        viewingMaterial.clear();
        previousGUI.clear();

        if (manager != null) {
            manager.shutdown();
        }
        super.onDisable();
    }

    @Override
    public void reload() {
        super.reload();
        if (manager != null) {
            manager.load();
        }
        loadAllEnchantableMaterials();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ENCHANTING_TABLE, "§6Limited Enchantment Slots",
                "§7First-come-first-serve enchantment limits");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7Limit how many of an item can be");
        lore.add("§7enchanted server-wide");
        lore.add("");
        int limitsCount = manager != null ? manager.getAllLimits().size() : 0;
        int registrations = manager != null ? manager.getTotalRegistrations() : 0;
        lore.add("§7Material Limits: §e" + limitsCount);
        lore.add("§7Registered Items: §e" + registrations);
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Open Manager");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        if (!isEnabled()) {
            player.sendMessage("§c[Vanilla Core] Limited Enchantment Slots is disabled! Enable it first.");
            return;
        }
        openMainGUI(player);
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack configItem = new ItemStack(Material.ANVIL);
        ItemMeta configMeta = configItem.getItemMeta();
        if (configMeta != null) {
            configMeta.setDisplayName("§eConfigure Material Limits");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Set limits for specific materials");
            lore.add("§7Total Limits: §e" + manager.getAllLimits().size());
            lore.add("");
            lore.add("§eClick to configure");
            configMeta.setLore(lore);
            configItem.setItemMeta(configMeta);
        }
        gui.setItem(11, configItem);

        ItemStack viewItem = new ItemStack(Material.SPYGLASS);
        ItemMeta viewMeta = viewItem.getItemMeta();
        if (viewMeta != null) {
            viewMeta.setDisplayName("§eView Registered Items");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7See who owns registered");
            lore.add("§7enchanted items");
            lore.add("§7Total Registrations: §e" + manager.getTotalRegistrations());
            lore.add("");
            lore.add("§eClick to view");
            viewMeta.setLore(lore);
            viewItem.setItemMeta(viewMeta);
        }
        gui.setItem(13, viewItem);

        ItemStack scanItem = new ItemStack(Material.COMPASS);
        ItemMeta scanMeta = scanItem.getItemMeta();
        if (scanMeta != null) {
            scanMeta.setDisplayName("§eScan All Online Players");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Scan existing enchanted items");
            lore.add("§7from all online players");
            lore.add("");
            lore.add("§eClick to scan");
            scanMeta.setLore(lore);
            scanItem.setItemMeta(scanMeta);
        }
        gui.setItem(15, scanItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§eBack to Main Menu");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(22, backItem);

        player.openInventory(gui);
    }

    public void openConfigGUI(Player player, int page) {
        int totalPages = getTotalPages();
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPages.put(player.getUniqueId(), page);
        previousGUI.put(player.getUniqueId(), "CONFIG");

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Limited Enchants Config - Opening page " + page +
                    " for " + player.getName() + " (total pages: " + totalPages + ", total materials: " +
                    allMaterials.size() + ")");
        }

        Inventory gui = Bukkit.createInventory(null, 54,
                CONFIG_GUI_TITLE + " §7(" + (page + 1) + "/" + totalPages + ")");

        int startIndex = page * CONTENT_SLOTS.length;
        int slotIndex = 0;

        for (int i = startIndex; i < allMaterials.size() && slotIndex < CONTENT_SLOTS.length; i++) {
            Material material = allMaterials.get(i);
            gui.setItem(CONTENT_SLOTS[slotIndex], createMaterialItem(material));
            slotIndex++;
        }

        if (page > 0) {
            gui.setItem(45, createNavItem(Material.ARROW, "§a« Previous Page",
                    "§7Page §e" + page + "§7/§e" + totalPages));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createNavItem(Material.ARROW, "§aNext Page »",
                    "§7Page §e" + (page + 2) + "§7/§e" + totalPages));
        }

        gui.setItem(49, createNavItem(Material.OAK_DOOR, "§eBack to Menu", "§7Return to main menu"));
        gui.setItem(50, createNavItem(Material.BARRIER, "§cClose", "§7Close this menu"));

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6How to Use");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§aLeft Click: §7Increase limit by 1");
            lore.add("§cRight Click: §7Decrease limit by 1");
            lore.add("§eShift + Left: §7Increase limit by 5");
            lore.add("§eShift + Right: §7Decrease limit by 5");
            lore.add("§dMiddle Click: §7Remove limit");
            lore.add("§bDrop Key: §7View registrations");
            lore.add("");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(47, infoItem);

        player.openInventory(gui);
    }

    private ItemStack createMaterialItem(Material material) {
        int limit = manager.getLimit(material);
        int registered = manager.getRegisteredCount(material);
        boolean hasLimit = limit >= 0;

        Material displayMaterial = hasLimit ? material : Material.GRAY_STAINED_GLASS_PANE;

        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String nameColor = hasLimit ? "§e" : "§7";
            meta.setDisplayName(nameColor + material.name());

            List<String> lore = new ArrayList<>();
            lore.add("");

            if (hasLimit) {
                lore.add("§7Limit: §e" + limit);
                lore.add("§7Registered: §a" + registered + "§7/§e" + limit);
                if (registered >= limit) {
                    lore.add("§c⚠ Limit Reached!");
                } else {
                    lore.add("§a✓ " + (limit - registered) + " slots available");
                }
            } else {
                lore.add("§aStatus: §2No Limit");
            }

            lore.add("");
            lore.add("§aLeft Click: §7+1 limit");
            lore.add("§cRight Click: §7-1 limit");
            lore.add("§eShift+Left: §7+5 limit");
            lore.add("§eShift+Right: §7-5 limit");
            lore.add("§dMiddle Click: §7Remove limit");
            lore.add("§bDrop Key: §7View registered items");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public void openViewRegistrationsGUI(Player player, Material material) {
        List<EnchantedItemRegistry> registrations = manager.getRegistrationsForMaterial(material);

        Inventory gui = Bukkit.createInventory(null, 54,
                VIEW_GUI_TITLE + material.name());

        String previousMenu = previousGUI.getOrDefault(player.getUniqueId(), "MAIN");

        viewingMaterial.put(player.getUniqueId(), material);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        for (int i = 0; i < Math.min(registrations.size(), 45); i++) {
            EnchantedItemRegistry registration = registrations.get(i);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + registration.getPlayerName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Registered: §e" + dateFormat.format(new Date(registration.getTimestamp())));
                lore.add("§7Last Seen: §e" + dateFormat.format(new Date(registration.getLastSeen())));
                lore.add("");
                lore.add("§7UUID: §8" + registration.getItemUUID().substring(0, 8) + "...");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

        ItemStack clearItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearItem.getItemMeta();
        if (clearMeta != null) {
            clearMeta.setDisplayName("§cClear All Registrations");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Remove all registrations for");
            lore.add("§7" + material.name());
            lore.add("");
            lore.add("§eClick to clear");
            clearMeta.setLore(lore);
            clearItem.setItemMeta(clearMeta);
        }
        gui.setItem(49, clearItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            if (previousMenu.equals("CONFIG")) {
                backMeta.setDisplayName("§eBack to Config");
            } else {
                backMeta.setDisplayName("§eBack to Material Selection");
            }
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(50, backItem);

        player.openInventory(gui);
    }

    private ItemStack createNavItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                Collections.addAll(lore, loreLines);
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allMaterials.size() / CONTENT_SLOTS.length));
    }

    public void handleMainGUIClick(int slot, Player player) {
        if (slot == 11) {
            openConfigGUI(player, 0);
        } else if (slot == 13) {
            openViewMaterialSelectionGUI(player);
            previousGUI.put(player.getUniqueId(), "SELECTION");
        } else if (slot == 15) {
            player.closeInventory();
            player.sendMessage("§a[Vanilla Core] §7Scanning all online players...");
            manager.scanAllOnlinePlayers();
            player.sendMessage("§a[Vanilla Core] §7Scan complete! Total registrations: §e"
                    + manager.getTotalRegistrations());
        } else if (slot == 22) {
            player.closeInventory();
            plugin.getMenuManager().openMainMenu(player);
        }
    }

    private void openViewMaterialSelectionGUI(Player player) {
        List<Material> materialsWithLimits = manager.getAllLimits().keySet().stream()
                .sorted(Comparator.comparing(Material::name))
                .collect(Collectors.toList());

        Inventory gui = Bukkit.createInventory(null, 54, "§6Select Material to View");

        for (int i = 0; i < Math.min(materialsWithLimits.size(), 45); i++) {
            Material material = materialsWithLimits.get(i);
            int registered = manager.getRegisteredCount(material);
            int limit = manager.getLimit(material);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + material.name());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Registered: §a" + registered + "§7/§e" + limit);
                lore.add("");
                lore.add("§eClick to view registrations");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§eBack");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    public void handleViewMaterialSelectionClick(int slot, Player player, ItemStack clickedItem) {
        if (slot == 49) {
            openMainGUI(player);
            return;
        }

        if (slot < 45 && clickedItem != null && clickedItem.getType() != Material.AIR) {
            Material material = clickedItem.getType();
            if (manager.hasLimit(material)) {
                openViewRegistrationsGUI(player, material);
            }
        }
    }

    public void handleConfigGUIClick(int slot, int page, boolean isLeftClick, boolean isRightClick,
            boolean isShiftClick, boolean isMiddleClick, boolean isDropKey, Player player) {
        if (slot == 45 && page > 0) {
            openConfigGUI(player, page - 1);
            return;
        }

        if (slot == 53 && page < getTotalPages() - 1) {
            openConfigGUI(player, page + 1);
            return;
        }

        if (slot == 49) {
            openMainGUI(player);
            return;
        }

        if (slot == 50) {
            player.closeInventory();
            return;
        }

        if (slot == 47) {
            return;
        }

        if (slot >= 0 && slot < 45) {
            int index = page * 45 + slot;
            if (index >= 0 && index < allMaterials.size()) {
                Material material = allMaterials.get(index);

                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Limited Enchants Config - Page: " + page +
                            ", Slot: " + slot + ", Index: " + index + ", Material: " + material.name());
                }

                handleMaterialClick(material, isLeftClick, isRightClick, isShiftClick, isMiddleClick, isDropKey,
                        player);
                if (!isDropKey) {
                    openConfigGUI(player, page);
                }
            } else if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Limited Enchants Config - Invalid index: " + index +
                        " (page=" + page + ", slot=" + slot + ", total materials=" + allMaterials.size() + ")");
            }
        }
    }

    private void handleMaterialClick(Material material, boolean isLeftClick, boolean isRightClick,
            boolean isShiftClick, boolean isMiddleClick, boolean isDropKey, Player player) {
        if (isDropKey) {
            if (manager.hasLimit(material)) {
                openViewRegistrationsGUI(player, material);
            } else {
                player.sendMessage("§c[Vanilla Core] §7No limit set for " + material.name());
            }
            return;
        }

        if (isMiddleClick) {
            manager.removeLimit(material);
            player.sendMessage("§a[Vanilla Core] §7Removed limit for " + material.name());
            return;
        }

        int currentLimit = manager.getLimit(material);
        int newLimit;

        if (isShiftClick) {
            int change = 5;
            if (isLeftClick) {
                newLimit = (currentLimit < 0 ? 0 : currentLimit) + change;
            } else if (isRightClick) {
                newLimit = Math.max(0, (currentLimit < 0 ? 0 : currentLimit) - change);
            } else {
                return;
            }
        } else {
            if (isLeftClick) {
                newLimit = (currentLimit < 0 ? 1 : currentLimit + 1);
            } else if (isRightClick) {
                newLimit = Math.max(0, (currentLimit < 0 ? 0 : currentLimit - 1));
            } else {
                return;
            }
        }

        manager.setLimit(material, newLimit);
        player.sendMessage(
                "§a[Vanilla Core] §7Set " + material.name() + " limit to: §e" + newLimit);
    }

    public void handleViewRegistrationsClick(int slot, Player player) {
        Material material = viewingMaterial.get(player.getUniqueId());
        if (material == null) {
            player.closeInventory();
            return;
        }

        if (slot == 49) {
            manager.clearRegistrationsForMaterial(material);
            player.sendMessage("§a[Vanilla Core] §7Cleared all registrations for " + material.name());
            player.closeInventory();
            openViewRegistrationsGUI(player, material);
        } else if (slot == 50) {
            String previousMenu = previousGUI.getOrDefault(player.getUniqueId(), "MAIN");
            if (previousMenu.equals("CONFIG")) {
                int page = playerPages.getOrDefault(player.getUniqueId(), 0);
                openConfigGUI(player, page);
            } else if (previousMenu.equals("SELECTION")) {
                openViewMaterialSelectionGUI(player);
            } else {
                openMainGUI(player);
            }
        }
    }

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public void cleanupPlayer(UUID playerId) {
        playerPages.remove(playerId);
        viewingMaterial.remove(playerId);
        previousGUI.remove(playerId);
    }

    public LimitedEnchantsManager getManager() {
        return manager;
    }
}
