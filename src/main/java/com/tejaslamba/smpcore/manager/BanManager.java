package com.tejaslamba.smpcore.manager;

import com.tejaslamba.smpcore.Main;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
public class BanManager {

    private final Main plugin;
    private final Set<Material> bannedItems = new HashSet<>();
    private final Set<PotionEffectType> bannedEffects = new HashSet<>();

    public BanManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        bannedItems.clear();
        bannedEffects.clear();

        loadBannedItems();
        loadBannedEffects();
    }

    private void loadBannedItems() {
        if (!plugin.getConfigManager().get().getBoolean("bans.items.enabled", false)) {
            return;
        }

        List<String> itemList = plugin.getConfigManager().get().getStringList("bans.items.list");
        for (String itemName : itemList) {
            try {
                Material material = Material.matchMaterial(itemName.toUpperCase());
                if (material != null) {
                    bannedItems.add(material);
                } else {
                    plugin.getLogger().log(Level.WARNING, "Invalid banned item: {0}", itemName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Invalid banned item: {0}", itemName);
            }
        }
    }

    private void loadBannedEffects() {
        if (!plugin.getConfigManager().get().getBoolean("bans.effects.enabled", false)) {
            return;
        }

        List<String> effectList = plugin.getConfigManager().get().getStringList("bans.effects.list");
        for (String effectName : effectList) {
            try {
                PotionEffectType effect = PotionEffectType.getByName(effectName.toUpperCase());
                if (effect != null) {
                    bannedEffects.add(effect);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid banned effect: " + effectName);
            }
        }
    }

    public boolean isItemBanned(Material material) {
        return bannedItems.contains(material);
    }

    public boolean isEffectBanned(PotionEffectType effect) {
        return bannedEffects.contains(effect);
    }

    public Set<Material> getBannedItems() {
        return Collections.unmodifiableSet(bannedItems);
    }

    public Set<PotionEffectType> getBannedEffects() {
        return Collections.unmodifiableSet(bannedEffects);
    }

}
