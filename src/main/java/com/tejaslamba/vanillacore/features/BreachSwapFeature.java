package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.BreachSwapListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

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
                "<red>Breach swapping is disabled.");
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
        return createMenuItem(Material.MACE, "<!italic><red>Breach Swap",
                "<!italic><gray>Prevent swapping between Breach mace and sword/axe");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Blocks hotbar/hand swaps between");
        lore.add("<gray>a Breach mace and a sword or axe.");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}
