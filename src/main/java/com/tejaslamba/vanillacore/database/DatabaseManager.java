package com.tejaslamba.vanillacore.database;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.social.SocialPreferences;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {

    private final VanillaCorePlugin plugin;
    private Connection connection;

    public DatabaseManager(VanillaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            String relativePath = plugin.getConfigManager().get().getString("database.path", "data/watchdog.db");
            File dbFile = new File(plugin.getDataFolder(), relativePath);
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_prefs ("
                        + "uuid TEXT PRIMARY KEY,"
                        + "chat_enabled INTEGER NOT NULL DEFAULT 1,"
                        + "pm_enabled INTEGER NOT NULL DEFAULT 1,"
                        + "mentions_enabled INTEGER NOT NULL DEFAULT 1,"
                        + "socialspy_enabled INTEGER NOT NULL DEFAULT 0,"
                        + "last_reply_target TEXT)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_blocks ("
                        + "blocker_uuid TEXT NOT NULL,"
                        + "blocked_uuid TEXT NOT NULL,"
                        + "PRIMARY KEY(blocker_uuid, blocked_uuid))");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    public synchronized void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to close database: " + e.getMessage());
        }
    }

    public synchronized SocialPreferences getOrCreatePreferences(UUID uuid) {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT chat_enabled, pm_enabled, mentions_enabled, socialspy_enabled, last_reply_target FROM player_prefs WHERE uuid = ?")) {
            select.setString(1, uuid.toString());
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    String lastReply = rs.getString("last_reply_target");
                    return new SocialPreferences(uuid,
                            rs.getInt("chat_enabled") == 1,
                            rs.getInt("pm_enabled") == 1,
                            rs.getInt("mentions_enabled") == 1,
                            rs.getInt("socialspy_enabled") == 1,
                            lastReply == null || lastReply.isBlank() ? null : UUID.fromString(lastReply));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load preferences for " + uuid + ": " + e.getMessage());
        }

        SocialPreferences defaults = new SocialPreferences(uuid, true, true, true, false, null);
        savePreferences(defaults);
        return defaults;
    }

    public synchronized void savePreferences(SocialPreferences preferences) {
        try (PreparedStatement upsert = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_prefs (uuid, chat_enabled, pm_enabled, mentions_enabled, socialspy_enabled, last_reply_target) VALUES (?, ?, ?, ?, ?, ?)")) {
            upsert.setString(1, preferences.getUuid().toString());
            upsert.setInt(2, preferences.isChatEnabled() ? 1 : 0);
            upsert.setInt(3, preferences.isPmEnabled() ? 1 : 0);
            upsert.setInt(4, preferences.isMentionsEnabled() ? 1 : 0);
            upsert.setInt(5, preferences.isSocialSpyEnabled() ? 1 : 0);
            upsert.setString(6,
                    preferences.getLastReplyTarget() == null ? null : preferences.getLastReplyTarget().toString());
            upsert.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger()
                    .warning("Failed to save preferences for " + preferences.getUuid() + ": " + e.getMessage());
        }
    }

    public synchronized boolean isBlocked(UUID blocker, UUID blocked) {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT 1 FROM player_blocks WHERE blocker_uuid = ? AND blocked_uuid = ?")) {
            statement.setString(1, blocker.toString());
            statement.setString(2, blocked.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check block relation: " + e.getMessage());
            return false;
        }
    }

    public synchronized void block(UUID blocker, UUID blocked) {
        try (PreparedStatement statement = connection
                .prepareStatement("INSERT OR IGNORE INTO player_blocks (blocker_uuid, blocked_uuid) VALUES (?, ?)")) {
            statement.setString(1, blocker.toString());
            statement.setString(2, blocked.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to block player: " + e.getMessage());
        }
    }

    public synchronized void unblock(UUID blocker, UUID blocked) {
        try (PreparedStatement statement = connection
                .prepareStatement("DELETE FROM player_blocks WHERE blocker_uuid = ? AND blocked_uuid = ?")) {
            statement.setString(1, blocker.toString());
            statement.setString(2, blocked.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unblock player: " + e.getMessage());
        }
    }

    public synchronized Set<UUID> getBlockedPlayers(UUID blocker) {
        Set<UUID> blockedPlayers = new HashSet<>();
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT blocked_uuid FROM player_blocks WHERE blocker_uuid = ?")) {
            statement.setString(1, blocker.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    blockedPlayers.add(UUID.fromString(rs.getString("blocked_uuid")));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to list blocked players: " + e.getMessage());
        }
        return blockedPlayers;
    }
}
