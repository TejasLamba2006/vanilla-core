package com.tejaslamba.vanillacore.itemlimiter;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.MessageManager;
import org.bukkit.Bukkit;
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

public class ItemLimiterManager {

    private final VanillaCorePlugin plugin;
    private final Map<String, ItemLimit> itemLimits = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Map<Material, List<String>> materialIndex = new HashMap<>();
    private static final long MESSAGE_COOLDOWN = 5000L;
    private int taskId = -1;
    private int playerCheckIndex = 0;
    private boolean notifyPlayer = true;
    private String notifyMessage = "<red>[Vanilla Core] <gray>Excess items removed: <yellow>{item} x{amount} <gray>(limit: {limit})";
    private boolean dropExcess = true;

    public ItemLimiterManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        itemLimits.clear();
        materialIndex.clear();

        notifyPlayer = plugin.getConfigManager().get().getBoolean("features.item-limiter.notify-player", true);
        notifyMessage = plugin.getConfigManager().get().getString("features.item-limiter.notify-message",
                "<red>[Vanilla Core] <gray>Excess items removed: <yellow>{item} x{amount} <gray>(limit: {limit})");
        dropExcess = plugin.getConfigManager().get().getBoolean("features.item-limiter.drop-excess", true);

        ConfigurationSection limitsSection = plugin.getConfigManager().get()
                .getConfigurationSection("features.item-limiter.limits");

        if (limitsSection == null) {
            return;
        }

