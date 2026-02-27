package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.MinimapControlListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MinimapControlFeature extends BaseFeature {

    public static final String GUI_TITLE = "§8Minimap Control";
    public static final String WORLD_SETTINGS_GUI_TITLE = "§8Minimap - World Settings";
    private static final String CONFIG_PATH_PREFIX = "features.minimap-control.";
    private static final String VERBOSE_PREFIX = "[VERBOSE] Minimap Control - ";

    public static final String XAEROS_DISABLE = "§n§o§m§i§n§i§m§a§p";
    public static final String XAEROS_FAIR = "§f§a§i§r§x§a§e§r§o";
    public static final String XAEROS_NETHER_FAIR = "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r";
    public static final String XAEROS_RESET = "§r§e§s§e§t§x§a§e§r§o";

    public enum MinimapMode {
        DISABLED,
        FAIR,
        FULL
    }

    private MinimapMode globalMode = MinimapMode.FAIR;
    private final Map<String, MinimapMode> worldModes = new HashMap<>();
    private boolean netherFairMode = true;
    private boolean sendOnJoin = true;
    private boolean sendOnWorldChange = true;

    private MinimapControlListener listener;
    private final Map<UUID, String> playerSelectedWorld = new HashMap<>();

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new MinimapControlListener(plugin, this);
        super.onEnable(plugin);
        loadConfig();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Global mode: " + globalMode);
            plugin.getLogger().info(VERBOSE_PREFIX + "Nether fair mode: " + netherFairMode);
            plugin.getLogger().info(VERBOSE_PREFIX + "Send on join: " + sendOnJoin);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveConfig();
        playerSelectedWorld.clear();
    }

    private void loadConfig() {
        String modeStr = plugin.getConfigManager().get().getString(CONFIG_PATH_PREFIX + "global-mode", "FAIR");
        try {
            globalMode = MinimapMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            globalMode = MinimapMode.FAIR;
        }

        netherFairMode = plugin.getConfigManager().get().getBoolean(CONFIG_PATH_PREFIX + "nether-fair-mode", true);
        sendOnJoin = plugin.getConfigManager().get().getBoolean(CONFIG_PATH_PREFIX + "send-on-join", true);
        sendOnWorldChange = plugin.getConfigManager().get().getBoolean(CONFIG_PATH_PREFIX + "send-on-world-change",
                true);

        worldModes.clear();
        if (plugin.getConfigManager().get().isConfigurationSection(CONFIG_PATH_PREFIX + "worlds")) {
            for (String worldName : plugin.getConfigManager().get()
                    .getConfigurationSection(CONFIG_PATH_PREFIX + "worlds").getKeys(false)) {
                String worldModeStr = plugin.getConfigManager().get()
                        .getString(CONFIG_PATH_PREFIX + "worlds." + worldName, "FAIR");
                try {
                    worldModes.put(worldName, MinimapMode.valueOf(worldModeStr.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveConfig() {
        plugin.getConfigManager().get().set(CONFIG_PATH_PREFIX + "global-mode", globalMode.name());
        plugin.getConfigManager().get().set(CONFIG_PATH_PREFIX + "nether-fair-mode", netherFairMode);
        plugin.getConfigManager().get().set(CONFIG_PATH_PREFIX + "send-on-join", sendOnJoin);
        plugin.getConfigManager().get().set(CONFIG_PATH_PREFIX + "send-on-world-change", sendOnWorldChange);

        for (Map.Entry<String, MinimapMode> entry : worldModes.entrySet()) {
            plugin.getConfigManager().get().set(CONFIG_PATH_PREFIX + "worlds." + entry.getKey(),
                    entry.getValue().name());
        }

        plugin.getConfigManager().save();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 33;
    }

    @Override
    public String getName() {
        return "Minimap Control";
    }

    @Override
    public String getConfigPath() {
        return "features.minimap-control";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.FILLED_MAP, "§6Minimap Control",
                "§7Control Xaero's minimap features");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        lore.add("§7Mode: §e" + globalMode.name());
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Open GUI");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        if (!isEnabled()) {
            player.sendMessage(plugin.getMessageManager().get("minimap-control.feature-disabled",
                    "§cMinimap Control is disabled! Enable it first."));
            return;
        }
        openMainGUI(player);
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        ItemStack modeItem = createModeItem();
        gui.setItem(13, modeItem);

        ItemStack netherItem = createToggleItem(Material.NETHERRACK, "§6Nether Fair Mode",
                netherFairMode,
                "§7When in FAIR mode, sends extra",
                "§7restrictions for the Nether.");
        gui.setItem(20, netherItem);

        ItemStack joinItem = createToggleItem(Material.OAK_DOOR, "§6Send on Join",
                sendOnJoin,
                "§7Apply settings when",
                "§7players join the server.");
        gui.setItem(22, joinItem);

        ItemStack worldChangeItem = createToggleItem(Material.ENDER_PEARL, "§6Send on World Change",
                sendOnWorldChange,
                "§7Apply settings when players",
                "§7change worlds/dimensions.");
        gui.setItem(24, worldChangeItem);

        ItemStack worldSettings = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta worldMeta = worldSettings.getItemMeta();
        if (worldMeta != null) {
            worldMeta.setDisplayName("§6Per-World Settings");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Configure different modes");
            lore.add("§7for each world.");
            lore.add("");
            lore.add("§eClick to configure!");
            worldMeta.setLore(lore);
            worldSettings.setItemMeta(worldMeta);
        }
        gui.setItem(30, worldSettings);

        ItemStack applyAll = new ItemStack(Material.LIME_DYE);
        ItemMeta applyMeta = applyAll.getItemMeta();
        if (applyMeta != null) {
            applyMeta.setDisplayName("§aApply to All Online");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Send current settings to");
            lore.add("§7all online players now.");
            lore.add("");
            lore.add("§eClick to apply!");
            applyMeta.setLore(lore);
            applyAll.setItemMeta(applyMeta);
        }
        gui.setItem(32, applyAll);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(35, closeItem);

        player.openInventory(gui);
    }

    private ItemStack createModeItem() {
        Material material = switch (globalMode) {
            case DISABLED -> Material.BARRIER;
            case FAIR -> Material.MAP;
            case FULL -> Material.FILLED_MAP;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Global Mode: §e" + globalMode.name());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(globalMode == MinimapMode.DISABLED ? "§a» §cDISABLED §7- Minimap fully disabled"
                    : "§7  §8DISABLED - Minimap fully disabled");
            lore.add(globalMode == MinimapMode.FAIR ? "§a» §eFAIR §7- No cave map, no radar"
                    : "§7  §8FAIR - No cave map, no radar");
            lore.add(globalMode == MinimapMode.FULL ? "§a» §aFULL §7- All features enabled"
                    : "§7  §8FULL - All features enabled");
            lore.add("");
            lore.add("§eClick to cycle modes!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled, String... descriptionLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"));
            lore.add("");
            for (String line : descriptionLines) {
                lore.add(line);
            }
            lore.add("");
            lore.add("§eClick to toggle!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openWorldSettingsGUI(Player player) {
        List<World> worlds = Bukkit.getWorlds();
        int size = Math.min(54, ((worlds.size() + 8) / 9 + 1) * 9);
        size = Math.max(27, size);
        Inventory gui = Bukkit.createInventory(null, size, WORLD_SETTINGS_GUI_TITLE);

        int slot = 0;
        for (World world : worlds) {
            if (slot >= size - 9)
                break;

            Material material = switch (world.getEnvironment()) {
                case NORMAL -> Material.GRASS_BLOCK;
                case NETHER -> Material.NETHERRACK;
                case THE_END -> Material.END_STONE;
                default -> Material.STONE;
            };

            MinimapMode mode = worldModes.getOrDefault(world.getName(), globalMode);

            ItemStack worldItem = new ItemStack(material);
            ItemMeta meta = worldItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + world.getName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Environment: §e" + world.getEnvironment().name());
                lore.add("§7Mode: §e" + mode.name());
                lore.add("");
                lore.add("§eLeft Click: Cycle mode");
                lore.add("§eRight Click: Use global");
                meta.setLore(lore);
                worldItem.setItemMeta(meta);
            }
            gui.setItem(slot++, worldItem);
        }

        ItemStack backItem = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§eBack");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(size - 5, backItem);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(size - 1, closeItem);

        player.openInventory(gui);
    }

    public void sendMinimapSettings(Player player) {
        if (!isEnabled())
            return;

        World world = player.getWorld();
        MinimapMode mode = worldModes.getOrDefault(world.getName(), globalMode);

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Sending minimap settings to " + player.getName()
                    + " in world " + world.getName() + " with mode " + mode);
        }

        switch (mode) {
            case DISABLED -> sendTellraw(player, XAEROS_DISABLE);
            case FAIR -> {
                sendTellraw(player, XAEROS_FAIR);
                if (world.getEnvironment() == World.Environment.NETHER && netherFairMode) {
                    sendTellraw(player, XAEROS_NETHER_FAIR);
                }
            }
            case FULL -> sendTellraw(player, XAEROS_RESET);
        }
    }

    public void sendToAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMinimapSettings(player);
        }
    }

    private void sendTellraw(Player player, String message) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                String.format("tellraw %s \"%s\"", player.getName(), message));
    }

    public MinimapMode getGlobalMode() {
        return globalMode;
    }

    public void setGlobalMode(MinimapMode mode) {
        this.globalMode = mode;
        saveConfig();
        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Global mode changed to: " + mode);
        }
    }

    public void cycleGlobalMode() {
        globalMode = switch (globalMode) {
            case DISABLED -> MinimapMode.FAIR;
            case FAIR -> MinimapMode.FULL;
            case FULL -> MinimapMode.DISABLED;
        };
        saveConfig();
    }

    public MinimapMode getWorldMode(String worldName) {
        return worldModes.getOrDefault(worldName, globalMode);
    }

    public void setWorldMode(String worldName, MinimapMode mode) {
        if (mode == null) {
            worldModes.remove(worldName);
        } else {
            worldModes.put(worldName, mode);
        }
        saveConfig();
    }

    public void cycleWorldMode(String worldName) {
        MinimapMode current = worldModes.getOrDefault(worldName, globalMode);
        MinimapMode next = switch (current) {
            case DISABLED -> MinimapMode.FAIR;
            case FAIR -> MinimapMode.FULL;
            case FULL -> MinimapMode.DISABLED;
        };
        worldModes.put(worldName, next);
        saveConfig();
    }

    public void clearWorldMode(String worldName) {
        worldModes.remove(worldName);
        saveConfig();
    }

    public boolean isNetherFairMode() {
        return netherFairMode;
    }

    public void setNetherFairMode(boolean enabled) {
        this.netherFairMode = enabled;
        saveConfig();
    }

    public boolean isSendOnJoin() {
        return sendOnJoin;
    }

    public void setSendOnJoin(boolean enabled) {
        this.sendOnJoin = enabled;
        saveConfig();
    }

    public boolean isSendOnWorldChange() {
        return sendOnWorldChange;
    }

    public void setSendOnWorldChange(boolean enabled) {
        this.sendOnWorldChange = enabled;
        saveConfig();
    }

    public String getPlayerSelectedWorld(Player player) {
        return playerSelectedWorld.get(player.getUniqueId());
    }

    public void setPlayerSelectedWorld(Player player, String worldName) {
        playerSelectedWorld.put(player.getUniqueId(), worldName);
    }

    public void cleanupPlayer(UUID playerId) {
        playerSelectedWorld.remove(playerId);
    }

    @Override
    public void reload() {
        super.reload();
        loadConfig();
    }
}
