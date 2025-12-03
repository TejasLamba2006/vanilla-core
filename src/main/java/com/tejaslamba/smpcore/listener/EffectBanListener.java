package com.tejaslamba.smpcore.listener;

import com.tejaslamba.smpcore.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectBanListener implements Listener {

    private final Main plugin;

    public EffectBanListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!plugin.getConfigManager().get().getBoolean("bans.effects.enabled", false)) {
            return;
        }

        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null) {
            return;
        }

        if (plugin.getBanManager().isEffectBanned(newEffect.getType())) {
            event.setCancelled(true);
            String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
            player.sendMessage(prefix + " §cThis potion effect is banned!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().get().getBoolean("bans.effects.enabled", false)) {
            return;
        }

        ItemStack item = event.getItem();
        if (!item.hasItemMeta() || !(item.getItemMeta() instanceof PotionMeta potionMeta)) {
            return;
        }

        if (potionMeta.hasCustomEffects()) {
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                if (plugin.getBanManager().isEffectBanned(effect.getType())) {
                    event.setCancelled(true);
                    String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
                    event.getPlayer().sendMessage(prefix + " §cThis potion contains a banned effect!");
                    return;
                }
            }
        }

        if (potionMeta.getBasePotionType() != null) {
            for (PotionEffect effect : potionMeta.getBasePotionType().getPotionEffects()) {
                if (plugin.getBanManager().isEffectBanned(effect.getType())) {
                    event.setCancelled(true);
                    String prefix = plugin.getConfigManager().get().getString("plugin.prefix", "§8[§6SMP§8]§r");
                    event.getPlayer().sendMessage(prefix + " §cThis potion contains a banned effect!");
                    return;
                }
            }
        }
    }

}
