package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.InvisibleKillsListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
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
        return createMenuItem(Material.POTION, "<!italic><dark_purple>Invisible Kills",
                "<!italic><gray>Hide killer names when invisible");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>When an invisible player kills");
        lore.add("<gray>someone, the death message will");
        lore.add("<gray>hide the killer's name");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Info");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        String deathMessage = plugin.getConfigManager().get()
                .getString("features.invisible-kills.death-message", "{victim} was killed by §k?????????");

        player.sendMessage(MessageManager.parse("<gold><bold>=== Invisible Kills ==="));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>Status: " + (isEnabled() ? "<green>Enabled" : "<red>Disabled")));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>When enabled, if an invisible player"));
        player.sendMessage(MessageManager.parse("<gray>kills someone, the death message will"));
        player.sendMessage(MessageManager.parse("<gray>be obfuscated to hide the killer's name."));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gold>Death Message Format:"));
        player.sendMessage(MessageManager.parse("<gray>" + deathMessage.replace("{victim}", "PlayerName")));
        player.sendMessage(Component.empty());
        player.sendMessage(MessageManager.parse("<gray>Configure in <white>config.yml"));
    }
}
