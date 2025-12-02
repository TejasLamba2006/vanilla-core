package com.tejaslamba.smpcore.manager;

import com.tejaslamba.smpcore.Main;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BanManager {

    private final Main plugin;
    private final Set<Material> bannedItems = new HashSet<>();
    private final Set<Enchantment> bannedEnchantments = new HashSet<>();
    private final Set<PotionEffectType> bannedEffects = new HashSet<>();

    public BanManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        bannedItems.clear();
        bannedEnchantments.clear();
        bannedEffects.clear();

        loadBannedItems();
        loadBannedEnchantments();
        loadBannedEffects();
    }

    private void loadBannedItems() {
        bannedItems.clear();

        if (plugin.getConfigManager().get().getBoolean("features.ban-mace.enabled", false)) {
            bannedItems.add(Material.MACE);
        }
        if (plugin.getConfigManager().get().getBoolean("features.ban-anchors.enabled", false)) {
            bannedItems.add(Material.RESPAWN_ANCHOR);
        }
        if (plugin.getConfigManager().get().getBoolean("features.ban-crystals.enabled", false)) {
            bannedItems.add(Material.END_CRYSTAL);
        }
        if (plugin.getConfigManager().get().getBoolean("features.ban-pearls.enabled", false)) {
            bannedItems.add(Material.ENDER_PEARL);
        }
        if (plugin.getConfigManager().get().getBoolean("features.ban-netherite.enabled", false)) {
            bannedItems.add(Material.NETHERITE_HELMET);
            bannedItems.add(Material.NETHERITE_CHESTPLATE);
            bannedItems.add(Material.NETHERITE_LEGGINGS);
            bannedItems.add(Material.NETHERITE_BOOTS);
            bannedItems.add(Material.NETHERITE_SWORD);
            bannedItems.add(Material.NETHERITE_AXE);
        }
    }

    private void loadBannedEnchantments() {
        if (!plugin.getConfigManager().get().getBoolean("bans.enchantments.enabled", false)) {
            return;
        }

        List<String> enchantList = plugin.getConfigManager().get().getStringList("bans.enchantments.list");
        for (String enchantName : enchantList) {
            try {
                Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
                if (enchantment != null) {
                    bannedEnchantments.add(enchantment);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid banned enchantment: " + enchantName);
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

    public boolean isEnchantmentBanned(Enchantment enchantment) {
        return bannedEnchantments.contains(enchantment);
    }

    public boolean isEffectBanned(PotionEffectType effect) {
        return bannedEffects.contains(effect);
    }

    public Set<Material> getBannedItems() {
        return Collections.unmodifiableSet(bannedItems);
    }

    public Set<Enchantment> getBannedEnchantments() {
        return Collections.unmodifiableSet(bannedEnchantments);
    }

    public Set<PotionEffectType> getBannedEffects() {
        return Collections.unmodifiableSet(bannedEffects);
    }

}
