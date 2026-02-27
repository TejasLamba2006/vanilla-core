package com.tejaslamba.vanillacore.feature;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Feature {

    String getName();

    String getConfigPath();

    boolean isEnabled();

    void onEnable(VanillaCorePlugin plugin);

    void onDisable();

    void reload();

    Listener getListener();

    ItemStack getMenuItem();

    void onLeftClick(Player player);

    void onRightClick(Player player);

    List<String> getMenuLore();

    default int getDisplayOrder() {
        return 100;
    }
}
