package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.MobManagerListener;
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

    public static final String GUI_TITLE = "§8Mob Manager";
    public static final String SPAWN_REASONS_GUI_TITLE = "§8Spawn Reasons Config";
    public static final String WORLD_SELECT_GUI_TITLE = "§8Select World";
    public static final String SETTINGS_GUI_TITLE = "§8Mob Manager Settings";
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
        return createMenuItem(Material.ZOMBIE_SPAWN_EGG, "<!italic><gold>Mob Manager",
                "<!italic><gray>Control mob spawning per world");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Open GUI");
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        if (!isEnabled()) {
            player.sendMessage(MessageManager.parse("<red>Mob Manager is disabled! Enable it first."));
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
                meta.displayName(MessageManager.parse("<!italic><green>" + world.getName()));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<gray>Environment: <yellow>" + world.getEnvironment().name()));
                Map<EntityType, Boolean> worldMobs = worldDisabledMobs.get(world.getName());
                long disabledCount = worldMobs != null ? worldMobs.values().stream().filter(b -> b).count() : 0;
                lore.add(MessageManager.parse("<gray>Disabled Mobs: <yellow>" + disabledCount));
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<yellow>Click to manage!"));
                meta.lore(lore);
                worldItem.setItemMeta(meta);
            }
            gui.setItem(slot++, worldItem);
        }

        ItemStack allWorlds = new ItemStack(Material.NETHER_STAR);
        ItemMeta allMeta = allWorlds.getItemMeta();
        if (allMeta != null) {
            allMeta.displayName(MessageManager.parse("<!italic><gold>\u2605 All Worlds"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Manage mob spawning for"));
            lore.add(MessageManager.parse("<gray>ALL worlds at once."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<yellow>Click to manage!"));
            allMeta.lore(lore);
            allWorlds.setItemMeta(allMeta);
        }
        gui.setItem(size - 5, allWorlds);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(MessageManager.parse("<!italic><red>Close"));
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(size - 1, closeItem);

        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        if (settingsMeta != null) {
            settingsMeta.displayName(MessageManager.parse("<!italic><gold>Global Settings"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager
                    .parse("<gray>Chunk Cleanup: " + (chunkCleanupEnabled ? "<green>Enabled" : "<red>Disabled")));
            lore.add(MessageManager
                    .parse("<gray>WorldGuard Bypass: " + (worldGuardBypass ? "<green>Enabled" : "<red>Disabled")));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<yellow>Click to configure!"));
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
        Inventory gui = Bukkit.createInventory(null, 54, MessageManager.parse(
                "<dark_gray>Mob Manager <gray>[<yellow>" + titleWorld + "<gray>] (" + (page + 1) + "/" + totalPages
                        + ")"));

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
            meta.displayName(MessageManager.parse("<!italic>" + (isDisabled ? "<red>" : "<green>") + mobName));
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
            lore.add(MessageManager.parse("<red>Spawning: <dark_red>Disabled"));
        } else {
            lore.add(MessageManager.parse("<green>Spawning: <dark_green>Enabled"));
        }

        if (!hasSpawnEgg(entityType)) {
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray><italic>(No spawn egg - special mob)"));
        }

        lore.add(Component.empty());
        lore.add(MessageManager.parse("<yellow>Click to toggle!"));
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
            gui.setItem(45, createButtonWithLore(Material.ARROW, "<!italic><green>\u00ab Previous Page",
                    "<gray>Page <yellow>" + page + "<gray>/<yellow>" + totalPages));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createButtonWithLore(Material.ARROW, "<!italic><green>Next Page \u00bb",
                    "<gray>Page <yellow>" + (page + 2) + "<gray>/<yellow>" + totalPages));
        }

        gui.setItem(49, createSimpleButton(Material.OAK_DOOR, "<!italic><yellow>Back to World Select"));
        gui.setItem(50, createSimpleButton(Material.BARRIER, "<!italic><red>Close"));
        gui.setItem(47, createButtonWithLore(Material.LIME_DYE, "<!italic><green>Enable All Mobs",
                "<gray>Click to enable spawning", "<gray>for all mob types."));
        gui.setItem(51, createButtonWithLore(Material.RED_DYE, "<!italic><red>Disable All Mobs",
                "<gray>Click to disable spawning", "<gray>for all mob types."));
        gui.setItem(48, createButtonWithLore(Material.SPAWNER, "<!italic><gold>Spawn Reasons Config",
                "<gray>Configure which spawn reasons", "<gray>bypass mob blocking.", "",
                "<yellow>Click to configure!"));
    }

    private ItemStack createButtonWithLore(Material material, String displayName, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse(displayName));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            for (String line : loreLines) {
                lore.add(MessageManager.parse(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSimpleButton(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse(displayName));
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
            meta.displayName(MessageManager.parse("<!italic>" + (isAllowed ? "<green>" : "<red>") + reasonName));
            meta.lore(createSpawnReasonLore(reason, isAllowed));
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<Component> createSpawnReasonLore(CreatureSpawnEvent.SpawnReason reason, boolean isAllowed) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        String description = getSpawnReasonDescription(reason);
        lore.add(MessageManager.parse("<dark_gray>" + description));
        lore.add(Component.empty());

        if (isAllowed) {
            lore.add(MessageManager.parse("<green>Bypass: <dark_green>Allowed"));
            lore.add(MessageManager.parse("<gray>Mobs CAN spawn via this reason"));
            lore.add(MessageManager.parse("<gray>even if mob type is disabled."));
        } else {
            lore.add(MessageManager.parse("<red>Bypass: <dark_red>Blocked"));
            lore.add(MessageManager.parse("<gray>Mobs CANNOT spawn via this"));
            lore.add(MessageManager.parse("<gray>reason if mob type is disabled."));
        }
        lore.add(Component.empty());
        lore.add(MessageManager.parse("<yellow>Click to toggle!"));
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
            backMeta.displayName(MessageManager.parse("<!italic><yellow>Back to Mob Manager"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(MessageManager.parse("<!italic><red>Close"));
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
            infoMeta.displayName(MessageManager.parse("<!italic><gold>Spawn Reasons Info"));
            List<Component> infoLore = new ArrayList<>();
            infoLore.add(Component.empty());
            infoLore.add(MessageManager.parse("<gray>Allowed spawn reasons will"));
            infoLore.add(MessageManager.parse("<gray>bypass mob blocking."));
            infoLore.add(Component.empty());
            infoLore.add(MessageManager.parse("<gray>Example: If <green>SPAWNER_EGG <gray>is allowed,"));
            infoLore.add(MessageManager.parse("<gray>disabled mobs can still spawn"));
            infoLore.add(MessageManager.parse("<gray>from spawn eggs."));
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
        Inventory gui = Bukkit.createInventory(null, 27, SETTINGS_GUI_TITLE);

        ItemStack chunkCleanup = new ItemStack(
                chunkCleanupEnabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta chunkMeta = chunkCleanup.getItemMeta();
        if (chunkMeta != null) {
            chunkMeta.displayName(MessageManager.parse("<!italic><gold>Chunk Cleanup"));
            List<Component> chunkLore = new ArrayList<>();
            chunkLore.add(Component.empty());
            chunkLore.add(MessageManager
                    .parse("<gray>Status: " + (chunkCleanupEnabled ? "<green>Enabled" : "<red>Disabled")));
            chunkLore.add(Component.empty());
            chunkLore.add(MessageManager.parse("<gray>When enabled, removes disabled"));
            chunkLore.add(MessageManager.parse("<gray>mobs from chunks when they load."));
            chunkLore.add(Component.empty());
            chunkLore.add(MessageManager.parse("<red>⚠ Warning: This is destructive!"));
            chunkLore.add(MessageManager.parse("<red>Existing mobs will be deleted."));
            chunkLore.add(Component.empty());
            chunkLore.add(MessageManager.parse("<yellow>Click to toggle!"));
            chunkMeta.lore(chunkLore);
            chunkCleanup.setItemMeta(chunkMeta);
        }
        gui.setItem(11, chunkCleanup);

        ItemStack worldGuardItem = new ItemStack(
                worldGuardBypass ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta wgMeta = worldGuardItem.getItemMeta();
        if (wgMeta != null) {
            wgMeta.displayName(MessageManager.parse("<!italic><gold>WorldGuard Bypass"));
            List<Component> wgLore = new ArrayList<>();
            wgLore.add(Component.empty());
            wgLore.add(
                    MessageManager.parse("<gray>Status: " + (worldGuardBypass ? "<green>Enabled" : "<red>Disabled")));
            wgLore.add(Component.empty());
            wgLore.add(MessageManager.parse("<gray>When enabled, mobs can spawn"));
            wgLore.add(MessageManager.parse("<gray>in WorldGuard protected regions"));
            wgLore.add(MessageManager.parse("<gray>even if disabled in that world."));
            wgLore.add(Component.empty());
            wgLore.add(MessageManager.parse("<yellow>Click to toggle!"));
            wgMeta.lore(wgLore);
            worldGuardItem.setItemMeta(wgMeta);
        }
        gui.setItem(15, worldGuardItem);

        ItemStack backButton = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(MessageManager.parse("<!italic><yellow>Back to World Select"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(22, backButton);

        player.openInventory(gui);
    }
}
