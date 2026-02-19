package com.tejaslamba.vanillacore.limitedenchants;

import org.bukkit.Material;

import java.util.UUID;

public class EnchantedItemRegistry {

    private final String itemUUID;
    private final Material material;
    private final String playerName;
    private final UUID playerUUID;
    private final long timestamp;
    private long lastSeen;

    public EnchantedItemRegistry(String itemUUID, Material material, String playerName, UUID playerUUID,
            long timestamp) {
        this.itemUUID = itemUUID;
        this.material = material;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.timestamp = timestamp;
        this.lastSeen = timestamp;
    }

    public EnchantedItemRegistry(String itemUUID, Material material, String playerName, UUID playerUUID,
            long timestamp, long lastSeen) {
        this.itemUUID = itemUUID;
        this.material = material;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.timestamp = timestamp;
        this.lastSeen = lastSeen;
    }

    public String getItemUUID() {
        return itemUUID;
    }

    public Material getMaterial() {
        return material;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
}
