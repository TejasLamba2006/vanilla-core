package com.tejaslamba.vanillacore.ritual;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.features.RitualFeature;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RitualManager {

    private final VanillaCorePlugin plugin;
    private RitualSession activeSession;
    private BukkitTask activeTask;
    private BossBar activeBossBar;

    public RitualManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasActiveRitual() {
        return activeSession != null;
    }

    public int getRemainingSeconds() {
        return activeSession != null ? activeSession.remainingSeconds : 0;
    }

    public String getActiveItemName() {
        return activeSession != null ? activeSession.itemName : "";
    }

    public String getActiveStarterName() {
        return activeSession != null ? activeSession.starterName : "";
    }

    public boolean startRitual(Player starter, RitualFeature feature) {
        int configuredSeconds = Math.max(1, feature.getDurationMinutes()) * 60;
        return startRitual(starter, feature, configuredSeconds, feature.getParticleColorName());
    }

    public boolean startRitual(Player starter, RitualFeature feature, int durationSeconds, String colorName) {
        if (starter == null || feature == null || activeSession != null) {
            return false;
        }

        ItemStack hand = starter.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            return false;
        }

        World world = starter.getWorld();
        Location center = starter.getLocation().clone().add(0.0, 1.0, 0.0);
        center.setPitch(0.0F);

        String itemName = formatMaterialName(hand.getType().name());
        int totalSeconds = Math.max(1, durationSeconds);
        String normalizedColorName = feature.normalizeParticleColorName(colorName);
        if (normalizedColorName == null) {
            normalizedColorName = feature.normalizeParticleColorName(feature.getParticleColorName());
        }
        if (normalizedColorName == null) {
            normalizedColorName = "BLUE";
        }
        Color particleColor = feature.getParticleColor(normalizedColorName);

        RitualSession session = new RitualSession(feature, starter.getName(), center, hand.clone(), itemName,
                totalSeconds, particleColor);
        activeSession = session;

        spawnDisplays(starter, session);
        createBossBar(session);

        if (feature.isPlayStartSound()) {
            try {
                Sound sound = Sound.valueOf(feature.getStartSound());
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    online.playSound(online.getLocation(), sound, 1.0F, 1.0F);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        MessageManager messages = plugin.getMessageManager();
        int roundedX = (int) Math.round(center.getX());
        int roundedY = (int) Math.round(center.getY());
        int roundedZ = (int) Math.round(center.getZ());

        Component lineOne = messages.getPrefixed("ritual.started-line1",
                "player", starter.getName(),
                "x", String.valueOf(roundedX),
                "y", String.valueOf(roundedY),
                "z", String.valueOf(roundedZ));

        Component lineTwo = messages.getPrefixed("ritual.started-line2",
                "world", world.getName(),
                "item", itemName);

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.sendMessage(lineOne);
            online.sendMessage(lineTwo);
        }

        updateDisplays();
        updateBossBar();

        activeTask = new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (activeSession != session) {
                    cancel();
                    return;
                }

                drawRitualCircle(session, ticks);
                ticks++;

                if (ticks % 20 == 0) {
                    session.remainingSeconds--;
                    if (session.remainingSeconds <= 0) {
                        completeRitual();
                        cancel();
                        return;
                    }

                    updateDisplays();
                    updateBossBar();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    public boolean cancelActiveRitual(String cancelledBy) {
        if (activeSession == null) {
            return false;
        }

        finishActiveRitual(false, cancelledBy);
        return true;
    }

    public void addBossBarViewer(Player player) {
        if (player != null && activeBossBar != null) {
            player.showBossBar(activeBossBar);
        }
    }

    public void shutdown() {
        if (activeSession != null) {
            finishActiveRitual(false, "system");
        }
    }

    private void completeRitual() {
        finishActiveRitual(true, null);
    }

    private void finishActiveRitual(boolean completed, String cancelledBy) {
        RitualSession session = activeSession;
        if (session == null) {
            return;
        }

        if (activeTask != null) {
            activeTask.cancel();
            activeTask = null;
        }

        if (completed) {
            playCompletionEffects(session);

            if (session.feature.isDropItemAtEnd() && session.center.getWorld() != null) {
                session.center.getWorld().dropItemNaturally(session.center, session.ritualItem.clone());
            }

            Component ended = plugin.getMessageManager().getPrefixed("ritual.ended", "item", session.itemName);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(ended);
            }
        } else {
            Component cancelled = plugin.getMessageManager().getPrefixed("ritual.cancelled",
                    "player", cancelledBy == null ? "unknown" : cancelledBy);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(cancelled);
            }
        }

        if (activeBossBar != null) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.hideBossBar(activeBossBar);
            }
            activeBossBar = null;
        }

        if (session.nameDisplay != null && session.nameDisplay.isValid()) {
            session.nameDisplay.remove();
        }
        if (session.timeDisplay != null && session.timeDisplay.isValid()) {
            session.timeDisplay.remove();
        }
        if (session.itemDisplay != null && session.itemDisplay.isValid()) {
            session.itemDisplay.remove();
        }

        activeSession = null;
    }

    private void playCompletionEffects(RitualSession session) {
        World world = session.center.getWorld();
        if (world == null) {
            return;
        }

        if (session.feature.isStrikeLightning()) {
            world.strikeLightningEffect(session.center);
        }

        if (session.feature.isBeaconBeamEffectEnabled()) {
            playBeaconBeam(world, session.center.clone(), session.feature.getBeaconBeamDurationSeconds(),
                    session.particleColor);
        }

        if (session.feature.isFireworkBurstEffectEnabled()) {
            launchFireworkBurst(world, session.center.clone(), session.particleColor);
        }
    }

    private void playBeaconBeam(World world, Location center, int durationSeconds, Color color) {
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);

        int maxTicks = Math.max(2, durationSeconds) * 10;
        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }

                Particle.DustOptions dust = new Particle.DustOptions(color, 1.5F);
                for (double y = 0.0D; y <= 12.0D; y += 0.5D) {
                    Location point = center.clone().add(0.0D, y, 0.0D);
                    world.spawnParticle(Particle.DUST, point, 1, dust);
                    world.spawnParticle(Particle.END_ROD, point, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void launchFireworkBurst(World world, Location center, Color color) {
        for (int i = 0; i < 3; i++) {
            int delay = i * 4;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Firework firework = world.spawn(center.clone().add(0.0D, 0.3D, 0.0D), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.clearEffects();
                meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(color)
                        .withFade(Color.WHITE)
                        .flicker(true)
                        .trail(true)
                        .build());
                meta.setPower(0);
                firework.setFireworkMeta(meta);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (firework.isValid()) {
                        firework.detonate();
                    }
                }, 2L);
            }, delay);
        }
    }

    private void spawnDisplays(Player starter, RitualSession session) {
        World world = starter.getWorld();

        session.nameDisplay = world.spawn(starter.getEyeLocation(), TextDisplay.class, display -> {
            display.setBillboard(Display.Billboard.CENTER);
            display.text(MessageManager.parse("<light_purple>" + session.itemName));
        });

        session.timeDisplay = world.spawn(starter.getEyeLocation().clone().subtract(0.0, 0.3, 0.0), TextDisplay.class,
                display -> {
                    display.setBillboard(Display.Billboard.CENTER);
                    display.text(MessageManager.parse("<yellow>" + formatClock(session.remainingSeconds)));
                });

        session.itemDisplay = world.spawn(session.center, ItemDisplay.class, display -> {
            display.setItemStack(session.ritualItem.clone());
            display.setGlowing(true);
        });
    }

    private void createBossBar(RitualSession session) {
        activeBossBar = BossBar.bossBar(Component.empty(), 1.0F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showBossBar(activeBossBar);
        }
    }

    private void updateDisplays() {
        RitualSession session = activeSession;
        if (session == null) {
            return;
        }

        if (session.timeDisplay != null && session.timeDisplay.isValid()) {
            session.timeDisplay.text(MessageManager.parse("<yellow>" + formatClock(session.remainingSeconds)));
        }
    }

    private void updateBossBar() {
        RitualSession session = activeSession;
        if (session == null || activeBossBar == null) {
            return;
        }

        int roundedX = (int) Math.round(session.center.getX());
        int roundedY = (int) Math.round(session.center.getY());
        int roundedZ = (int) Math.round(session.center.getZ());

        float progress = (float) session.remainingSeconds / (float) session.totalSeconds;
        activeBossBar.progress(Math.max(0.0F, Math.min(1.0F, progress)));
        activeBossBar.name(MessageManager.parse(
                "<light_purple>Ritual: <yellow>" + session.itemName + " <gray>(" + roundedX + ", " + roundedY + ", "
                        + roundedZ + ") <yellow>- " + session.remainingSeconds + "s"));

        if (session.remainingSeconds > session.totalSeconds / 2) {
            activeBossBar.color(BossBar.Color.PURPLE);
        } else if (session.remainingSeconds > session.totalSeconds / 4) {
            activeBossBar.color(BossBar.Color.BLUE);
        } else if (session.remainingSeconds > 10) {
            activeBossBar.color(BossBar.Color.YELLOW);
        } else {
            activeBossBar.color(BossBar.Color.RED);
        }
    }

    private void drawRitualCircle(RitualSession session, int ticks) {
        RitualFeature feature = session.feature;
        Location center = session.center;
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        double radius = Math.max(1.0D, feature.getRadius()) / 2.0D;
        double phase = ticks * 0.08D;
        Particle.DustOptions dust = new Particle.DustOptions(session.particleColor, 2.0F);

        for (int i = 0; i < 28; i++) {
            double angle = phase + (Math.PI * 2.0D * i / 28.0D);
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            Location point = center.clone().add(offsetX, 0.0D, offsetZ);
            world.spawnParticle(Particle.DUST, point, 1, dust);
            world.spawnParticle(Particle.ENCHANT, point, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private String formatClock(int seconds) {
        int minutes = seconds / 60;
        int remaining = seconds % 60;
        return String.format("%d:%02d", minutes, remaining);
    }

    private String formatMaterialName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private static final class RitualSession {
        private final RitualFeature feature;
        private final String starterName;
        private final Location center;
        private final ItemStack ritualItem;
        private final String itemName;
        private final int totalSeconds;
        private final Color particleColor;
        private int remainingSeconds;
        private TextDisplay nameDisplay;
        private TextDisplay timeDisplay;
        private ItemDisplay itemDisplay;

        private RitualSession(RitualFeature feature, String starterName, Location center, ItemStack ritualItem,
                String itemName, int totalSeconds, Color particleColor) {
            this.feature = feature;
            this.starterName = starterName;
            this.center = center;
            this.ritualItem = ritualItem;
            this.itemName = itemName;
            this.totalSeconds = totalSeconds;
            this.remainingSeconds = totalSeconds;
            this.particleColor = particleColor;
        }
    }
}
