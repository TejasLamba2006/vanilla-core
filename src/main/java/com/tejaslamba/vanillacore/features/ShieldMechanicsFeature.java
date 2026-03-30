package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ShieldMechanicsListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
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
        return createMenuItem(Material.SHIELD,
                plugin.getMessageManager().getRaw("feature-menus.shield-mechanics.name"),
                plugin.getMessageManager().getRaw("feature-menus.shield-mechanics.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        lore.add("");
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shield-mechanics.mace")
                .replace("<state>", plugin.getMessageManager().getRaw(
                        maceStunEnabled ? "feature-menus.shared.on" : "feature-menus.shared.off"))
                .replace("<ticks>", String.valueOf(maceStunDurationTicks)));
        lore.add(plugin.getMessageManager().getRaw("feature-menus.shield-mechanics.axe")
                .replace("<state>", plugin.getMessageManager().getRaw(
                        axeStunEnabled ? "feature-menus.shared.on" : "feature-menus.shared.off"))
                .replace("<ticks>", String.valueOf(axeStunDurationTicks)));
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
        Inventory gui = plugin.getServer().createInventory(new GuiHolder("shield-mechanics"), 27,
                plugin.getMessageManager().get("shield-mechanics.gui.title"));

        fillFiller(gui, 27);

        gui.setItem(4, buildMaceStunItem());
        gui.setItem(3, buildDecreaseItem("shield-mechanics.gui.mace.decrease-name", maceStunDurationTicks));
        gui.setItem(5, buildIncreaseItem("shield-mechanics.gui.mace.increase-name", maceStunDurationTicks));

        gui.setItem(13, buildAxeStunItem());
        gui.setItem(12, buildDecreaseItem("shield-mechanics.gui.axe.decrease-name", axeStunDurationTicks));
        gui.setItem(14, buildIncreaseItem("shield-mechanics.gui.axe.increase-name", axeStunDurationTicks));

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getMessageManager().get("shield-mechanics.gui.back"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(22, backButton);

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

    private List<Component> buildStunLore(boolean enabled, int durationTicks) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.description-1"));
        lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.description-2"));
        lore.add(Component.empty());
        lore.add(plugin.getMessageManager().get(
                enabled ? "shield-mechanics.gui.shared.status-enabled"
                        : "shield-mechanics.gui.shared.status-disabled"));
        lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.duration",
                "ticks", durationTicks,
                "seconds", String.format("%.1f", durationTicks / 20.0)));
        lore.add(Component.empty());
        lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.toggle"));
        return lore;
    }

    private ItemStack buildMaceStunItem() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("shield-mechanics.gui.mace.name"));
            meta.lore(buildStunLore(maceStunEnabled, maceStunDurationTicks));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildAxeStunItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("shield-mechanics.gui.axe.name"));
            meta.lore(buildStunLore(axeStunEnabled, axeStunDurationTicks));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildDecreaseItem(String namePath, int current) {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(namePath));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.current", "ticks", current));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.decrease-left"));
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.decrease-shift"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildIncreaseItem(String namePath, int current) {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get(namePath));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.current", "ticks", current));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.increase-left"));
            lore.add(plugin.getMessageManager().get("shield-mechanics.gui.shared.increase-shift"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleSettingsGUIClick(int slot, boolean isShiftClick, Player player) {
        if (slot == 22) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMenuManager().openMainMenu(player));
            return;
        }
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
