package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.MaceLimiterFeature;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class MaceLimiterListener implements Listener {

    private final Main plugin;

    public MaceLimiterListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack resultItem = event.getCurrentItem();
        if (resultItem == null || resultItem.getType() != Material.MACE) {
            return;
        }

        MaceLimiterFeature feature = (MaceLimiterFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof MaceLimiterFeature && f.isEnabled())
                .findFirst()
                .orElse(null);

        if (feature == null || feature.isMaceCrafted()) {
            return;
        }

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger()
                    .info("[VERBOSE] Mace Limiter - Player " + event.getWhoClicked().getName() + " is crafting a mace");
        }

        Player crafter = (Player) event.getWhoClicked();
        String crafterName = crafter.getName();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            ItemStack[] matrix = event.getInventory().getMatrix();
            for (ItemStack itemStack : matrix) {
                if (itemStack != null && !itemStack.getType().isAir()) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
            }
            event.getInventory().setMatrix(matrix);
            event.getWhoClicked().setItemOnCursor(resultItem.clone());
        }

        feature.setMaceCrafted(true);

        String broadcastMessage = plugin.getConfigManager().get()
                .getString("features.mace-limiter.broadcast-message",
                        "§6{player} §ehas crafted the only Mace on the server!");

        if (plugin.getConfigManager().get().getBoolean("features.mace-limiter.broadcast-enabled", true)) {
            String formattedMessage = broadcastMessage.replace("{player}", crafterName);
            Bukkit.broadcastMessage(formattedMessage);

            boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Broadcasted mace craft message for " + crafterName);
            }
        }

        Bukkit.getScheduler().runTask(plugin, feature::removeAllMaceRecipes);
    }
}
