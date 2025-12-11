package com.tejaslamba.smpcore.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public final class EnchantmentUtils {

    private EnchantmentUtils() {
        // Utility class - no instantiation
    }

    public static Enchantment parseEnchantment(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String normalized = input.toLowerCase().replace(" ", "_");

        if (normalized.contains(":")) {
            String[] parts = normalized.split(":");
            normalized = parts[parts.length - 1];
        }

        try {
            return Enchantment.getByKey(NamespacedKey.minecraft(normalized));
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<Enchantment, Integer> getAllEnchantments(ItemStack item) {
        Map<Enchantment, Integer> enchants = new HashMap<>();

        if (item == null || !item.hasItemMeta()) {
            return enchants;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            enchants.putAll(storageMeta.getStoredEnchants());
        } else {
            enchants.putAll(item.getEnchantments());
        }

        return enchants;
    }

    public static void setEnchantments(ItemStack item, Map<Enchantment, Integer> enchants) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.getStoredEnchants().keySet().forEach(storageMeta::removeStoredEnchant);

            enchants.forEach((ench, level) -> {
                if (level > 0) {
                    storageMeta.addStoredEnchant(ench, level, true);
                }
            });

            item.setItemMeta(storageMeta);
        } else {
            meta.getEnchants().keySet().forEach(meta::removeEnchant);

            enchants.forEach((ench, level) -> {
                if (level > 0) {
                    meta.addEnchant(ench, level, true);
                }
            });

            item.setItemMeta(meta);
        }
    }

    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchantment);
        } else {
            item.removeEnchantment(enchantment);
        }

        item.setItemMeta(meta);
    }

    public static boolean isProtectionEnchantment(Enchantment enchantment) {
        return enchantment == Enchantment.PROTECTION ||
                enchantment == Enchantment.FIRE_PROTECTION ||
                enchantment == Enchantment.BLAST_PROTECTION ||
                enchantment == Enchantment.PROJECTILE_PROTECTION ||
                enchantment == Enchantment.FEATHER_FALLING;
    }

    public static Enchantment getHighestProtection(Map<Enchantment, Integer> enchants) {
        Enchantment highest = null;
        int highestLevel = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (isProtectionEnchantment(entry.getKey()) && entry.getValue() > highestLevel) {
                highest = entry.getKey();
                highestLevel = entry.getValue();
            }
        }

        return highest;
    }
}
