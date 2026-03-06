package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.BreachSwapFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BreachSwapListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final Map<UUID, Long> recentBreachSwap = new HashMap<>();
    private static final long SWAP_WINDOW_MS = 500L;

    public BreachSwapListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        BreachSwapFeature feature = plugin.getFeatureManager().getFeature(BreachSwapFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (isBreachMace(oldItem) && isSwordOrAxe(newItem)) {
            recentBreachSwap.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        BreachSwapFeature feature = plugin.getFeatureManager().getFeature(BreachSwapFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (isBreachMace(event.getMainHandItem()) && isSwordOrAxe(event.getOffHandItem())) {
            recentBreachSwap.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        BreachSwapFeature feature = plugin.getFeatureManager().getFeature(BreachSwapFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!isSwordOrAxe(player.getInventory().getItemInMainHand())) {
            return;
        }

        Long lastSwapTime = recentBreachSwap.get(player.getUniqueId());
        if (lastSwapTime == null) {
            return;
        }

        if (System.currentTimeMillis() - lastSwapTime <= SWAP_WINDOW_MS) {
            event.setCancelled(true);
            player.sendActionBar(MessageManager.parse(feature.getDeniedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        recentBreachSwap.remove(event.getPlayer().getUniqueId());
    }

    private boolean isBreachMace(ItemStack stack) {
        if (stack == null || stack.getType() != Material.MACE) {
            return false;
        }
        return stack.getEnchantments().containsKey(Enchantment.BREACH)
                || (stack.getItemMeta() != null && stack.getItemMeta().hasEnchant(Enchantment.BREACH));
    }

    private boolean isSwordOrAxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        String name = stack.getType().name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
