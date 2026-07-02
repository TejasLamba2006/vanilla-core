package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    public enum RequestType {
        TPA,
        TPA_HERE
    }

    public record TeleportRequest(UUID requester, RequestType type, long expiresAtMs) {
    }

    private final VanillaCorePlugin plugin;
    private final TeleportDataManager dataManager;
    private final Map<UUID, TeleportRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, GuiState> homesGuiState = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> warpsGuiState = new ConcurrentHashMap<>();

    public TeleportManager(VanillaCorePlugin plugin, TeleportDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void cleanupExpiredRequests() {
        long now = System.currentTimeMillis();
        List<UUID> expiredTargets = new ArrayList<>();
        for (Map.Entry<UUID, TeleportRequest> entry : pendingRequests.entrySet()) {
            if (entry.getValue().expiresAtMs() <= now) {
                expiredTargets.add(entry.getKey());
            }
        }

        for (UUID targetId : expiredTargets) {
            TeleportRequest request = pendingRequests.remove(targetId);
            if (request == null) {
                continue;
            }
            Player requester = Bukkit.getPlayer(request.requester());
            if (requester != null) {
                plugin.getMessageManager().sendPrefixed(requester, "teleport.request.expired");
            }
        }
    }

    public boolean requestTeleport(Player requester, Player target, RequestType type) {
        if (requester.getUniqueId().equals(target.getUniqueId())) {
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request.self");
            return false;
        }

        long timeoutSeconds = Math.max(5L,
                plugin.getConfigManager().get().getLong("teleport.request-timeout-seconds", 60L));
        pendingRequests.put(target.getUniqueId(), new TeleportRequest(requester.getUniqueId(), type,
                System.currentTimeMillis() + (timeoutSeconds * 1000L)));

        if (type == RequestType.TPA) {
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request.sent", "player", target.getName());
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.received", "player", requester.getName());
        } else {
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request-here.sent", "player",
                    target.getName());
            plugin.getMessageManager().sendPrefixed(target, "teleport.request-here.received", "player",
                    requester.getName());
        }
        return true;
    }

    public boolean acceptTeleport(Player target) {
        TeleportRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.none");
            return false;
        }

        if (request.expiresAtMs() < System.currentTimeMillis()) {
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.none");
            return false;
        }

        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null) {
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.offline");
            return false;
        }

        if (request.type() == RequestType.TPA) {
            teleportPlayer(requester, target.getLocation(), true);
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.accepted", "player", requester.getName());
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request.accepted-by", "player",
                    target.getName());
        } else {
            teleportPlayer(target, requester.getLocation(), true);
            plugin.getMessageManager().sendPrefixed(target, "teleport.request-here.accepted", "player",
                    requester.getName());
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request-here.accepted-by", "player",
                    target.getName());
        }
        return true;
    }

    public boolean denyTeleport(Player target) {
        TeleportRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            plugin.getMessageManager().sendPrefixed(target, "teleport.request.none");
            return false;
        }
        Player requester = Bukkit.getPlayer(request.requester());
        if (requester != null) {
            plugin.getMessageManager().sendPrefixed(requester, "teleport.request.denied", "player", target.getName());
        }
        plugin.getMessageManager().sendPrefixed(target, "teleport.request.denied-self");
        return true;
    }

    public boolean teleportToSpawn(Player player) {
        Location spawn = dataManager.getSpawn();
        if (spawn == null) {
            spawn = player.getWorld().getSpawnLocation();
        }
        return teleportPlayer(player, spawn, true);
    }

    public void setSpawn(Location location) {
        dataManager.setSpawn(location);
    }

    public boolean teleportBack(Player player) {
        Location back = dataManager.getBack(player.getUniqueId());
        if (back == null || back.getWorld() == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.back.none");
            return false;
        }
        return teleportPlayer(player, back, true);
    }

    public void setBack(Player player, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        dataManager.setBack(player.getUniqueId(), location.clone());
    }

    public boolean teleportPlayer(Player player, Location target, boolean withSafety) {
        if (target == null || target.getWorld() == null) {
            return false;
        }

        Location destination = withSafety ? toSafeLocation(target.clone()) : target.clone();
        if (destination.getWorld() == null) {
            return false;
        }
        setBack(player, player.getLocation());
        return player.teleport(destination);
    }

    public Map<String, Location> getHomes(UUID playerId) {
        return dataManager.getHomes(playerId);
    }

    public Location getHome(UUID playerId, String homeName) {
        return dataManager.getHome(playerId, homeName);
    }

    public void setHome(UUID playerId, String homeName, Location location) {
        dataManager.setHome(playerId, homeName.toLowerCase(Locale.ROOT), location);
    }

    public void deleteHome(UUID playerId, String homeName) {
        dataManager.deleteHome(playerId, homeName.toLowerCase(Locale.ROOT));
    }

    public Map<String, Location> getWarps() {
        return dataManager.getWarps();
    }

    public Location getWarp(String warpName) {
        return dataManager.getWarp(warpName.toLowerCase(Locale.ROOT));
    }

    public void setWarp(String warpName, Location location) {
        dataManager.setWarp(warpName.toLowerCase(Locale.ROOT), location);
    }

    public void deleteWarp(String warpName) {
        dataManager.deleteWarp(warpName.toLowerCase(Locale.ROOT));
    }

    public void openHomesGui(Player viewer, UUID owner) {
        Map<String, Location> homes = new HashMap<>(getHomes(owner));
        List<String> names = homes.keySet().stream().sorted(Comparator.naturalOrder()).toList();

        Inventory inventory = Bukkit.createInventory(new GuiHolder("homes:" + owner), 54,
                plugin.getMessageManager().get("teleport.homes.gui.title", "player", resolveOwnerName(owner)));

        int slot = 0;
        for (String name : names) {
            if (slot >= 45) {
                break;
            }
            Location location = homes.get(name);
            ItemStack item = new ItemStack(Material.RED_BED);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getMessageManager().get("teleport.homes.gui.item", "home", name));
                List<net.kyori.adventure.text.Component> lore = List.of(
                        plugin.getMessageManager().get("teleport.homes.gui.lore-world", "world",
                                location.getWorld().getName()),
                        plugin.getMessageManager().get("teleport.homes.gui.lore-coords", "x",
                                String.valueOf(location.getBlockX()),
                                "y", String.valueOf(location.getBlockY()), "z", String.valueOf(location.getBlockZ())));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }

        homesGuiState.put(viewer.getUniqueId(), new GuiState(owner, names));
        viewer.openInventory(inventory);
    }

    public void openWarpsGui(Player viewer) {
        Map<String, Location> warps = new HashMap<>(getWarps());
        List<String> names = warps.keySet().stream().sorted(Comparator.naturalOrder()).toList();

        Inventory inventory = Bukkit.createInventory(new GuiHolder("warps"), 54,
                plugin.getMessageManager().get("teleport.warps.gui.title"));

        int slot = 0;
        for (String name : names) {
            if (slot >= 45) {
                break;
            }
            Location location = warps.get(name);
            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getMessageManager().get("teleport.warps.gui.item", "warp", name));
                List<net.kyori.adventure.text.Component> lore = List.of(
                        plugin.getMessageManager().get("teleport.warps.gui.lore-world", "world",
                                location.getWorld().getName()),
                        plugin.getMessageManager().get("teleport.warps.gui.lore-coords", "x",
                                String.valueOf(location.getBlockX()),
                                "y", String.valueOf(location.getBlockY()), "z", String.valueOf(location.getBlockZ())));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }

        warpsGuiState.put(viewer.getUniqueId(), names);
        viewer.openInventory(inventory);
    }

    public void handleHomesGuiClick(Player player, int slot) {
        GuiState state = homesGuiState.get(player.getUniqueId());
        if (state == null || slot < 0 || slot >= state.homeNames().size() || slot >= 45) {
            return;
        }
        String homeName = state.homeNames().get(slot);
        Location target = getHome(state.owner(), homeName);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.homes.not-found", "home", homeName);
            player.closeInventory();
            return;
        }
        teleportPlayer(player, target, true);
        plugin.getMessageManager().sendPrefixed(player, "teleport.homes.teleported", "home", homeName);
        player.closeInventory();
    }

    public void handleWarpsGuiClick(Player player, int slot) {
        List<String> names = warpsGuiState.get(player.getUniqueId());
        if (names == null || slot < 0 || slot >= names.size() || slot >= 45) {
            return;
        }
        String warpName = names.get(slot);
        Location target = getWarp(warpName);
        if (target == null) {
            plugin.getMessageManager().sendPrefixed(player, "teleport.warps.not-found", "warp", warpName);
            player.closeInventory();
            return;
        }
        teleportPlayer(player, target, true);
        plugin.getMessageManager().sendPrefixed(player, "teleport.warps.teleported", "warp", warpName);
        player.closeInventory();
    }

    public void clearGuiState(UUID viewer) {
        homesGuiState.remove(viewer);
        warpsGuiState.remove(viewer);
    }

    private String resolveOwnerName(UUID owner) {
        Player online = Bukkit.getPlayer(owner);
        if (online != null) {
            return online.getName();
        }
        return Bukkit.getOfflinePlayer(owner).getName() == null ? owner.toString()
                : Bukkit.getOfflinePlayer(owner).getName();
    }

    private Location toSafeLocation(Location location) {
        boolean safetyEnabled = plugin.getConfigManager().get().getBoolean("teleport.safety.enabled", true);
        if (!safetyEnabled || location.getWorld() == null) {
            return location;
        }

        if (isSafe(location)) {
            return center(location);
        }

        World world = location.getWorld();
        int safeY = world.getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1;
        Location highest = new Location(world, location.getX(), safeY, location.getZ(), location.getYaw(),
                location.getPitch());
        if (isSafe(highest)) {
            return center(highest);
        }

        return center(world.getSpawnLocation());
    }

    private boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);
        return feet.isPassable() && head.isPassable() && ground.getType().isSolid();
    }

    private Location center(Location location) {
        return new Location(location.getWorld(), location.getBlockX() + 0.5, location.getY(),
                location.getBlockZ() + 0.5,
                location.getYaw(), location.getPitch());
    }

    private record GuiState(UUID owner, List<String> homeNames) {
    }
}

