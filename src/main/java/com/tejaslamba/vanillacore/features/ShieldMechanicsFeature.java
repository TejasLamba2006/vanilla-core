package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ShieldMechanicsListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShieldMechanicsFeature extends BaseFeature {

    public static final String GUI_TITLE = "§8Shield Mechanics Settings";

    private ShieldMechanicsListener listener;

    private boolean maceStunEnabled;
    private int maceStunDurationTicks;
    private boolean axeStunEnabled;
    private int axeStunDurationTicks;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new ShieldMechanicsListener(plugin);
        super.onEnable(plugin);
        loadSettings();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Shield Mechanics - Feature loaded");
        }
    }

    @Override
    public void reload() {
        super.reload();
        loadSettings();
    }

    private void loadSettings() {
        var cfg = plugin.getConfigManager().get();
        String path = getConfigPath();
        maceStunEnabled = cfg.getBoolean(path + ".mace-stun.enabled", true);
        maceStunDurationTicks = cfg.getInt(path + ".mace-stun.duration-ticks", 100);
        axeStunEnabled = cfg.getBoolean(path + ".axe-stun.enabled", true);
        axeStunDurationTicks = cfg.getInt(path + ".axe-stun.duration-ticks", 100);
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 14;
    }

    @Override
    public String getName() {
        return "Shield Mechanics";
    }

    @Override
    public String getConfigPath() {
        return "features.shield-mechanics";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.SHIELD, "<!italic><light_purple>Shield Mechanics",
                "<!italic><gray>Customize shield stun and disable durations");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("");
        lore.add("<gray>Mace Stun: " + (maceStunEnabled ? "<green>On" : "<red>Off") + " <dark_gray>(<yellow>"
                + maceStunDurationTicks + " ticks<dark_gray>)");
        lore.add("<gray>Axe Stun:  " + (axeStunEnabled ? "<green>On" : "<red>Off") + " <dark_gray>(<yellow>"
                + axeStunDurationTicks + " ticks<dark_gray>)");
        lore.add("");
        lore.add("<yellow>Left Click: Toggle");
        lore.add("<yellow>Right Click: Open Settings");
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
        Inventory gui = plugin.getServer().createInventory(null, 27, GUI_TITLE);

        fillFiller(gui, 27);

        gui.setItem(4, buildMaceStunItem());
        gui.setItem(3, buildDecreaseItem("<red>Decrease Mace Stun", maceStunDurationTicks));
        gui.setItem(5, buildIncreaseItem("<green>Increase Mace Stun", maceStunDurationTicks));

        gui.setItem(13, buildAxeStunItem());
        gui.setItem(12, buildDecreaseItem("<red>Decrease Axe Stun", axeStunDurationTicks));
        gui.setItem(14, buildIncreaseItem("<green>Increase Axe Stun", axeStunDurationTicks));

        player.openInventory(gui);
    }

    private void fillFiller(Inventory gui, int size) {
        ItemStack pane = filler();
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pane);
            }
        }
    }

    private ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack buildMaceStunItem() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><light_purple>Mace Shield Stun"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Hitting a blocking player with a mace"));
            lore.add(MessageManager.parse("<!italic><gray>will put their shield on cooldown."));
            lore.add(Component.empty());
            lore.add(MessageManager
                    .parse("<!italic><gray>Status: " + (maceStunEnabled ? "<green>Enabled" : "<red>Disabled")));
            lore.add(MessageManager
                    .parse("<!italic><gray>Duration: <yellow>" + maceStunDurationTicks + " ticks <dark_gray>(<gray>"
                            + String.format("%.1f", maceStunDurationTicks / 20.0) + "s<dark_gray>)"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click: Toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildAxeStunItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><gold>Axe Shield Stun"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Hitting a blocking player with an axe"));
            lore.add(MessageManager.parse("<!italic><gray>will put their shield on cooldown."));
            lore.add(Component.empty());
            lore.add(MessageManager
                    .parse("<!italic><gray>Status: " + (axeStunEnabled ? "<green>Enabled" : "<red>Disabled")));
            lore.add(MessageManager
                    .parse("<!italic><gray>Duration: <yellow>" + axeStunDurationTicks + " ticks <dark_gray>(<gray>"
                            + String.format("%.1f", axeStunDurationTicks / 20.0) + "s<dark_gray>)"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click: Toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildDecreaseItem(String name, int current) {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic>" + name));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Current: <yellow>" + current + " ticks"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Left Click: <gray>-5 ticks"));
            lore.add(MessageManager.parse("<!italic><yellow>Shift Click: <gray>-20 ticks"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildIncreaseItem(String name, int current) {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic>" + name));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Current: <yellow>" + current + " ticks"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Left Click: <gray>+5 ticks"));
            lore.add(MessageManager.parse("<!italic><yellow>Shift Click: <gray>+20 ticks"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleSettingsGUIClick(int slot, boolean isShiftClick, Player player) {
        switch (slot) {
            case 4 -> {
                maceStunEnabled = !maceStunEnabled;

            }
            case 3 -> {
                int delta = isShiftClick ? 20 : 5;
                maceStunDurationTicks = Math.max(1, maceStunDurationTicks - delta);

            }
            case 5 -> {
                int delta = isShiftClick ? 20 : 5;
                maceStunDurationTicks = Math.min(600, maceStunDurationTicks + delta);

            }
            case 13 -> {
                axeStunEnabled = !axeStunEnabled;

            }
            case 12 -> {
                int delta = isShiftClick ? 20 : 5;
                axeStunDurationTicks = Math.max(1, axeStunDurationTicks - delta);

            }
            case 14 -> {
                int delta = isShiftClick ? 20 : 5;
                axeStunDurationTicks = Math.min(600, axeStunDurationTicks + delta);

            }
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
        cfg.set(path + ".mace-stun.enabled", maceStunEnabled);
        cfg.set(path + ".mace-stun.duration-ticks", maceStunDurationTicks);
        cfg.set(path + ".axe-stun.enabled", axeStunEnabled);
        cfg.set(path + ".axe-stun.duration-ticks", axeStunDurationTicks);
        plugin.getConfigManager().save();
    }

    public boolean isMaceStunEnabled() {
        return maceStunEnabled;
    }

    public int getMaceStunDurationTicks() {
        return maceStunDurationTicks;
    }

    public boolean isAxeStunEnabled() {
        return axeStunEnabled;
    }

    public int getAxeStunDurationTicks() {
        return axeStunDurationTicks;
    }
}
