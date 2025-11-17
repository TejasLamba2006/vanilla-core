package com.tejaslamba.smpcore.features;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.feature.BaseFeature;
import com.tejaslamba.smpcore.listener.MaceLimiterListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class MaceLimiterFeature extends BaseFeature {

    private MaceLimiterListener listener;
    private boolean maceCrafted = false;

    @Override
    public void onEnable(Main plugin) {
        super.onEnable(plugin);
        listener = new MaceLimiterListener(plugin);
        maceCrafted = plugin.getConfigManager().get().getBoolean("features.mace-limiter.mace-crafted", false);

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - Loaded state: maceCrafted=" + maceCrafted);
        }

        if (maceCrafted) {
            removeAllMaceRecipes();
            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Removed mace recipes on enable");
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        plugin.getConfigManager().get().set("features.mace-limiter.mace-crafted", maceCrafted);
        plugin.getConfigManager().save();
    }

    @Override
    public Listener getListener() {
        return listener;
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
        return createMenuItem(Material.MACE, "§5Mace Limiter",
                "§7Allows only one mace to be crafted");
    }

    public boolean isMaceCrafted() {
        return maceCrafted;
    }

    public void setMaceCrafted(boolean crafted) {
        this.maceCrafted = crafted;
        plugin.getConfigManager().get().set("features.mace-limiter.mace-crafted", crafted);
        plugin.getConfigManager().save();

        boolean verbose = plugin.getConfigManager().get().getBoolean("plugin.verbose", false);
        if (verbose) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter - State changed: maceCrafted=" + crafted);
        }

        if (crafted) {
            removeAllMaceRecipes();
        }
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

        if (recipesRemoved > 0 && plugin.getConfigManager().get().getBoolean("plugin.verbose", false)) {
            plugin.getLogger().info("Removed " + recipesRemoved + " Mace recipe(s).");
        }
    }
}
