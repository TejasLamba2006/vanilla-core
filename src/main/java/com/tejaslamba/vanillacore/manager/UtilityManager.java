package com.tejaslamba.vanillacore.manager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UtilityManager {

    private final Set<UUID> godModePlayers = ConcurrentHashMap.newKeySet();

    public boolean toggleGod(UUID playerId) {
        if (godModePlayers.contains(playerId)) {
            godModePlayers.remove(playerId);
            return false;
        }
        godModePlayers.add(playerId);
        return true;
    }

    public boolean isGod(UUID playerId) {
        return godModePlayers.contains(playerId);
    }

    public void clear() {
        godModePlayers.clear();
    }
}

