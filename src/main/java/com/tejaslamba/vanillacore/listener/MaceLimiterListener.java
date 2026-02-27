package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.MaceLimiterFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.menu.MainMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;

public class MaceLimiterListener implements Listener {

    private final VanillaCorePlugin plugin;

    public MaceLimiterListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack resultItem = event.getCurrentItem();
        if (resultItem == null || resultItem.getType() != Material.MACE) {
            return;
        }

        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);

        if (feature == null || !feature.isEnabled()) {
            return;
        }

        if (!feature.canCraftMace()) {
            event.setCancelled(true);
            plugin.getMessageManager().sendPrefixed(event.getWhoClicked(), "mace-limiter.craft-limit-reached");
            return;
        }

        if (plugin.isVerbose()) {
            plugin.getLogger()
                    .info("[VERBOSE] Mace Limiter - Player " + event.getWhoClicked().getName() + " is crafting a mace");
        }

        Player crafter = (Player) event.getWhoClicked();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            ItemStack[] matrix = event.getInventory().getMatrix();
            for (ItemStack itemStack : matrix) {
                if (itemStack != null && !itemStack.getType().isAir()) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
            }
            event.getInventory().setMatrix(matrix);
            event.getWhoClicked().setItemOnCursor(resultItem.clone());
        }

        feature.incrementMacesCrafted();

        broadcastMaceCraft(crafter, feature.getMacesCrafted());

        if (!feature.canCraftMace()) {
            Bukkit.getScheduler().runTask(plugin, (Runnable) feature::removeAllMaceRecipes);
        }
    }

    private void broadcastMaceCraft(Player crafter, int count) {
        String crafterName = crafter.getName();

        boolean titleEnabled = plugin.getConfigManager().get().getBoolean("features.mace-limiter.title.enabled", true);
        boolean chatEnabled = plugin.getConfigManager().get().getBoolean("features.mace-limiter.chat.enabled", true);
        boolean soundEnabled = plugin.getConfigManager().get().getBoolean("features.mace-limiter.sound.enabled", true);

        if (titleEnabled) {
            String title = plugin.getConfigManager().get()
                    .getString("features.mace-limiter.title.title", "§6⚔ MACE CRAFTED ⚔")
                    .replace("{player}", crafterName)
                    .replace("{count}", String.valueOf(count));

            String subtitle = plugin.getConfigManager().get()
                    .getString("features.mace-limiter.title.subtitle", "§e{player} §7has crafted mace §e#§6{count}")
                    .replace("{player}", crafterName)
                    .replace("{count}", String.valueOf(count));

            int fadeIn = plugin.getConfigManager().get().getInt("features.mace-limiter.title.fade-in", 10);
            int stay = plugin.getConfigManager().get().getInt("features.mace-limiter.title.stay", 70);
            int fadeOut = plugin.getConfigManager().get().getInt("features.mace-limiter.title.fade-out", 20);

            Component titleComp = MessageManager.parse(title);
            Component subtitleComp = MessageManager.parse(subtitle);
            Title.Times times = Title.Times.times(
                    Duration.ofMillis(fadeIn * 50L),
                    Duration.ofMillis(stay * 50L),
                    Duration.ofMillis(fadeOut * 50L));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(Title.title(titleComp, subtitleComp, times));
            }

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Sent title announcement for " + crafterName);
            }
        }

        if (chatEnabled) {
            String chatMessage = plugin.getConfigManager().get()
                    .getString("features.mace-limiter.chat.message", "§6{player} §ehas crafted mace #§6{count}§e!")
                    .replace("{player}", crafterName)
                    .replace("{count}", String.valueOf(count));

            Bukkit.getServer().broadcast(MessageManager.parse(chatMessage));

            if (plugin.isVerbose()) {
                plugin.getLogger().info("[VERBOSE] Mace Limiter - Sent chat message for " + crafterName);
            }
        }

        if (soundEnabled) {
            String soundName = plugin.getConfigManager().get()
                    .getString("features.mace-limiter.sound.sound", "ENTITY_ENDER_DRAGON_GROWL");
            float volume = (float) plugin.getConfigManager().get().getDouble("features.mace-limiter.sound.volume", 1.0);
            float pitch = (float) plugin.getConfigManager().get().getDouble("features.mace-limiter.sound.pitch", 1.0);

            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }

                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Mace Limiter - Played sound " + soundName + " for all players");
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[Mace Limiter] Invalid sound: " + soundName);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.equals(MaceLimiterFeature.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        MaceLimiterFeature feature = plugin.getFeatureManager().getFeature(MaceLimiterFeature.class);
        if (feature == null || !feature.isEnabled()) {
            plugin.getMessageManager().sendPrefixed(player, "mace-limiter.feature-disabled");
            plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            return;
        }

        int slot = event.getRawSlot();

        switch (slot) {
            case 10 -> {
                int change = event.isShiftClick() ? 5 : 1;
                int newMax = Math.max(1, feature.getMaxMaces() - change);
                feature.setMaxMaces(newMax);
                plugin.getMessageManager().sendPrefixed(player, "mace-limiter.max-maces-set",
                        "{count}", String.valueOf(newMax));
                feature.openMaceGUI(player);
            }
            case 16 -> {
                int change = event.isShiftClick() ? 5 : 1;
                int newMax = feature.getMaxMaces() + change;
                feature.setMaxMaces(newMax);
                plugin.getMessageManager().sendPrefixed(player, "mace-limiter.max-maces-set",
                        "{count}", String.valueOf(newMax));
                feature.openMaceGUI(player);
            }
            case 22 -> {
                feature.resetCraftCount();
                plugin.getMessageManager().sendPrefixed(player, "mace-limiter.craft-count-reset");
                feature.openMaceGUI(player);
            }
            case 18 -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MainMenu mainMenu = new MainMenu(plugin);
                    mainMenu.open(player);
                });
            }
        }

        if (plugin.isVerbose() && (slot == 10 || slot == 16 || slot == 22)) {
            plugin.getLogger().info("[VERBOSE] Mace Limiter GUI - " + player.getName() + " clicked slot " + slot);
        }
    }
}
