package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KitManager {

    public record KitDefinition(String name, String permission, long cooldownSeconds, List<ItemStack> items) {
    }

    private final VanillaCorePlugin plugin;
    private final TeleportDataManager dataManager;
    private final Map<String, KitDefinition> kits = new HashMap<>();

    public KitManager(VanillaCorePlugin plugin, TeleportDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void reload() {
        kits.clear();
        ConfigurationSection root = plugin.getConfigManager().get().getConfigurationSection("kits.definitions");
        if (root == null) {
            return;
        }

        for (String kitName : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(kitName);
            if (section == null) {
                continue;
            }
            String permission = section.getString("permission", "smp.kit." + kitName.toLowerCase(Locale.ROOT));
            long cooldown = Math.max(0L, section.getLong("cooldown-seconds", 0L));
            List<ItemStack> items = parseItems(section.getStringList("items"));
            kits.put(kitName.toLowerCase(Locale.ROOT), new KitDefinition(kitName.toLowerCase(Locale.ROOT), permission,
                    cooldown, items));
        }
    }

    public Set<String> getKitNames() {
        return kits.keySet();
    }

    public KitDefinition getKit(String name) {
        return kits.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean giveKit(Player player, String kitName, boolean ignoreCooldown, boolean ignorePermission) {
        KitDefinition kit = getKit(kitName);
        if (kit == null) {
            plugin.getMessageManager().sendPrefixed(player, "kits.not-found", "kit", kitName);
            return false;
        }

        if (!ignorePermission && !kit.permission().isBlank() && !player.hasPermission(kit.permission())) {
            plugin.getMessageManager().sendPrefixed(player, "kits.no-permission", "kit", kitName);
            return false;
        }

        long now = System.currentTimeMillis();
        long lastClaim = dataManager.getKitLastClaim(player.getUniqueId(), kit.name());
        long cooldownMs = kit.cooldownSeconds() * 1000L;
        if (!ignoreCooldown && cooldownMs > 0 && (lastClaim + cooldownMs) > now) {
            long seconds = ((lastClaim + cooldownMs) - now + 999L) / 1000L;
            plugin.getMessageManager().sendPrefixed(player, "kits.cooldown", "seconds", String.valueOf(seconds));
            return false;
        }

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(kit.items().toArray(new ItemStack[0]));
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        dataManager.setKitLastClaim(player.getUniqueId(), kit.name(), now);
        plugin.getMessageManager().sendPrefixed(player, "kits.claimed", "kit", kit.name());
        return true;
    }

    public void giveFirstJoinKit(Player player) {
        String firstJoinKit = plugin.getConfigManager().get().getString("kits.first-join-kit", "")
                .toLowerCase(Locale.ROOT);
        if (firstJoinKit.isBlank()) {
            return;
        }
        if (getKit(firstJoinKit) == null) {
            return;
        }
        giveKit(player, firstJoinKit, true, true);
        plugin.getMessageManager().sendPrefixed(player, "kits.first-join-received", "kit", firstJoinKit);
    }

    private List<ItemStack> parseItems(List<String> rawItems) {
        if (rawItems == null || rawItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> items = new ArrayList<>();
        for (String raw : rawItems) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.trim().split("\\s+");
            String materialName = parts[0].toUpperCase(Locale.ROOT);
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Math.max(1, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                continue;
            }
            items.add(new ItemStack(material, Math.min(amount, material.getMaxStackSize())));
        }
        return items;
    }

    public void save() {
        dataManager.saveAll();
    }

    public UUID getUuid(Player player) {
        return player.getUniqueId();
    }
}

