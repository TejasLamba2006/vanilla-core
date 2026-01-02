package com.tejaslamba.smpcore.manager;

import com.tejaslamba.smpcore.Main;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CDNManager {

    private static final String CDN_BASE = "https://smpcore.tejaslamba.com/cdn";
    private static final String MANIFEST_URL = CDN_BASE + "/manifest.json";
    private static final int TIMEOUT_MS = 5000;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000;

    private final Main plugin;
    private final Gson gson;
    private final ConcurrentHashMap<String, CachedData> cache;

    private String latestVersion;
    private String configBuilderUrl;
    private String documentationUrl;
    private Set<String> disabledFeatures;
    private boolean maintenanceMode;
    private String disabledMessage;
    private String maintenanceMessage;
    private boolean updateNotificationEnabled;
    private String updateNotificationPermission;
    private String updateNotificationTitle;
    private String updateNotificationMessage;
    private String updateNotificationActionMessage;
    private long lastFetch;

    public CDNManager(Main plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.cache = new ConcurrentHashMap<>();
        this.disabledFeatures = new HashSet<>();
        this.maintenanceMode = false;
        this.disabledMessage = "This feature has been temporarily disabled by the plugin author.";
        this.maintenanceMessage = "SMP Core is currently in maintenance mode. Please try again later.";
        this.updateNotificationEnabled = true;
        this.updateNotificationPermission = "smpcore.admin";
        this.updateNotificationTitle = "SMP Core Update";
        this.updateNotificationMessage = "A new version is available! Current: {current}, Latest: {latest}";
        this.updateNotificationActionMessage = "Visit {url} to download";
        this.lastFetch = 0;
    }

    public void initialize() {
        fetchManifestAsync().thenAccept(success -> {
            if (success) {
                fetchMenuConfigAsync();
                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] CDN data fetched successfully");
                    plugin.getLogger().info("[VERBOSE] Latest version: " + latestVersion);
                }
            }
        });
    }

    public CompletableFuture<Boolean> fetchManifestAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = fetchUrl(MANIFEST_URL);
                if (json == null)
                    return false;

                JsonObject manifest = JsonParser.parseString(json).getAsJsonObject();
                latestVersion = manifest.has("latestVersion") ? manifest.get("latestVersion").getAsString() : null;
                configBuilderUrl = manifest.has("configBuilderUrl") ? manifest.get("configBuilderUrl").getAsString()
                        : "https://smpcore.tejaslamba.com/config-builder";
                documentationUrl = manifest.has("documentationUrl") ? manifest.get("documentationUrl").getAsString()
                        : "https://smpcore.tejaslamba.com/docs";
                lastFetch = System.currentTimeMillis();
                return true;
            } catch (Exception e) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().warning("[VERBOSE] Failed to fetch CDN manifest: " + e.getMessage());
                }
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> fetchMenuConfigAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String version = latestVersion != null ? latestVersion : "1.1.0";
                String menuUrl = CDN_BASE + "/config/" + version + "/menu.json";
                String json = fetchUrl(menuUrl);
                if (json == null)
                    return false;

                JsonObject menuConfig = JsonParser.parseString(json).getAsJsonObject();

                if (menuConfig.has("featureControl")) {
                    JsonObject featureControl = menuConfig.getAsJsonObject("featureControl");

                    disabledFeatures.clear();
                    if (featureControl.has("disabledFeatures")) {
                        JsonArray disabled = featureControl.getAsJsonArray("disabledFeatures");
                        for (int i = 0; i < disabled.size(); i++) {
                            disabledFeatures.add(disabled.get(i).getAsString());
                        }
                    }

                    maintenanceMode = featureControl.has("maintenanceMode")
                            && featureControl.get("maintenanceMode").getAsBoolean();

                    if (featureControl.has("disabledMessage")) {
                        disabledMessage = featureControl.get("disabledMessage").getAsString();
                    }
                    if (featureControl.has("maintenanceMessage")) {
                        maintenanceMessage = featureControl.get("maintenanceMessage").getAsString();
                    }
                }

                if (menuConfig.has("updateNotification")) {
                    JsonObject updateNotif = menuConfig.getAsJsonObject("updateNotification");

                    if (updateNotif.has("enabled")) {
                        updateNotificationEnabled = updateNotif.get("enabled").getAsBoolean();
                    }
                    if (updateNotif.has("permission")) {
                        updateNotificationPermission = updateNotif.get("permission").getAsString();
                    }
                    if (updateNotif.has("title")) {
                        updateNotificationTitle = updateNotif.get("title").getAsString();
                    }
                    if (updateNotif.has("message")) {
                        updateNotificationMessage = updateNotif.get("message").getAsString();
                    }
                    if (updateNotif.has("actionMessage")) {
                        updateNotificationActionMessage = updateNotif.get("actionMessage").getAsString();
                    }
                }

                if (plugin.isVerbose()) {
                    plugin.getLogger().info("[VERBOSE] Menu config fetched. Disabled features: " + disabledFeatures);
                    plugin.getLogger().info("[VERBOSE] Maintenance mode: " + maintenanceMode);
                    plugin.getLogger().info("[VERBOSE] Update notifications enabled: " + updateNotificationEnabled);
                }

                return true;
            } catch (Exception e) {
                if (plugin.isVerbose()) {
                    plugin.getLogger().warning("[VERBOSE] Failed to fetch menu config: " + e.getMessage());
                }
                return false;
            }
        });
    }

    private String fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "SMPCore/" + plugin.getDescription().getVersion());

            if (conn.getResponseCode() != 200) {
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void refreshIfNeeded() {
        if (System.currentTimeMillis() - lastFetch > CACHE_DURATION_MS) {
            fetchManifestAsync().thenAccept(success -> {
                if (success) {
                    fetchMenuConfigAsync();
                }
            });
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return plugin.getDescription().getVersion();
    }

    public boolean isUpdateAvailable() {
        if (latestVersion == null)
            return false;
        return compareVersions(latestVersion, getCurrentVersion()) > 0;
    }

    public String getConfigBuilderUrl() {
        return configBuilderUrl != null ? configBuilderUrl : "https://smpcore.tejaslamba.com/config-builder";
    }

    public String getDocumentationUrl() {
        return documentationUrl != null ? documentationUrl : "https://smpcore.tejaslamba.com/docs";
    }

    public boolean isFeatureDisabled(String featureId) {
        return disabledFeatures.contains(featureId);
    }

    public Set<String> getDisabledFeatures() {
        return new HashSet<>(disabledFeatures);
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public String getDisabledMessage() {
        return disabledMessage;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public boolean isUpdateNotificationEnabled() {
        return updateNotificationEnabled;
    }

    public String getUpdateNotificationPermission() {
        return updateNotificationPermission;
    }

    public String getUpdateNotificationTitle() {
        return updateNotificationTitle;
    }

    public String getUpdateNotificationMessage() {
        return updateNotificationMessage;
    }

    public String getUpdateNotificationActionMessage() {
        return updateNotificationActionMessage;
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.replace("-SNAPSHOT", "").split("\\.");
        String[] parts2 = v2.replace("-SNAPSHOT", "").split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class CachedData {
        String data;
        long timestamp;

        CachedData(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
