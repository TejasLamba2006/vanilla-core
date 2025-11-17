package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.features.NetheriteDisablerFeature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class NetheriteDisablerListener implements Listener {

    private final Main plugin;

    public NetheriteDisablerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        NetheriteDisablerFeature feature = (NetheriteDisablerFeature) plugin.getFeatureManager().getFeatures().stream()
                .filter(f -> f instanceof NetheriteDisablerFeature && f.isEnabled())
                .findFirst()
                .orElse(null);

        if (feature == null) {
            return;
        }

        SmithingInventory inventory = event.getInventory();
        ItemStack result = event.getResult();

        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        Material resultType = result.getType();

        if (feature.isDisabled(resultType)) {
            Player player = (Player) event.getView().getPlayer();

            String permission = "smpcore.netherite.craft." +
                    resultType.name().toLowerCase().replace("netherite_", "");

            if (player.hasPermission(permission)) {
                boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
                if (verbose) {
                    plugin.getLogger().info("[VERBOSE] Netherite Disabler - " + player.getName()
                            + " bypassed with permission: " + permission);
                }
                return;
            }

            boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Netherite Disabler - Blocked " + player.getName() + " from crafting "
                        + resultType.name());
            }

            event.setResult(null);
        }
    }
}
