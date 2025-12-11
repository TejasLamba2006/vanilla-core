package com.tejaslamba.smpcore.features;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.feature.BaseFeature;
import com.tejaslamba.smpcore.listener.MobSpawningListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private static final int[] CONTENT_SLOTS = calculateContentSlots();
    private static final String CONFIG_PATH_PREFIX = "features.mob-spawning.disabled-mobs.";
    private static final String VERBOSE_CONFIG = "plugin.verbose";
    private static final String VERBOSE_PREFIX = "[VERBOSE] Mob Spawning - ";
    private static final String SPAWN_EGG_SUFFIX = "_SPAWN_EGG";

    private final Map<EntityType, Boolean> disabledMobs = new EnumMap<>(EntityType.class);
    private final Set<CreatureSpawnEvent.SpawnReason> allowedSpawnReasons = new HashSet<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final List<EntityType> spawnableEntities = new ArrayList<>();
    private final List<CreatureSpawnEvent.SpawnReason> allSpawnReasons = new ArrayList<>();
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
            if (material.name().endsWith(SPAWN_EGG_SUFFIX)) {
                EntityType entityType = getEntityTypeFromSpawnEgg(material);
                if (entityType != null && entityType != EntityType.UNKNOWN) {
                    spawnableEntities.add(entityType);
                }
            }
        }
        spawnableEntities.sort(Comparator.comparing(Enum::name));
    }

    private EntityType getEntityTypeFromSpawnEgg(Material spawnEgg) {
        String name = spawnEgg.name().replace(SPAWN_EGG_SUFFIX, "");
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Material getSpawnEggFromEntityType(EntityType entityType) {
        String materialName = entityType.name() + SPAWN_EGG_SUFFIX;
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void onEnable(Main plugin) {
        listener = new MobSpawningListener(plugin, this);
        super.onEnable(plugin);
        loadDisabledMobs();
        loadAllowedSpawnReasons();

        boolean verbose = plugin.getConfigManager().get().getBoolean(VERBOSE_CONFIG, false);
        if (verbose) {
            long disabledCount = disabledMobs.values().stream().filter(b -> b).count();
            String msg1 = String.format("%sLoaded %d/%d mobs as disabled",
                    VERBOSE_PREFIX, disabledCount, spawnableEntities.size());
            plugin.getLogger().info(msg1);
            String msg2 = String.format("%sLoaded %d allowed spawn reasons",
                    VERBOSE_PREFIX, allowedSpawnReasons.size());
            plugin.getLogger().info(msg2);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveDisabledMobs();
        saveAllowedSpawnReasons();
        playerPages.clear();
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
        openMobGUI(player, 0);
    }

    public void openMobGUI(Player player, int page) {
        int totalPages = getTotalPages();
        page = Math.clamp(page, 0, totalPages - 1);
        playerPages.put(player.getUniqueId(), page);

        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE + " §7(Page " + (page + 1) + "/" + totalPages + ")");

        populateMobItems(gui, page);
        addMobGUINavigationButtons(gui, page, totalPages);

        player.openInventory(gui);
    }

    private void populateMobItems(Inventory gui, int page) {
        int startIndex = page * CONTENT_SLOTS.length;
        int slotIndex = 0;

        for (int i = startIndex; i < spawnableEntities.size() && slotIndex < CONTENT_SLOTS.length; i++) {
            EntityType entityType = spawnableEntities.get(i);
            Material spawnEgg = getSpawnEggFromEntityType(entityType);

            if (spawnEgg != null) {
                gui.setItem(CONTENT_SLOTS[slotIndex], createMobItem(entityType, spawnEgg));
                slotIndex++;
            }
        }
    }

    private ItemStack createMobItem(EntityType entityType, Material spawnEgg) {
        boolean isDisabled = isDisabled(entityType);
        ItemStack item = new ItemStack(spawnEgg);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String mobName = formatEntityName(entityType);
            meta.setDisplayName((isDisabled ? "§c" : "§a") + mobName);
            meta.setLore(createMobItemLore(isDisabled));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<String> createMobItemLore(boolean isDisabled) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isDisabled) {
            lore.add("§cSpawning: §4Disabled");
        } else {
            lore.add("§aSpawning: §2Enabled");
        }
        lore.add("");
        lore.add("§eClick to toggle!");
        return lore;
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

        gui.setItem(49, createSimpleButton(Material.OAK_DOOR, "§eBack to Main Menu"));
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

    private void loadDisabledMobs() {
        disabledMobs.clear();
        for (EntityType entityType : spawnableEntities) {
            String configKey = CONFIG_PATH_PREFIX + entityType.name().toLowerCase();
            boolean isDisabled = plugin.getConfigManager().get().getBoolean(configKey, false);
            disabledMobs.put(entityType, isDisabled);
        }
    }

    private void saveDisabledMobs() {
        for (Map.Entry<EntityType, Boolean> entry : disabledMobs.entrySet()) {
            String configKey = CONFIG_PATH_PREFIX + entry.getKey().name().toLowerCase();
            plugin.getConfigManager().get().set(configKey, entry.getValue());
        }
        plugin.getConfigManager().save();
    }

    public boolean isDisabled(EntityType entityType) {
        return disabledMobs.getOrDefault(entityType, false);
    }

    public void setDisabled(EntityType entityType, boolean disabled) {
        disabledMobs.put(entityType, disabled);
        String configKey = CONFIG_PATH_PREFIX + entityType.name().toLowerCase();
        plugin.getConfigManager().get().set(configKey, disabled);
        plugin.getConfigManager().save();

        boolean verbose = plugin.getConfigManager().get().getBoolean(VERBOSE_CONFIG, false);
        if (verbose) {
            String msg = String.format("%s%s state changed: disabled=%s",
                    VERBOSE_PREFIX, entityType.name(), disabled);
            plugin.getLogger().info(msg);
        }
    }

    public void setAllDisabled(boolean disabled) {
        for (EntityType entityType : spawnableEntities) {
            disabledMobs.put(entityType, disabled);
            String configKey = CONFIG_PATH_PREFIX + entityType.name().toLowerCase();
            plugin.getConfigManager().get().set(configKey, disabled);
        }
        plugin.getConfigManager().save();

        boolean verbose = plugin.getConfigManager().get().getBoolean(VERBOSE_CONFIG, false);
        if (verbose) {
            String msg = String.format("%sAll mobs set to disabled=%s",
                    VERBOSE_PREFIX, disabled);
            plugin.getLogger().info(msg);
        }
    }

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
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
        loadDisabledMobs();
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
                // Skip invalid spawn reason names from config
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

        boolean verbose = plugin.getConfigManager().get().getBoolean(VERBOSE_CONFIG, false);
        if (verbose) {
            String msg = String.format("%sSpawn reason %s state changed: allowed=%s",
                    VERBOSE_PREFIX, reason.name(), allowed);
            plugin.getLogger().info(msg);
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
}
