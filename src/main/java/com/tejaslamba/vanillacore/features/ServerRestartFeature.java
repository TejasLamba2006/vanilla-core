package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ServerRestartListener;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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

public class ServerRestartFeature extends BaseFeature {

    public static final String GUI_TITLE = "§8Server Restart Settings";
    public static final String SCHEDULE_GUI_TITLE = "§8Scheduled Restarts";

    private ServerRestartListener listener;
    private int activeTaskId = -1;
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
        cancelRestart();
        super.onDisable();
    }

    @Override
    public void reload() {
        super.reload();
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
        return createMenuItem(Material.CLOCK, "§c§lServer Restart",
                "§7Schedule and manage server restarts");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "§aEnabled" : "§cDisabled");
        if (activeTaskId != -1) {
            lore.add("§e⚠ Restart in progress!");
            lore.add("§7Time left: §c" + countdownSeconds + "s");
        }
        lore.add("");
        lore.add("§eLeft Click: Toggle");
        lore.add("§eRight Click: Open Settings");
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
        Inventory gui = Bukkit.createInventory(null, 45, GUI_TITLE);

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
        Inventory gui = Bukkit.createInventory(null, 45, SCHEDULE_GUI_TITLE);

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
            meta.setDisplayName("§c§lRestart Now");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Immediately restart the server");
            lore.add("§7with a save-all command.");
            lore.add("");
            lore.add("§c⚠ This will kick all players!");
            lore.add("");
            lore.add("§eShift + Click to confirm");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createStartCountdownItem() {
        int defaultTime = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lStart Countdown");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Start a restart countdown");
            lore.add("§7with warnings to players.");
            lore.add("");
            lore.add("§7Default time: §e" + defaultTime + " seconds");
            lore.add("");
            lore.add("§eLeft Click: Start (" + defaultTime + "s)");
            lore.add("§eRight Click: Custom time");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelRestartItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lCancel Restart");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (activeTaskId != -1) {
                lore.add("§a✔ Restart is active");
                lore.add("§7Time remaining: §c" + countdownSeconds + "s");
                lore.add("");
                lore.add("§eClick to cancel");
            } else {
                lore.add("§7No restart in progress");
            }
            meta.setLore(lore);
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
            meta.setDisplayName("§b§lScheduled Restarts");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(scheduledEnabled ? "§aEnabled" : "§cDisabled");
            lore.add("§7Configured times: §e" + schedules.size());
            lore.add("");
            lore.add("§eClick to manage schedules");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNotificationSettingsItem() {
        List<String> types = plugin.getConfigManager().get().getStringList(getConfigPath() + ".notification-types");
        ItemStack item = new ItemStack(Material.BELL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lNotification Types");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Current notifications:");
            lore.add("§a• Chat: " + (types.contains("chat") ? "§a✔" : "§c✘"));
            lore.add("§a• ActionBar: " + (types.contains("actionbar") ? "§a✔" : "§c✘"));
            lore.add("§a• BossBar: " + (types.contains("bossbar") ? "§a✔" : "§c✘"));
            lore.add("§a• Title: " + (types.contains("title") ? "§a✔" : "§c✘"));
            lore.add("");
            lore.add("§eClick to cycle");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCountdownTimeItem() {
        int time = plugin.getConfigManager().get().getInt(getConfigPath() + ".countdown-time", 60);
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lCountdown Time");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Default countdown: §e" + time + " seconds");
            lore.add("");
            lore.add("§aLeft Click: +10s");
            lore.add("§cRight Click: -10s");
            lore.add("§eShift Click: ±60s");
            meta.setLore(lore);
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
            meta.setDisplayName("§d§lPre-Restart Commands");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(execute ? "§aEnabled" : "§cDisabled");
            lore.add("§7Commands: §e" + commands.size());
            if (!commands.isEmpty()) {
                lore.add("");
                for (String cmd : commands) {
                    lore.add("§8- §7/" + cmd);
                }
            }
            lore.add("");
            lore.add("§eClick to toggle");
            meta.setLore(lore);
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
            meta.setDisplayName("§5§lBossBar Color");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Current: §e" + color);
            lore.add("");
            lore.add("§eClick to cycle colors");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduleToggleItem() {
        boolean enabled = isScheduledRestartsEnabled();
        ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lScheduled Restarts");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(enabled ? "§aEnabled" : "§cDisabled");
            lore.add("");
            lore.add("§eClick to toggle");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduleEntryItem(String schedule, int index) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + schedule);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Schedule #" + (index + 1));
            lore.add("");
            lore.add("§cShift + Click to remove");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAddScheduleItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lAdd Schedule");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Add a new scheduled restart time.");
            lore.add("");
            lore.add("§7Formats supported:");
            lore.add("§8- §7HH:mm:ss §8(daily)");
            lore.add("§8- §7MON HH:mm:ss §8(weekly)");
            lore.add("§8- §7yyyy-MM-dd HH:mm:ss §8(one-time)");
            lore.add("");
            lore.add("§eClick to add via chat");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eBack to Main Menu");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackToMainItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eBack to Restart Settings");
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
        Bukkit.broadcastMessage(getRestartMessage("restart-now-message", 0)
                .replace("{player}", executor.getName()));

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
                .getString(getConfigPath() + ".bossbar-color", "RED").toUpperCase(Locale.ROOT);
        BarColor bossBarColor;
        try {
            bossBarColor = BarColor.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            bossBarColor = BarColor.RED;
        }

        if (notificationTypes.contains("bossbar")) {
            activeBossBar = Bukkit.createBossBar(
                    getRestartMessage("bossbar-message", seconds),
                    bossBarColor,
                    BarStyle.SEGMENTED_10,
                    BarFlag.DARKEN_SKY);
            activeBossBar.setProgress(1.0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                activeBossBar.addPlayer(player);
            }
        }

        final int totalSeconds = seconds;

        activeTaskId = new BukkitRunnable() {
            int timeLeft = totalSeconds;

            @Override
            public void run() {
                countdownSeconds = timeLeft;

                if (timeLeft <= 0) {
                    Bukkit.broadcastMessage(getRestartMessage("restart-done-message", 0));

                    if (activeBossBar != null) {
                        activeBossBar.removeAll();
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
                    Bukkit.broadcastMessage(getRestartMessage("countdown-message", timeLeft));
                }

                if (notificationTypes.contains("actionbar")) {
                    String actionBarMsg = getRestartMessage("actionbar-message", timeLeft);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(actionBarMsg));
                    }
                }

                if (notificationTypes.contains("title") && announcements.contains(timeLeft)) {
                    String titleMsg = getRestartMessage("title-message", timeLeft);
                    String subtitleMsg = getRestartMessage("subtitle-message", timeLeft);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendTitle(titleMsg, subtitleMsg, 10, 40, 10);
                    }
                }

                if (notificationTypes.contains("bossbar") && activeBossBar != null) {
                    activeBossBar.setTitle(getRestartMessage("bossbar-message", timeLeft));
                    activeBossBar.setProgress((double) timeLeft / totalSeconds);
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
                activeBossBar.removeAll();
                activeBossBar = null;
            }

            Bukkit.broadcastMessage(getRestartMessage("cancelled-message", 0));
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
        String message = plugin.getConfigManager().get()
                .getString(getConfigPath() + ".messages." + key, "§cServer restarting in {time} seconds!");
        return message.replace("{time}", String.valueOf(seconds));
    }

    public boolean isScheduledRestartsEnabled() {
        return plugin.getConfigManager().get().getBoolean(getConfigPath() + ".scheduled-restarts-enabled", false);
    }

    public void setScheduledRestartsEnabled(boolean enabled) {
        plugin.getConfigManager().get().set(getConfigPath() + ".scheduled-restarts-enabled", enabled);
        plugin.getConfigManager().save();

        if (enabled) {
            startScheduledRestartChecker();
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
        String timezone = plugin.getConfigManager().get().getString(getConfigPath() + ".timezone", "");
        ZoneId zoneId = timezone.isEmpty() ? ZoneId.systemDefault() : ZoneId.of(timezone);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
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
        }, 20L, 20L);
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
