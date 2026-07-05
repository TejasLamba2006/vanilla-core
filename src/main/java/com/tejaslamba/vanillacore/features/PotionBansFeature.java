package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.PotionBansListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PotionBansFeature extends BaseFeature {

    private static final Map<String, String> EFFECT_ALIASES = Map.ofEntries(
            Map.entry("SWIFTNESS", "speed"),
            Map.entry("HEALING", "instant_health"),
            Map.entry("HARMING", "instant_damage"),
            Map.entry("JUMP", "jump_boost"),
            Map.entry("FIRERESISTANCE", "fire_resistance"),
            Map.entry("TURTLEMASTER", "resistance"),
            Map.entry("REGEN", "regeneration"),
            Map.entry("SLOW", "slowness"));

    private static final List<EffectOption> EFFECT_OPTIONS = List.of(
            new EffectOption("strength", "Strength", Material.SPLASH_POTION),
            new EffectOption("speed", "Swiftness", Material.SUGAR),
            new EffectOption("regeneration", "Regeneration", Material.GHAST_TEAR),
            new EffectOption("fire_resistance", "Fire Resistance", Material.MAGMA_CREAM),
            new EffectOption("instant_health", "Instant Health", Material.GLISTERING_MELON_SLICE),
            new EffectOption("instant_damage", "Instant Damage", Material.SPIDER_EYE),
            new EffectOption("jump_boost", "Jump Boost", Material.RABBIT_FOOT),
            new EffectOption("night_vision", "Night Vision", Material.GOLDEN_CARROT),
            new EffectOption("invisibility", "Invisibility", Material.FERMENTED_SPIDER_EYE),
            new EffectOption("water_breathing", "Water Breathing", Material.PUFFERFISH),
            new EffectOption("slow_falling", "Slow Falling", Material.PHANTOM_MEMBRANE),
            new EffectOption("turtle_master", "Turtle Master", Material.TURTLE_HELMET),
            new EffectOption("poison", "Poison", Material.POISONOUS_POTATO),
            new EffectOption("weakness", "Weakness", Material.POTION),
            new EffectOption("slowness", "Slowness", Material.SOUL_SAND));

    private static final int[] EFFECT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28
    };

    private PotionBansListener listener;
    private final Map<String, TierBlockState> blockedEffects = new LinkedHashMap<>();

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new PotionBansListener(plugin);
        super.onEnable(plugin);
        loadSettings();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Potion Bans - Feature loaded");
        }
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        String basePath = getConfigPath();
        blockedEffects.clear();

        for (EffectOption option : EFFECT_OPTIONS) {
            blockedEffects.put(option.key(), new TierBlockState());
        }

        List<String> configuredEffects = plugin.getConfigManager().get().getStringList(basePath + ".blocked-effects");
        for (String configuredEffect : configuredEffects) {
            ParsedTierConfig parsed = parseBlockedEffectEntry(configuredEffect);
            if (parsed == null || parsed.key().isEmpty()) {
                continue;
            }

            TierBlockState state = blockedEffects.computeIfAbsent(parsed.key(), k -> new TierBlockState());
            state.setTier1Blocked(parsed.tier1Blocked());
            state.setTier2Blocked(parsed.tier2Blocked());
        }
    }

    public boolean isAllPotionsBanned() {
        for (EffectOption option : EFFECT_OPTIONS) {
            TierBlockState state = blockedEffects.get(option.key());
            if (state == null || !state.isTier1Blocked() || !state.isTier2Blocked()) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllTier2Banned() {
        for (EffectOption option : EFFECT_OPTIONS) {
            TierBlockState state = blockedEffects.get(option.key());
            if (state == null || !state.isTier2Blocked()) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getBlockedEffects() {
        Set<String> out = new LinkedHashSet<>();
        for (Map.Entry<String, TierBlockState> entry : blockedEffects.entrySet()) {
            if (entry.getValue().isTier1Blocked() || entry.getValue().isTier2Blocked()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    public boolean isEffectBlocked(PotionEffectType effectType, boolean isTier2) {
        if (effectType == null) {
            return false;
        }

        String namespacedKey = normalizeEffectToken(effectType.getKey().getKey());
        if (isEffectTierBlocked(namespacedKey, isTier2)) {
            return true;
        }

        String legacyName = effectType.getName();
        if (legacyName != null && isEffectTierBlocked(normalizeEffectToken(legacyName), isTier2)) {
            return true;
        }

        if (("resistance".equals(namespacedKey) || "slowness".equals(namespacedKey))
                && isEffectTierBlocked("turtle_master", isTier2)) {
            return true;
        }

        return false;
    }

    public boolean isPotionTypeBlocked(PotionType potionType) {
        if (potionType == null) {
            return false;
        }

        String potionName = potionType.name();
        boolean isTier2 = potionName.startsWith("STRONG_");

        String baseName = potionName;
        if (baseName.startsWith("STRONG_")) {
            baseName = baseName.substring("STRONG_".length());
        } else if (baseName.startsWith("LONG_")) {
            baseName = baseName.substring("LONG_".length());
        }

        String normalized = normalizeEffectToken(baseName);
        if (isEffectTierBlocked(normalized, isTier2)) {
            return true;
        }

        if ("turtle_master".equals(normalized)) {
            return isEffectTierBlocked("resistance", isTier2)
                    || isEffectTierBlocked("slowness", isTier2)
                    || isEffectTierBlocked("turtle_master", isTier2);
        }

        return false;
    }

    public int getTier1BlockedCount() {
        int count = 0;
        for (EffectOption option : EFFECT_OPTIONS) {
            TierBlockState state = blockedEffects.get(option.key());
            if (state != null && state.isTier1Blocked()) {
                count++;
            }
        }
        return count;
    }

    public int getTier2BlockedCount() {
        int count = 0;
        for (EffectOption option : EFFECT_OPTIONS) {
            TierBlockState state = blockedEffects.get(option.key());
            if (state != null && state.isTier2Blocked()) {
                count++;
            }
        }
        return count;
    }

    private boolean isEffectTierBlocked(String effectKey, boolean tier2) {
        if (effectKey == null || effectKey.isEmpty()) {
            return false;
        }

        TierBlockState state = blockedEffects.get(effectKey);
        if (state == null) {
            return false;
        }

        return tier2 ? state.isTier2Blocked() : state.isTier1Blocked();
    }

    private ParsedTierConfig parseBlockedEffectEntry(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String[] parts = raw.split(":");
        if (parts.length == 1) {
            String key = normalizeEffectToken(parts[0]);
            return key.isEmpty() ? null : new ParsedTierConfig(key, true, true);
        }

        if (parts.length != 3) {
            return null;
        }

        String key = normalizeEffectToken(parts[0]);
        if (key.isEmpty()) {
            return null;
        }

        boolean tier1 = Boolean.parseBoolean(parts[1].trim());
        boolean tier2 = Boolean.parseBoolean(parts[2].trim());
        return new ParsedTierConfig(key, tier1, tier2);
    }

    private String normalizeEffectToken(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }

        String normalized = token.trim().toLowerCase(Locale.ROOT)
                .replace("minecraft:", "")
                .replace('-', '_')
                .replace(' ', '_');

        String alias = EFFECT_ALIASES.get(normalized.toUpperCase(Locale.ROOT));
        if (alias != null) {
            return alias;
        }

        return switch (normalized) {
            case "strength", "increase_damage" -> "strength";
            case "speed", "swiftness" -> "speed";
            case "instant_healing", "instant_health", "heal" -> "instant_health";
            case "instant_damage", "harming", "harm" -> "instant_damage";
            case "jump", "jump_boost", "leaping" -> "jump_boost";
            case "slowness", "slow" -> "slowness";
            case "regeneration", "regen" -> "regeneration";
            case "fire_resistance", "fireresistance" -> "fire_resistance";
            case "water_breathing", "waterbreathing" -> "water_breathing";
            case "night_vision", "nightvision" -> "night_vision";
            case "slow_falling", "slowfalling" -> "slow_falling";
            case "turtle_master", "turtlemaster" -> "turtle_master";
            default -> normalized;
        };
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 16;
    }

    @Override
    public String getName() {
        return "Potion Bans";
    }

    @Override
    public String getConfigPath() {
        return "features.potion-bans";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.SPLASH_POTION,
                plugin.getMessageManager().getRaw("feature-menus.potion-bans.name"),
                plugin.getMessageManager().getRaw("feature-menus.potion-bans.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.potion-bans.all-banned")
                .replace("<state>", plugin.getMessageManager().getRaw(
                        isAllPotionsBanned() ? "feature-menus.shared.yes" : "feature-menus.shared.no")));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.potion-bans.tier1")
                .replace("<tier1>", String.valueOf(getTier1BlockedCount())));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.potion-bans.tier2")
                .replace("<tier2>", String.valueOf(getTier2BlockedCount())));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.left-click-toggle"));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shared.right-click-open-settings"));
        return lore;
    }

    @Override
    public void onLeftClick(Player player) {
        toggleDefault(player);
    }

    @Override
    public void onRightClick(Player player) {
        openSettingsGUI(player);
    }

    public void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GuiHolder("potion-bans-settings"), 54,
                plugin.getMessageManager().get("potion-bans.gui.title"));
        fillFiller(gui, 54);

        gui.setItem(45, buildAllPotionsItem());
        gui.setItem(46, buildClickGuideItem());
        gui.setItem(47, buildClearEffectsItem());
        gui.setItem(49, buildBackItem());

        for (int i = 0; i < EFFECT_OPTIONS.size() && i < EFFECT_SLOTS.length; i++) {
            EffectOption option = EFFECT_OPTIONS.get(i);
            gui.setItem(EFFECT_SLOTS[i], buildEffectItem(option));
        }

        player.openInventory(gui);
    }

    public void handleSettingsGUIClick(int slot, boolean isRightClick, Player player) {
        if (slot == 49) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMenuManager().openMainMenu(player));
            return;
        }

        if (slot == 45) {
            boolean newState = !isAllPotionsBanned();
            for (EffectOption option : EFFECT_OPTIONS) {
                TierBlockState state = blockedEffects.computeIfAbsent(option.key(), k -> new TierBlockState());
                state.setTier1Blocked(newState);
                state.setTier2Blocked(newState);
            }
            saveSettings();
            plugin.getServer().getScheduler().runTask(plugin, () -> openSettingsGUI(player));
            return;
        }

        if (slot == 47) {
            for (TierBlockState state : blockedEffects.values()) {
                state.setTier1Blocked(false);
                state.setTier2Blocked(false);
            }
            saveSettings();
            plugin.getServer().getScheduler().runTask(plugin, () -> openSettingsGUI(player));
            return;
        }

        for (int i = 0; i < EFFECT_OPTIONS.size() && i < EFFECT_SLOTS.length; i++) {
            if (EFFECT_SLOTS[i] != slot) {
                continue;
            }

            String key = EFFECT_OPTIONS.get(i).key();
            TierBlockState state = blockedEffects.computeIfAbsent(key, k -> new TierBlockState());
            if (isRightClick) {
                state.setTier1Blocked(!state.isTier1Blocked());
            } else {
                state.setTier2Blocked(!state.isTier2Blocked());
            }

            saveSettings();
            plugin.getServer().getScheduler().runTask(plugin, () -> openSettingsGUI(player));
            return;
        }
    }

    private void saveSettings() {
        String basePath = getConfigPath();
        var cfg = plugin.getConfigManager().get();

        List<String> encoded = new ArrayList<>();
        for (EffectOption option : EFFECT_OPTIONS) {
            TierBlockState state = blockedEffects.get(option.key());
            if (state != null && (state.isTier1Blocked() || state.isTier2Blocked())) {
                encoded.add(option.key() + ":" + state.isTier1Blocked() + ":" + state.isTier2Blocked());
            }
        }

        for (Map.Entry<String, TierBlockState> entry : blockedEffects.entrySet()) {
            boolean known = false;
            for (EffectOption option : EFFECT_OPTIONS) {
                if (option.key().equals(entry.getKey())) {
                    known = true;
                    break;
                }
            }

            if (known) {
                continue;
            }

            TierBlockState state = entry.getValue();
            if (state.isTier1Blocked() || state.isTier2Blocked()) {
                encoded.add(entry.getKey() + ":" + state.isTier1Blocked() + ":" + state.isTier2Blocked());
            }
        }

        cfg.set(basePath + ".blocked-effects", encoded);
        plugin.getConfigManager().save();
    }

    private ItemStack buildAllPotionsItem() {
        return buildToggleItem(Material.BARRIER, "potion-bans.gui.all-potions.name", isAllPotionsBanned());
    }

    private ItemStack buildClickGuideItem() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("potion-bans.gui.click-guide.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.click-guide.left-click"));
            lore.add(plugin.getMessageManager().get("potion-bans.gui.click-guide.right-click"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.click-guide.config-format-label"));
            lore.add(plugin.getMessageManager().get("potion-bans.gui.click-guide.config-format-value"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildToggleItem(Material material, String titlePath, boolean value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(titlePath));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    value ? "potion-bans.gui.shared.status-enabled" : "potion-bans.gui.shared.status-disabled"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.shared.toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildEffectItem(EffectOption option) {
        ItemStack item = new ItemStack(option.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            TierBlockState state = blockedEffects.computeIfAbsent(option.key(), k -> new TierBlockState());
            meta.displayName(
                    plugin.getMessageManager().get("potion-bans.gui.effect.name", "effect", option.displayName()));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    state.isTier1Blocked() ? "potion-bans.gui.effect.tier1-disabled"
                            : "potion-bans.gui.effect.tier1-allowed"));
            lore.add(plugin.getMessageManager().get(
                    state.isTier2Blocked() ? "potion-bans.gui.effect.tier2-disabled"
                            : "potion-bans.gui.effect.tier2-allowed"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.effect.left-click"));
            lore.add(plugin.getMessageManager().get("potion-bans.gui.effect.right-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildClearEffectsItem() {
        ItemStack item = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("potion-bans.gui.clear-effects.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.clear-effects.tier1", "count",
                    getTier1BlockedCount()));
            lore.add(plugin.getMessageManager().get("potion-bans.gui.clear-effects.tier2", "count",
                    getTier2BlockedCount()));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("potion-bans.gui.clear-effects.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("potion-bans.gui.back"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillFiller(Inventory gui, int size) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }

        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pane);
            }
        }
    }

    private record EffectOption(String key, String displayName, Material material) {
    }

    private record ParsedTierConfig(String key, boolean tier1Blocked, boolean tier2Blocked) {
    }

    private static final class TierBlockState {
        private boolean tier1Blocked;
        private boolean tier2Blocked;

        private boolean isTier1Blocked() {
            return tier1Blocked;
        }

        private boolean isTier2Blocked() {
            return tier2Blocked;
        }

        private void setTier1Blocked(boolean tier1Blocked) {
            this.tier1Blocked = tier1Blocked;
        }

        private void setTier2Blocked(boolean tier2Blocked) {
            this.tier2Blocked = tier2Blocked;
        }
    }
}

