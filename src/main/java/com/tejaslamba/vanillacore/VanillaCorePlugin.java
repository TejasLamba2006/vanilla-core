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
import com.tejaslamba.vanillacore.manager.KitManager;
import com.tejaslamba.vanillacore.manager.TeleportDataManager;
import com.tejaslamba.vanillacore.manager.TeleportManager;
import com.tejaslamba.vanillacore.manager.UtilityManager;
import com.tejaslamba.vanillacore.listener.KitJoinListener;
import com.tejaslamba.vanillacore.listener.TeleportGuiListener;
import com.tejaslamba.vanillacore.listener.TeleportListener;
import com.tejaslamba.vanillacore.listener.UpdateNotificationListener;
import com.tejaslamba.vanillacore.listener.UtilityListener;
import com.tejaslamba.vanillacore.database.DatabaseManager;
import com.tejaslamba.vanillacore.social.AnnouncementsManager;
import com.tejaslamba.vanillacore.social.SocialListener;
import com.tejaslamba.vanillacore.social.SocialManager;

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
    private DatabaseManager databaseManager;
    private SocialManager socialManager;
    private AnnouncementsManager announcementsManager;
    private TeleportDataManager teleportDataManager;
    private TeleportManager teleportManager;
    private KitManager kitManager;
    private UtilityManager utilityManager;
    private boolean verboseLogging = false;

    @Override
    public void onEnable() {
        instance = this;

        int bStatsId = 29512;
        new Metrics(this, bStatsId);

        configManager = new ConfigManager(this);
        configManager.load();
        refreshVerbose();
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        socialManager = new SocialManager(this, databaseManager);
        announcementsManager = new AnnouncementsManager(this);
        teleportDataManager = new TeleportDataManager(this);
        teleportDataManager.load();
        teleportManager = new TeleportManager(this, teleportDataManager);
        utilityManager = new UtilityManager();
        kitManager = new KitManager(this, teleportDataManager);
        kitManager.reload();
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
        getServer().getPluginManager().registerEvents(new SocialListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new KitJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new UtilityListener(this), this);
        getServer().getScheduler().runTaskTimer(this, teleportManager::cleanupExpiredRequests, 40L, 40L);

        cdnManager = new CDNManager(this);
        cdnManager.initialize();
        announcementsManager.start();
        updateNotificationListener = new UpdateNotificationListener(this);
        getServer().getPluginManager().registerEvents(updateNotificationListener, this);

        getLogger().info("Vanilla Core has been enabled!");
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
        if (announcementsManager != null) {
            announcementsManager.stop();
        }
        if (socialManager != null) {
            socialManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (teleportDataManager != null) {
            teleportDataManager.saveAll();
        }
        if (kitManager != null) {
            kitManager.save();
        }
        if (utilityManager != null) {
            utilityManager.clear();
        }
        getLogger().info("Vanilla Core has been disabled!");
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

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SocialManager getSocialManager() {
        return socialManager;
    }

    public AnnouncementsManager getAnnouncementsManager() {
        return announcementsManager;
    }

    public TeleportDataManager getTeleportDataManager() {
        return teleportDataManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public UtilityManager getUtilityManager() {
        return utilityManager;
    }

}
