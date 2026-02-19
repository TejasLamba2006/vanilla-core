package com.tejaslamba.vanillacore.limitedenchants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tejaslamba.vanillacore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LimitedEnchantsManager {

    private final Main plugin;
    private final File dataFile;
    private final Gson gson;
    private final NamespacedKey itemUUIDKey;

    private final Map<Material, Integer> materialLimits = new ConcurrentHashMap<>();
    private final Map<String, EnchantedItemRegistry> registeredItems = new ConcurrentHashMap<>();
    private final Map<Material, Set<String>> itemsByMaterial = new ConcurrentHashMap<>();

    public LimitedEnchantsManager(Main plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "limited-enchants-registry.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.itemUUIDKey = new NamespacedKey(plugin, "limited_enchant_uuid");
    }

    public void load() {
        materialLimits.clear();
        loadLimits();
        loadRegistry();

        if (plugin.isVerbose()) {
            plugin.getLogger()
                    .info("[VERBOSE] Limited Enchants - Loaded " + materialLimits.size() + " material limits");
            plugin.getLogger()
                    .info("[VERBOSE] Limited Enchants - Loaded " + registeredItems.size() + " registered items");
        }
    }

    private void loadLimits() {
        Map<String, Object> limitsMap = plugin.getConfigManager().get()
                .getConfigurationSection("features.limited-enchantment-slots.limits").getValues(false);

        for (Map.Entry<String, Object> entry : limitsMap.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey().toUpperCase());
                int limit = (int) entry.getValue();
                materialLimits.put(material, limit);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in limited-enchantment-slots config: " + entry.getKey());
            }
        }
    }

    private void loadRegistry() {
        registeredItems.clear();
        itemsByMaterial.clear();

        if (!dataFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("registrations")) {
                return;
            }

            JsonArray registrations = root.getAsJsonArray("registrations");
            for (int i = 0; i < registrations.size(); i++) {
                JsonObject obj = registrations.get(i).getAsJsonObject();
                String itemUUID = obj.get("itemUUID").getAsString();
                Material material = Material.valueOf(obj.get("material").getAsString());
                String playerName = obj.get("playerName").getAsString();
                UUID playerUUID = UUID.fromString(obj.get("playerUUID").getAsString());
                long timestamp = obj.get("timestamp").getAsLong();
                long lastSeen = obj.has("lastSeen") ? obj.get("lastSeen").getAsLong() : timestamp;

                EnchantedItemRegistry registration = new EnchantedItemRegistry(itemUUID, material, playerName,
                        playerUUID, timestamp, lastSeen);
                registeredItems.put(itemUUID, registration);

                itemsByMaterial.computeIfAbsent(material, k -> ConcurrentHashMap.newKeySet()).add(itemUUID);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load limited enchants registry: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            JsonObject root = new JsonObject();
            JsonArray registrations = new JsonArray();

            for (EnchantedItemRegistry registration : registeredItems.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("itemUUID", registration.getItemUUID());
                obj.addProperty("material", registration.getMaterial().name());
                obj.addProperty("playerName", registration.getPlayerName());
                obj.addProperty("playerUUID", registration.getPlayerUUID().toString());
                obj.addProperty("timestamp", registration.getTimestamp());
                obj.addProperty("lastSeen", registration.getLastSeen());
                registrations.add(obj);
            }

            root.add("registrations", registrations);

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(root, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save limited enchants registry: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
        materialLimits.clear();
        registeredItems.clear();
        itemsByMaterial.clear();
    }

    public boolean hasLimit(Material material) {
        return materialLimits.containsKey(material);
    }

    public int getLimit(Material material) {
        return materialLimits.getOrDefault(material, -1);
    }

    public int getRegisteredCount(Material material) {
        Set<String> items = itemsByMaterial.get(material);
        return items != null ? items.size() : 0;
    }

    public boolean canEnchant(Material material) {
        if (!hasLimit(material)) {
            return true;
        }

        int limit = getLimit(material);
        int registered = getRegisteredCount(material);
        return registered < limit;
    }

    public boolean registerItem(ItemStack item, Player player) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material material = item.getType();
        if (!hasLimit(material)) {
            return true;
        }

        String existingUUID = getItemUUID(item);
        if (existingUUID != null) {
            if (registeredItems.containsKey(existingUUID)) {
                EnchantedItemRegistry registration = registeredItems.get(existingUUID);
                registration.updateLastSeen(System.currentTimeMillis());
                save();
                return true;
            }
        }

        if (!canEnchant(material)) {
            return false;
        }

        String newUUID = UUID.randomUUID().toString();
        setItemUUID(item, newUUID);

        EnchantedItemRegistry registration = new EnchantedItemRegistry(newUUID, material, player.getName(),
                player.getUniqueId(), System.currentTimeMillis());

        registeredItems.put(newUUID, registration);
        itemsByMaterial.computeIfAbsent(material, k -> ConcurrentHashMap.newKeySet()).add(newUUID);

        save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Limited Enchants - Registered " + material.name() + " for player "
                    + player.getName() + " (" + (getRegisteredCount(material)) + "/" + getLimit(material) + ")");
        }

        return true;
    }

    public void unregisterItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        String uuid = getItemUUID(item);
        if (uuid != null) {
            unregisterByUUID(uuid);
        }
    }

    public void unregisterByUUID(String uuid) {
        EnchantedItemRegistry registration = registeredItems.remove(uuid);
        if (registration != null) {
            Set<String> materialSet = itemsByMaterial.get(registration.getMaterial());
            if (materialSet != null) {
                materialSet.remove(uuid);
            }
            save();

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Limited Enchants - Unregistered " + registration.getMaterial().name()
                        + " from player " + registration.getPlayerName());
            }
        }
    }

    public String getItemUUID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(itemUUIDKey, PersistentDataType.STRING)) {
            return container.get(itemUUIDKey, PersistentDataType.STRING);
        }

        return null;
    }

    private void setItemUUID(ItemStack item, String uuid) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(itemUUIDKey, PersistentDataType.STRING, uuid);
        item.setItemMeta(meta);
    }

    public boolean isItemRegistered(ItemStack item) {
        String uuid = getItemUUID(item);
        return uuid != null && registeredItems.containsKey(uuid);
    }

    public void scanPlayerItems(Player player) {
        int registered = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR && !item.getEnchantments().isEmpty()) {
                Material material = item.getType();
                if (hasLimit(material)) {
                    if (registerItem(item, player)) {
                        registered++;
                    }
                }
            }
        }

        if (plugin.isVerbose() && registered > 0) {
            plugin.getLogger().info(
                    "[VERBOSE] Limited Enchants - Scanned " + player.getName() + ", registered " + registered
                            + " items");
        }
    }

    public void scanAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scanPlayerItems(player);
        }
    }

    public List<EnchantedItemRegistry> getRegistrationsForMaterial(Material material) {
        List<EnchantedItemRegistry> result = new ArrayList<>();
        Set<String> uuids = itemsByMaterial.get(material);
        if (uuids != null) {
            for (String uuid : uuids) {
                EnchantedItemRegistry registration = registeredItems.get(uuid);
                if (registration != null) {
                    result.add(registration);
                }
            }
        }
        result.sort(Comparator.comparingLong(EnchantedItemRegistry::getTimestamp));
        return result;
    }

    public void clearRegistrationsForMaterial(Material material) {
        Set<String> uuids = itemsByMaterial.remove(material);
        if (uuids != null) {
            for (String uuid : uuids) {
                registeredItems.remove(uuid);
            }
            save();

            if (plugin.isVerbose()) {
                plugin.getLogger()
                        .info("[VERBOSE] Limited Enchants - Cleared " + uuids.size() + " registrations for "
                                + material.name());
            }
        }
    }

    public void clearAllRegistrations() {
        int count = registeredItems.size();
        registeredItems.clear();
        itemsByMaterial.clear();
        save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Limited Enchants - Cleared all " + count + " registrations");
        }
    }

    public void setLimit(Material material, int limit) {
        materialLimits.put(material, limit);
        plugin.getConfigManager().get().set("features.limited-enchantment-slots.limits." + material.name(), limit);
        plugin.getConfigManager().save();
    }

    public void removeLimit(Material material) {
        materialLimits.remove(material);
        plugin.getConfigManager().get().set("features.limited-enchantment-slots.limits." + material.name(), null);
        plugin.getConfigManager().save();
    }

    public Map<Material, Integer> getAllLimits() {
        return new HashMap<>(materialLimits);
    }

    public int getTotalRegistrations() {
        return registeredItems.size();
    }
}
