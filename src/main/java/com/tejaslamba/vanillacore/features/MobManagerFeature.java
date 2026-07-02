package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.MobManagerListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
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

public class MobManagerFeature extends BaseFeature {

    public static final String GUI_TITLE_PLAIN = "Mob Manager";
    private static final int[] CONTENT_SLOTS = calculateContentSlots();
    private static final String CONFIG_PATH_PREFIX = "features.mob-manager.worlds.";
    private static final String VERBOSE_PREFIX = "[VERBOSE] Mob Manager - ";

    private final Map<String, Map<EntityType, Boolean>> worldDisabledMobs = new HashMap<>();
    private final Set<CreatureSpawnEvent.SpawnReason> allowedSpawnReasons = new HashSet<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> playerSelectedWorld = new HashMap<>();
    private final List<EntityType> spawnableEntities = new ArrayList<>();
    private final List<CreatureSpawnEvent.SpawnReason> allSpawnReasons = new ArrayList<>();

    private boolean chunkCleanupEnabled = false;
    private boolean worldGuardBypass = true;
    private MobManagerListener listener;

    public MobManagerFeature() {
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
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new MobManagerListener(plugin, this);
        super.onEnable(plugin);

        chunkCleanupEnabled = plugin.getConfigManager().get()
                .getBoolean("features.mob-manager.chunk-cleanup-enabled", false);
        worldGuardBypass = plugin.getConfigManager().get()
                .getBoolean("features.mob-manager.worldguard-bypass", true);

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
    public int getDisplayOrder() {
        return 22;
    }

    @Override
    public String getName() {
        return "Mob Manager";
    }

    @Override
    public String getConfigPath() {
        return "features.mob-manager";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ZOMBIE_SPAWN_EGG,
                plugin.getMessageManager().getRaw("feature-menus.mob-manager.name"),
                plugin.getMessageManager().getRaw("feature-menus.mob-manager.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-open-gui"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        if (!isEnabled()) {
            plugin.getMessageManager().sendPrefixed(player, "mob-manager.feature-disabled");
            return;
        }
        openWorldSelectGUI(player);
    }

    public void openWorldSelectGUI(Player player) {
        List<World> worlds = Bukkit.getWorlds();
        int size = Math.min(54, ((worlds.size() + 8) / 9 + 1) * 9);
        size = Math.max(27, size);
        Inventory gui = Bukkit.createInventory(new GuiHolder("mob-manager-world-select"), size,
                plugin.getMessageManager().get("mob-manager.gui.world-select.title"));

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
                meta.displayName(plugin.getMessageManager().get("mob-manager.gui.world-select.world.name",
                        "world", world.getName()));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("mob-manager.gui.world-select.world.environment",
                        "environment", world.getEnvironment().name()));
                Map<EntityType, Boolean> worldMobs = worldDisabledMobs.get(world.getName());
                long disabledCount = worldMobs != null ? worldMobs.values().stream().filter(b -> b).count() : 0;
                lore.add(plugin.getMessageManager().get("mob-manager.gui.world-select.world.disabled-mobs",
                        "count", disabledCount));
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("mob-manager.gui.shared.manage-action"));
                meta.lore(lore);
                worldItem.setItemMeta(meta);
            }
            gui.setItem(slot++, worldItem);
        }

        ItemStack allWorlds = new ItemStack(Material.NETHER_STAR);
        ItemMeta allMeta = allWorlds.getItemMeta();
        if (allMeta != null) {
            allMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.world-select.all-worlds.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("mob-manager.gui.world-select.all-worlds.lore-1"));
            lore.add(plugin.getMessageManager().get("mob-manager.gui.world-select.all-worlds.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("mob-manager.gui.shared.manage-action"));
            allMeta.lore(lore);
            allWorlds.setItemMeta(allMeta);
        }
        gui.setItem(size - 5, allWorlds);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        if (backItemMeta != null) {
            backItemMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.shared.back-main"));
            backItem.setItemMeta(backItemMeta);
        }
        gui.setItem(size - 3, backItem);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.shared.close"));
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(size - 1, closeItem);

        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        if (settingsMeta != null) {
            settingsMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.world-select.settings.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    chunkCleanupEnabled
                            ? "mob-manager.gui.world-select.settings.chunk-cleanup-enabled"
                            : "mob-manager.gui.world-select.settings.chunk-cleanup-disabled"));
            lore.add(plugin.getMessageManager().get(
                    worldGuardBypass
                            ? "mob-manager.gui.world-select.settings.worldguard-enabled"
                            : "mob-manager.gui.world-select.settings.worldguard-disabled"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("mob-manager.gui.world-select.settings.action"));
            settingsMeta.lore(lore);
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
        Inventory gui = Bukkit.createInventory(new GuiHolder("mob-manager"), 54,
                plugin.getMessageManager().get("mob-manager.gui.main.title",
                        "world", titleWorld,
                        "page", page + 1,
                        "total", totalPages));

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
            meta.displayName(plugin.getMessageManager().get(
                    isDisabled ? "mob-manager.gui.main.mob.disabled-name" : "mob-manager.gui.main.mob.enabled-name",
                    "mob", mobName));
            meta.lore(createMobItemLore(isDisabled, entityType));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<Component> createMobItemLore(boolean isDisabled, EntityType entityType) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (isDisabled) {
            lore.add(plugin.getMessageManager().get("mob-manager.gui.main.mob.spawning-disabled"));
        } else {
            lore.add(plugin.getMessageManager().get("mob-manager.gui.main.mob.spawning-enabled"));
        }

        if (!hasSpawnEgg(entityType)) {
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("mob-manager.gui.main.mob.no-egg"));
        }

        lore.add(Component.empty());
        lore.add(plugin.getMessageManager().get("mob-manager.gui.shared.toggle-action"));
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
            gui.setItem(45, createButtonWithLore(Material.ARROW,
                    "mob-manager.gui.main.navigation.previous.name",
                    List.of(plugin.getMessageManager().get("mob-manager.gui.main.navigation.page",
                            "page", page,
                            "total", totalPages))));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createButtonWithLore(Material.ARROW,
                    "mob-manager.gui.main.navigation.next.name",
                    List.of(plugin.getMessageManager().get("mob-manager.gui.main.navigation.page",
                            "page", page + 2,
                            "total", totalPages))));
        }

        gui.setItem(49, createSimpleButton(Material.OAK_DOOR, "mob-manager.gui.main.navigation.back"));
        gui.setItem(50, createSimpleButton(Material.BARRIER, "mob-manager.gui.shared.close"));
        gui.setItem(47, createButtonWithLore(Material.LIME_DYE, "mob-manager.gui.main.navigation.enable-all.name",
                List.of(
                        plugin.getMessageManager().get("mob-manager.gui.main.navigation.enable-all.lore-1"),
                        plugin.getMessageManager().get("mob-manager.gui.main.navigation.enable-all.lore-2"))));
        gui.setItem(51,
                createButtonWithLore(Material.RED_DYE, "mob-manager.gui.main.navigation.disable-all.name",
                        List.of(
                                plugin.getMessageManager().get("mob-manager.gui.main.navigation.disable-all.lore-1"),
                                plugin.getMessageManager().get("mob-manager.gui.main.navigation.disable-all.lore-2"))));
        gui.setItem(48,
                createButtonWithLore(Material.SPAWNER, "mob-manager.gui.main.navigation.spawn-reasons.name",
                        List.of(
                                plugin.getMessageManager().get("mob-manager.gui.main.navigation.spawn-reasons.lore-1"),
                                plugin.getMessageManager().get("mob-manager.gui.main.navigation.spawn-reasons.lore-2"),
                                Component.empty(),
                                plugin.getMessageManager()
                                        .get("mob-manager.gui.main.navigation.spawn-reasons.action"))));
    }

    private ItemStack createButtonWithLore(Material material, String displayNamePath, List<Component> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(displayNamePath));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.addAll(loreLines);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSimpleButton(Material material, String displayNamePath) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(displayNamePath));
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
                .getBoolean("features.mob-manager.chunk-cleanup-enabled", false);
        worldGuardBypass = plugin.getConfigManager().get()
                .getBoolean("features.mob-manager.worldguard-bypass", true);
        initializeWorldData();
        loadAllWorldDisabledMobs();
        loadAllowedSpawnReasons();
    }

    private void loadAllowedSpawnReasons() {
        allowedSpawnReasons.clear();
        List<String> reasons = plugin.getConfigManager().get()
                .getStringList("features.mob-manager.allowed-spawn-reasons");
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
        plugin.getConfigManager().get().set("features.mob-manager.allowed-spawn-reasons", reasons);
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
        Inventory gui = Bukkit.createInventory(new GuiHolder("mob-manager-spawn-reasons"), 54,
                plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.title"));

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
            meta.displayName(plugin.getMessageManager().get(
                    isAllowed
                            ? "mob-manager.gui.spawn-reasons.reason.allowed-name"
                            : "mob-manager.gui.spawn-reasons.reason.blocked-name",
                    "reason", reasonName));
            meta.lore(createSpawnReasonLore(reason, isAllowed));
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<Component> createSpawnReasonLore(CreatureSpawnEvent.SpawnReason reason, boolean isAllowed) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        String description = getSpawnReasonDescription(reason);
        lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.description",
                "description", description));
        lore.add(Component.empty());

        if (isAllowed) {
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.allowed-status"));
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.allowed-lore-1"));
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.allowed-lore-2"));
        } else {
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.blocked-status"));
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.blocked-lore-1"));
            lore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.reason.blocked-lore-2"));
        }
        lore.add(Component.empty());
        lore.add(plugin.getMessageManager().get("mob-manager.gui.shared.toggle-action"));
        return lore;
    }

    private String getSpawnReasonDescription(CreatureSpawnEvent.SpawnReason reason) {
        return switch (reason) {
            case NATURAL -> "Mob spawned naturally (random world spawn)";
            case JOCKEY -> "Mob spawned as part of a jockey (e.g. skeleton on spider)";
            case CHUNK_GEN -> "Mob spawned during chunk generation";
            case SPAWNER -> "Mob spawned from a mob spawner block";
            case EGG -> "Mob spawned from a thrown egg";
            case SPAWNER_EGG -> "Mob spawned using a spawn egg item";
            case LIGHTNING -> "Mob spawned from lightning strike (e.g. skeleton horse)";
            case BUILD_SNOWMAN -> "Snow golem built by player";
            case BUILD_IRONGOLEM -> "Iron golem built by player";
            case BUILD_WITHER -> "Wither boss built by player";
            case VILLAGE_DEFENSE -> "Iron golem spawned for village defense";
            case VILLAGE_INVASION -> "Zombie spawned during village siege";
            case BREEDING -> "Mob spawned from animal breeding";
            case SLIME_SPLIT -> "Small slime spawned from larger slime death";
            case REINFORCEMENTS -> "Zombie reinforcement spawned in combat";
            case NETHER_PORTAL -> "Zombified piglin spawned near portal";
            case DISPENSE_EGG -> "Mob spawned from dispensed spawn egg";
            case INFECTION -> "Villager converted to zombie villager";
            case CURED -> "Zombie villager cured back to villager";
            case OCELOT_BABY -> "Ocelot baby spawned with parent";
            case SILVERFISH_BLOCK -> "Silverfish emerged from infested block";
            case MOUNT -> "Mob spawned as mount for another entity";
            case TRAP -> "Skeleton trap horse spawned during storm";
            case ENDER_PEARL -> "Endermite spawned from ender pearl";
            case SHOULDER_ENTITY -> "Parrot spawned from player shoulder";
            case DROWNED -> "Zombie converted to drowned in water";
            case SHEARED -> "Mushroom spawned from sheared mooshroom";
            case EXPLOSION -> "Mob spawned from explosion (e.g. creeper)";
            case RAID -> "Mob spawned as part of a raid";
            case PATROL -> "Pillager patrol spawned in world";
            case BEEHIVE -> "Bee spawned from beehive/nest";
            case PIGLIN_ZOMBIFIED -> "Piglin converted to zombified piglin";
            case SPELL -> "Mob spawned by magic spell (e.g. evoker)";
            case FROZEN -> "Entity frozen (stray from skeleton)";
            case COMMAND -> "Mob spawned via /summon command";
            case CUSTOM -> "Mob spawned by plugin/custom code";
            case DEFAULT -> "Default spawn (unknown reason)";
            case METAMORPHOSIS -> "Mob transformed (e.g. tadpole to frog)";
            case TRIAL_SPAWNER -> "Mob spawned from trial spawner";
            case DUPLICATION -> "Mob duplicated (e.g. allay duplication)";
            case ENCHANTMENT -> "Mob spawned via enchantment effect";
            default -> "Unknown spawn reason";
        };
    }

    private void addSpawnReasonNavigationButtons(Inventory gui) {
        ItemStack backButton = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.back"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.shared.close"));
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
            infoMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.name"));
            List<Component> infoLore = new ArrayList<>();
            infoLore.add(Component.empty());
            infoLore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.lore-1"));
            infoLore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.lore-2"));
            infoLore.add(Component.empty());
            infoLore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.example-1"));
            infoLore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.example-2"));
            infoLore.add(plugin.getMessageManager().get("mob-manager.gui.spawn-reasons.info.example-3"));
            infoMeta.lore(infoLore);
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
        plugin.getConfigManager().get().set("features.mob-manager.chunk-cleanup-enabled", enabled);
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
        plugin.getConfigManager().get().set("features.mob-manager.worldguard-bypass", enabled);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info(VERBOSE_PREFIX + "WorldGuard bypass " + (enabled ? "enabled" : "disabled"));
        }
    }

    public void openGlobalSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GuiHolder("mob-manager-settings"), 27,
                plugin.getMessageManager().get("mob-manager.gui.settings.title"));

        ItemStack chunkCleanup = new ItemStack(
                chunkCleanupEnabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta chunkMeta = chunkCleanup.getItemMeta();
        if (chunkMeta != null) {
            chunkMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.settings.chunk-cleanup.name"));
            List<Component> chunkLore = new ArrayList<>();
            chunkLore.add(Component.empty());
            chunkLore.add(plugin.getMessageManager().get(
                    chunkCleanupEnabled
                            ? "mob-manager.gui.settings.chunk-cleanup.status-enabled"
                            : "mob-manager.gui.settings.chunk-cleanup.status-disabled"));
            chunkLore.add(Component.empty());
            chunkLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.chunk-cleanup.lore-1"));
            chunkLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.chunk-cleanup.lore-2"));
            chunkLore.add(Component.empty());
            chunkLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.chunk-cleanup.warning-1"));
            chunkLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.chunk-cleanup.warning-2"));
            chunkLore.add(Component.empty());
            chunkLore.add(plugin.getMessageManager().get("mob-manager.gui.shared.toggle-action"));
            chunkMeta.lore(chunkLore);
            chunkCleanup.setItemMeta(chunkMeta);
        }
        gui.setItem(11, chunkCleanup);

        ItemStack worldGuardItem = new ItemStack(
                worldGuardBypass ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta wgMeta = worldGuardItem.getItemMeta();
        if (wgMeta != null) {
            wgMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.settings.worldguard.name"));
            List<Component> wgLore = new ArrayList<>();
            wgLore.add(Component.empty());
            wgLore.add(plugin.getMessageManager().get(
                    worldGuardBypass
                            ? "mob-manager.gui.settings.worldguard.status-enabled"
                            : "mob-manager.gui.settings.worldguard.status-disabled"));
            wgLore.add(Component.empty());
            wgLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.worldguard.lore-1"));
            wgLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.worldguard.lore-2"));
            wgLore.add(plugin.getMessageManager().get("mob-manager.gui.settings.worldguard.lore-3"));
            wgLore.add(Component.empty());
            wgLore.add(plugin.getMessageManager().get("mob-manager.gui.shared.toggle-action"));
            wgMeta.lore(wgLore);
            worldGuardItem.setItemMeta(wgMeta);
        }
        gui.setItem(15, worldGuardItem);

        ItemStack backButton = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getMessageManager().get("mob-manager.gui.settings.back"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(22, backButton);

        player.openInventory(gui);
    }
}

