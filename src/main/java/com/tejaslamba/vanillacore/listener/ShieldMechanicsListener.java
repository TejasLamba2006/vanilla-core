package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.ShieldMechanicsFeature;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ShieldMechanicsListener implements Listener {

    private final Main plugin;

    public ShieldMechanicsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        ShieldMechanicsFeature feature = plugin.getFeatureManager().getFeature(ShieldMechanicsFeature.class);
        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!victim.isBlocking()) {
            return;
        }

        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weapon = attacker.getEquipment() != null ? attacker.getEquipment().getItemInMainHand() : null;
        if (weapon == null || weapon.getType().isAir()) {
            return;
        }

        Material type = weapon.getType();

        if (feature.isMaceStunEnabled() && type == Material.MACE) {
            victim.setCooldown(Material.SHIELD, feature.getMaceStunDurationTicks());
            return;
        }

        if (feature.isAxeStunEnabled() && isAxe(type)) {
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> victim.setCooldown(Material.SHIELD, feature.getAxeStunDurationTicks()), 1L);
        }
    }

    private boolean isAxe(Material material) {
        return switch (material) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }
}
