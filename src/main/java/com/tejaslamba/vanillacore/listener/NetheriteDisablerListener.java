package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.NetheriteDisablerFeature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

public class NetheriteDisablerListener implements Listener {

    private final Main plugin;

    public NetheriteDisablerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack result = event.getResult();

        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        Material resultType = result.getType();

        if (!resultType.name().startsWith("NETHERITE_")) {
            return;
        }

        NetheriteDisablerFeature feature = plugin.getFeatureManager().getFeature(NetheriteDisablerFeature.class);

        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (feature.isDisabled(resultType)) {
            Player player = (Player) event.getView().getPlayer();

            String permission = "vanillacore.netherite.craft." +
                    resultType.name().toLowerCase().replace("netherite_", "");

            if (player.hasPermission(permission)) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Netherite Disabler - " + player.getName()
                            + " bypassed with permission: " + permission);
                }
                return;
            }

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Netherite Disabler - Blocked " + player.getName() + " from crafting "
                        + resultType.name());
            }

            event.setResult(null);
        }
    }
}
