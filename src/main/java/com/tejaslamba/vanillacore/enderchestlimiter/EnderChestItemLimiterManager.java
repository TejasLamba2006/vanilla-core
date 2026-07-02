package com.tejaslamba.vanillacore.enderchestlimiter;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.itemlimiter.ItemLimit;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnderChestItemLimiterManager {

    private static final EnumSet<Material> POTION_MATERIALS = EnumSet.of(
            Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);

    private final VanillaCorePlugin plugin;
    private final Map<String, ItemLimit> blockedItems = new ConcurrentHashMap<>();
    private final Map<Material, List<String>> materialIndex = new HashMap<>();
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN = 5000L;
    private boolean notifyPlayer = true;
    private String notifyMessage = "<red>[SMP Watchdog] <gray>You cannot put <yellow>{item}<gray> in an ender chest";

    public EnderChestItemLimiterManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        blockedItems.clear();
        materialIndex.clear();

        notifyPlayer = plugin.getConfigManager().get().getBoolean("features.ender-chest-item-limiter.notify-player",
                true);
        notifyMessage = plugin.getConfigManager().get().getString("features.ender-chest-item-limiter.notify-message",
                "<red>[SMP Watchdog] <gray>You cannot put <yellow>{item}<gray> in an ender chest");

        ConfigurationSection blockedSection = plugin.getConfigManager().get()
                .getConfigurationSection("features.ender-chest-item-limiter.blocked-items");

        if (blockedSection == null) {
            return;
        }

        for (String key : blockedSection.getKeys(false)) {
            ConfigurationSection itemSection = blockedSection.getConfigurationSection(key);
            if (itemSection != null) {
                loadComplexBlockedItem(key, itemSection);
            } else {
                loadSimpleBlockedItem(key);
            }
        }

        if (plugin.isVerbose()) {
            plugin.getLogger()
                    .info("[VERBOSE] EnderChestItemLimiter - Loaded " + blockedItems.size() + " blocked items");
        }
    }

    private void loadComplexBlockedItem(String key, ConfigurationSection itemSection) {
        String materialName = itemSection.getString("material", "").toUpperCase();
        Integer customModelData = itemSection.isSet("custom_model_data") ? itemSection.getInt("custom_model_data") : null;
        String displayName = itemSection.getString("display_name");
        String potionTypeString = itemSection.getString("potion_type");

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("[EnderChestItemLimiter] Invalid material for: " + key);
            return;
        }

        PotionType potionType = null;
        if (potionTypeString != null && POTION_MATERIALS.contains(material)) {
            try {
                potionType = PotionType.valueOf(potionTypeString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[EnderChestItemLimiter] Invalid potion type: " + potionTypeString);
            }
        }

        ItemLimit blocked = new ItemLimit(material, 0, customModelData, displayName, potionType);
        blockedItems.put(key, blocked);
        indexBlockedItem(key, material);
    }

    private void loadSimpleBlockedItem(String key) {
        String materialName = key.toUpperCase();
        Material material = Material.getMaterial(materialName);

        if (material == null) {
            plugin.getLogger().warning("[EnderChestItemLimiter] Unknown material: " + key);
            return;
        }

        ItemLimit blocked = new ItemLimit(material, 0, null, null);
        blockedItems.put(materialName, blocked);
        indexBlockedItem(materialName, material);
    }

    public void addBlockedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        String key = generateItemKey(item);
        ItemMeta meta = item.getItemMeta();
        Integer customModelData = (meta != null && meta.hasCustomModelData()) ? meta.getCustomModelData() : null;
        String displayName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : null;

        PotionType potionType = null;
        if (POTION_MATERIALS.contains(item.getType()) && meta instanceof PotionMeta potionMeta) {
            potionType = potionMeta.getBasePotionType();
            customModelData = null;
            displayName = null;
        }

        ItemLimit blocked = new ItemLimit(item.getType(), 0, customModelData, displayName, potionType);
        ItemLimit previous = blockedItems.put(key, blocked);
        if (previous != null) {
            unindexBlockedItem(key, previous.getMaterial());
        }
        indexBlockedItem(key, item.getType());
        saveBlockedItem(key, blocked);
    }

    private void saveBlockedItem(String key, ItemLimit blocked) {
        String basePath = "features.ender-chest-item-limiter.blocked-items." + key;
        plugin.getConfigManager().get().set(basePath, null);
        plugin.getConfigManager().get().set(basePath + ".material", blocked.getMaterial().name());

        if (blocked.getCustomModelData() != null) {
            plugin.getConfigManager().get().set(basePath + ".customModelData", blocked.getCustomModelData());
        }
        if (blocked.getDisplayName() != null) {
            plugin.getConfigManager().get().set(basePath + ".displayName", blocked.getDisplayName());
        }
        if (blocked.getPotionType() != null) {
            plugin.getConfigManager().get().set(basePath + ".potionType", blocked.getPotionType().toString());
        }

        plugin.getConfigManager().save();
    }

    public void removeBlockedItem(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }

        ItemLimit removed = blockedItems.remove(key);
        if (removed != null) {
            unindexBlockedItem(key, removed.getMaterial());
        }
        plugin.getConfigManager().get().set("features.ender-chest-item-limiter.blocked-items." + key, null);
        plugin.getConfigManager().save();
    }

    public String findMatchingItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        List<String> candidates = materialIndex.get(item.getType());
        if (candidates == null) {
            return null;
        }

        for (String key : candidates) {
            ItemLimit blocked = blockedItems.get(key);
            if (blocked != null && blocked.matches(item)) {
                return key;
            }
        }

        return null;
    }

    public boolean isBlocked(ItemStack item) {
        return findMatchingItemKey(item) != null;
    }

    public String generateItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "INVALID_ITEM";
        }

        ItemMeta meta = item.getItemMeta();
        StringBuilder key = new StringBuilder(item.getType().name());

        if (POTION_MATERIALS.contains(item.getType()) && meta instanceof PotionMeta potionMeta) {
            if (potionMeta.getBasePotionType() != null) {
                key.append("_").append(potionMeta.getBasePotionType().toString());
                return key.toString();
            }
        }

        if (meta != null) {
            if (meta.hasCustomModelData()) {
                key.append("_CMD_").append(meta.getCustomModelData());
            }
            if (meta.hasDisplayName()) {
                String cleanName = meta.getDisplayName()
                        .replaceAll("§.", "")
                        .replaceAll("[^a-zA-Z0-9_]", "_")
                        .replaceAll("_+", "_");
                key.append("_").append(cleanName);
            }
        }

        return key.toString();
    }

    public void sendBlockedMessage(Player player, ItemStack item) {
        if (!notifyPlayer) {
            return;
        }

        String message = notifyMessage.replace("{item}", formatItemName(item));
        sendCooldownMessage(player, message);
    }

    private String formatItemName(ItemStack item) {
        if (item == null) {
            return "Unknown Item";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName().replaceAll("§.", "");
        }

        String[] parts = item.getType().name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    public void sendCooldownMessage(Player player, String message) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(playerId);

        if (lastTime == null || currentTime - lastTime >= MESSAGE_COOLDOWN) {
            player.sendMessage(MessageManager.parse(message));
            lastMessageTime.put(playerId, currentTime);
        }
    }

    public Map<String, ItemLimit> getBlockedItems() {
        return new HashMap<>(blockedItems);
    }

    public ItemLimit getBlockedItem(String key) {
        return blockedItems.get(key);
    }

    public int getBlockedItemCount() {
        return blockedItems.size();
    }

    private void indexBlockedItem(String key, Material material) {
        materialIndex.computeIfAbsent(material, k -> new ArrayList<>()).add(key);
    }

    private void unindexBlockedItem(String key, Material material) {
        List<String> keys = materialIndex.get(material);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty()) {
                materialIndex.remove(material);
            }
        }
    }
}

