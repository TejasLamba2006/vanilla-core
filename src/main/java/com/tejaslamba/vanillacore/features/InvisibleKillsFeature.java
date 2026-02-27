package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.InvisibleKillsListener;
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
        return createMenuItem(Material.POTION, "§5Invisible Kills",
                "§7Hide killer names when invisible");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7When an invisible player kills");
        lore.add("§7someone, the death message will");
        lore.add("§7hide the killer's name");
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Info");
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

        player.sendMessage("§6§l=== Invisible Kills ===");
        player.sendMessage("");
        player.sendMessage("§7Status: " + (isEnabled() ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("");
        player.sendMessage("§7When enabled, if an invisible player");
        player.sendMessage("§7kills someone, the death message will");
        player.sendMessage("§7be obfuscated to hide the killer's name.");
        player.sendMessage("");
        player.sendMessage("§6Death Message Format:");
        player.sendMessage("§7" + deathMessage.replace("{victim}", "PlayerName"));
        player.sendMessage("");
        player.sendMessage("§7Configure in §fconfig.yml");
    }
}
