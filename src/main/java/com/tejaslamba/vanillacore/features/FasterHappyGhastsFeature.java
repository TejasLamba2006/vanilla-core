package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.FasterHappyGhastsListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
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
        return createMenuItem(Material.GHAST_TEAR, "<!italic><aqua>Faster Happy Ghasts",
                "<!italic><gray>Increase flying speed of spawned happy ghasts");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Flying Speed: <yellow>" + flyingSpeed);
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        player.sendMessage(MessageManager.parse("<gold><bold>=== Faster Happy Ghasts ==="));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>Status: " + (isEnabled() ? "<green>Enabled" : "<red>Disabled")));
        player.sendMessage(MessageManager.parse("<gray>Configured Flying Speed: <yellow>" + flyingSpeed));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>When enabled, newly spawned"));
        player.sendMessage(MessageManager.parse("<gray>happy ghasts get boosted flying speed."));
    }
}
