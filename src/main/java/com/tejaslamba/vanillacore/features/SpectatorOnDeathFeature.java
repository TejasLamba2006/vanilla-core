package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.SpectatorOnDeathListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpectatorOnDeathFeature extends BaseFeature {

    private SpectatorOnDeathListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new SpectatorOnDeathListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 24;
    }

    @Override
    public String getName() {
        return "Spectator On Death";
    }

    @Override
    public String getConfigPath() {
        return "features.spectator-on-death";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.SKELETON_SKULL, "<!italic><gray>Spectator On Death",
                "<!italic><gray>Respawn players in spectator mode after death");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }
}
