package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ServerRestartListener;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ServerRestartFeature extends BaseFeature {

    private ServerRestartListener listener;
    private int activeTaskId = -1;
    private int scheduledCheckerTaskId = -1;
    private BossBar activeBossBar;
    private int countdownSeconds = 0;

    @Override
    public String getName() {
        return "Server Restart";
    }

    @Override
    public String getConfigPath() {
        return "features.server-restart";
    }

    @Override
    public int getDisplayOrder() {
        return 50;
    }

    @Override
    public void onEnable(VanillaCorePlugin plugin) {
        listener = new ServerRestartListener(plugin);
        super.onEnable(plugin);

        if (isScheduledRestartsEnabled()) {
            startScheduledRestartChecker();
        }
    }

    @Override
    public void onDisable() {
        if (scheduledCheckerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(scheduledCheckerTaskId);
            scheduledCheckerTaskId = -1;
        }
        cancelRestart();
        super.onDisable();
    }

    @Override
    public void reload() {
        super.reload();
        if (scheduledCheckerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(scheduledCheckerTaskId);
            scheduledCheckerTaskId = -1;
        }
        if (isEnabled() && isScheduledRestartsEnabled()) {
            startScheduledRestartChecker();
        }
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public ItemStack getMenuItem() {
        return createMenuItem(Material.CLOCK,
                plugin.getMessageManager().getRaw("feature-menus.server-restart.name"),
                plugin.getMessageManager().getRaw("feature-menus.server-restart.description"));
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getRaw(enabled ? "feature.enabled" : "feature.disabled"));
        if (activeTaskId != -1) {
            lore.add(plugin.getMessageManager().getRaw("feature-menus.server-restart.active"));
            lore.add(plugin.getMessageManager().getRaw("feature-menus.server-restart.time-left")
                    .replace("<seconds>", String.valueOf(countdownSeconds)));
        }
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
        openMainGUI(player);
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GuiHolder("server-restart"), 45,
                plugin.getMessageManager().get("server-restart.gui.main.title"));

        gui.setItem(10, createRestartNowItem());
        gui.setItem(12, createStartCountdownItem());
        gui.setItem(14, createCancelRestartItem());
        gui.setItem(16, createScheduledRestartsItem());

        gui.setItem(28, createNotificationSettingsItem());
        gui.setItem(30, createCountdownTimeItem());
        gui.setItem(32, createPreRestartCommandsItem());
        gui.setItem(34, createBossBarColorItem());

        gui.setItem(40, createBackItem());

        player.openInventory(gui);
    }

    public void openScheduleGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GuiHolder("server-restart-schedule"), 45,
                plugin.getMessageManager().get("server-restart.gui.schedule.title"));

        gui.setItem(4, createScheduleToggleItem());

        List<String> schedules = plugin.getConfigManager().get().getStringList(getConfigPath() + ".scheduled-times");
        int slot = 19;
        for (int i = 0; i < Math.min(schedules.size(), 7); i++) {
            gui.setItem(slot, createScheduleEntryItem(schedules.get(i), i));
            slot++;
        }

        gui.setItem(36, createBackToMainItem());
        gui.setItem(40, createAddScheduleItem());

        player.openInventory(gui);
    }

    private ItemStack createRestartNowItem() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.restart-now.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.restart-now.lore-1"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.restart-now.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.restart-now.warning"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.restart-now.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createStartCountdownItem() {
        int defaultTime = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.countdown.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown.lore-1"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown.default-time",
                    "seconds", defaultTime));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown.left-click",
                    "seconds", defaultTime));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown.right-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelRestartItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.cancel.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (activeTaskId != -1) {
                lore.add(plugin.getMessageManager().get("server-restart.gui.main.cancel.active"));
                lore.add(plugin.getMessageManager().get("server-restart.gui.main.cancel.remaining",
                        "seconds", countdownSeconds));
                lore.add(Component.empty());
                lore.add(plugin.getMessageManager().get("server-restart.gui.main.cancel.action"));
            } else {
                lore.add(plugin.getMessageManager().get("server-restart.gui.main.cancel.inactive"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduledRestartsItem() {
        boolean scheduledEnabled = isScheduledRestartsEnabled();
        List<String> schedules = plugin.getConfigManager().get().getStringList(getConfigPath() + ".scheduled-times");

        ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.scheduled.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    scheduledEnabled ? "server-restart.gui.shared.status-enabled"
                            : "server-restart.gui.shared.status-disabled"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.scheduled.count",
                    "count", schedules.size()));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.scheduled.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNotificationSettingsItem() {
        List<String> types = plugin.getConfigManager().get().getStringList(getConfigPath() + ".notification-types");
        ItemStack item = new ItemStack(Material.BELL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.notifications.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.notifications.header"));
            lore.add(plugin.getMessageManager().get(
                    types.contains("chat")
                            ? "server-restart.gui.main.notifications.chat-enabled"
                            : "server-restart.gui.main.notifications.chat-disabled"));
            lore.add(plugin.getMessageManager().get(
                    types.contains("actionbar")
                            ? "server-restart.gui.main.notifications.actionbar-enabled"
                            : "server-restart.gui.main.notifications.actionbar-disabled"));
            lore.add(plugin.getMessageManager().get(
                    types.contains("bossbar")
                            ? "server-restart.gui.main.notifications.bossbar-enabled"
                            : "server-restart.gui.main.notifications.bossbar-disabled"));
            lore.add(plugin.getMessageManager().get(
                    types.contains("title")
                            ? "server-restart.gui.main.notifications.title-enabled"
                            : "server-restart.gui.main.notifications.title-disabled"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.shared.cycle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCountdownTimeItem() {
        int time = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.countdown-time.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown-time.current",
                    "seconds", time));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown-time.left-click"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown-time.right-click"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.countdown-time.shift-click"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPreRestartCommandsItem() {
        boolean execute = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".execute-pre-commands", true);
        List<String> commands = plugin.getConfigManager().get()
                .getStringList(getConfigPath() + ".pre-restart-commands");

        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.pre-commands.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    execute ? "server-restart.gui.shared.status-enabled"
                            : "server-restart.gui.shared.status-disabled"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.pre-commands.count",
                    "count", commands.size()));
            if (!commands.isEmpty()) {
                lore.add(Component.empty());
                for (String cmd : commands) {
                    lore.add(plugin.getMessageManager().get("server-restart.gui.main.pre-commands.command",
                            "command", cmd));
                }
            }
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.shared.toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBossBarColorItem() {
        String color = plugin.getConfigManager().get().getString(getConfigPath() + ".bossbar-color", "RED");
        Material dye = getMaterialForColor(color);

        ItemStack item = new ItemStack(dye);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.main.bossbar-color.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.bossbar-color.current",
                    "color", color));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.main.bossbar-color.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduleToggleItem() {
        boolean enabled = isScheduledRestartsEnabled();
        ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.schedule.toggle.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get(
                    enabled ? "server-restart.gui.shared.status-enabled"
                            : "server-restart.gui.shared.status-disabled"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.shared.toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduleEntryItem(String schedule, int index) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.schedule.entry.name",
                    "schedule", schedule));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.entry.index",
                    "index", index + 1));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.entry.remove"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAddScheduleItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.schedule.add.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.lore-1"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.formats"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.format-daily"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.format-weekly"));
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.format-once"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("server-restart.gui.schedule.add.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.shared.back-main"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackToMainItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("server-restart.gui.shared.back-settings"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getMaterialForColor(String color) {
        return switch (color.toUpperCase()) {
            case "BLUE" -> Material.BLUE_DYE;
            case "GREEN" -> Material.GREEN_DYE;
            case "PINK" -> Material.PINK_DYE;
            case "PURPLE" -> Material.PURPLE_DYE;
            case "WHITE" -> Material.WHITE_DYE;
            case "YELLOW" -> Material.YELLOW_DYE;
            default -> Material.RED_DYE;
        };
    }

    public void restartNow(Player executor) {
        Bukkit.broadcast(MessageManager.parse(getRestartMessage("restart-now-message", 0, executor.getName())));

        Bukkit.getScheduler().runTask(plugin, () -> {
            executePreRestartCommands();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
            }, 20L);
        });
    }

    public void startCountdown(int seconds) {
        if (activeTaskId != -1) {
            cancelRestart();
        }

        countdownSeconds = seconds;
        List<Integer> announcements = plugin.getConfigManager().get()
                .getIntegerList(getConfigPath() + ".countdown-announcements");
        List<String> notificationTypes = plugin.getConfigManager().get()
                .getStringList(getConfigPath() + ".notification-types");

        String colorName = plugin.getConfigManager().get()
                .getString(getConfigPath() + ".bossbar-color", "RED").toLowerCase(Locale.ROOT);
        BossBar.Color bossBarColor = BossBar.Color.NAMES.value(colorName);
        if (bossBarColor == null) {
            bossBarColor = BossBar.Color.RED;
        }

        if (notificationTypes.contains("bossbar")) {
            activeBossBar = BossBar.bossBar(
                    MessageManager.parse(getRestartMessage("bossbar-message", seconds)),
                    1.0f,
                    bossBarColor,
                    BossBar.Overlay.NOTCHED_10,
                    Set.of(BossBar.Flag.DARKEN_SCREEN));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showBossBar(activeBossBar);
            }
        }

        final int totalSeconds = seconds;

        activeTaskId = new BukkitRunnable() {
            int timeLeft = totalSeconds;

            @Override
            public void run() {
                countdownSeconds = timeLeft;

                if (timeLeft <= 0) {
                    Bukkit.broadcast(MessageManager.parse(getRestartMessage("restart-done-message", 0)));

                    if (activeBossBar != null) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.hideBossBar(activeBossBar);
                        }
                        activeBossBar = null;
                    }

                    executePreRestartCommands();

                    plugin.getConfigManager().get().set(getConfigPath() + ".last-restart",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    plugin.getConfigManager().save();

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                    cancel();
                    activeTaskId = -1;
                    return;
                }

                if (notificationTypes.contains("chat") && announcements.contains(timeLeft)) {
                    Bukkit.broadcast(MessageManager.parse(getRestartMessage("countdown-message", timeLeft)));
                }

                if (notificationTypes.contains("actionbar")) {
                    String actionBarMsg = getRestartMessage("actionbar-message", timeLeft);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendActionBar(MessageManager.parse(actionBarMsg));
                    }
                }

                if (notificationTypes.contains("title") && announcements.contains(timeLeft)) {
                    String titleMsg = getRestartMessage("title-message", timeLeft);
                    String subtitleMsg = getRestartMessage("subtitle-message", timeLeft);
                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500L), Duration.ofMillis(2000L), Duration.ofMillis(500L));
                    Title titleObj = Title.title(
                            MessageManager.parse(titleMsg), MessageManager.parse(subtitleMsg), times);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.showTitle(titleObj);
                    }
                }

                if (notificationTypes.contains("bossbar") && activeBossBar != null) {
                    activeBossBar.name(MessageManager.parse(getRestartMessage("bossbar-message", timeLeft)));
                    activeBossBar.progress((float) timeLeft / totalSeconds);
                }

                boolean soundEnabled = plugin.getConfigManager().get()
                        .getBoolean(getConfigPath() + ".sound.enabled", true);
                if (soundEnabled && announcements.contains(timeLeft)) {
                    playWarningSound();
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    public void cancelRestart() {
        if (activeTaskId != -1) {
            Bukkit.getScheduler().cancelTask(activeTaskId);
            activeTaskId = -1;
            countdownSeconds = 0;

            if (activeBossBar != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hideBossBar(activeBossBar);
                }
                activeBossBar = null;
            }

            Bukkit.broadcast(MessageManager.parse(getRestartMessage("cancelled-message", 0)));
        }
    }

    public boolean isRestartActive() {
        return activeTaskId != -1;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    private void executePreRestartCommands() {
        if (!plugin.getConfigManager().get().getBoolean(getConfigPath() + ".execute-pre-commands", true)) {
            return;
        }

        List<String> commands = plugin.getConfigManager().get()
                .getStringList(getConfigPath() + ".pre-restart-commands");
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private void playWarningSound() {
        String soundName = plugin.getConfigManager().get()
                .getString(getConfigPath() + ".sound.sound", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) plugin.getConfigManager().get().getDouble(getConfigPath() + ".sound.volume", 1.0);
        float pitch = (float) plugin.getConfigManager().get().getDouble(getConfigPath() + ".sound.pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String getRestartMessage(String key, int seconds) {
        return getRestartMessage(key, seconds, null);
    }

    private String getRestartMessage(String key, int seconds, String playerName) {
        String message = plugin.getConfigManager().get()
                .getString(getConfigPath() + ".messages." + key,
                        plugin.getMessageManager().getRaw("server-restart.messages.default-countdown"));
        message = message.replace("{time}", String.valueOf(seconds));
        if (playerName != null) {
            message = message.replace("{player}", playerName);
        }
        return message;
    }

    public boolean isScheduledRestartsEnabled() {
        return plugin.getConfigManager().get().getBoolean(getConfigPath() + ".scheduled-restarts-enabled", false);
    }

    public void setScheduledRestartsEnabled(boolean enabled) {
        plugin.getConfigManager().get().set(getConfigPath() + ".scheduled-restarts-enabled", enabled);
        plugin.getConfigManager().save();

        if (enabled) {
            startScheduledRestartChecker();
        } else {
            if (scheduledCheckerTaskId != -1) {
                Bukkit.getScheduler().cancelTask(scheduledCheckerTaskId);
                scheduledCheckerTaskId = -1;
            }
        }
    }

    public void addScheduledTime(String time) {
        List<String> times = plugin.getConfigManager().get().getStringList(getConfigPath() + ".scheduled-times");
        times.add(time);
        plugin.getConfigManager().get().set(getConfigPath() + ".scheduled-times", times);
        plugin.getConfigManager().save();
    }

    public void removeScheduledTime(int index) {
        List<String> times = plugin.getConfigManager().get().getStringList(getConfigPath() + ".scheduled-times");
        if (index >= 0 && index < times.size()) {
            times.remove(index);
            plugin.getConfigManager().get().set(getConfigPath() + ".scheduled-times", times);
            plugin.getConfigManager().save();
        }
    }

    public void cycleNotificationType() {
        List<String> current = plugin.getConfigManager().get()
                .getStringList(getConfigPath() + ".notification-types");

        List<String> all = List.of("chat", "actionbar", "bossbar", "title");
        List<String> newTypes = new ArrayList<>();

        if (current.isEmpty() || current.equals(List.of("chat"))) {
            newTypes.add("actionbar");
        } else if (current.equals(List.of("actionbar"))) {
            newTypes.add("bossbar");
        } else if (current.equals(List.of("bossbar"))) {
            newTypes.add("title");
        } else if (current.equals(List.of("title"))) {
            newTypes.addAll(all);
        } else {
            newTypes.add("chat");
        }

        plugin.getConfigManager().get().set(getConfigPath() + ".notification-types", newTypes);
        plugin.getConfigManager().save();
    }

    public void adjustCountdownTime(int delta) {
        int current = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
        int newTime = Math.max(10, current + delta);
        plugin.getConfigManager().get().set(getConfigPath() + ".countdown-time", newTime);
        plugin.getConfigManager().save();
    }

    public void togglePreRestartCommands() {
        boolean current = plugin.getConfigManager().get().getBoolean(getConfigPath() + ".execute-pre-commands", true);
        plugin.getConfigManager().get().set(getConfigPath() + ".execute-pre-commands", !current);
        plugin.getConfigManager().save();
    }

    public void cycleBossBarColor() {
        String current = plugin.getConfigManager().get().getString(getConfigPath() + ".bossbar-color", "RED");
        String[] colors = { "RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "PINK", "WHITE" };

        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }

        String newColor = colors[(currentIndex + 1) % colors.length];
        plugin.getConfigManager().get().set(getConfigPath() + ".bossbar-color", newColor);
        plugin.getConfigManager().save();
    }

    private void startScheduledRestartChecker() {
        if (scheduledCheckerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(scheduledCheckerTaskId);
            scheduledCheckerTaskId = -1;
        }
        String timezone = plugin.getConfigManager().get().getString(getConfigPath() + ".timezone", "");
        ZoneId zoneId = timezone.isEmpty() ? ZoneId.systemDefault() : ZoneId.of(timezone);

        scheduledCheckerTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isEnabled() || !isScheduledRestartsEnabled())
                return;

            ZonedDateTime now = ZonedDateTime.now(zoneId);
            int countdownTime = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
            List<String> schedules = plugin.getConfigManager().get()
                    .getStringList(getConfigPath() + ".scheduled-times");

            for (String schedule : schedules) {
                try {
                    schedule = schedule.trim();

                    if (schedule.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                        LocalDateTime scheduled = LocalDateTime.parse(schedule,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        ZonedDateTime scheduledZoned = scheduled.atZone(zoneId).minusSeconds(countdownTime);

                        if (!now.isBefore(scheduledZoned) && now.isBefore(scheduledZoned.plusSeconds(1))) {
                            startCountdown(countdownTime);
                        }
                    } else if (schedule.matches("(?i)(MON|TUE|WED|THU|FRI|SAT|SUN) \\d{2}:\\d{2}:\\d{2}")) {
                        String[] parts = schedule.split("\\s+");
                        DayOfWeek scheduledDay = parseDayOfWeek(parts[0]);
                        LocalTime scheduledTime = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH:mm:ss"));
                        LocalTime countdownStart = scheduledTime.minusSeconds(countdownTime);

                        if (now.getDayOfWeek() == scheduledDay &&
                                now.toLocalTime().getHour() == countdownStart.getHour() &&
                                now.toLocalTime().getMinute() == countdownStart.getMinute() &&
                                now.toLocalTime().getSecond() == countdownStart.getSecond()) {
                            startCountdown(countdownTime);
                        }
                    } else if (schedule.matches("\\d{2}:\\d{2}:\\d{2}")) {
                        LocalTime scheduledTime = LocalTime.parse(schedule, DateTimeFormatter.ofPattern("HH:mm:ss"));
                        LocalTime countdownStart = scheduledTime.minusSeconds(countdownTime);

                        if (now.toLocalTime().getHour() == countdownStart.getHour() &&
                                now.toLocalTime().getMinute() == countdownStart.getMinute() &&
                                now.toLocalTime().getSecond() == countdownStart.getSecond()) {
                            startCountdown(countdownTime);
                        }
                    }
                } catch (Exception e) {
                    if (plugin.isVerbose()) {
                        plugin.getLogger().warning("[VERBOSE] Invalid restart schedule: " + schedule);
                    }
                }
            }
        }, 20L, 20L).getTaskId();
    }

    private DayOfWeek parseDayOfWeek(String day) {
        return switch (day.toUpperCase()) {
            case "MON", "MONDAY" -> DayOfWeek.MONDAY;
            case "TUE", "TUESDAY" -> DayOfWeek.TUESDAY;
            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "THU", "THURSDAY" -> DayOfWeek.THURSDAY;
            case "FRI", "FRIDAY" -> DayOfWeek.FRIDAY;
            case "SAT", "SATURDAY" -> DayOfWeek.SATURDAY;
            case "SUN", "SUNDAY" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
}

