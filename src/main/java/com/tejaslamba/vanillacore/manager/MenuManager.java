package com.tejaslamba.vanillacore.manager;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.menu.MainMenu;
import org.bukkit.entity.Player;

public class MenuManager {

    private final Main plugin;
    private MainMenu mainMenu;

    public MenuManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        mainMenu = new MainMenu(plugin);
    }

    public void openMainMenu(Player player) {
        if (mainMenu != null) {
            mainMenu.open(player);
        }
    }

    public void shutdown() {
        mainMenu = null;
    }

}
