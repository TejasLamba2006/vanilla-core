package com.tejaslamba.vanillacore.social;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.database.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialManager {

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+)");

    private final VanillaCorePlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, SocialPreferences> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> blockCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SocialManager(VanillaCorePlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void preload(UUID uuid) {
        getPreferences(uuid);
        Set<UUID> blocked = databaseManager.getBlockedPlayers(uuid);
        Set<UUID> cachedBlocked = ConcurrentHashMap.newKeySet();
        if (blocked != null) {
            cachedBlocked.addAll(blocked);
        }
        blockCache.put(uuid, cachedBlocked);
    }

    public void unload(UUID uuid) {
        SocialPreferences prefs = cache.remove(uuid);
        if (prefs != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.savePreferences(prefs));
        }
        blockCache.remove(uuid);
    }

    public void shutdown() {
        for (SocialPreferences prefs : cache.values()) {
            try {
                databaseManager.savePreferences(prefs);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save preferences on shutdown for " + prefs.getUuid() + ": " + e.getMessage());
            }
        }
        cache.clear();
        blockCache.clear();
    }

    public boolean isCommandEnabled(String key) {
        return plugin.getConfigManager().get().getBoolean("social.commands." + key + ".enabled", true);
    }

    public long applyCooldown(Player player, String key) {
        long cooldownSeconds = plugin.getConfigManager().get().getLong("social.commands." + key + ".cooldown-seconds",
                0L);
        if (cooldownSeconds <= 0) {
            return 0;
        }
        String cooldownKey = player.getUniqueId() + ":" + key;
        long now = System.currentTimeMillis();
        long expireAt = cooldowns.getOrDefault(cooldownKey, 0L);
        if (expireAt > now) {
            return Math.max(1L, (expireAt - now + 999L) / 1000L);
        }
        cooldowns.put(cooldownKey, now + (cooldownSeconds * 1000L));
        return 0;
    }

    public void toggleChat(Player player) {
        SocialPreferences preferences = getPreferences(player.getUniqueId());
        preferences.setChatEnabled(!preferences.isChatEnabled());
        saveAsync(preferences);
    }

    public void togglePm(Player player) {
        SocialPreferences preferences = getPreferences(player.getUniqueId());
        preferences.setPmEnabled(!preferences.isPmEnabled());
        saveAsync(preferences);
    }

    public void toggleMentions(Player player) {
        SocialPreferences preferences = getPreferences(player.getUniqueId());
        preferences.setMentionsEnabled(!preferences.isMentionsEnabled());
        saveAsync(preferences);
    }

    public void toggleSocialSpy(Player player) {
        SocialPreferences preferences = getPreferences(player.getUniqueId());
        preferences.setSocialSpyEnabled(!preferences.isSocialSpyEnabled());
        saveAsync(preferences);
    }

    public boolean isChatEnabled(Player player) {
        return getPreferences(player.getUniqueId()).isChatEnabled();
    }

    public boolean isPmEnabled(Player player) {
        return getPreferences(player.getUniqueId()).isPmEnabled();
    }

    public boolean isMentionsEnabled(Player player) {
        return getPreferences(player.getUniqueId()).isMentionsEnabled();
    }

    public boolean isSocialSpyEnabled(Player player) {
        return getPreferences(player.getUniqueId()).isSocialSpyEnabled();
    }

    public void block(Player blocker, Player blocked) {
        databaseManager.block(blocker.getUniqueId(), blocked.getUniqueId());
        Set<UUID> blockedSet = blockCache.computeIfAbsent(blocker.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        blockedSet.add(blocked.getUniqueId());
    }

    public void unblock(Player blocker, Player blocked) {
        databaseManager.unblock(blocker.getUniqueId(), blocked.getUniqueId());
        Set<UUID> blockedSet = blockCache.get(blocker.getUniqueId());
        if (blockedSet != null) {
            blockedSet.remove(blocked.getUniqueId());
        }
    }

    public boolean isBlockedEitherWay(UUID a, UUID b) {
        Set<UUID> blockedByA = blockCache.get(a);
        Set<UUID> blockedByB = blockCache.get(b);
        boolean aBlocksB = (blockedByA != null) ? blockedByA.contains(b) : databaseManager.isBlocked(a, b);
        boolean bBlocksA = (blockedByB != null) ? blockedByB.contains(a) : databaseManager.isBlocked(b, a);
        return aBlocksB || bBlocksA;
    }

    public Set<UUID> getBlockedPlayers(UUID blocker) {
        Set<UUID> cached = blockCache.get(blocker);
        if (cached != null) {
            return new HashSet<>(cached);
        }
        return databaseManager.getBlockedPlayers(blocker);
    }

    public Player getLastReplyTarget(Player player) {
        UUID last = getPreferences(player.getUniqueId()).getLastReplyTarget();
        return last == null ? null : Bukkit.getPlayer(last);
    }

    public void sendPrivateMessage(Player sender, Player target, String content) {
        Component senderLine = miniMessage.deserialize(buildPmFormat(
                plugin.getConfigManager().get().getString("social.pm.format-sender",
                        "<gray>[to <yellow>{target}<gray>] <white>{message}"),
                sender, target, content));
        Component targetLine = miniMessage.deserialize(buildPmFormat(
                plugin.getConfigManager().get().getString("social.pm.format-target",
                        "<gray>[from <yellow>{sender}<gray>] <white>{message}"),
                sender, target, content));

        sender.sendMessage(senderLine);
        target.sendMessage(targetLine);

        SocialPreferences senderPrefs = getPreferences(sender.getUniqueId());
        SocialPreferences targetPrefs = getPreferences(target.getUniqueId());
        senderPrefs.setLastReplyTarget(target.getUniqueId());
        targetPrefs.setLastReplyTarget(sender.getUniqueId());
        saveAsync(senderPrefs);
        saveAsync(targetPrefs);

        broadcastSpy(sender, target, content);
    }

    public Component formatChat(Player sender, String plainMessage) {
        String enriched = enrichUrlsAndMentions(plainMessage);
        String format = resolveGroupFormat(sender);
        String nameEscaped = MiniMessage.miniMessage().escapeTags(sender.getName());
        String displayMM = MiniMessage.miniMessage().serialize(sender.displayName());
        String withValues = format
                .replace("{name}", nameEscaped)
                .replace("<name>", nameEscaped)
                .replace("{display}", displayMM)
                .replace("<display>", displayMM)
                .replace("{message}", enriched)
                .replace("<message>", enriched);
        return miniMessage.deserialize(withValues);
    }

    public void handleMentions(Player sender, String plainMessage) {
        if (!plugin.getConfigManager().get().getBoolean("social.chat.mentions.enabled", true)) {
            return;
        }

        String mentionPrefix = plugin.getConfigManager().get().getString("social.chat.mentions.prefix", "@");
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Set<UUID> notified = new HashSet<>();
        for (Player online : players) {
            String token = mentionPrefix + online.getName();
            boolean shouldNotify = !online.getUniqueId().equals(sender.getUniqueId())
                    && plainMessage.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT))
                    && isMentionsEnabled(online)
                    && !notified.contains(online.getUniqueId());
            if (!shouldNotify) {
                continue;
            }
            notified.add(online.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                String soundName = plugin.getConfigManager().get().getString("social.chat.mentions.sound",
                        "ENTITY_EXPERIENCE_ORB_PICKUP");
                try {
                    online.playSound(online.getLocation(), Sound.valueOf(soundName), 1.0f, 1.2f);
                } catch (Exception ignored) {
                    online.playSound(online.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
                online.sendMessage(plugin.getMessageManager().getPrefixed("social.mentions.notified", "player",
                        sender.getName()));
            });
        }
    }

    public String enrichUrlsAndMentions(String input) {
        String escaped = MiniMessage.miniMessage().escapeTags(input);
        String withUrls = applyUrlFormatting(escaped);
        return applyMentionFormatting(withUrls);
    }

    private String applyUrlFormatting(String input) {
        if (!plugin.getConfigManager().get().getBoolean("social.chat.url-formatting.enabled", true)) {
            return input;
        }
        String urlStyle = plugin.getConfigManager().get().getString("social.chat.url-formatting.style",
                "<aqua><underlined>{url}</underlined></aqua>");
        Matcher matcher = URL_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String rawUrl = matcher.group(1);
            String finalUrl = rawUrl.startsWith("http") ? rawUrl : "https://" + rawUrl;
            String replacement = "<click:open_url:'" + finalUrl + "'>"
                    + urlStyle.replace("{url}", rawUrl)
                    + "</click>";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String applyMentionFormatting(String input) {
        if (!plugin.getConfigManager().get().getBoolean("social.chat.mentions.enabled", true)) {
            return input;
        }
        String mentionPrefix = plugin.getConfigManager().get().getString("social.chat.mentions.prefix", "@");
        String mentionStyle = plugin.getConfigManager().get().getString("social.chat.mentions.style",
                "<yellow>{mention}</yellow>");
        String result = input;
        for (Player online : Bukkit.getOnlinePlayers()) {
            String mention = mentionPrefix + online.getName();
            if (result.toLowerCase(Locale.ROOT).contains(mention.toLowerCase(Locale.ROOT))) {
                result = replaceIgnoreCase(result, mention, mentionStyle.replace("{mention}", mention));
            }
        }
        return result;
    }

    private String replaceIgnoreCase(String text, String find, String replacement) {
        return text.replaceAll("(?i)" + Pattern.quote(find), Matcher.quoteReplacement(replacement));
    }

    private String resolveGroupFormat(Player player) {
        String fallback = plugin.getConfigManager().get().getString("social.chat.default-format",
                "<gray><name><dark_gray>: <white>{message}");
        ConfigurationSection groupsSection = plugin.getConfigManager().get()
                .getConfigurationSection("social.chat.groups");
        if (groupsSection == null) {
            return fallback;
        }

        List<String> keys = new ArrayList<>(groupsSection.getKeys(false));
        for (String key : keys) {
            String permission = groupsSection.getString(key + ".permission", "");
            if (permission.isBlank() || player.hasPermission(permission)) {
                return groupsSection.getString(key + ".format", fallback);
            }
        }
        return fallback;
    }

    private String buildPmFormat(String template, Player sender, Player target, String content) {
        String senderEscaped = MiniMessage.miniMessage().escapeTags(sender.getName());
        String targetEscaped = MiniMessage.miniMessage().escapeTags(target.getName());
        String messageEnriched = enrichUrlsAndMentions(content);
        return template
                .replace("{sender}", senderEscaped)
                .replace("<sender>", senderEscaped)
                .replace("{target}", targetEscaped)
                .replace("<target>", targetEscaped)
                .replace("{message}", messageEnriched)
                .replace("<message>", messageEnriched);
    }

    private void broadcastSpy(Player sender, Player target, String content) {
        if (!plugin.getConfigManager().get().getBoolean("social.pm.socialspy-enabled", true)) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean canSpy = !online.getUniqueId().equals(sender.getUniqueId())
                    && !online.getUniqueId().equals(target.getUniqueId())
                    && online.hasPermission("smp.socialspy")
                    && isSocialSpyEnabled(online);
            if (!canSpy) {
                continue;
            }
            online.sendMessage(plugin.getMessageManager().getPrefixed("social.pm.spy",
                    "sender", sender.getName(),
                    "target", target.getName(),
                    "message", content));
        }
    }

    private SocialPreferences getPreferences(UUID uuid) {
        return cache.computeIfAbsent(uuid, databaseManager::getOrCreatePreferences);
    }

    private void saveAsync(SocialPreferences preferences) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.savePreferences(preferences));
    }
}

