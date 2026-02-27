package com.tejaslamba.vanillacore.features;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.feature.BaseFeature;
import com.tejaslamba.vanillacore.listener.ServerRestartListener;
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
        return createMenuItem(Material.CLOCK, "<!italic><red><bold>Server Restart",
                "<!italic><gray>Schedule and manage server restarts");
    }

    @Override
    public List<String> getMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(enabled ? "<green>Enabled" : "<red>Disabled");
        if (activeTaskId != -1) {
            lore.add("<yellow>⚠ Restart in progress!");
            lore.add("<gray>Time left: <red>" + countdownSeconds + "s");
        }
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
            meta.displayName(MessageManager.parse("<!italic><red><bold>Restart Now"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Immediately restart the server"));
            lore.add(MessageManager.parse("<!italic><gray>with a save-all command."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><red>⚠ This will kick all players!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Shift + Click to confirm"));
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
            meta.displayName(MessageManager.parse("<!italic><yellow><bold>Start Countdown"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Start a restart countdown"));
            lore.add(MessageManager.parse("<!italic><gray>with warnings to players."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Default time: <yellow>" + defaultTime + " seconds"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Left Click: Start (" + defaultTime + "s)"));
            lore.add(MessageManager.parse("<!italic><yellow>Right Click: Custom time"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCancelRestartItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><red><bold>Cancel Restart"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (activeTaskId != -1) {
                lore.add(MessageManager.parse("<!italic><green>✔ Restart is active"));
                lore.add(MessageManager.parse("<!italic><gray>Time remaining: <red>" + countdownSeconds + "s"));
                lore.add(Component.empty());
                lore.add(MessageManager.parse("<!italic><yellow>Click to cancel"));
            } else {
                lore.add(MessageManager.parse("<!italic><gray>No restart in progress"));
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
            meta.displayName(MessageManager.parse("<!italic><aqua><bold>Scheduled Restarts"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse(scheduledEnabled ? "<!italic><green>Enabled" : "<!italic><red>Disabled"));
            lore.add(MessageManager.parse("<!italic><gray>Configured times: <yellow>" + schedules.size()));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to manage schedules"));
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
            meta.displayName(MessageManager.parse("<!italic><gold><bold>Notification Types"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Current notifications:"));
            lore.add(MessageManager
                    .parse("<!italic><green>• <gray>Chat: " + (types.contains("chat") ? "<green>✔" : "<red>✘")));
            lore.add(MessageManager.parse(
                    "<!italic><green>• <gray>ActionBar: " + (types.contains("actionbar") ? "<green>✔" : "<red>✘")));
            lore.add(MessageManager
                    .parse("<!italic><green>• <gray>BossBar: " + (types.contains("bossbar") ? "<green>✔" : "<red>✘")));
            lore.add(MessageManager
                    .parse("<!italic><green>• <gray>Title: " + (types.contains("title") ? "<green>✔" : "<red>✘")));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to cycle"));
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
            meta.displayName(MessageManager.parse("<!italic><yellow><bold>Countdown Time"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Default countdown: <yellow>" + time + " seconds"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><green>Left Click: +10s"));
            lore.add(MessageManager.parse("<!italic><red>Right Click: -10s"));
            lore.add(MessageManager.parse("<!italic><yellow>Shift Click: ±60s"));
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
            meta.displayName(MessageManager.parse("<!italic><light_purple><bold>Pre-Restart Commands"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse(execute ? "<!italic><green>Enabled" : "<!italic><red>Disabled"));
            lore.add(MessageManager.parse("<!italic><gray>Commands: <yellow>" + commands.size()));
            if (!commands.isEmpty()) {
                lore.add(Component.empty());
                for (String cmd : commands) {
                    lore.add(MessageManager.parse("<!italic><dark_gray>- <gray>/" + cmd));
                }
            }
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to toggle"));
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
            meta.displayName(MessageManager.parse("<!italic><light_purple><bold>BossBar Color"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Current: <yellow>" + color));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to cycle colors"));
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
            meta.displayName(MessageManager.parse("<!italic><yellow><bold>Scheduled Restarts"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse(enabled ? "<!italic><green>Enabled" : "<!italic><red>Disabled"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to toggle"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createScheduleEntryItem(String schedule, int index) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><yellow>" + schedule));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Schedule #" + (index + 1)));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><red>Shift + Click to remove"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAddScheduleItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><green><bold>Add Schedule"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Add a new scheduled restart time."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Formats supported:"));
            lore.add(MessageManager.parse("<!italic><dark_gray>- <gray>HH:mm:ss <dark_gray>(daily)"));
            lore.add(MessageManager.parse("<!italic><dark_gray>- <gray>MON HH:mm:ss <dark_gray>(weekly)"));
            lore.add(MessageManager.parse("<!italic><dark_gray>- <gray>yyyy-MM-dd HH:mm:ss <dark_gray>(one-time)"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to add via chat"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><yellow>Back to Main Menu"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackToMainItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><yellow>Back to Restart Settings"));
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
        Bukkit.broadcast(MessageManager.parse(getRestartMessage("restart-now-message", 0)
                .replace("{player}", executor.getName())));

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
        String message = plugin.getConfigManager().get()
                .getString(getConfigPath() + ".messages." + key, "<red>Server restarting in {time} seconds!");
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
