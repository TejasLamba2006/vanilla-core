package com.tejaslamba.vanillacore.menu;

import com.tejaslamba.vanillacore.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseMenu implements InventoryHolder {

    protected final Main plugin;
    protected final Inventory inventory;

    public BaseMenu(Main plugin, Inventory inventory) {
        this.plugin = plugin;
        this.inventory = inventory != null ? inventory : createInventory();
    }

    protected abstract Inventory createInventory();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    protected ItemStack createToggleItem(Material material, String name, String configPath, String... description) {
        boolean enabled = plugin.getConfigManager().get().getBoolean("features." + configPath + ".enabled", false);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>(Arrays.asList(description));
            lore.add("");
            lore.add("§8Config: §7features." + configPath + ".enabled");
            lore.add("");
            lore.add(enabled ? "§aEnabled" : "§cDisabled");

            if (configPath.equals("protection-limiter") || configPath.equals("sharpness-limiter")) {
                String configKey = configPath.equals("protection-limiter") ? "enchantment-limits.protection"
                        : "enchantment-limits.sharpness";
                int maxLevel = plugin.getConfigManager().get().getInt(configKey + ".max-level", 4);
                lore.add("§7Max Level: §e" + maxLevel);
                lore.add("");
                lore.add("§eLeft Click: Toggle On/Off");
                lore.add("§eRight Click: Set Level");
            } else {
                lore.add("§eClick to toggle");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    protected void toggleFeature(Player player, String configPath, boolean isRightClick) {
        if (configPath.equals("features.protection-limiter.enabled")
                || configPath.equals("features.sharpness-limiter.enabled")) {
            handleEnchantmentLimiter(player, configPath, isRightClick);
            return;
        }

        boolean current = plugin.getConfigManager().get().getBoolean(configPath, false);
        plugin.getConfigManager().get().set(configPath, !current);
        plugin.getConfigManager().save();
        plugin.getConfigManager().load();

        plugin.getFeatureManager().getFeatures().forEach(feature -> {
            if (configPath.equals(feature.getConfigPath() + ".enabled")) {
                feature.reload();
            }
        });

        String displayName = plugin.getMenuConfigManager().getDisplayNameForConfig(configPath);
        player.sendMessage(
                "§6[Vanilla Core] §7Toggled " + displayName + " §7to §" + (!current ? "aEnabled" : "cDisabled"));
    }

    protected void handleEnchantmentLimiter(Player player, String configPath, boolean isRightClick) {
        boolean isProtection = configPath.contains("protection");
        String enchantType = isProtection ? "Protection" : "Sharpness";
        String configKey = isProtection ? "enchantment-limits.protection" : "enchantment-limits.sharpness";

        if (!isRightClick) {
            boolean current = plugin.getConfigManager().get().getBoolean(configPath, false);
            plugin.getConfigManager().get().set(configPath, !current);
            plugin.getConfigManager().get().set(configKey + ".enabled", !current);
            plugin.getConfigManager().save();
            plugin.getConfigManager().load();
            player.sendMessage(
                    "§6[Vanilla Core] §7Toggled " + enchantType + " Limiter §7to §"
                            + (!current ? "aEnabled" : "cDisabled"));
            return;
        }

        player.closeInventory();
        player.sendMessage("§6[Vanilla Core] §7Enter the max level for " + enchantType + " (1-10, or 'cancel'):");

        Main.getInstance().getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage("§6[Vanilla Core] §cCancelled.");
                plugin.getMenuManager().openMainMenu(p);
                return;
            }

            try {
                int level = Integer.parseInt(input);
                if (level < 1 || level > 10) {
                    p.sendMessage("§6[Vanilla Core] §cInvalid level! Must be between 1-10.");
                    plugin.getMenuManager().openMainMenu(p);
                    return;
                }

                plugin.getConfigManager().get().set(configKey + ".max-level", level);
                plugin.getConfigManager().get().set(configKey + ".enabled", true);
                plugin.getConfigManager().get().set(configPath, true);
                plugin.getConfigManager().save();
                plugin.getConfigManager().load();

                p.sendMessage("§6[Vanilla Core] §a" + enchantType + " limited to level " + level);
                plugin.getMenuManager().openMainMenu(p);
            } catch (NumberFormatException e) {
                p.sendMessage("§6[Vanilla Core] §cInvalid number! Please enter a valid level.");
                plugin.getMenuManager().openMainMenu(p);
            }
        });
    }

    protected void refresh(Player player) {
        inventory.clear();
    }

}
