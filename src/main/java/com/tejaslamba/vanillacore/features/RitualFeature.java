package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.RitualListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import com.tejaslamba.vanillacore.ritual.RitualManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RitualFeature extends BaseFeature {

    private static final List<String> SUPPORTED_PARTICLE_COLORS = List.of(
            "BLUE",
            "YELLOW",
            "AQUA",
            "FUCHSIA",
            "LIME",
            "ORANGE",
            "PURPLE",
            "WHITE",
            "BLACK",
            "GRAY",
            "RED",
            "GREEN");

    private RitualListener listener;
    private RitualManager ritualManager;

    private int radius;
    private int durationMinutes;
    private String particleColor;
    private boolean strikeLightning;
    private boolean beaconBeamEffect;
    private int beaconBeamDurationSeconds;
    private boolean fireworkBurstEffect;
    private boolean dropItemAtEnd;
    private boolean playStartSound;
    private String startSound;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new RitualListener(plugin);
        ritualManager = new RitualManager(plugin);
        super.onEnable(plugin);
        loadSettings();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Ritual - Feature loaded");
        }
    }

    @Override
    public void onDisable() {
        if (ritualManager != null) {
            ritualManager.shutdown();
        }
        super.onDisable();
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        radius = Math.max(1, plugin.getConfigManager().get().getInt(getConfigPath() + ".radius", 5));
        durationMinutes = Math.max(1, plugin.getConfigManager().get().getInt(getConfigPath() + ".duration-minutes", 1));
        particleColor = plugin.getConfigManager().get().getString(getConfigPath() + ".particle-color", "BLUE");
        strikeLightning = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".strike-lightning", true);
        beaconBeamEffect = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".end-effect-beacon-beam",
                false);
        beaconBeamDurationSeconds = Math.max(2,
                plugin.getConfigManager().get().getInt(getConfigPath() + ".beacon-beam-duration-seconds", 8));
        fireworkBurstEffect = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".end-effect-firework-burst",
                false);
        dropItemAtEnd = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".drop-item-at-end", false);
        playStartSound = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".play-start-sound", true);
        startSound = plugin.getConfigManager().get().getString(getConfigPath() + ".start-sound", "ENTITY_WITHER_SPAWN");
    }

    public RitualManager getRitualManager() {
        return ritualManager;
    }

    public int getRadius() {
        return radius;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getParticleColorName() {
        return particleColor;
    }

    public Color getParticleColor() {
        return getParticleColor(particleColor);
    }

    public Color getParticleColor(String colorName) {
        String normalized = normalizeParticleColorName(colorName);
        if (normalized == null) {
            normalized = "BLUE";
        }

        return switch (normalized) {
            case "YELLOW" -> Color.YELLOW;
            case "AQUA" -> Color.AQUA;
            case "FUCHSIA", "MAGENTA" -> Color.FUCHSIA;
            case "LIME" -> Color.LIME;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "WHITE" -> Color.WHITE;
            case "BLACK" -> Color.BLACK;
            case "GRAY", "GREY" -> Color.GRAY;
            case "RED" -> Color.RED;
            case "GREEN" -> Color.GREEN;
            default -> Color.BLUE;
        };
    }

    public String normalizeParticleColorName(String colorName) {
        if (colorName == null || colorName.isBlank()) {
            return null;
        }

        String normalized = colorName.trim().toUpperCase(Locale.ROOT);
        if ("MAGENTA".equals(normalized)) {
            normalized = "FUCHSIA";
        }
        if ("GREY".equals(normalized)) {
            normalized = "GRAY";
        }

        return SUPPORTED_PARTICLE_COLORS.contains(normalized) ? normalized : null;
    }

    public List<String> getSupportedParticleColors() {
        return Collections.unmodifiableList(SUPPORTED_PARTICLE_COLORS);
    }

    public boolean isStrikeLightning() {
        return strikeLightning;
    }

    public boolean isBeaconBeamEffectEnabled() {
        return beaconBeamEffect;
    }

    public int getBeaconBeamDurationSeconds() {
        return beaconBeamDurationSeconds;
    }

    public boolean isFireworkBurstEffectEnabled() {
        return fireworkBurstEffect;
    }

    public boolean isDropItemAtEnd() {
        return dropItemAtEnd;
    }

    public boolean isPlayStartSound() {
        return playStartSound;
    }

    public String getStartSound() {
        return startSound;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 35;
    }

    @Override
    public String getName() {
        return "Ritual";
    }

    @Override
    public String getConfigPath() {
        return "features.ritual";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.ECHO_SHARD,
                plugin.getMessageManager().getRaw("feature-menus.ritual.name"),
                plugin.getMessageManager().getRaw("feature-menus.ritual.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.ritual.duration")
                .replace("<value>", String.valueOf(durationMinutes)));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.ritual.radius")
                .replace("<value>", String.valueOf(radius)));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.ritual.color")
                .replace("<value>", getParticleColorName()));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.ritual.end-effects")
                .replace("<count>", String.valueOf(countEnabledEndEffects())));
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
        Inventory gui = Bukkit.createInventory(new GuiHolder("ritual-settings"), 45,
                plugin.getMessageManager().get("ritual.gui.title"));
        fillFiller(gui, 45);

        gui.setItem(10, buildDurationItem());
        gui.setItem(12, buildRadiusItem());
        gui.setItem(14, buildColorItem());
        gui.setItem(16, buildStartSoundItem());

        gui.setItem(28, buildLightningItem());
        gui.setItem(30, buildBeaconBeamItem());
        gui.setItem(32, buildBeaconDurationItem());
        gui.setItem(34, buildFireworkItem());
        gui.setItem(36, buildDropItemAtEndItem());

        gui.setItem(40, buildBackItem());
        player.openInventory(gui);
    }

    public void handleSettingsGUIClick(int slot, boolean isShiftClick, boolean isRightClick, Player player) {
        if (slot == 40) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMenuManager().openMainMenu(player));
            return;
        }

        switch (slot) {
            case 10 -> {
                int delta = isShiftClick ? 10 : 1;
                durationMinutes = Math.max(1,
                        Math.min(10080, durationMinutes + (isRightClick ? -delta : delta)));
            }
            case 12 -> {
                int delta = isShiftClick ? 5 : 1;
                radius = Math.max(1, Math.min(30, radius + (isRightClick ? -delta : delta)));
            }
            case 14 -> cycleParticleColor();
            case 16 -> playStartSound = !playStartSound;
            case 28 -> strikeLightning = !strikeLightning;
            case 30 -> beaconBeamEffect = !beaconBeamEffect;
            case 32 -> {
                int delta = isShiftClick ? 5 : 1;
                beaconBeamDurationSeconds = Math.max(2,
                        Math.min(30, beaconBeamDurationSeconds + (isRightClick ? -delta : delta)));
            }
            case 34 -> fireworkBurstEffect = !fireworkBurstEffect;
            case 36 -> dropItemAtEnd = !dropItemAtEnd;
            default -> {
                return;
            }
        }

        saveSettings();
        plugin.getServer().getScheduler().runTask(plugin, () -> openSettingsGUI(player));
    }

    private void saveSettings() {
        var cfg = plugin.getConfigManager().get();
        String path = getConfigPath();

        cfg.set(path + ".duration-minutes", durationMinutes);
        cfg.set(path + ".radius", radius);
        cfg.set(path + ".particle-color", particleColor);
        cfg.set(path + ".strike-lightning", strikeLightning);
        cfg.set(path + ".end-effect-beacon-beam", beaconBeamEffect);
        cfg.set(path + ".beacon-beam-duration-seconds", beaconBeamDurationSeconds);
        cfg.set(path + ".end-effect-firework-burst", fireworkBurstEffect);
        cfg.set(path + ".drop-item-at-end", dropItemAtEnd);
        cfg.set(path + ".play-start-sound", playStartSound);
        cfg.set(path + ".start-sound", startSound);

        plugin.getConfigManager().save();
    }

    private void cycleParticleColor() {
        String normalized = normalizeParticleColorName(particleColor);
        if (normalized == null) {
            particleColor = "BLUE";
            return;
        }

        int index = SUPPORTED_PARTICLE_COLORS.indexOf(normalized);
        if (index < 0) {
            particleColor = "BLUE";
            return;
        }

        particleColor = SUPPORTED_PARTICLE_COLORS.get((index + 1) % SUPPORTED_PARTICLE_COLORS.size());
    }

    private int countEnabledEndEffects() {
        int count = 0;
        if (strikeLightning) {
            count++;
        }
        if (beaconBeamEffect) {
            count++;
        }
        if (fireworkBurstEffect) {
            count++;
        }
        return count;
    }

    private void fillFiller(Inventory gui, int size) {
        ItemStack pane = createFillerPane();
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pane);
            }
        }
    }

    private ItemStack createFillerPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack buildDurationItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.duration.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.duration.current",
                    "value", formatDuration(durationMinutes)));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.duration.left-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.duration.right-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.duration.shift-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildRadiusItem() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.radius.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.radius.current", "value", radius));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.radius.left-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.radius.right-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.radius.shift-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildColorItem() {
        ItemStack item = new ItemStack(getColorMaterial(getParticleColorName()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.color.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.color.current", "value", getParticleColorName()));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.color.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildStartSoundItem() {
        ItemStack item = new ItemStack(playStartSound ? Material.JUKEBOX : Material.NOTE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.start-sound.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    playStartSound ? "ritual.gui.shared.status-enabled" : "ritual.gui.shared.status-disabled"));
            lore.add(plugin.getMessageManager().get("ritual.gui.start-sound.sound", "value", startSound));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.shared.toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildLightningItem() {
        return buildBooleanItem(Material.LIGHTNING_ROD, "ritual.gui.effects.lightning.name",
                strikeLightning);
    }

    private ItemStack buildBeaconBeamItem() {
        return buildBooleanItem(Material.BEACON, "ritual.gui.effects.beacon.name", beaconBeamEffect);
    }

    private ItemStack buildBeaconDurationItem() {
        ItemStack item = new ItemStack(Material.SEA_LANTERN);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.beacon-duration.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.beacon-duration.current",
                    "value", beaconBeamDurationSeconds));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.beacon-duration.left-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.beacon-duration.right-click"));
            lore.add(plugin.getMessageManager().get("ritual.gui.beacon-duration.shift-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildFireworkItem() {
        return buildBooleanItem(Material.FIREWORK_ROCKET, "ritual.gui.effects.firework.name",
                fireworkBurstEffect);
    }

    private ItemStack buildDropItemAtEndItem() {
        return buildBooleanItem(Material.CHEST, "ritual.gui.effects.drop-item.name", dropItemAtEnd);
    }

    private ItemStack buildBooleanItem(Material material, String titlePath, boolean enabledValue) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(titlePath));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    enabledValue ? "ritual.gui.shared.status-enabled" : "ritual.gui.shared.status-disabled"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("ritual.gui.shared.toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("ritual.gui.back"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getColorMaterial(String colorName) {
        String normalized = normalizeParticleColorName(colorName);
        if (normalized == null) {
            normalized = "BLUE";
        }
        return switch (normalized) {
            case "YELLOW" -> Material.YELLOW_DYE;
            case "AQUA" -> Material.LIGHT_BLUE_DYE;
            case "FUCHSIA" -> Material.MAGENTA_DYE;
            case "LIME" -> Material.LIME_DYE;
            case "ORANGE" -> Material.ORANGE_DYE;
            case "PURPLE" -> Material.PURPLE_DYE;
            case "WHITE" -> Material.WHITE_DYE;
            case "BLACK" -> Material.BLACK_DYE;
            case "GRAY" -> Material.GRAY_DYE;
            case "RED" -> Material.RED_DYE;
            case "GREEN" -> Material.GREEN_DYE;
            default -> Material.BLUE_DYE;
        };
    }

    private String formatDuration(int minutes) {
        int days = minutes / 1440;
        int remainingAfterDays = minutes % 1440;
        int hours = remainingAfterDays / 60;
        int mins = remainingAfterDays % 60;

        StringBuilder value = new StringBuilder();
        if (days > 0) {
            value.append(days).append("d ");
        }
        if (hours > 0) {
            value.append(hours).append("h ");
        }
        if (mins > 0 || value.isEmpty()) {
            value.append(mins).append("m");
        }
        return value.toString().trim();
    }
}
