package com.tejaslamba.vanillacore.listener;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.ServerRestartFeature;
import com.tejaslamba.vanillacore.menu.GuiHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRestartListener implements Listener {

    private final VanillaCorePlugin plugin;
    private final Map<UUID, ScheduleInputState> awaitingInput = new ConcurrentHashMap<>();

    public ServerRestartListener(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ServerRestartFeature feature = getFeature();

        if (feature == null || !feature.isRestartActive())
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (feature.isRestartActive()) {
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.active-warning"));
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.time-remaining",
                        "seconds", feature.getCountdownSeconds()));
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.cancel-hint"));
            }
        }, 60L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder gh)) {
            return;
        }
        if (gh.getId().equals("server-restart")) {
            handleMainGUI(event, player);
        } else if (gh.getId().equals("server-restart-schedule")) {
            handleScheduleGUI(event, player);
        }
    }

    private void handleMainGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        ServerRestartFeature feature = getFeature();
        if (feature == null)
            return;

        int slot = event.getRawSlot();
        ClickType clickType = event.getClick();

        switch (slot) {
            case 10 -> {
                if (clickType.isShiftClick()) {
                    player.closeInventory();
                    feature.restartNow(player);
                } else {
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.shift-confirm"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
            case 12 -> {
                if (clickType.isRightClick()) {
                    player.closeInventory();
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.countdown-input"));
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.cancel-input-hint"));
                    awaitingInput.put(player.getUniqueId(), ScheduleInputState.COUNTDOWN_TIME);
                } else {
                    int defaultTime = plugin.getConfigManager().get()
                            .getInt(feature.getConfigPath() + ".countdown-time", 60);
                    feature.startCountdown(defaultTime);
                    player.closeInventory();
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.countdown-started",
                            "seconds", defaultTime));
                }
            }
            case 14 -> {
                if (feature.isRestartActive()) {
                    feature.cancelRestart();
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.cancelled"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    feature.openMainGUI(player);
                } else {
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.not-in-progress"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
            case 16 -> {
                feature.openScheduleGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 28 -> {
                feature.cycleNotificationType();
                feature.openMainGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 30 -> {
                int delta = clickType.isShiftClick() ? 60 : 10;
                if (clickType.isRightClick()) {
                    delta = -delta;
                }
                feature.adjustCountdownTime(delta);
                feature.openMainGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 32 -> {
                feature.togglePreRestartCommands();
                feature.openMainGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 34 -> {
                feature.cycleBossBarColor();
                feature.openMainGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 40 -> {
                plugin.getMenuManager().openMainMenu(player);
            }
        }
    }

    private void handleScheduleGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        ServerRestartFeature feature = getFeature();
        if (feature == null)
            return;

        int slot = event.getRawSlot();
        ClickType clickType = event.getClick();

        switch (slot) {
            case 4 -> {
                boolean current = feature.isScheduledRestartsEnabled();
                feature.setScheduledRestartsEnabled(!current);
                feature.openScheduleGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 36 -> {
                feature.openMainGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case 40 -> {
                player.closeInventory();
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.schedule-input"));
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.schedule-formats"));
                player.sendMessage(plugin.getMessageManager().get("server-restart.messages.cancel-input-hint"));
                awaitingInput.put(player.getUniqueId(), ScheduleInputState.ADD_SCHEDULE);
            }
            default -> {
                if (slot >= 19 && slot <= 25 && clickType.isShiftClick()) {
                    int index = slot - 19;
                    feature.removeScheduledTime(index);
                    feature.openScheduleGUI(player);
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.schedule-removed"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        event.setCancelled(true);
        String message = event.getMessage().trim();
        ScheduleInputState state = awaitingInput.remove(uuid);
        if (state == null)
            return;

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getMessageManager().get("general.cancelled"));
            return;
        }

        ServerRestartFeature feature = getFeature();
        if (feature == null)
            return;

        switch (state) {
            case COUNTDOWN_TIME -> {
                try {
                    int seconds = Integer.parseInt(message);
                    if (seconds < 10) {
                        player.sendMessage(plugin.getMessageManager().get("server-restart.messages.minimum-countdown"));
                        return;
                    }
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        feature.startCountdown(seconds);
                        player.sendMessage(plugin.getMessageManager().get("server-restart.messages.countdown-started",
                                "seconds", seconds));
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.invalid-seconds"));
                }
            }
            case ADD_SCHEDULE -> {
                if (isValidScheduleFormat(message)) {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        feature.addScheduledTime(message);
                        player.sendMessage(plugin.getMessageManager().get("server-restart.messages.schedule-added",
                                "schedule", message));
                        feature.openScheduleGUI(player);
                    });
                } else {
                    player.sendMessage(plugin.getMessageManager().get("server-restart.messages.invalid-format"));
                }
            }
        }
    }

    private boolean isValidScheduleFormat(String schedule) {
        return schedule.matches("\\d{2}:\\d{2}:\\d{2}") ||
                schedule.matches("(?i)(MON|TUE|WED|THU|FRI|SAT|SUN) \\d{2}:\\d{2}:\\d{2}") ||
                schedule.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    private ServerRestartFeature getFeature() {
        return (ServerRestartFeature) plugin.getFeatureManager().getFeature("Server Restart");
    }

    private enum ScheduleInputState {
        COUNTDOWN_TIME,
        ADD_SCHEDULE
    }
}

