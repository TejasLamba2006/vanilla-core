package com.tejaslamba.vanillacore.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
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

    protected final VanillaCorePlugin plugin;
    protected final Inventory inventory;

    public BaseMenu(VanillaCorePlugin plugin, Inventory inventory) {
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
            meta.displayName(MessageManager.parse("<!italic>" + name));
            List<Component> lore = new ArrayList<>();
            for (String desc : description) {
                lore.add(MessageManager.parse("<!italic>" + desc));
            }
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><dark_gray>Config: <gray>features." + configPath + ".enabled"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse(enabled ? "<!italic><green>Enabled" : "<!italic><red>Disabled"));

            if (configPath.equals("protection-limiter") || configPath.equals("sharpness-limiter")) {
                String configKey = configPath.equals("protection-limiter") ? "enchantment-limits.protection"
                        : "enchantment-limits.sharpness";
                int maxLevel = plugin.getConfigManager().get().getInt(configKey + ".max-level", 4);
                lore.add(MessageManager.parse("<!italic><gray>Max Level: <yellow>" + maxLevel));
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<!italic><yellow>Left Click: Toggle On/Off"));
                lore.add(MessageManager.parse("<!italic><yellow>Right Click: Set Level"));
            } else {
                lore.add(MessageManager.parse("<!italic><yellow>Click to toggle"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic>" + name));
            meta.lore(Arrays.stream(lore).map(s -> MessageManager.parse("<!italic>" + s)).toList());
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
        player.sendMessage(MessageManager.parse(
                "<gold>[Vanilla Core] <gray>Toggled " + displayName + " <gray>to "
                        + (!current ? "<green>Enabled" : "<red>Disabled")));
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
            player.sendMessage(MessageManager.parse(
                    "<gold>[Vanilla Core] <gray>Toggled " + enchantType + " Limiter <gray>to "
                            + (!current ? "<green>Enabled" : "<red>Disabled")));
            return;
        }

        player.closeInventory();
        player.sendMessage(MessageManager
                .parse("<gold>[Vanilla Core] <gray>Enter the max level for " + enchantType + " (1-10, or 'cancel'):"));

        VanillaCorePlugin.getInstance().getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage(MessageManager.parse("<gold>[Vanilla Core] <red>Cancelled."));
                plugin.getMenuManager().openMainMenu(p);
                return;
            }

            try {
                int level = Integer.parseInt(input);
                if (level < 1 || level > 10) {
                    p.sendMessage(
                            MessageManager.parse("<gold>[Vanilla Core] <red>Invalid level! Must be between 1-10."));
                    plugin.getMenuManager().openMainMenu(p);
                    return;
                }

                plugin.getConfigManager().get().set(configKey + ".max-level", level);
                plugin.getConfigManager().get().set(configKey + ".enabled", true);
                plugin.getConfigManager().get().set(configPath, true);
                plugin.getConfigManager().save();
                plugin.getConfigManager().load();

                p.sendMessage(MessageManager
                        .parse("<gold>[Vanilla Core] <green>" + enchantType + " limited to level " + level));
                plugin.getMenuManager().openMainMenu(p);
            } catch (NumberFormatException e) {
                p.sendMessage(
                        MessageManager.parse("<gold>[Vanilla Core] <red>Invalid number! Please enter a valid level."));
                plugin.getMenuManager().openMainMenu(p);
            }
        });
    }

    protected void refresh(Player player) {
        inventory.clear();
    }

}
