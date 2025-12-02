package com.tejaslamba.smpcore.features;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.feature.BaseFeature;
import com.tejaslamba.smpcore.listener.ItemLimiterListener;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLimiterFeature extends BaseFeature {

    private ItemLimiterListener listener;
    private final Map<Material, Integer> itemLimits = new HashMap<>();
    private String checkMethod;

    @Override
    public void onEnable(Main plugin) {
        listener = new ItemLimiterListener(plugin);
        super.onEnable(plugin);
        loadItemLimits();

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger()
                    .info("[VERBOSE] Item Limiter - Feature loaded with " + itemLimits.size() + " item limits");
        }
    }

    @Override
    public void reload() {
        super.reload();
        loadItemLimits();
    }

    private void loadItemLimits() {
        itemLimits.clear();
        checkMethod = plugin.getConfigManager().get().getString(getConfigPath() + ".check-method", "on-hit");

        ConfigurationSection limitsSection = plugin.getConfigManager().get()
                .getConfigurationSection(getConfigPath() + ".limits");
        if (limitsSection == null) {
            return;
        }

        for (String key : limitsSection.getKeys(false)) {
            Material material = Material.matchMaterial(key.toUpperCase());
            if (material != null) {
                int limit = limitsSection.getInt(key);
                itemLimits.put(material, limit);
            } else {
                plugin.getLogger().warning("[Item Limiter] Unknown material: " + key);
            }
        }
    }

    public Map<Material, Integer> getItemLimits() {
        return Collections.unmodifiableMap(itemLimits);
    }

    public int getLimit(Material material) {
        return itemLimits.getOrDefault(material, -1);
    }

    public boolean hasLimit(Material material) {
        return itemLimits.containsKey(material);
    }

    public String getCheckMethod() {
        return checkMethod;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public String getName() {
        return "Item Limiter";
    }

    @Override
    public String getConfigPath() {
        return "features.item-limiter";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.HOPPER, "§cItem Limiter",
                "§7Limits item quantities in inventories");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7Limits how many of certain items");
        lore.add("§7players can carry at once");
        lore.add("");
        lore.add("§7Check Method: §e" + checkMethod);
        lore.add("§7Limited Items: §e" + itemLimits.size());
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: View Limits");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage("§6§l=== Item Limiter ===");
        player.sendMessage("");
        player.sendMessage("§7Status: " + (isEnabled() ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7Check Method: §e" + checkMethod);
        player.sendMessage("");
        player.sendMessage("§7Current Limits:");

        if (itemLimits.isEmpty()) {
            player.sendMessage("§c  No limits configured");
        } else {
            for (Map.Entry<Material, Integer> entry : itemLimits.entrySet()) {
                String name = entry.getKey().name().toLowerCase().replace("_", " ");
                player.sendMessage("§a• §7" + name + ": §e" + entry.getValue());
            }
        }

        player.sendMessage("");
        player.sendMessage("§7Excess items are dropped with");
        player.sendMessage("§7a 2 second pickup delay.");
    }
}
