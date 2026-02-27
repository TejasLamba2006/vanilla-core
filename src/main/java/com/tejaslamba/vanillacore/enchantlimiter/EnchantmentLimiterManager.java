package com.tejaslamba.vanillacore.enchantlimiter;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentLimiterManager {

    private final VanillaCorePlugin plugin;
    private final Map<Enchantment, Integer> enchantmentLimits = new ConcurrentHashMap<>();
    private final Map<String, Enchantment> enchantmentCache = new ConcurrentHashMap<>();
    private final Map<String, String> enchantmentNameMap = new HashMap<>();

    public EnchantmentLimiterManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
        initEnchantmentNameMap();
    }

    private void initEnchantmentNameMap() {
        enchantmentNameMap.put("PROTECTION", "PROTECTION_ENVIRONMENTAL");
        enchantmentNameMap.put("FIRE_PROTECTION", "PROTECTION_FIRE");
        enchantmentNameMap.put("FEATHER_FALLING", "PROTECTION_FALL");
        enchantmentNameMap.put("BLAST_PROTECTION", "PROTECTION_EXPLOSIONS");
        enchantmentNameMap.put("PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE");
        enchantmentNameMap.put("RESPIRATION", "OXYGEN");
        enchantmentNameMap.put("AQUA_AFFINITY", "WATER_WORKER");
        enchantmentNameMap.put("SHARPNESS", "DAMAGE_ALL");
        enchantmentNameMap.put("SMITE", "DAMAGE_UNDEAD");
        enchantmentNameMap.put("BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS");
        enchantmentNameMap.put("LOOTING", "LOOT_BONUS_MOBS");
        enchantmentNameMap.put("EFFICIENCY", "DIG_SPEED");
        enchantmentNameMap.put("UNBREAKING", "DURABILITY");
        enchantmentNameMap.put("FORTUNE", "LOOT_BONUS_BLOCKS");
        enchantmentNameMap.put("POWER", "ARROW_DAMAGE");
        enchantmentNameMap.put("PUNCH", "ARROW_KNOCKBACK");
        enchantmentNameMap.put("FLAME", "ARROW_FIRE");
        enchantmentNameMap.put("INFINITY", "ARROW_INFINITE");
        enchantmentNameMap.put("LUCK_OF_THE_SEA", "LUCK");

        Map<String, String> reverseMap = new HashMap<>(enchantmentNameMap);
        reverseMap.forEach((key, value) -> enchantmentNameMap.put(value, key));
    }

    public void load() {
        enchantmentLimits.clear();

        ConfigurationSection limitsSection = plugin.getConfigManager().get()
                .getConfigurationSection("features.enchantment-limiter.limits");

        if (limitsSection == null) {
            if (plugin.isVerbose()) {
                plugin.getLogger().warning("[VERBOSE] No enchantment limits found in config!");
            }
            return;
        }

        for (String enchantName : limitsSection.getKeys(false)) {
            try {
                Enchantment enchant = getEnchantmentByName(enchantName);
                int maxLevel = limitsSection.getInt(enchantName);

                if (enchant != null) {
                    enchantmentLimits.put(enchant, maxLevel);
                    if (plugin.isVerbose()) {
                        plugin.getLogger().info("[VERBOSE] Loaded limit for " + enchantName + ": " + maxLevel);
                    }
                } else {
                    plugin.getLogger().warning("[EnchantLimiter] Unknown enchantment: " + enchantName);
                }
            } catch (Exception e) {
                plugin.getLogger()
                        .warning("[EnchantLimiter] Error loading enchantment " + enchantName + ": " + e.getMessage());
            }
        }

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] EnchantmentLimiter loaded " + enchantmentLimits.size() + " limits");
        }
    }

    @SuppressWarnings("deprecation")
    private Enchantment findEnchantmentByExactName(String name) {
        for (Enchantment enchant : Enchantment.values()) {
            if (enchant.getKey().getKey().equalsIgnoreCase(name) ||
                    enchant.getName().equalsIgnoreCase(name)) {
                return enchant;
            }
        }

        try {
            return Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
        } catch (Exception e) {
            return Enchantment.getByName(name);
        }
    }

    public Enchantment getEnchantmentByName(String name) {
        String upperName = name.toUpperCase();

        if (enchantmentCache.containsKey(upperName)) {
            return enchantmentCache.get(upperName);
        }

        Enchantment enchant = findEnchantmentByExactName(upperName);

        if (enchant == null && enchantmentNameMap.containsKey(upperName)) {
            String alternativeName = enchantmentNameMap.get(upperName);
            enchant = findEnchantmentByExactName(alternativeName);
        }

        if (enchant == null) {
            enchant = findEnchantmentByExactName(name.toLowerCase().replace("_", ""));
        }

        enchantmentCache.put(upperName, enchant);
        return enchant;
    }

    public boolean checkItemEnchantments(HumanEntity human, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        if (human != null && !(human instanceof Player)) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        boolean modified = false;

        if (!item.getEnchantments().isEmpty()) {
            Map<Enchantment, Integer> enchants = new HashMap<>(item.getEnchantments());
            boolean limited = checkEnchantments(human, enchants);

            if (limited) {
                itemMeta = item.getItemMeta();
                for (Enchantment enchant : item.getEnchantments().keySet()) {
                    itemMeta.removeEnchant(enchant);
                }
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    if (entry.getValue() > 0) {
                        itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                }
                item.setItemMeta(itemMeta);
                modified = true;
            }
        }

        if (itemMeta instanceof EnchantmentStorageMeta bookMeta) {
            if (!bookMeta.getStoredEnchants().isEmpty()) {
                Map<Enchantment, Integer> enchants = new HashMap<>(bookMeta.getStoredEnchants());
                boolean limited = checkEnchantments(human, enchants);

                if (limited) {
                    for (Enchantment enchant : bookMeta.getStoredEnchants().keySet()) {
                        bookMeta.removeStoredEnchant(enchant);
                    }
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        if (entry.getValue() > 0) {
                            bookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                        }
                    }
                    item.setItemMeta(bookMeta);
                    modified = true;
                }
            }
        }

        return modified;
    }

    public boolean checkEnchantments(HumanEntity human, Map<Enchantment, Integer> enchantsToAdd) {
        Collection<Enchantment> keys = new HashSet<>(enchantsToAdd.keySet());
        boolean modified = false;

        for (Enchantment enchant : keys) {
            if (enchantmentLimits.containsKey(enchant)) {
                int limit = enchantmentLimits.get(enchant);
                int level = enchantsToAdd.get(enchant);

                if (limit == 0) {
                    enchantsToAdd.remove(enchant);
                    if (human != null) {
                        sendLimitMessage(human, enchant, limit);
                    }
                    modified = true;
                } else if (level > limit) {
                    enchantsToAdd.put(enchant, limit);
                    if (human != null) {
                        sendLimitMessage(human, enchant, limit);
                    }
                    modified = true;
                }
            }
        }

        return modified;
    }

    public void sendLimitMessage(HumanEntity human, Enchantment enchant, int maxLevel) {
        String enchantName = getEnchantDisplayName(enchant);

        if (maxLevel == 0) {
            plugin.getMessageManager().sendPrefixed(human, "enchantment-limiter.banned",
                    "{enchant}", enchantName);
        } else {
            plugin.getMessageManager().sendPrefixed(human, "enchantment-limiter.limited",
                    "{enchant}", enchantName, "{limit}", String.valueOf(maxLevel));
        }
    }

    public String getEnchantDisplayName(Enchantment enchant) {
        String key = enchant.getKey().getKey();
        return key.substring(0, 1).toUpperCase() + key.substring(1).replace("_", " ");
    }

    public int getLimit(Enchantment enchant) {
        return enchantmentLimits.getOrDefault(enchant, -1);
    }

    public Map<Enchantment, Integer> getEnchantmentLimits() {
        return new HashMap<>(enchantmentLimits);
    }

    public int getLimitsCount() {
        return enchantmentLimits.size();
    }

    public void setLimit(String enchantName, int limit) {
        Enchantment enchant = getEnchantmentByName(enchantName);
        if (enchant != null) {
            if (limit < 0) {
                enchantmentLimits.remove(enchant);
                plugin.getConfigManager().get().set("features.enchantment-limiter.limits." + enchantName.toLowerCase(),
                        null);
            } else {
                enchantmentLimits.put(enchant, limit);
                plugin.getConfigManager().get().set("features.enchantment-limiter.limits." + enchantName.toLowerCase(),
                        limit);
            }
            plugin.getConfigManager().save();
        }
    }

    public void shutdown() {
        enchantmentLimits.clear();
        enchantmentCache.clear();
    }
}
