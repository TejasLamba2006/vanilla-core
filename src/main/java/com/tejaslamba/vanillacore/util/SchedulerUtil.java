package com.tejaslamba.vanillacore.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class SchedulerUtil {

    @FunctionalInterface
    public interface Cancellable {
        void cancel();
    }

    private SchedulerUtil() {
    }

    public static void runSync(Plugin plugin, Runnable runnable) {
        Object scheduler = getScheduler("getGlobalRegionScheduler");
        if (scheduler != null) {
            try {
                Method execute = scheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
                execute.invoke(scheduler, plugin, runnable);
                return;
            } catch (Exception ignored) {
            }
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        Object scheduler = getScheduler("getAsyncScheduler");
        if (scheduler != null) {
            try {
                Method runNow = scheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                Consumer<Object> consumer = task -> runnable.run();
                runNow.invoke(scheduler, plugin, consumer);
                return;
            } catch (Exception ignored) {
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static Cancellable runRepeating(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        Object scheduler = getScheduler("getGlobalRegionScheduler");
        if (scheduler != null) {
            try {
                Method runAtFixedRate = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class,
                        long.class, long.class);
                Consumer<Object> consumer = task -> runnable.run();
                Object task = runAtFixedRate.invoke(scheduler, plugin, consumer, delayTicks, periodTicks);
                return () -> {
                    try {
                        Method cancel = task.getClass().getMethod("cancel");
                        cancel.invoke(task);
                    } catch (Exception ignored) {
                    }
                };
            } catch (Exception ignored) {
            }
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        return task::cancel;
    }

    private static Object getScheduler(String getter) {
        try {
            Method method = Bukkit.getServer().getClass().getMethod(getter);
            return method.invoke(Bukkit.getServer());
        } catch (Exception ignored) {
            return null;
        }
    }
}
