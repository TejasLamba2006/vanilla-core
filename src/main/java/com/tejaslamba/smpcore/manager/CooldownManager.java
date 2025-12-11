package com.tejaslamba.smpcore.manager;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager() {
        // Default constructor
    }

    public void setCooldown(Player player, String type, long durationMillis) {
        cooldowns.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .put(player.getUniqueId(), System.currentTimeMillis() + durationMillis);
    }

    public boolean hasCooldown(Player player, String type) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns == null) {
            return false;
        }

        Long expiry = typeCooldowns.get(player.getUniqueId());
        if (expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expiry) {
            typeCooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public long getRemainingCooldown(Player player, String type) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns == null) {
            return 0;
        }

        Long expiry = typeCooldowns.get(player.getUniqueId());
        if (expiry == null) {
            return 0;
        }

        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void removeCooldown(Player player, String type) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns != null) {
            typeCooldowns.remove(player.getUniqueId());
        }
    }

    public void clearAllCooldowns(Player player) {
        UUID uuid = player.getUniqueId();
        cooldowns.values().forEach(map -> map.remove(uuid));
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.values().forEach(map -> map.entrySet().removeIf(entry -> entry.getValue() <= now));
    }

    public void shutdown() {
        cooldowns.clear();
    }

}
