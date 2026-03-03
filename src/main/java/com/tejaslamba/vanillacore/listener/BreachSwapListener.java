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
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BreachSwapListener implements Listener {

    private final VanillaCorePlugin plugin;

    public BreachSwapListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        BreachSwapFeature feature = plugin.getFeatureManager().getFeature(BreachSwapFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        ItemStack oldItem = inv.getItem(event.getPreviousSlot());
        ItemStack newItem = inv.getItem(event.getNewSlot());

        if (isBreachMace(oldItem) && isSwordOrAxe(newItem) || isSwordOrAxe(oldItem) && isBreachMace(newItem)) {
            event.setCancelled(true);
            player.sendActionBar(MessageManager.parse(feature.getDeniedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        BreachSwapFeature feature = plugin.getFeatureManager().getFeature(BreachSwapFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();

        if (isBreachMace(main) && isSwordOrAxe(off) || isSwordOrAxe(main) && isBreachMace(off)) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(MessageManager.parse(feature.getDeniedMessage()));
        }
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
