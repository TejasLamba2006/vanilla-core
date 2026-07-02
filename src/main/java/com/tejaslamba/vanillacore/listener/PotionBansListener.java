package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.PotionBansFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class PotionBansListener implements Listener {

    private final VanillaCorePlugin plugin;

    public PotionBansListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        PotionBansFeature feature = plugin.getFeatureManager().getFeature(PotionBansFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED
                && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) {
            return;
        }

        if (!isPotionRelatedCause(event.getCause())) {
            return;
        }

        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null) {
            return;
        }

        Player player = event.getEntity() instanceof Player p ? p : null;
        boolean tier2 = newEffect.getAmplifier() >= 1;

        if (feature.isAllPotionsBanned()) {
            event.setCancelled(true);
            sendMessage(player, "<red>[Vanilla Core] <gray>All potion effects are disabled on this server");
            return;
        }

        if (feature.isEffectBlocked(newEffect.getType(), tier2)) {
            event.setCancelled(true);
            sendMessage(player,
                    "<red>[Vanilla Core] <gray>This potion effect is disabled: <yellow>"
                            + newEffect.getType().getKey().getKey());
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        PotionBansFeature feature = plugin.getFeatureManager().getFeature(PotionBansFeature.class);
        if (feature == null || !feature.isEnabled() || !feature.areAllTier2Banned()) {
            return;
        }

        if (event.getView().getTopInventory().getType() != InventoryType.BREWING) {
            return;
        }

        if (isGlowstone(event.getCursor())) {
            event.setCancelled(true);
            sendMessage(player, "<red>[Vanilla Core] <gray>Tier 2 potion brewing is disabled");
            return;
        }

        if (event.isShiftClick() && isGlowstone(event.getCurrentItem())) {
            event.setCancelled(true);
            sendMessage(player, "<red>[Vanilla Core] <gray>Tier 2 potion brewing is disabled");
            return;
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND && isGlowstone(player.getInventory().getItemInOffHand())) {
            event.setCancelled(true);
            sendMessage(player, "<red>[Vanilla Core] <gray>Tier 2 potion brewing is disabled");
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton >= 0 && hotbarButton < 9) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                if (isGlowstone(hotbarItem)) {
                    event.setCancelled(true);
                    sendMessage(player, "<red>[Vanilla Core] <gray>Tier 2 potion brewing is disabled");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        PotionBansFeature feature = plugin.getFeatureManager().getFeature(PotionBansFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof PotionMeta potionMeta)) {
            return;
        }

        if (feature.isAllPotionsBanned()) {
            event.setCancelled(true);
            sendMessage(event.getPlayer(), "<red>[Vanilla Core] <gray>All potions are disabled on this server");
            return;
        }

        PotionType potionType = potionMeta.getBasePotionType();
        if (potionType != null && feature.isPotionTypeBlocked(potionType)) {
            event.setCancelled(true);
            sendMessage(event.getPlayer(), "<red>[Vanilla Core] <gray>This potion type is disabled on this server");
            return;
        }

        for (PotionEffect customEffect : potionMeta.getCustomEffects()) {
            boolean tier2 = customEffect.getAmplifier() >= 1;
            if (feature.isEffectBlocked(customEffect.getType(), tier2)) {
                event.setCancelled(true);
                sendMessage(event.getPlayer(),
                        "<red>[Vanilla Core] <gray>This potion effect is disabled on this server");
                return;
            }
        }
    }

    private boolean isPotionRelatedCause(EntityPotionEffectEvent.Cause cause) {
        if (cause == null) {
            return false;
        }

        String causeName = cause.name();
        return causeName.contains("POTION") || "AREA_EFFECT_CLOUD".equals(causeName) || "ARROW".equals(causeName);
    }

    private boolean isGlowstone(ItemStack item) {
        return item != null && item.getType() == Material.GLOWSTONE_DUST;
    }

    private void sendMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(MessageManager.parse(message));
        }
    }
}

