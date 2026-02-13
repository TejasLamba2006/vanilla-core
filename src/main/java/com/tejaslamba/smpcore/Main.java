package com.tejaslamba.smpcore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import com.tejaslamba.smpcore.manager.CommandManager;
import com.tejaslamba.smpcore.manager.ConfigManager;
import com.tejaslamba.smpcore.manager.MenuManager;
import com.tejaslamba.smpcore.manager.MenuConfigManager;
import com.tejaslamba.smpcore.manager.MessageManager;
import com.tejaslamba.smpcore.manager.CooldownManager;
import com.tejaslamba.smpcore.manager.ChatInputManager;
import com.tejaslamba.smpcore.manager.FeatureManager;
import com.tejaslamba.smpcore.manager.CDNManager;
import com.tejaslamba.smpcore.listener.UpdateNotificationListener;

public class Main extends JavaPlugin {

    private static Main instance;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private MenuConfigManager menuConfigManager;
    private CooldownManager cooldownManager;
    private ChatInputManager chatInputManager;
    private FeatureManager featureManager;
    private CDNManager cdnManager;
    private UpdateNotificationListener updateNotificationListener;
    private boolean verboseLogging = false;

    @Override
    public void onEnable() {
        instance = this;

        migrateDataFolderIfNeeded();

        int bStatsId = 28654;
        new Metrics(this, bStatsId);

        configManager = new ConfigManager(this);
        configManager.load();
        refreshVerbose();
        messageManager = new MessageManager(this);
        messageManager.load();
        menuConfigManager = new MenuConfigManager(this);
        menuConfigManager.load();
        cooldownManager = new CooldownManager();
        chatInputManager = new ChatInputManager();
        featureManager = new FeatureManager(this);
        featureManager.loadFeatures();
        menuManager = new MenuManager(this);
        menuManager.load();
        commandManager = new CommandManager(this);
        commandManager.registerDefaults();
        getServer().getPluginManager().registerEvents(new com.tejaslamba.smpcore.listener.MenuClickListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.tejaslamba.smpcore.listener.ChatInputListener(this),
                this);

        cdnManager = new CDNManager(this);
        cdnManager.initialize();
        updateNotificationListener = new UpdateNotificationListener(this);
        getServer().getPluginManager().registerEvents(updateNotificationListener, this);

        getLogger().info("Vanilla Core has been enabled!");
        getLogger().info("Config Builder: https://smpcore.tejaslamba.com/config-builder");
    }

    private void migrateDataFolderIfNeeded() {
        File newDataFolder = getDataFolder();
        File newConfigFile = new File(newDataFolder, "config.yml");
        if (newConfigFile.exists()) {
            return;
        }

        File pluginsFolder = newDataFolder.getParentFile();
        if (pluginsFolder == null || !pluginsFolder.isDirectory()) {
            return;
        }

        String[] oldFolderNames = { "smp-core", "SMP-Core", "SMPCore", "smpcore", "SMPcore" };
        File oldDataFolder = null;
        for (String oldFolderName : oldFolderNames) {
            File candidate = new File(pluginsFolder, oldFolderName);
            if (candidate.isDirectory() && new File(candidate, "config.yml").exists()) {
                oldDataFolder = candidate;
                break;
            }
        }

        if (oldDataFolder == null) {
            return;
        }

        try {
            Path sourceRoot = oldDataFolder.toPath();
            Path targetRoot = newDataFolder.toPath();

            Files.walk(sourceRoot).forEach(sourcePath -> {
                try {
                    Path relative = sourceRoot.relativize(sourcePath);
                    Path targetPath = targetRoot.resolve(relative);

                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                        return;
                    }

                    if (Files.exists(targetPath)) {
                        return;
                    }

                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath);
                } catch (IOException e) {
                    getLogger().warning("Failed migrating file: " + sourcePath + " (" + e.getMessage() + ")");
                }
            });

            getLogger().info("Migrated data folder from " + oldDataFolder.getName() + " to " + newDataFolder.getName());
        } catch (IOException e) {
            getLogger().warning("Failed migrating plugin data folder (" + e.getMessage() + ")");
        }
    }

    @Override
    public void onDisable() {
        if (featureManager != null) {
            featureManager.disableAll();
        }
        if (cooldownManager != null) {
            cooldownManager.shutdown();
        }
        if (menuManager != null) {
            menuManager.shutdown();
        }
        getLogger().info("Vanilla Core has been disabled!");
    }

    public void refreshVerbose() {
        this.verboseLogging = configManager.get().getBoolean("plugin.verbose", false);
    }

    public boolean isVerbose() {
        return verboseLogging;
    }

    public static Main getInstance() {
        return instance;
    }

    public CommandManager getSmpCommandManager() {
        return commandManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public MenuConfigManager getMenuConfigManager() {
        return menuConfigManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CDNManager getCDNManager() {
        return cdnManager;
    }

    public UpdateNotificationListener getUpdateNotificationListener() {
        return updateNotificationListener;
    }

}