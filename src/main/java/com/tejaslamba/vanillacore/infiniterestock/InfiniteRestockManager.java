package com.tejaslamba.vanillacore.infiniterestock;

import com.tejaslamba.vanillacore.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfiniteRestockManager {

    private final Main plugin;
    private final NamespacedKey backupKey;

    private int maxTrades;
    private boolean disablePricePenalty;
    private boolean allowWanderingTraders;
    private boolean uninstallMode;
    private final Set<String> villagerBlacklist = new HashSet<>();

    public InfiniteRestockManager(Main plugin) {
        this.plugin = plugin;
        this.backupKey = new NamespacedKey(plugin, "infinite_restock_backup");
        load();
    }

    public void load() {
        var cfg = plugin.getConfigManager().get();
        this.maxTrades = cfg.getInt("features.infinite-restock.max-trades", 0);
        this.disablePricePenalty = cfg.getBoolean("features.infinite-restock.disable-price-penalty", true);
        this.allowWanderingTraders = cfg.getBoolean("features.infinite-restock.allow-wandering-traders", true);
        this.uninstallMode = cfg.getBoolean("features.infinite-restock.uninstall-mode", false);

        villagerBlacklist.clear();
        List<String> list = cfg.getStringList("features.infinite-restock.villager-blacklist");
        for (String s : list) {
            if (s != null && !s.isEmpty()) {
                villagerBlacklist.add(s.trim().toUpperCase());
            }
        }
    }

    public void applyOnInteract(AbstractVillager villager) {
        if (uninstallMode) {
            restoreVillagerTrades(villager);
            removeBackup(villager);
            return;
        }

        if (villager.getType() == EntityType.WANDERING_TRADER && !allowWanderingTraders) {
            restoreVillagerTrades(villager);
            removeBackup(villager);
            return;
        }

        if (villager.getType() == EntityType.VILLAGER) {
            if (isBlacklistedProfession((Villager) villager)) {
                restoreVillagerTrades(villager);
                removeBackup(villager);
                return;
            }
        }

        if (!hasBackup(villager)) {
            saveVillagerTrades(villager);
        }
        setMaxTrades(villager);
    }

    public void applyOnMerchantOpen(AbstractVillager villager) {
        if (uninstallMode) {
            restoreVillagerTrades(villager);
            removeBackup(villager);
            return;
        }
        setMaxTrades(villager);
    }

    public void applyOnVillagerAcquireTrade(AbstractVillager villager) {
        if (uninstallMode) {
            return;
        }
        if (villager.getType() == EntityType.WANDERING_TRADER && !allowWanderingTraders) {
            return;
        }
        if (hasBackup(villager)) {
            restoreVillagerTrades(villager);
            saveVillagerTrades(villager);
            setMaxTrades(villager);
        }
    }

    private void saveVillagerTrades(AbstractVillager villager) {
        int[] maxUses = villager.getRecipes().stream().mapToInt(MerchantRecipe::getMaxUses).toArray();
        villager.getPersistentDataContainer().set(backupKey, PersistentDataType.INTEGER_ARRAY, maxUses);
    }

    private boolean hasBackup(AbstractVillager villager) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        return pdc.has(backupKey, PersistentDataType.INTEGER_ARRAY);
    }

    private void removeBackup(AbstractVillager villager) {
        villager.getPersistentDataContainer().remove(backupKey);
    }

    private void restoreVillagerTrades(AbstractVillager villager) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        if (!pdc.has(backupKey, PersistentDataType.INTEGER_ARRAY))
            return;
        int[] backup = pdc.get(backupKey, PersistentDataType.INTEGER_ARRAY);
        if (backup == null)
            return;

        List<MerchantRecipe> recipes = villager.getRecipes();
        for (int i = 0; i < recipes.size() && i < backup.length; i++) {
            try {
                recipes.get(i).setMaxUses(backup[i]);
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }

    private void setMaxTrades(AbstractVillager villager) {
        List<MerchantRecipe> newRecipes = new ArrayList<>();
        for (MerchantRecipe old : villager.getRecipes()) {
            int uses = 0;
            int max = (maxTrades <= 0) ? Integer.MAX_VALUE : maxTrades;
            float priceMultiplier = old.getPriceMultiplier();
            boolean expReward = old.hasExperienceReward();
            int villagerExp = old.getVillagerExperience();

            MerchantRecipe nr = new MerchantRecipe(
                    old.getResult(),
                    uses,
                    max,
                    expReward,
                    villagerExp,
                    priceMultiplier,
                    disablePricePenalty ? 0 : old.getDemand(),
                    0);
            nr.setIngredients(old.getIngredients());
            newRecipes.add(nr);
        }
        villager.setRecipes(newRecipes);
    }

    private boolean isBlacklistedProfession(Villager villager) {
        String key = getProfessionKey(villager);
        return key != null && villagerBlacklist.contains(key.toUpperCase());
    }

    // Compatible across 1.20.6 and 1.21+
    private String getProfessionKey(Villager villager) {
        try {
            Method getProfession = villager.getClass().getMethod("getProfession");
            Object profession = getProfession.invoke(villager);
            Method getKey = profession.getClass().getMethod("getKey");
            Object keyObj = getKey.invoke(profession);
            Method getKeyStr = keyObj.getClass().getMethod("getKey");
            Object keyStr = getKeyStr.invoke(keyObj);
            return String.valueOf(keyStr);
        } catch (Throwable t) {
            return null;
        }
    }

    public int getMaxTrades() {
        return maxTrades;
    }

    public void setMaxTrades(int value) {
        this.maxTrades = value;
        plugin.getConfigManager().get().set("features.infinite-restock.max-trades", value);
        plugin.getConfigManager().save();
    }

    public boolean isDisablePricePenalty() {
        return disablePricePenalty;
    }

    public void setDisablePricePenalty(boolean value) {
        this.disablePricePenalty = value;
        plugin.getConfigManager().get().set("features.infinite-restock.disable-price-penalty", value);
        plugin.getConfigManager().save();
    }

    public boolean isAllowWanderingTraders() {
        return allowWanderingTraders;
    }

    public void setAllowWanderingTraders(boolean value) {
        this.allowWanderingTraders = value;
        plugin.getConfigManager().get().set("features.infinite-restock.allow-wandering-traders", value);
        plugin.getConfigManager().save();
    }

    public boolean isUninstallMode() {
        return uninstallMode;
    }

    public void setUninstallMode(boolean value) {
        this.uninstallMode = value;
        plugin.getConfigManager().get().set("features.infinite-restock.uninstall-mode", value);
        plugin.getConfigManager().save();
    }

    public Set<String> getVillagerBlacklist() {
        return new HashSet<>(villagerBlacklist);
    }

    public int getBlacklistCount() {
        return villagerBlacklist.size();
    }

    public boolean isProfessionBlacklisted(String profession) {
        return villagerBlacklist.contains(profession.toUpperCase());
    }

    public void toggleProfessionBlacklist(String profession) {
        String upper = profession.toUpperCase();
        if (villagerBlacklist.contains(upper)) {
            villagerBlacklist.remove(upper);
        } else {
            villagerBlacklist.add(upper);
        }
        saveBlacklist();
    }

    public void addToBlacklist(String profession) {
        villagerBlacklist.add(profession.toUpperCase());
        saveBlacklist();
    }

    public void removeFromBlacklist(String profession) {
        villagerBlacklist.remove(profession.toUpperCase());
        saveBlacklist();
    }

    private void saveBlacklist() {
        List<String> list = new ArrayList<>(villagerBlacklist);
        plugin.getConfigManager().get().set("features.infinite-restock.villager-blacklist", list);
        plugin.getConfigManager().save();
    }
}
