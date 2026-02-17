package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.gui.feature.OnePlayerSleepConfigGUI;
import com.tejaslamba.vanillacore.listener.OnePlayerSleepListener;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OnePlayerSleepFeature extends BaseFeature {

    private static final int DEFAULT_SLEEP_PERCENTAGE = 100;
    private OnePlayerSleepListener listener;

    @Override
    public void onEnable(Main plugin) {
        super.onEnable(plugin);
        listener = new OnePlayerSleepListener(plugin);
        applySleepPercentage();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] One Player Sleep - Feature loaded");
        }
    }

    @Override
    public void reload() {
        super.reload();
        applySleepPercentage();
    }

    private void applySleepPercentage() {
        int percentage = enabled ? 0 : DEFAULT_SLEEP_PERCENTAGE;

        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, percentage);

                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] One Player Sleep - Set playersSleepingPercentage to "
                            + percentage + " in " + world.getName());
                }
            }
        }
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
        return "One Player Sleep";
    }

    @Override
    public String getConfigPath() {
        return "features.one-player-sleep";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.RED_BED, "§9One Player Sleep",
                "§7Only one player needs to sleep");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7When enabled, only one player");
        lore.add("§7needs to sleep to skip the night");
        lore.add("");
        String sleepMsg = plugin.getConfigManager().get().getString("features.one-player-sleep.sleep-message", "");
        String skipMsg = plugin.getConfigManager().get().getString("features.one-player-sleep.skip-message", "");
        lore.add("§7Sleep Message: " + (sleepMsg.isEmpty() ? "§8None" : "§a✓"));
        lore.add("§7Skip Message: " + (skipMsg.isEmpty() ? "§8None" : "§a✓"));
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Configure");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
        applySleepPercentage();
    }

    @Override
    public void onRightClick(Player player) {
        OnePlayerSleepConfigGUI gui = new OnePlayerSleepConfigGUI(plugin, player);
        gui.open();
    }
}
