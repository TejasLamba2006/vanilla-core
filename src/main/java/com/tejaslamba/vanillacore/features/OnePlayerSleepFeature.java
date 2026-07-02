package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.OnePlayerSleepListener;
import net.kyori.adventure.text.Component;
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
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new OnePlayerSleepListener(plugin);
        super.onEnable(plugin);
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
        return createMenuItem(Material.RED_BED,
                plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.name"),
                plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.lore-2"));
        lore.add("");
        String sleepMsg = plugin.getConfigManager().get().getString("features.one-player-sleep.sleep-message", "");
        String skipMsg = plugin.getConfigManager().get().getString("features.one-player-sleep.skip-message", "");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.sleep-message")
                .replace("<state>", plugin.getMessageManager().getRaw(
                        sleepMsg.isEmpty() ? "feature-menus.shared.none" : "feature-menus.shared.enabled-check")));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.one-player-sleep.skip-message")
                .replace("<state>", plugin.getMessageManager().getRaw(
                        skipMsg.isEmpty() ? "feature-menus.shared.none" : "feature-menus.shared.enabled-check")));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
        applySleepPercentage();
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.title"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get(
                isEnabled() ? "one-player-sleep.info.status-enabled" : "one-player-sleep.info.status-disabled"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-1"));
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-2"));
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-3"));
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-4"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-5"));
        player.sendMessage(plugin.getMessageManager().get("one-player-sleep.info.line-6"));
    }
}

