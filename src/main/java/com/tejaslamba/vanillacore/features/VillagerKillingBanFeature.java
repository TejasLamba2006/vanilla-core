package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.VillagerKillingBanListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VillagerKillingBanFeature extends BaseFeature {

    private VillagerKillingBanListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new VillagerKillingBanListener(plugin);
        super.onEnable(plugin);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 19;
    }

    @Override
    public String getName() {
        return "Villager Killing Ban";
    }

    @Override
    public String getConfigPath() {
        return "features.villager-killing-ban";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.VILLAGER_SPAWN_EGG, "<!italic><red>Villager Killing Ban",
                "<!italic><gray>Prevent players from killing villagers");
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