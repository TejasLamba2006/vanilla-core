package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.InvisibleKillsListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InvisibleKillsFeature extends BaseFeature {

    private InvisibleKillsListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new InvisibleKillsListener(plugin);
        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Invisible Kills - Feature loaded");
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 31;
    }

    @Override
    public String getName() {
        return "Invisible Kills";
    }

    @Override
    public String getConfigPath() {
        return "features.invisible-kills";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.POTION,
                plugin.getMessageManager().getRaw("feature-menus.invisible-kills.name"),
                plugin.getMessageManager().getRaw("feature-menus.invisible-kills.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.invisible-kills.lore-1"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.invisible-kills.lore-2"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.invisible-kills.lore-3"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-info"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        String deathMessage = plugin.getConfigManager().get()
                .getString("features.invisible-kills.death-message", "{victim} was killed by <obf>?????????");

        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.title"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get(
                isEnabled() ? "invisible-kills.info.status-enabled" : "invisible-kills.info.status-disabled"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.line-1"));
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.line-2"));
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.line-3"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.format-title"));
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.format-value", "format",
                deathMessage.replace("{victim}", "PlayerName")));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getMessageManager().get("invisible-kills.info.config-hint"));
    }
}

