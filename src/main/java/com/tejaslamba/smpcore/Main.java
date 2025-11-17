package com.tejaslamba.smpcore;

import org.bukkit.plugin.java.JavaPlugin;
import com.tejaslamba.smpcore.manager.CommandManager;
import com.tejaslamba.smpcore.manager.ConfigManager;
import com.tejaslamba.smpcore.manager.PlayerManager;
import com.tejaslamba.smpcore.manager.MenuManager;
import com.tejaslamba.smpcore.manager.MenuConfigManager;
import com.tejaslamba.smpcore.manager.BanManager;
import com.tejaslamba.smpcore.manager.CooldownManager;
import com.tejaslamba.smpcore.manager.CombatManager;
import com.tejaslamba.smpcore.manager.ChatInputManager;
import com.tejaslamba.smpcore.manager.FeatureManager;
import com.tejaslamba.smpcore.listener.PlayerJoinListener;
import com.tejaslamba.smpcore.listener.ItemBanListener;

public class Main extends JavaPlugin {

    private static Main instance;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private MenuManager menuManager;
    private MenuConfigManager menuConfigManager;
    private BanManager banManager;
    private CooldownManager cooldownManager;
    private CombatManager combatManager;
    private ChatInputManager chatInputManager;
    private FeatureManager featureManager;
    private ItemBanListener sharedItemBanListener;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        configManager.load();
        menuConfigManager = new MenuConfigManager(this);
        menuConfigManager.load();
        banManager = new BanManager(this);
        banManager.load();
        cooldownManager = new CooldownManager(this);
        combatManager = new CombatManager(this);
        combatManager.load();
        chatInputManager = new ChatInputManager();
        playerManager = new PlayerManager(this);
        sharedItemBanListener = new ItemBanListener(this);
        featureManager = new FeatureManager(this);
        featureManager.loadFeatures();
        menuManager = new MenuManager(this);
        menuManager.load();
        commandManager = new CommandManager(this);
        commandManager.registerDefaults();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new com.tejaslamba.smpcore.listener.MenuClickListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.tejaslamba.smpcore.listener.ChatInputListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.tejaslamba.smpcore.command.NetheriteCommand(this), this);

        getLogger().info("SMP Core has been enabled!");
    }

    @Override
    public void onDisable() {
        if (featureManager != null) {
            featureManager.disableAll();
        }
        if (cooldownManager != null) {
            cooldownManager.shutdown();
        }
        if (combatManager != null) {
            combatManager.shutdown();
        }
        if (playerManager != null) {
            playerManager.shutdown();
        }
        if (menuManager != null) {
            menuManager.shutdown();
        }
        getLogger().info("SMP Core has been disabled!");
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

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public MenuConfigManager getMenuConfigManager() {
        return menuConfigManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public ItemBanListener getSharedItemBanListener() {
        return sharedItemBanListener;
    }

}