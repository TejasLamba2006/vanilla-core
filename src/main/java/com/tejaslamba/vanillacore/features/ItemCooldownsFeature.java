package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ItemCooldownsListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemCooldownsFeature extends BaseFeature {

    private static final Material[] GUI_ITEMS = {
            Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.ENDER_PEARL,
            Material.WIND_CHARGE
    };

    private static final Material[] SPEAR_MATERIALS = {
            Material.WOODEN_SPEAR,
            Material.STONE_SPEAR,
            Material.IRON_SPEAR,
            Material.GOLDEN_SPEAR,
            Material.DIAMOND_SPEAR,
            Material.NETHERITE_SPEAR,
            Material.COPPER_SPEAR
    };

    private static final int[] SPEAR_SLOTS = { 10, 11, 12, 13, 14, 15, 16 };

    private final Map<Material, Integer> cooldownTicks = new EnumMap<>(Material.class);
    private ItemCooldownsListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new ItemCooldownsListener(plugin);
        super.onEnable(plugin);
        loadSettings();
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        cooldownTicks.clear();

        var section = plugin.getConfigManager().get().getConfigurationSection(getConfigPath() + ".cooldowns");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key.toUpperCase(Locale.ROOT));
                if (material == null) {
                    continue;
                }

                double seconds = section.getDouble(key, 0.0D);
                int ticks = Math.max(0, (int) Math.round(seconds * 20.0D));
                cooldownTicks.put(material, ticks);
            }
        }

        ensureDefaultEntry(Material.GOLDEN_APPLE, 0);
        ensureDefaultEntry(Material.ENCHANTED_GOLDEN_APPLE, 0);
        ensureDefaultEntry(Material.ENDER_PEARL, 0);
        ensureDefaultEntry(Material.WIND_CHARGE, 10);
        for (Material spear : SPEAR_MATERIALS) {
            ensureDefaultEntry(spear, 0);
        }
    }

    public int getCooldownTicks(Material material) {
        return cooldownTicks.getOrDefault(material, 0);
    }

    public boolean hasConfiguredCooldown(Material material) {
        return getCooldownTicks(material) > 0;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 23;
    }

    @Override
    public String getName() {
        return "Item Cooldowns";
    }

    @Override
    public String getConfigPath() {
        return "features.item-cooldowns";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.CLOCK,
                plugin.getMessageManager().getRaw("feature-menus.item-cooldowns.name"),
                plugin.getMessageManager().getRaw("feature-menus.item-cooldowns.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.item-cooldowns.entries")
                .replace("<entries>", String.valueOf(cooldownTicks.size())));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-open-settings"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        openSettingsGUI(player);
    }

    public void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GuiHolder("item-cooldowns"), 27,
                plugin.getMessageManager().get("item-cooldowns.gui.title"));
        fillFiller(gui, 27);

        gui.setItem(1, buildCooldownItem(Material.GOLDEN_APPLE));
        gui.setItem(3, buildCooldownItem(Material.ENCHANTED_GOLDEN_APPLE));
        gui.setItem(5, buildCooldownItem(Material.ENDER_PEARL));
        gui.setItem(7, buildCooldownItem(Material.WIND_CHARGE));

        for (int i = 0; i < SPEAR_MATERIALS.length; i++) {
            gui.setItem(SPEAR_SLOTS[i], buildCooldownItem(SPEAR_MATERIALS[i]));
        }

        gui.setItem(20, buildInfoItem());
        gui.setItem(24, buildBackItem());

        player.openInventory(gui);
    }

    public void handleSettingsGUIClick(int slot, boolean isShiftClick, boolean isRightClick, Player player) {
        if (slot == 24) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMenuManager().openMainMenu(player));
            return;
        }

        Material target = switch (slot) {
            case 1 -> Material.GOLDEN_APPLE;
            case 3 -> Material.ENCHANTED_GOLDEN_APPLE;
            case 5 -> Material.ENDER_PEARL;
            case 7 -> Material.WIND_CHARGE;
            default -> spearMaterialForSlot(slot);
        };

        if (target == null) {
            return;
        }

        int deltaTicks = isShiftClick ? 100 : 10;
        int current = getCooldownTicks(target);
        int next = Math.max(0, Math.min(72000, current + (isRightClick ? -deltaTicks : deltaTicks)));
        cooldownTicks.put(target, next);

        saveSettings();
        plugin.getServer().getScheduler().runTask(plugin, () -> openSettingsGUI(player));
    }

    private void saveSettings() {
        var cfg = plugin.getConfigManager().get();
        String basePath = getConfigPath() + ".cooldowns";

        for (Map.Entry<Material, Integer> entry : cooldownTicks.entrySet()) {
            double seconds = entry.getValue() / 20.0D;
            cfg.set(basePath + "." + entry.getKey().name(), seconds);
        }

        plugin.getConfigManager().save();
    }

    private Material spearMaterialForSlot(int slot) {
        for (int i = 0; i < SPEAR_SLOTS.length; i++) {
            if (SPEAR_SLOTS[i] == slot) {
                return SPEAR_MATERIALS[i];
            }
        }
        return null;
    }

    private void ensureDefaultEntry(Material material, int ticks) {
        cooldownTicks.putIfAbsent(material, ticks);
    }

    private ItemStack buildCooldownItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            double seconds = getCooldownTicks(material) / 20.0D;
            meta.displayName(plugin.getMessageManager().get("item-cooldowns.gui.item.name", "item",
                    prettyMaterial(material)));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.item.cooldown", "seconds", seconds));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.item.left-click"));
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.item.right-click"));
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.item.shift-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("item-cooldowns.gui.info.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.info.lore-1"));
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.info.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("item-cooldowns.gui.info.lore-3"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildBackItem() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("item-cooldowns.gui.back"));
            back.setItemMeta(meta);
        }
        return back;
    }

    private void fillFiller(Inventory gui, int size) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }

        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pane);
            }
        }
    }

    private String prettyMaterial(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return out.toString().trim();
    }
}