        for (String key : limitsSection.getKeys(false)) {
            ConfigurationSection itemSection = limitsSection.getConfigurationSection(key);
            if (itemSection != null) {
                loadComplexLimit(key, itemSection);
            } else {
                loadSimpleLimit(key, limitsSection.getInt(key, 0));
            }
        }

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] ItemLimiter - Loaded " + itemLimits.size() + " item limits");
        }
    }

    private void loadComplexLimit(String key, ConfigurationSection itemSection) {
        String materialName = itemSection.getString("material", "").toUpperCase();
        int limit = itemSection.getInt("limit", 0);
        Integer customModelData = itemSection.isSet("customModelData") ? itemSection.getInt("customModelData") : null;
        String displayName = itemSection.getString("displayName");
        String potionTypeString = itemSection.getString("potionType");

        Material material = Material.getMaterial(materialName);
        if (material == null || limit < 0) {
            plugin.getLogger().warning("[ItemLimiter] Invalid material or limit for: " + key);
            return;
        }

        PotionType potionType = null;
        if (potionTypeString != null && ItemLimit.POTION_MATERIALS.contains(material)) {
            try {
                potionType = PotionType.valueOf(potionTypeString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[ItemLimiter] Invalid potion type: " + potionTypeString);
            }
        }

        ItemLimit itemLimit = new ItemLimit(material, limit, customModelData, displayName, potionType);
        itemLimits.put(key, itemLimit);
        indexLimit(key, material);
    }

    private void loadSimpleLimit(String key, int limit) {
        String materialName = key.toUpperCase();
        Material material = Material.getMaterial(materialName);

        if (material == null || limit < 0) {
            plugin.getLogger().warning("[ItemLimiter] Unknown material: " + key);
            return;
        }

        ItemLimit itemLimit = new ItemLimit(material, limit, null, null);
        itemLimits.put(materialName, itemLimit);
        indexLimit(materialName, material);
    }

    public void startChecker() {
        if (taskId != -1) {
            stopChecker();
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty())
                return;
            if (playerCheckIndex >= players.size())
                playerCheckIndex = 0;
            Player player = players.get(playerCheckIndex++);
            if (!player.hasPermission("vanillacore.itemlimiter.bypass")) {
                checkAndEnforceLimits(player);
            }
        }, 0L, 1L).getTaskId();
    }

    public void stopChecker() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void shutdown() {
        stopChecker();
        itemLimits.clear();
        lastMessageTime.clear();
    }

    public void addItemLimit(ItemStack item, int limit) {
        if (item == null || item.getType() == Material.AIR || limit < 0) {
            return;
        }

        String key = generateItemKey(item);
        ItemMeta meta = item.getItemMeta();
        Integer customModelData = (meta != null && meta.hasCustomModelData()) ? meta.getCustomModelData() : null;
        String displayName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : null;

        PotionType potionType = null;
        if (ItemLimit.POTION_MATERIALS.contains(item.getType()) && meta instanceof PotionMeta potionMeta) {
            potionType = potionMeta.getBasePotionType();
            customModelData = null;
            displayName = null;
        }

        ItemLimit itemLimit = new ItemLimit(item.getType(), limit, customModelData, displayName, potionType);
        itemLimits.put(key, itemLimit);
        indexLimit(key, item.getType());
        saveLimit(key, itemLimit);
    }

    private void saveLimit(String key, ItemLimit itemLimit) {
        String basePath = "features.item-limiter.limits." + key;
        plugin.getConfigManager().get().set(basePath + ".material", itemLimit.getMaterial().name());
        plugin.getConfigManager().get().set(basePath + ".limit", itemLimit.getLimit());

        if (itemLimit.getCustomModelData() != null) {
            plugin.getConfigManager().get().set(basePath + ".customModelData", itemLimit.getCustomModelData());
        }
        if (itemLimit.getDisplayName() != null) {
            plugin.getConfigManager().get().set(basePath + ".displayName", itemLimit.getDisplayName());
        }
        if (itemLimit.getPotionType() != null) {
            plugin.getConfigManager().get().set(basePath + ".potionType", itemLimit.getPotionType().toString());
        }

        plugin.getConfigManager().save();
    }

    public void removeItemLimit(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }

        ItemLimit removed = itemLimits.remove(key);
        if (removed != null)
            unindexLimit(key, removed.getMaterial());
        plugin.getConfigManager().get().set("features.item-limiter.limits." + key, null);
        plugin.getConfigManager().save();
    }

    public void updateItemLimit(String key, int newLimit) {
        if (key == null || key.isEmpty() || newLimit < 0) {
            return;
        }

        ItemLimit itemLimit = itemLimits.get(key);
        if (itemLimit != null) {
            itemLimit.setLimit(newLimit);
            plugin.getConfigManager().get().set("features.item-limiter.limits." + key + ".limit", newLimit);
            plugin.getConfigManager().save();
        }
    }

    public String generateItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "INVALID_ITEM";
        }

        ItemMeta meta = item.getItemMeta();
        StringBuilder key = new StringBuilder(item.getType().name());

        if (ItemLimit.POTION_MATERIALS.contains(item.getType()) && meta instanceof PotionMeta potionMeta) {
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

    public String findMatchingItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        List<String> candidates = materialIndex.get(item.getType());
        if (candidates == null) {
            return null;
        }
        for (String key : candidates) {
            ItemLimit limit = itemLimits.get(key);
            if (limit != null && limit.matches(item)) {
                return key;
            }
        }
        return null;
    }

    public int getPlayerItemCount(Player player, ItemStack targetItem) {
        if (player == null || targetItem == null || targetItem.getType() == Material.AIR) {
            return 0;
        }
        String targetKey = findMatchingItemKey(targetItem);
        if (targetKey == null)
            return 0;
        ItemLimit targetLimit = itemLimits.get(targetKey);
        if (targetLimit == null)
            return 0;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && targetLimit.matches(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public void checkAndEnforceLimits(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Map<String, Integer> itemCounts = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            String itemKey = findMatchingItemKey(item);
            if (itemKey != null) {
                itemCounts.merge(itemKey, item.getAmount(), Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> countEntry : itemCounts.entrySet()) {
            String itemKey = countEntry.getKey();
            int currentCount = countEntry.getValue();

            ItemLimit itemLimit = itemLimits.get(itemKey);
            if (itemLimit != null && currentCount > itemLimit.getLimit()) {
                int excessAmount = currentCount - itemLimit.getLimit();
                if (dropExcess) {
                    dropExcessItems(player, itemLimit, excessAmount);
                } else {
                    removeExcessItems(player, itemLimit, excessAmount);
                }
                if (notifyPlayer) {
                    String message = notifyMessage
                            .replace("{item}", formatMaterialName(itemLimit.getMaterial().name()))
                            .replace("{amount}", String.valueOf(excessAmount))
                            .replace("{limit}", String.valueOf(itemLimit.getLimit()));
                    sendCooldownMessage(player, message);
                }
            }
        }
    }

    private String formatMaterialName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private void removeExcessItems(Player player, ItemLimit itemLimit, int excessAmount) {
        if (player == null || itemLimit == null || excessAmount <= 0) {
            return;
        }

        ItemStack[] contents = player.getInventory().getContents();

        for (int i = contents.length - 1; i >= 0 && excessAmount > 0; i--) {
            ItemStack item = contents[i];
            if (item == null || !itemLimit.matches(item)) {
                continue;
            }

            int amountToRemove = Math.min(item.getAmount(), excessAmount);

            if (amountToRemove >= item.getAmount()) {
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - amountToRemove);
            }

            excessAmount -= amountToRemove;
        }

        player.updateInventory();
    }

    private void dropExcessItems(Player player, ItemLimit itemLimit, int excessAmount) {
        if (player == null || itemLimit == null || excessAmount <= 0) {
            return;
        }

        ItemStack[] contents = player.getInventory().getContents();

        for (int i = contents.length - 1; i >= 0 && excessAmount > 0; i--) {
            ItemStack item = contents[i];
            if (item == null || !itemLimit.matches(item)) {
                continue;
            }

            int amountToDrop = Math.min(item.getAmount(), excessAmount);
            ItemStack dropItem = item.clone();
            dropItem.setAmount(amountToDrop);

            player.getWorld().dropItemNaturally(player.getLocation(), dropItem);

            if (amountToDrop >= item.getAmount()) {
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - amountToDrop);
            }

            excessAmount -= amountToDrop;
        }

        player.updateInventory();
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

    public Map<String, ItemLimit> getItemLimits() {
        return new HashMap<>(itemLimits);
    }

    private void indexLimit(String key, Material material) {
        materialIndex.computeIfAbsent(material, k -> new ArrayList<>()).add(key);
    }

    private void unindexLimit(String key, Material material) {
        List<String> keys = materialIndex.get(material);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty())
                materialIndex.remove(material);
        }
    }

    public ItemLimit getItemLimit(String key) {
        return itemLimits.get(key);
    }

    public int getLimitsCount() {
        return itemLimits.size();
    }
}
