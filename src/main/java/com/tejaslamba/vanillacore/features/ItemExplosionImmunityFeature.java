package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ItemExplosionImmunityListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemExplosionImmunityFeature extends BaseFeature {

    private ItemExplosionImmunityListener listener;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new ItemExplosionImmunityListener(plugin);
        super.onEnable(plugin);

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Item Explosion Immunity - Feature loaded");
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 32;
    }

    @Override
    public String getName() {
        return "Item Explosion Immunity";
    }

    @Override
    public String getConfigPath() {
        return "features.item-explosion-immunity";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.TNT, "<!italic><dark_purple>Item Explosion Immunity",
                "<!italic><gray>Protect dropped items from explosions");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Dropped items will not be");
        lore.add("<gray>destroyed by explosions");
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
        player.sendMessage(MessageManager.parse("<gold><bold>=== Item Explosion Immunity ==="));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>Status: " + (isEnabled() ? "<green>Enabled" : "<red>Disabled")));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>When enabled, dropped items on"));
        player.sendMessage(MessageManager.parse("<gray>the ground will be protected from"));
        player.sendMessage(MessageManager.parse("<gray>TNT, creepers, crystals, and all"));
        player.sendMessage(MessageManager.parse("<gray>other explosion damage."));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>Useful for preventing item loss"));
        player.sendMessage(MessageManager.parse("<gray>during PvP or griefing attempts."));
    }
}
