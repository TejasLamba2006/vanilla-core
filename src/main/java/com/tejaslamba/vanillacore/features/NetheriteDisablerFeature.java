package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.NetheriteDisablerListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetheriteDisablerFeature extends BaseFeature {

    private final Map<Material, Boolean> disabledItems = new HashMap<>();
    private final Map<Integer, Material> slotMapping = new HashMap<>();
    private NetheriteDisablerListener listener;

    public NetheriteDisablerFeature() {
        initializeSlotMapping();
    }

    private void initializeSlotMapping() {
        slotMapping.put(11, Material.NETHERITE_SWORD);
        slotMapping.put(12, Material.NETHERITE_AXE);
        slotMapping.put(13, Material.NETHERITE_PICKAXE);
        slotMapping.put(14, Material.NETHERITE_SHOVEL);
        slotMapping.put(15, Material.NETHERITE_HOE);
        slotMapping.put(20, Material.NETHERITE_HELMET);
        slotMapping.put(21, Material.NETHERITE_CHESTPLATE);
        slotMapping.put(23, Material.NETHERITE_LEGGINGS);
        slotMapping.put(24, Material.NETHERITE_BOOTS);
    }

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new NetheriteDisablerListener(plugin);
        super.onEnable(plugin);
        loadDisabledItems();

        if (plugin.isVerbose()) {
            long disabledCount = disabledItems.values().stream().filter(b -> b).count();
            plugin.getLogger().info("[VERBOSE] Netherite Disabler - Loaded " + disabledCount + "/9 items as disabled");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveDisabledItems();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 13;
    }

    @Override
    public String getName() {
        return "Netherite Disabler";
    }

    @Override
    public String getConfigPath() {
        return "features.netherite-disabler";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.NETHERITE_CHESTPLATE,
                plugin.getMessageManager().getRaw("feature-menus.netherite-disabler.name"),
                plugin.getMessageManager().getRaw("feature-menus.netherite-disabler.description"));
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
            player.sendMessage(plugin.getMessageManager().get("netherite-disabler.feature-disabled"));
            return;
        }
        openNetheriteGUI(player);
    }

    public void openNetheriteGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(new GuiHolder("netherite-disabler"), 45,
                plugin.getMessageManager().getRaw("netherite-disabler.gui.title"));

        for (Map.Entry<Integer, Material> entry : slotMapping.entrySet()) {
            int slot = entry.getKey();
            Material material = entry.getValue();
            boolean isDisabled = isDisabled(material);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String itemName = material.name().toLowerCase().replace("netherite_", "");
                itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);

                meta.displayName(plugin.getMessageManager().get("netherite-disabler.gui.item.name", "item", itemName));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                if (isDisabled) {
                    lore.add(plugin.getMessageManager().get("netherite-disabler.gui.item.status-disabled"));
                } else {
                    lore.add(plugin.getMessageManager().get("netherite-disabler.gui.item.status-enabled"));
                }
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("netherite-disabler.gui.item.click-toggle"));
                meta.lore(lore);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getMessageManager().get("netherite-disabler.gui.back"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(39, backButton);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(plugin.getMessageManager().get("netherite-disabler.gui.close"));
            barrier.setItemMeta(barrierMeta);
        }
        gui.setItem(40, barrier);

        player.openInventory(gui);
    }

    public Map<Integer, Material> getSlotMapping() {
        return new HashMap<>(slotMapping);
    }

    private void loadDisabledItems() {
        disabledItems.clear();

        disabledItems.put(Material.NETHERITE_SWORD,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.sword", true));
        disabledItems.put(Material.NETHERITE_AXE,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.axe", true));
        disabledItems.put(Material.NETHERITE_PICKAXE,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.pickaxe", true));
        disabledItems.put(Material.NETHERITE_SHOVEL,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.shovel", true));
        disabledItems.put(Material.NETHERITE_HOE,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.hoe", true));
        disabledItems.put(Material.NETHERITE_HELMET,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.helmet", true));
        disabledItems.put(Material.NETHERITE_CHESTPLATE,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.chestplate",
                        true));
        disabledItems.put(Material.NETHERITE_LEGGINGS,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.leggings",
                        true));
        disabledItems.put(Material.NETHERITE_BOOTS,
                plugin.getConfigManager().get().getBoolean("features.netherite-disabler.disabled-items.boots", true));
    }

    private void saveDisabledItems() {
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.sword",
                disabledItems.getOrDefault(Material.NETHERITE_SWORD, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.axe",
                disabledItems.getOrDefault(Material.NETHERITE_AXE, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.pickaxe",
                disabledItems.getOrDefault(Material.NETHERITE_PICKAXE, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.shovel",
                disabledItems.getOrDefault(Material.NETHERITE_SHOVEL, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.hoe",
                disabledItems.getOrDefault(Material.NETHERITE_HOE, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.helmet",
                disabledItems.getOrDefault(Material.NETHERITE_HELMET, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.chestplate",
                disabledItems.getOrDefault(Material.NETHERITE_CHESTPLATE, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.leggings",
                disabledItems.getOrDefault(Material.NETHERITE_LEGGINGS, true));
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items.boots",
                disabledItems.getOrDefault(Material.NETHERITE_BOOTS, true));

        plugin.getConfigManager().save();
    }

    public boolean isDisabled(Material material) {
        return disabledItems.getOrDefault(material, false);
    }

    public void setDisabled(Material material, boolean disabled) {
        disabledItems.put(material, disabled);
        String itemName = material.name().toLowerCase().replace("netherite_", "");
        plugin.getConfigManager().get().set("features.netherite-disabler.disabled-items." + itemName, disabled);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger()
                    .info("[VERBOSE] Netherite Disabler - " + material.name() + " state changed: disabled=" + disabled);
        }
    }

    public Map<Material, Boolean> getDisabledItems() {
        return new HashMap<>(disabledItems);
    }

    @Override
    public void reload() {
        super.reload();
        loadDisabledItems();
    }
}

