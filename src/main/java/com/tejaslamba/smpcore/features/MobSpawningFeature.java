package com.tejaslamba.smpcore.features;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.feature.BaseFeature;
import com.tejaslamba.smpcore.listener.MobSpawningListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MobSpawningFeature extends BaseFeature {

    public static final String GUI_TITLE = "§8Mob Spawning Manager";
    public static final String SPAWN_REASONS_GUI_TITLE = "§8Spawn Reasons Config";
    public static final String WORLD_SELECT_GUI_TITLE = "§8Select World";
    public static final String SETTINGS_GUI_TITLE = "§8Mob Spawning Settings";
    private static final int[] CONTENT_SLOTS = calculateContentSlots();
    private static final String CONFIG_PATH_PREFIX = "features.mob-spawning.worlds.";
    private static final String VERBOSE_PREFIX = "[VERBOSE] Mob Spawning - ";

    private final Map<String, Map<EntityType, Boolean>> worldDisabledMobs = new HashMap<>();
    private final Set<CreatureSpawnEvent.SpawnReason> allowedSpawnReasons = new HashSet<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> playerSelectedWorld = new HashMap<>();
    private final List<EntityType> spawnableEntities = new ArrayList<>();
    private final List<CreatureSpawnEvent.SpawnReason> allSpawnReasons = new ArrayList<>();

    private boolean chunkCleanupEnabled = false;
    private boolean worldGuardBypass = true;
    private MobSpawningListener listener;

    public MobSpawningFeature() {
        loadSpawnableEntities();
        loadAllSpawnReasons();
    }

    private void loadAllSpawnReasons() {
        allSpawnReasons.clear();
        Collections.addAll(allSpawnReasons, CreatureSpawnEvent.SpawnReason.values());
        allSpawnReasons.sort(Comparator.comparing(Enum::name));
    }

    private static int[] calculateContentSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            slots.add(i);
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private void loadSpawnableEntities() {
        spawnableEntities.clear();

        for (Material material : Material.values()) {
            if (material.name().endsWith("_SPAWN_EGG")) {
                EntityType entityType = getEntityTypeFromSpawnEgg(material);
                if (entityType != null && entityType != EntityType.UNKNOWN) {
                    spawnableEntities.add(entityType);
                }
            }
        }

        addNonSpawnEggEntity(EntityType.IRON_GOLEM);
        addNonSpawnEggEntity(EntityType.SNOW_GOLEM);
        addNonSpawnEggEntity(EntityType.WITHER);
        addNonSpawnEggEntity(EntityType.ENDER_DRAGON);

        spawnableEntities.sort(Comparator.comparing(Enum::name));
    }

    private void addNonSpawnEggEntity(EntityType entityType) {
        if (!spawnableEntities.contains(entityType)) {
            spawnableEntities.add(entityType);
        }
    }

    private EntityType getEntityTypeFromSpawnEgg(Material spawnEgg) {
        String name = spawnEgg.name().replace("_SPAWN_EGG", "");
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Material getDisplayMaterialForEntity(EntityType entityType) {
        String materialName = entityType.name() + "_SPAWN_EGG";
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return switch (entityType) {
                case IRON_GOLEM -> Material.IRON_BLOCK;
                case SNOW_GOLEM -> Material.CARVED_PUMPKIN;
                case WITHER -> Material.WITHER_SKELETON_SKULL;
                case ENDER_DRAGON -> Material.DRAGON_HEAD;
                default -> Material.BARRIER;
            };
        }
    }

    @Override
    public void onEnable(Main plugin) {
        listener = new MobSpawningListener(plugin, this);
        super.onEnable(plugin);

        chunkCleanupEnabled = plugin.getConfigManager().get()
                .getBoolean("features.mob-spawning.chunk-cleanup-enabled", false);
        worldGuardBypass = plugin.getConfigManager().get()
                .getBoolean("features.mob-spawning.worldguard-bypass", true);

        initializeWorldData();
        loadAllWorldDisabledMobs();
        loadAllowedSpawnReasons();

        if (plugin.isVerbose()) {
            int totalDisabled = worldDisabledMobs.values().stream()
                    .mapToInt(map -> (int) map.values().stream().filter(b -> b).count())
                    .sum();
            plugin.getLogger().info(VERBOSE_PREFIX + "Loaded " + totalDisabled + " disabled mobs across "
                    + worldDisabledMobs.size() + " worlds");
            plugin.getLogger().info(VERBOSE_PREFIX + "Loaded " + allowedSpawnReasons.size() + " allowed spawn reasons");
            plugin.getLogger()
                    .info(VERBOSE_PREFIX + "Chunk cleanup: " + (chunkCleanupEnabled ? "enabled" : "disabled"));
            plugin.getLogger()
                    .info(VERBOSE_PREFIX + "WorldGuard bypass: " + (worldGuardBypass ? "enabled" : "disabled"));
        }
    }

    private void initializeWorldData() {
        for (World world : Bukkit.getWorlds()) {
            if (!worldDisabledMobs.containsKey(world.getName())) {
                worldDisabledMobs.put(world.getName(), new EnumMap<>(EntityType.class));
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveAllWorldDisabledMobs();
        saveAllowedSpawnReasons();
        playerPages.clear();
        playerSelectedWorld.clear();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public String getName() {
        return "Mob Spawning";
    }

    @Override
    public String getConfigPath() {
        return "features.mob-spawning";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ZOMBIE_SPAWN_EGG, "§6Mob Spawning Manager",
                "§7Disable specific mob spawning");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
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
            player.sendMessage("§cMob Spawning Manager is disabled! Enable it first.");
            return;
        }
        openWorldSelectGUI(player);
    }

    public void openWorldSelectGUI(Player player) {
        List<World> worlds = Bukkit.getWorlds();
        int size = Math.min(54, ((worlds.size() + 8) / 9 + 1) * 9);
        size = Math.max(27, size);
        Inventory gui = Bukkit.createInventory(null, size, WORLD_SELECT_GUI_TITLE);

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

            ItemStack worldItem = new ItemStack(material);
            ItemMeta meta = worldItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + world.getName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Environment: §e" + world.getEnvironment().name());
                Map<EntityType, Boolean> worldMobs = worldDisabledMobs.get(world.getName());
                long disabledCount = worldMobs != null ? worldMobs.values().stream().filter(b -> b).count() : 0;
                lore.add("§7Disabled Mobs: §e" + disabledCount);
                lore.add("");
                lore.add("§eClick to manage!");
                meta.setLore(lore);
                worldItem.setItemMeta(meta);
            }
            gui.setItem(slot++, worldItem);
        }

        ItemStack allWorlds = new ItemStack(Material.NETHER_STAR);
        ItemMeta allMeta = allWorlds.getItemMeta();
        if (allMeta != null) {
            allMeta.setDisplayName("§6★ All Worlds");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Manage mob spawning for");
            lore.add("§7ALL worlds at once.");
            lore.add("");
            lore.add("§eClick to manage!");
            allMeta.setLore(lore);
            allWorlds.setItemMeta(allMeta);
        }
        gui.setItem(size - 5, allWorlds);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(size - 1, closeItem);

        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        if (settingsMeta != null) {
            settingsMeta.setDisplayName("§6Global Settings");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Chunk Cleanup: " + (chunkCleanupEnabled ? "§aEnabled" : "§cDisabled"));
            lore.add("§7WorldGuard Bypass: " + (worldGuardBypass ? "§aEnabled" : "§cDisabled"));
            lore.add("");
            lore.add("§eClick to configure!");
            settingsMeta.setLore(lore);
            settingsItem.setItemMeta(settingsMeta);
        }
        gui.setItem(size - 9, settingsItem);

        player.openInventory(gui);
    }

    public void openMobGUI(Player player, int page, String worldName) {
        int totalPages = getTotalPages();
        page = Math.clamp(page, 0, totalPages - 1);
        playerPages.put(player.getUniqueId(), page);
        playerSelectedWorld.put(player.getUniqueId(), worldName);

        String titleWorld = worldName == null ? "All Worlds" : worldName;
        Inventory gui = Bukkit.createInventory(null, 54,
                GUI_TITLE + " §7[" + titleWorld + "] (" + (page + 1) + "/" + totalPages + ")");

        populateMobItems(gui, page, worldName);
        addMobGUINavigationButtons(gui, page, totalPages);

        player.openInventory(gui);
    }

    private void populateMobItems(Inventory gui, int page, String worldName) {
        int startIndex = page * CONTENT_SLOTS.length;
        int slotIndex = 0;

        for (int i = startIndex; i < spawnableEntities.size() && slotIndex < CONTENT_SLOTS.length; i++) {
            EntityType entityType = spawnableEntities.get(i);
            Material displayMaterial = getDisplayMaterialForEntity(entityType);

            if (displayMaterial != null) {
                gui.setItem(CONTENT_SLOTS[slotIndex], createMobItem(entityType, displayMaterial, worldName));
                slotIndex++;
            }
        }
    }

    private ItemStack createMobItem(EntityType entityType, Material displayMaterial, String worldName) {
        boolean isDisabled = isDisabled(entityType, worldName);
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String mobName = formatEntityName(entityType);
            meta.setDisplayName((isDisabled ? "§c" : "§a") + mobName);
            meta.setLore(createMobItemLore(isDisabled, entityType));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<String> createMobItemLore(boolean isDisabled, EntityType entityType) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isDisabled) {
            lore.add("§cSpawning: §4Disabled");
        } else {
            lore.add("§aSpawning: §2Enabled");
        }

        if (!hasSpawnEgg(entityType)) {
            lore.add("");
            lore.add("§7§o(No spawn egg - special mob)");
        }

        lore.add("");
        lore.add("§eClick to toggle!");
        return lore;
    }

    private boolean hasSpawnEgg(EntityType entityType) {
        try {
            Material.valueOf(entityType.name() + "_SPAWN_EGG");
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void addMobGUINavigationButtons(Inventory gui, int page, int totalPages) {
        if (page > 0) {
            gui.setItem(45, createButtonWithLore(Material.ARROW, "§a« Previous Page",
                    "§7Page §e" + page + "§7/§e" + totalPages));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createButtonWithLore(Material.ARROW, "§aNext Page »",
                    "§7Page §e" + (page + 2) + "§7/§e" + totalPages));
        }

        gui.setItem(49, createSimpleButton(Material.OAK_DOOR, "§eBack to World Select"));
        gui.setItem(50, createSimpleButton(Material.BARRIER, "§cClose"));
        gui.setItem(47, createButtonWithLore(Material.LIME_DYE, "§aEnable All Mobs",
                "§7Click to enable spawning", "§7for all mob types."));
        gui.setItem(51, createButtonWithLore(Material.RED_DYE, "§cDisable All Mobs",
                "§7Click to disable spawning", "§7for all mob types."));
        gui.setItem(48, createButtonWithLore(Material.SPAWNER, "§6Spawn Reasons Config",
                "§7Configure which spawn reasons", "§7bypass mob blocking.", "", "§eClick to configure!"));
    }

    private ItemStack createButtonWithLore(Material material, String displayName, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            lore.add("");
            Collections.addAll(lore, loreLines);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSimpleButton(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) spawnableEntities.size() / CONTENT_SLOTS.length));
    }

    private String formatEntityName(EntityType entityType) {
        String name = entityType.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    private void loadAllWorldDisabledMobs() {
        worldDisabledMobs.clear();

        for (World world : Bukkit.getWorlds()) {
            Map<EntityType, Boolean> worldMobs = new EnumMap<>(EntityType.class);
            String worldConfigPath = CONFIG_PATH_PREFIX + world.getName() + ".disabled-mobs.";

            for (EntityType entityType : spawnableEntities) {
                String configKey = worldConfigPath + entityType.name().toLowerCase();
                boolean isDisabled = plugin.getConfigManager().get().getBoolean(configKey, false);
                worldMobs.put(entityType, isDisabled);
            }

            worldDisabledMobs.put(world.getName(), worldMobs);
        }
    }

    private void saveAllWorldDisabledMobs() {
        for (Map.Entry<String, Map<EntityType, Boolean>> worldEntry : worldDisabledMobs.entrySet()) {
            String worldConfigPath = CONFIG_PATH_PREFIX + worldEntry.getKey() + ".disabled-mobs.";

            for (Map.Entry<EntityType, Boolean> mobEntry : worldEntry.getValue().entrySet()) {
                String configKey = worldConfigPath + mobEntry.getKey().name().toLowerCase();
                plugin.getConfigManager().get().set(configKey, mobEntry.getValue());
            }
        }
        plugin.getConfigManager().save();
    }

    public boolean isDisabled(EntityType entityType, String worldName) {
        if (worldName == null) {
            return worldDisabledMobs.values().stream()
                    .anyMatch(map -> map.getOrDefault(entityType, false));
        }
        Map<EntityType, Boolean> worldMobs = worldDisabledMobs.get(worldName);
        return worldMobs != null && worldMobs.getOrDefault(entityType, false);
    }

    public boolean isDisabledInWorld(EntityType entityType, World world) {
        Map<EntityType, Boolean> worldMobs = worldDisabledMobs.get(world.getName());
        return worldMobs != null && worldMobs.getOrDefault(entityType, false);
    }

    public void setDisabled(EntityType entityType, boolean disabled, String worldName) {
        if (worldName == null) {
            for (String world : worldDisabledMobs.keySet()) {
                setDisabledForWorld(entityType, disabled, world);
            }
        } else {
            setDisabledForWorld(entityType, disabled, worldName);
        }

        if (plugin.isVerbose()) {
            String worldDisplay = worldName == null ? "all worlds" : worldName;
            plugin.getLogger().info(VERBOSE_PREFIX + entityType.name() + " state changed: disabled="
                    + disabled + " in " + worldDisplay);
        }
    }

    private void setDisabledForWorld(EntityType entityType, boolean disabled, String worldName) {
        Map<EntityType, Boolean> worldMobs = worldDisabledMobs.computeIfAbsent(worldName,
                k -> new EnumMap<>(EntityType.class));
        worldMobs.put(entityType, disabled);

        String configKey = CONFIG_PATH_PREFIX + worldName + ".disabled-mobs." + entityType.name().toLowerCase();
        plugin.getConfigManager().get().set(configKey, disabled);
        plugin.getConfigManager().save();
    }

    public void setAllDisabled(boolean disabled, String worldName) {
        if (worldName == null) {
            for (String world : worldDisabledMobs.keySet()) {
                setAllDisabledForWorld(disabled, world);
            }
        } else {
            setAllDisabledForWorld(disabled, worldName);
        }

        if (plugin.isVerbose()) {
            String worldDisplay = worldName == null ? "all worlds" : worldName;
            plugin.getLogger().info(VERBOSE_PREFIX + "All mobs set to disabled=" + disabled + " in " + worldDisplay);
        }
    }

    private void setAllDisabledForWorld(boolean disabled, String worldName) {
        Map<EntityType, Boolean> worldMobs = worldDisabledMobs.computeIfAbsent(worldName,
                k -> new EnumMap<>(EntityType.class));
        String worldConfigPath = CONFIG_PATH_PREFIX + worldName + ".disabled-mobs.";

        for (EntityType entityType : spawnableEntities) {
            worldMobs.put(entityType, disabled);
            String configKey = worldConfigPath + entityType.name().toLowerCase();
            plugin.getConfigManager().get().set(configKey, disabled);
        }
        plugin.getConfigManager().save();
    }

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public String getPlayerSelectedWorld(Player player) {
        return playerSelectedWorld.get(player.getUniqueId());
    }

    public void cleanupPlayer(UUID playerId) {
        playerPages.remove(playerId);
        playerSelectedWorld.remove(playerId);
    }

    public List<EntityType> getSpawnableEntities() {
        return new ArrayList<>(spawnableEntities);
    }

    public EntityType getEntityTypeAtSlot(int page, int slot) {
        if (slot < 0 || slot >= CONTENT_SLOTS.length) {
            return null;
        }
        int index = page * CONTENT_SLOTS.length + slot;
        if (index >= 0 && index < spawnableEntities.size()) {
            return spawnableEntities.get(index);
        }
        return null;
    }

    @Override
    public void reload() {
        super.reload();
        chunkCleanupEnabled = plugin.getConfigManager().get()
                .getBoolean("features.mob-spawning.chunk-cleanup-enabled", false);
        worldGuardBypass = plugin.getConfigManager().get()
                .getBoolean("features.mob-spawning.worldguard-bypass", true);
        initializeWorldData();
        loadAllWorldDisabledMobs();
        loadAllowedSpawnReasons();
    }

    private void loadAllowedSpawnReasons() {
        allowedSpawnReasons.clear();
        List<String> reasons = plugin.getConfigManager().get()
                .getStringList("features.mob-spawning.allowed-spawn-reasons");
        for (String reasonName : reasons) {
            try {
                CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason
                        .valueOf(reasonName.toUpperCase());
                allowedSpawnReasons.add(reason);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveAllowedSpawnReasons() {
        List<String> reasons = new ArrayList<>();
        for (CreatureSpawnEvent.SpawnReason reason : allowedSpawnReasons) {
            reasons.add(reason.name());
        }
        plugin.getConfigManager().get().set("features.mob-spawning.allowed-spawn-reasons", reasons);
        plugin.getConfigManager().save();
    }

    public boolean isSpawnReasonAllowed(CreatureSpawnEvent.SpawnReason reason) {
        return allowedSpawnReasons.contains(reason);
    }

    public void setSpawnReasonAllowed(CreatureSpawnEvent.SpawnReason reason, boolean allowed) {
        if (allowed) {
            allowedSpawnReasons.add(reason);
        } else {
            allowedSpawnReasons.remove(reason);
        }
        saveAllowedSpawnReasons();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Spawn reason " + reason.name()
                    + " state changed: allowed=" + allowed);
        }
    }

    public void openSpawnReasonsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, SPAWN_REASONS_GUI_TITLE);

        populateSpawnReasonItems(gui);
        addSpawnReasonNavigationButtons(gui);

        player.openInventory(gui);
    }

    private void populateSpawnReasonItems(Inventory gui) {
        int slot = 0;
        for (CreatureSpawnEvent.SpawnReason reason : allSpawnReasons) {
            if (slot >= 45) {
                break;
            }
            gui.setItem(slot, createSpawnReasonItem(reason));
            slot++;
        }
    }

    private ItemStack createSpawnReasonItem(CreatureSpawnEvent.SpawnReason reason) {
        boolean isAllowed = isSpawnReasonAllowed(reason);
        Material material = isAllowed ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String reasonName = formatReasonName(reason);
            meta.setDisplayName((isAllowed ? "§a" : "§c") + reasonName);
            meta.setLore(createSpawnReasonLore(isAllowed));
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<String> createSpawnReasonLore(boolean isAllowed) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isAllowed) {
            lore.add("§aBypass: §2Allowed");
            lore.add("§7Mobs CAN spawn via this reason");
            lore.add("§7even if mob type is disabled.");
        } else {
            lore.add("§cBypass: §4Blocked");
            lore.add("§7Mobs CANNOT spawn via this");
            lore.add("§7reason if mob type is disabled.");
        }
        lore.add("");
        lore.add("§eClick to toggle!");
        return lore;
    }

    private void addSpawnReasonNavigationButtons(Inventory gui) {
        ItemStack backButton = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§eBack to Mob Spawning");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName("§cClose");
            barrier.setItemMeta(barrierMeta);
        }
        gui.setItem(50, barrier);

        ItemStack infoItem = createSpawnReasonInfoItem();
        gui.setItem(45, infoItem);
    }

    private ItemStack createSpawnReasonInfoItem() {
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Spawn Reasons Info");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Allowed spawn reasons will");
            lore.add("§7bypass mob blocking.");
            lore.add("");
            lore.add("§7Example: If §aSPAWNER_EGG §7is allowed,");
            lore.add("§7disabled mobs can still spawn");
            lore.add("§7from spawn eggs.");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        return infoItem;
    }

    private String formatReasonName(CreatureSpawnEvent.SpawnReason reason) {
        String name = reason.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    public List<CreatureSpawnEvent.SpawnReason> getAllSpawnReasons() {
        return new ArrayList<>(allSpawnReasons);
    }

    public CreatureSpawnEvent.SpawnReason getSpawnReasonAtSlot(int slot) {
        if (slot >= 0 && slot < allSpawnReasons.size()) {
            return allSpawnReasons.get(slot);
        }
        return null;
    }

    public boolean isChunkCleanupEnabled() {
        return chunkCleanupEnabled;
    }

    public void setChunkCleanupEnabled(boolean enabled) {
        this.chunkCleanupEnabled = enabled;
        plugin.getConfigManager().get().set("features.mob-spawning.chunk-cleanup-enabled", enabled);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "Chunk cleanup " + (enabled ? "enabled" : "disabled"));
        }
    }

    public boolean isWorldGuardBypass() {
        return worldGuardBypass;
    }

    public void setWorldGuardBypass(boolean enabled) {
        this.worldGuardBypass = enabled;
        plugin.getConfigManager().get().set("features.mob-spawning.worldguard-bypass", enabled);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "WorldGuard bypass " + (enabled ? "enabled" : "disabled"));
        }
    }

    public void openGlobalSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, SETTINGS_GUI_TITLE);

        ItemStack chunkCleanup = new ItemStack(
                chunkCleanupEnabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta chunkMeta = chunkCleanup.getItemMeta();
        if (chunkMeta != null) {
            chunkMeta.setDisplayName("§6Chunk Cleanup");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Status: " + (chunkCleanupEnabled ? "§aEnabled" : "§cDisabled"));
            lore.add("");
            lore.add("§7When enabled, removes disabled");
            lore.add("§7mobs from chunks when they load.");
            lore.add("");
            lore.add("§c⚠ Warning: This is destructive!");
            lore.add("§cExisting mobs will be deleted.");
            lore.add("");
            lore.add("§eClick to toggle!");
            chunkMeta.setLore(lore);
            chunkCleanup.setItemMeta(chunkMeta);
        }
        gui.setItem(11, chunkCleanup);

        ItemStack worldGuardItem = new ItemStack(
                worldGuardBypass ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta wgMeta = worldGuardItem.getItemMeta();
        if (wgMeta != null) {
            wgMeta.setDisplayName("§6WorldGuard Bypass");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Status: " + (worldGuardBypass ? "§aEnabled" : "§cDisabled"));
            lore.add("");
            lore.add("§7When enabled, mobs can spawn");
            lore.add("§7in WorldGuard protected regions");
            lore.add("§7even if disabled in that world.");
            lore.add("");
            lore.add("§eClick to toggle!");
            wgMeta.setLore(lore);
            worldGuardItem.setItemMeta(wgMeta);
        }
        gui.setItem(15, worldGuardItem);

        ItemStack backButton = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§eBack to World Select");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(22, backButton);

        player.openInventory(gui);
    }
}
