package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.FasterHappyGhastsListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FasterHappyGhastsFeature extends BaseFeature {

    private FasterHappyGhastsListener listener;
    private double flyingSpeed = 0.10D;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new FasterHappyGhastsListener(plugin);
        super.onEnable(plugin);
        loadSettings();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Faster Happy Ghasts - Feature loaded");
        }
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        flyingSpeed = plugin.getConfigManager().get().getDouble(getConfigPath() + ".flying-speed", 0.10D);
    }

    public double getFlyingSpeed() {
        return flyingSpeed;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 34;
    }

    @Override
    public String getName() {
        return "Faster Happy Ghasts";
    }

    @Override
    public String getConfigPath() {
        return "features.faster-happy-ghasts";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.GHAST_TEAR,
                plugin.getMessageManager().getRaw("feature-menus.faster-happy-ghasts.name"),
                plugin.getMessageManager().getRaw("feature-menus.faster-happy-ghasts.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.faster-happy-ghasts.speed").replace("<speed>",
                String.valueOf(flyingSpeed)));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage(plugin.getMessageManager().get("faster-happy-ghasts.info.title"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get(
                isEnabled() ? "faster-happy-ghasts.info.status-enabled" : "faster-happy-ghasts.info.status-disabled"));
        player.sendMessage(
                plugin.getMessageManager().get("faster-happy-ghasts.info.flying-speed", "speed", flyingSpeed));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("faster-happy-ghasts.info.line-1"));
        player.sendMessage(plugin.getMessageManager().get("faster-happy-ghasts.info.line-2"));
    }
}

