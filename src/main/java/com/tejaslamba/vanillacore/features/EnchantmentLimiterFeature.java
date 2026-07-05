package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.enchantlimiter.EnchantmentLimiterManager;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.EnchantmentLimiterListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
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
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder gh
                    && gh.getId().startsWith("enchantment-limiter")) {
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
        return createMenuItem(Material.ENCHANTED_BOOK,
                plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.name"),
                plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.lore-2"));
        lore.add("");
        int limitsCount = manager != null ? manager.getLimitsCount() : 0;
        lore.add(plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.configured-count")
                .replace("<configured>", String.valueOf(limitsCount)));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.enchantment-limiter.available-count")
                .replace("<available>", String.valueOf(allEnchantments.size())));
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
        if (!isEnabled()) {
            player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.feature-disabled"));
            return;
        }
        openConfigGUI(player, 0);
    }

    public void openConfigGUI(Player player, int page) {
        int totalPages = getTotalPages();
        page = Math.clamp(page, 0, totalPages - 1);
        Inventory gui = Bukkit.createInventory(new GuiHolder("enchantment-limiter-config"), 54,
                plugin.getMessageManager().get("enchantment-limiter.gui.title", "page", page + 1, "total",
                        totalPages));

        int startIndex = page * CONTENT_SLOTS.length;
        int slotIndex = 0;

        for (int i = startIndex; i < allEnchantments.size() && slotIndex < CONTENT_SLOTS.length; i++) {
            Enchantment enchant = allEnchantments.get(i);
            gui.setItem(CONTENT_SLOTS[slotIndex], createEnchantmentItem(enchant));
            slotIndex++;
        }

        if (page > 0) {
            gui.setItem(45, createNavItem(Material.ARROW,
                    plugin.getMessageManager().getRaw("enchantment-limiter.gui.nav.previous"),
                    plugin.getMessageManager().getRaw("enchantment-limiter.gui.nav.page").replace("<page>",
                            String.valueOf(page)).replace("<total>", String.valueOf(totalPages))));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createNavItem(Material.ARROW,
                    plugin.getMessageManager().getRaw("enchantment-limiter.gui.nav.next"),
                    plugin.getMessageManager().getRaw("enchantment-limiter.gui.nav.page").replace("<page>",
                            String.valueOf(page + 2)).replace("<total>", String.valueOf(totalPages))));
        }

        gui.setItem(49, createNavItem(Material.OAK_DOOR,
                plugin.getMessageManager().getRaw("enchantment-limiter.gui.shared.back.name"),
                plugin.getMessageManager().getRaw("enchantment-limiter.gui.shared.back.lore-1")));
        gui.setItem(50, createNavItem(Material.BARRIER,
                plugin.getMessageManager().getRaw("enchantment-limiter.gui.shared.close.name"),
                plugin.getMessageManager().getRaw("enchantment-limiter.gui.shared.close.lore-1")));

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(plugin.getMessageManager().get("enchantment-limiter.gui.info.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.left-click"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.right-click"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.shift-left"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.shift-right"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.middle-click"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.info.supports-custom"));
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
                lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.custom"));
                lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.key", "key",
                        enchant.getKey().toString()));
                lore.add(Component.empty());
            }

            if (isBanned) {
                lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.status-banned"));
            } else if (hasLimit) {
                lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.limit", "limit", limit,
                        "max", enchant.getMaxLevel()));
            } else {
                lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.status-no-limit"));
            }

            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.left-click"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.right-click"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.shift-left", "max",
                    enchant.getMaxLevel()));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.shift-right"));
            lore.add(plugin.getMessageManager().get("enchantment-limiter.gui.item.middle-click"));

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
            player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.gui.messages.removed", "enchant",
                    manager.getEnchantDisplayName(enchant)));
            return;
        }

        if (isShiftClick) {
            if (isLeftClick) {
                manager.setLimit(enchantName, enchant.getMaxLevel());
                player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.gui.messages.set-max", "enchant",
                        manager.getEnchantDisplayName(enchant), "max", enchant.getMaxLevel()));
            } else if (isRightClick) {
                manager.setLimit(enchantName, 0);
                player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.gui.messages.banned", "enchant",
                        manager.getEnchantDisplayName(enchant)));
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
            player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.gui.messages.increased", "enchant",
                    manager.getEnchantDisplayName(enchant), "limit", newLimit));
        } else if (isRightClick) {
            if (currentLimit < 0) {
                newLimit = enchant.getMaxLevel();
            } else {
                newLimit = Math.max(currentLimit - 1, 0);
            }
            manager.setLimit(enchantName, newLimit);
            if (newLimit == 0) {
                player.sendMessage(plugin.getMessageManager().get("enchantment-limiter.gui.messages.banned", "enchant",
                        manager.getEnchantDisplayName(enchant)));
            } else {
                player.sendMessage(
                        plugin.getMessageManager().get("enchantment-limiter.gui.messages.decreased", "enchant",
                                manager.getEnchantDisplayName(enchant), "limit", newLimit));
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

