package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.BreachSwapListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BreachSwapFeature extends BaseFeature {

    private BreachSwapListener listener;
    private String deniedMessage;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new BreachSwapListener(plugin);
        super.onEnable(plugin);
        loadSettings();
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        deniedMessage = plugin.getConfigManager().get().getString(
                getConfigPath() + ".denied-message",
                plugin.getMessageManager().getRaw("breach-swap.denied-default"));
    }

    public String getDeniedMessage() {
        return deniedMessage;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 15;
    }

    @Override
    public String getName() {
        return "Breach Swap";
    }

    @Override
    public String getConfigPath() {
        return "features.breach-swap";
    }

    @Override
    public ItemStack getMenuItem() {
        ItemStack item = createMenuItem(Material.MACE,
                plugin.getMessageManager().getRaw("feature-menus.breach-swap.name"),
                plugin.getMessageManager().getRaw("feature-menus.breach-swap.description"));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.breach-swap.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.breach-swap.lore-2"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}

