package com.tejaslamba.vanillacore;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import com.tejaslamba.vanillacore.manager.CommandManager;
import com.tejaslamba.vanillacore.manager.ConfigManager;
import com.tejaslamba.vanillacore.manager.MenuManager;
import com.tejaslamba.vanillacore.manager.MenuConfigManager;
import com.tejaslamba.vanillacore.manager.MessageManager;
import com.tejaslamba.vanillacore.manager.CooldownManager;
import com.tejaslamba.vanillacore.manager.ChatInputManager;
import com.tejaslamba.vanillacore.manager.FeatureManager;
import com.tejaslamba.vanillacore.manager.CDNManager;
import com.tejaslamba.vanillacore.listener.UpdateNotificationListener;

public class VanillaCorePlugin extends JavaPlugin {

    private static VanillaCorePlugin instance;
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

        int bStatsId = 29512;
        new Metrics(this, bStatsId);

        configManager = new ConfigManager(this);
        configManager.load();
        refreshVerbose();
        messageManager = new MessageManager(this);
        messageManager.load();
        menuConfigManager = new MenuConfigManager(this);
        menuConfigManager.load();
        cooldownManager = new CooldownManager();
        getServer().getScheduler().runTaskTimerAsynchronously(this, cooldownManager::cleanup, 6000L, 6000L);
        chatInputManager = new ChatInputManager();
        featureManager = new FeatureManager(this);
        featureManager.loadFeatures();
        menuManager = new MenuManager(this);
        menuManager.load();
        commandManager = new CommandManager(this);
        commandManager.registerDefaults();
        getServer().getPluginManager().registerEvents(new com.tejaslamba.vanillacore.listener.MenuClickListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.tejaslamba.vanillacore.listener.ChatInputListener(this),
                this);

        cdnManager = new CDNManager(this);
        cdnManager.initialize();
        updateNotificationListener = new UpdateNotificationListener(this);
        getServer().getPluginManager().registerEvents(updateNotificationListener, this);

        getLogger().info("SMP Watchdog has been enabled!");
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
        getLogger().info("SMP Watchdog has been disabled!");
    }

    public void refreshVerbose() {
        this.verboseLogging = configManager.get().getBoolean("plugin.verbose", false);
    }

    public boolean isVerbose() {
        return verboseLogging;
    }

    public static VanillaCorePlugin getInstance() {
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