package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.enchantlimiter.EnchantmentLimiterManager;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.EnchantmentLimiterListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnchantmentLimiterFeature extends BaseFeature {

    public static final String GUI_TITLE = "§6Enchantment Limiter";
    public static final String CONFIG_GUI_TITLE = "§6Configure Limits";
    private static final int[] CONTENT_SLOTS = new int[45];
    private static final String VERBOSE_PREFIX = "[VERBOSE] Enchantment Limiter - ";

    static {
        for (int i = 0; i < 45; i++) {
            CONTENT_SLOTS[i] = i;
        }
    }

    private EnchantmentLimiterListener listener;
    private EnchantmentLimiterManager manager;
    private final List<Enchantment> allEnchantments = new ArrayList<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> editingSessions = new HashMap<>();

    @Override
    public String getName() {
        return "Enchantment Limiter";
    }

    @Override
    public String getConfigPath() {
        return "features.enchantment-limiter";
    }

    @Override
    public int getDisplayOrder() {
        return 10;
    }

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        manager = new EnchantmentLimiterManager(plugin);
        listener = new EnchantmentLimiterListener(plugin, manager);
        super.onEnable(plugin);
        manager.load();
        loadAllEnchantments();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Enabled with " + manager.getLimitsCount() + " limits");
            plugin.getLogger().info(
                    VERBOSE_PREFIX + "Found " + allEnchantments.size() + " total enchantments (including custom)");
        }
    }

    private void loadAllEnchantments() {
        allEnchantments.clear();
        Registry.ENCHANTMENT.forEach(allEnchantments::add);
        allEnchantments.sort(Comparator.comparing(e -> e.getKey().getKey()));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String title = player.getOpenInventory().getTitle();
            if (title.equals(GUI_TITLE) || title.startsWith(CONFIG_GUI_TITLE)) {
                player.closeInventory();
            }
        }

        playerPages.clear();
        editingSessions.clear();

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
        loadAllEnchantments();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ENCHANTED_BOOK, "<!italic><gold>Enchantment Limiter",
                "<gray>Limit enchantment levels on items");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Enforces enchantment level caps");
        lore.add("<gray>on all items automatically");
        lore.add("");
        int limitsCount = manager != null ? manager.getLimitsCount() : 0;
        lore.add("<gray>Configured Limits: <yellow>" + limitsCount);
        lore.add("<gray>Available Enchants: <yellow>" + allEnchantments.size());
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Open Manager");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        if (!isEnabled()) {
            player.sendMessage(MessageManager.parse("<red>Enchantment Limiter is disabled! Enable it first."));
            return;
        }
        openConfigGUI(player, 0);
    }

    public void openConfigGUI(Player player, int page) {
        int totalPages = getTotalPages();
        page = Math.clamp(page, 0, totalPages - 1);
        Inventory gui = Bukkit.createInventory(null, 54,
                CONFIG_GUI_TITLE + " §7(" + (page + 1) + "/" + totalPages + ")");

        int startIndex = page * CONTENT_SLOTS.length;
        int slotIndex = 0;

        for (int i = startIndex; i < allEnchantments.size() && slotIndex < CONTENT_SLOTS.length; i++) {
            Enchantment enchant = allEnchantments.get(i);
            gui.setItem(CONTENT_SLOTS[slotIndex], createEnchantmentItem(enchant));
            slotIndex++;
        }

        if (page > 0) {
            gui.setItem(45, createNavItem(Material.ARROW, "<!italic><green>\u00ab Previous Page",
                    "<gray>Page <yellow>" + page + "<gray>/<yellow>" + totalPages));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createNavItem(Material.ARROW, "<!italic><green>Next Page \u00bb",
                    "<gray>Page <yellow>" + (page + 2) + "<gray>/<yellow>" + totalPages));
        }

        gui.setItem(49, createNavItem(Material.OAK_DOOR, "<!italic><yellow>Back to Menu", "<gray>Return to main menu"));
        gui.setItem(50, createNavItem(Material.BARRIER, "<!italic><red>Close", "<gray>Close this menu"));

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(MessageManager.parse("<!italic><gold>How to Use"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<green>Left Click: <gray>Increase limit by 1"));
            lore.add(MessageManager.parse("<red>Right Click: <gray>Decrease limit by 1"));
            lore.add(MessageManager.parse("<yellow>Shift + Left: <gray>Set to max level"));
            lore.add(MessageManager.parse("<yellow>Shift + Right: <gray>Ban (set to 0)"));
            lore.add(MessageManager.parse("<light_purple>Middle Click: <gray>Remove limit"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Supports custom enchantments!"));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(47, infoItem);

        player.openInventory(gui);
        playerPages.put(player.getUniqueId(), page);
    }

    private ItemStack createEnchantmentItem(Enchantment enchant) {
        int limit = manager.getLimit(enchant);
        boolean hasLimit = limit >= 0;
        boolean isBanned = limit == 0;

        Material material;
        if (isBanned) {
            material = Material.BARRIER;
        } else if (hasLimit) {
            material = Material.ENCHANTED_BOOK;
        } else {
            material = Material.BOOK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = manager.getEnchantDisplayName(enchant);
            String namespace = enchant.getKey().getNamespace();
            boolean isCustom = !namespace.equals("minecraft");

            String nameColor = isBanned ? "<red>" : (hasLimit ? "<yellow>" : "<green>");
            meta.displayName(MessageManager.parse("<!italic>" + nameColor + displayName));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            if (isCustom) {
                lore.add(MessageManager.parse("<light_purple><italic>Custom Enchantment"));
                lore.add(MessageManager.parse("<dark_gray>" + enchant.getKey().toString()));
                lore.add(Component.empty());
            }

            if (isBanned) {
                lore.add(MessageManager.parse("<red>Status: <dark_red><bold>BANNED"));
            } else if (hasLimit) {
                lore.add(MessageManager
                        .parse("<gray>Limit: <yellow>" + limit + " <dark_gray>(Max: " + enchant.getMaxLevel() + ")"));
            } else {
                lore.add(MessageManager.parse("<green>Status: <dark_green>No Limit"));
            }

            lore.add(Component.empty());
            lore.add(MessageManager.parse("<green>Left Click: <gray>+1 limit"));
            lore.add(MessageManager.parse("<red>Right Click: <gray>-1 limit"));
            lore.add(MessageManager.parse("<yellow>Shift+Left: <gray>Max (" + enchant.getMaxLevel() + ")"));
            lore.add(MessageManager.parse("<yellow>Shift+Right: <gray>Ban"));
            lore.add(MessageManager.parse("<light_purple>Middle Click: <gray>Remove"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createNavItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse(name));
            if (loreLines.length > 0) {
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                for (String line : loreLines) {
                    lore.add(MessageManager.parse(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allEnchantments.size() / CONTENT_SLOTS.length));
    }

    public void handleConfigGUIClick(int slot, int page, boolean isLeftClick, boolean isRightClick,
            boolean isShiftClick, boolean isMiddleClick, Player player) {
        if (slot == 45 && page > 0) {
            openConfigGUI(player, page - 1);
            return;
        }

        if (slot == 53 && page < getTotalPages() - 1) {
            openConfigGUI(player, page + 1);

            return;
        }

        if (slot == 49) {
            player.closeInventory();
            plugin.getMenuManager().openMainMenu(player);
            return;
        }

        if (slot == 50) {
            player.closeInventory();
            return;
        }

        if (slot >= 0 && slot < CONTENT_SLOTS.length) {
            int index = page * CONTENT_SLOTS.length + slot;
            if (index >= 0 && index < allEnchantments.size()) {
                Enchantment enchant = allEnchantments.get(index);
                handleEnchantmentClick(enchant, isLeftClick, isRightClick, isShiftClick, isMiddleClick, player);
                openConfigGUI(player, page);
            }
        }
    }

    private void handleEnchantmentClick(Enchantment enchant, boolean isLeftClick, boolean isRightClick,
            boolean isShiftClick, boolean isMiddleClick, Player player) {
        int currentLimit = manager.getLimit(enchant);
        String enchantName = enchant.getKey().getKey();

        if (isMiddleClick) {
            manager.setLimit(enchantName, -1);
            player.sendMessage(MessageManager
                    .parse("<green>[Vanilla Core] <gray>Removed limit for " + manager.getEnchantDisplayName(enchant)));
            return;
        }

        if (isShiftClick) {
            if (isLeftClick) {
                manager.setLimit(enchantName, enchant.getMaxLevel());
                player.sendMessage(
                        MessageManager.parse("<green>[Vanilla Core] <gray>Set " + manager.getEnchantDisplayName(enchant)
                                + " to max level: " + enchant.getMaxLevel()));
            } else if (isRightClick) {
                manager.setLimit(enchantName, 0);
                player.sendMessage(MessageManager
                        .parse("<red>[Vanilla Core] <gray>Banned " + manager.getEnchantDisplayName(enchant)));
            }
            return;
        }

        int newLimit;
        if (isLeftClick) {
            if (currentLimit < 0) {
                newLimit = 1;
            } else {
                newLimit = Math.min(currentLimit + 1, 255);
            }
            manager.setLimit(enchantName, newLimit);
            player.sendMessage(MessageManager
                    .parse("<green>[Vanilla Core] <gray>Increased " + manager.getEnchantDisplayName(enchant)
                            + " limit to: " + newLimit));
        } else if (isRightClick) {
            if (currentLimit < 0) {
                newLimit = enchant.getMaxLevel();
            } else {
                newLimit = Math.max(currentLimit - 1, 0);
            }
            manager.setLimit(enchantName, newLimit);
            if (newLimit == 0) {
                player.sendMessage(MessageManager
                        .parse("<red>[Vanilla Core] <gray>Banned " + manager.getEnchantDisplayName(enchant)));
            } else {
                player.sendMessage(MessageManager
                        .parse("<yellow>[Vanilla Core] <gray>Decreased " + manager.getEnchantDisplayName(enchant)
                                + " limit to: " + newLimit));
            }
        }
    }

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public void cleanupPlayer(UUID playerId) {
        playerPages.remove(playerId);
        editingSessions.remove(playerId);
    }

    public EnchantmentLimiterManager getManager() {
        return manager;
    }

    public List<Enchantment> getAllEnchantments() {
        return new ArrayList<>(allEnchantments);
    }
}
