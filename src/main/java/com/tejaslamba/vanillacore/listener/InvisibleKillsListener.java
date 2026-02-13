package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.features.InvisibleKillsFeature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

public class InvisibleKillsListener implements Listener {

    private final Main plugin;

    public InvisibleKillsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            return;
        }

        InvisibleKillsFeature feature = plugin.getFeatureManager().getFeature(InvisibleKillsFeature.class);

        if (feature == null || !feature.isEnabled()) {
            return;
        }

        boolean isKillerInvisible = killer.isInvisible() || killer.hasPotionEffect(PotionEffectType.INVISIBILITY);

        if (!isKillerInvisible) {
            return;
        }

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Invisible Kills - " + killer.getName()
                    + " killed " + victim.getName() + " while invisible, hiding killer name");
        }

        String deathMessage = plugin.getConfigManager().get()
                .getString("features.invisible-kills.death-message", "{victim} was killed by §k?????????");

        String formattedMessage = deathMessage.replace("{victim}", victim.getName());

        event.setDeathMessage(formattedMessage);
    }
}
