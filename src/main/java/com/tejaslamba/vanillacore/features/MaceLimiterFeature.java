package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.MaceLimiterListener;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MaceLimiterFeature extends BaseFeature {

    public static final String GUI_TITLE = "§8Mace Limiter Settings";

    private MaceLimiterListener listener;
    private int macesCrafted = 0;
    private int maxMaces = 1;

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new MaceLimiterListener(plugin);
        super.onEnable(plugin);
        macesCrafted = plugin.getConfigManager().get().getInt("features.mace-limiter.maces-crafted", 0);
        maxMaces = plugin.getConfigManager().get().getInt("features.mace-limiter.max-maces", 1);

        if (plugin.isVerbose()) {
            plugin.getLogger().info(
                    "[VERBOSE] Mace Limiter - Loaded state: macesCrafted=" + macesCrafted + ", maxMaces=" + maxMaces);
        }

        if (macesCrafted >= maxMaces) {
            removeAllMaceRecipes();
            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Removed mace recipes on enable (limit reached)");
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        plugin.getConfigManager().get().set("features.mace-limiter.maces-crafted", macesCrafted);
        plugin.getConfigManager().get().set("features.mace-limiter.max-maces", maxMaces);
        plugin.getConfigManager().save();

        restoreMaceRecipes();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public int getDisplayOrder() {
        return 12;
    }

    @Override
    public String getName() {
        return "Mace Limiter";
    }

    @Override
    public String getConfigPath() {
        return "features.mace-limiter";
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.MACE, "<!italic><dark_purple>Mace Limiter",
                "<!italic><gray>Limit the number of maces craftable");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        lore.add("<gray>Crafted: <yellow>" + macesCrafted + "<gray>/<yellow>" + maxMaces);
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
        openMaceGUI(player);
    }

    public void openMaceGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, GUI_TITLE);

        ItemStack decreaseItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta decreaseMeta = decreaseItem.getItemMeta();
        if (decreaseMeta != null) {
            decreaseMeta.displayName(MessageManager.parse("<!italic><red>- Decrease Max Maces"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Current Max: <yellow>" + maxMaces));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<yellow>Left Click: <gray>-1"));
            lore.add(MessageManager.parse("<yellow>Shift Click: <gray>-5"));
            decreaseMeta.lore(lore);
            decreaseItem.setItemMeta(decreaseMeta);
        }
        gui.setItem(10, decreaseItem);

        ItemStack displayItem = new ItemStack(Material.MACE);
        ItemMeta displayMeta = displayItem.getItemMeta();
        if (displayMeta != null) {
            displayMeta.displayName(MessageManager.parse("<!italic><dark_purple>Mace Limit"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Max Maces: <yellow>" + maxMaces));
            lore.add(MessageManager.parse("<gray>Maces Crafted: <yellow>" + macesCrafted));
            lore.add(Component.empty());
            if (macesCrafted >= maxMaces) {
                lore.add(MessageManager.parse("<red>\u26a0 Limit Reached!"));
            } else {
                lore.add(MessageManager.parse("<green>" + (maxMaces - macesCrafted) + " maces remaining"));
            }
            displayMeta.lore(lore);
            displayItem.setItemMeta(displayMeta);
        }
        gui.setItem(13, displayItem);

        ItemStack increaseItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta increaseMeta = increaseItem.getItemMeta();
        if (increaseMeta != null) {
            increaseMeta.displayName(MessageManager.parse("<!italic><green>+ Increase Max Maces"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Current Max: <yellow>" + maxMaces));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<yellow>Left Click: <gray>+1"));
            lore.add(MessageManager.parse("<yellow>Shift Click: <gray>+5"));
            increaseMeta.lore(lore);
            increaseItem.setItemMeta(increaseMeta);
        }
        gui.setItem(16, increaseItem);

        ItemStack resetItem = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetItem.getItemMeta();
        if (resetMeta != null) {
            resetMeta.displayName(MessageManager.parse("<!italic><red>Reset Craft Count"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<gray>Resets maces crafted to 0"));
            lore.add(MessageManager.parse("<gray>and re-enables mace recipes"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<yellow>Click to reset"));
            resetMeta.lore(lore);
            resetItem.setItemMeta(resetMeta);
        }
        gui.setItem(22, resetItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(MessageManager.parse("<!italic><yellow>Back to Main Menu"));
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(18, backItem);

        player.openInventory(gui);
    }

    public int getMacesCrafted() {
        return macesCrafted;
    }

    public int getMaxMaces() {
        return maxMaces;
    }

    public boolean canCraftMace() {
        return macesCrafted < maxMaces;
    }

    public void incrementMacesCrafted() {
        macesCrafted++;
        plugin.getConfigManager().get().set("features.mace-limiter.maces-crafted", macesCrafted);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Maces crafted: " + macesCrafted + "/" + maxMaces);
        }

        if (macesCrafted >= maxMaces) {
            removeAllMaceRecipes();
        }
    }

    public void setMaxMaces(int max) {
        this.maxMaces = Math.max(1, max);
        plugin.getConfigManager().get().set("features.mace-limiter.max-maces", maxMaces);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Max maces set to: " + maxMaces);
        }

        if (macesCrafted >= maxMaces) {
            removeAllMaceRecipes();
        } else {
            restoreMaceRecipes();
        }
    }

    public void resetCraftCount() {
        macesCrafted = 0;
        plugin.getConfigManager().get().set("features.mace-limiter.maces-crafted", macesCrafted);
        plugin.getConfigManager().save();

        if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Craft count reset to 0");
        }

        restoreMaceRecipes();
    }

    public void removeAllMaceRecipes() {
        int recipesRemoved = 0;
        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe.getResult().getType() == Material.MACE) {
                iterator.remove();
                recipesRemoved++;
            }
        }

        if (recipesRemoved > 0 && plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Removed " + recipesRemoved + " Mace recipe(s)");
        }
    }

    public void restoreMaceRecipes() {
        boolean alreadyHasMaceRecipe = false;
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe.getResult().getType() == Material.MACE) {
                alreadyHasMaceRecipe = true;
                break;
            }
        }

        if (!alreadyHasMaceRecipe) {
            org.bukkit.NamespacedKey vanillaKey = org.bukkit.NamespacedKey.minecraft("mace");
            ShapedRecipe maceRecipe = new ShapedRecipe(vanillaKey, new ItemStack(Material.MACE));
            maceRecipe.shape(" B ", " R ");
            maceRecipe.setIngredient('R', Material.BREEZE_ROD);
            maceRecipe.setIngredient('B', Material.HEAVY_CORE);

            try {
                Bukkit.addRecipe(maceRecipe);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.discoverRecipe(vanillaKey);
                }

                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Mace Limiter - Mace recipe restored");
                }
            } catch (IllegalStateException e) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Mace Limiter - Mace recipe already exists");
                }
            }
        } else if (plugin.isVerbose()) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Mace recipe already present, skipping restore");
        }
    }

    @Override
    public void reload() {
        boolean wasEnabled = this.enabled;
        super.reload();
        macesCrafted = plugin.getConfigManager().get().getInt("features.mace-limiter.maces-crafted", 0);
        maxMaces = plugin.getConfigManager().get().getInt("features.mace-limiter.max-maces", 1);

        if (wasEnabled && !enabled) {
            restoreMaceRecipes();
            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Feature disabled, recipes restored");
            }
        } else if (enabled && macesCrafted >= maxMaces) {
            removeAllMaceRecipes();
            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Limit reached, recipes removed");
            }
        } else if (enabled && macesCrafted < maxMaces) {
            restoreMaceRecipes();
            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Under limit, recipes available");
            }
        }
    }
}
