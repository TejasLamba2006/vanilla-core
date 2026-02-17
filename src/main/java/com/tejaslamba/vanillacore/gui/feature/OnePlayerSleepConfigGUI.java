package com.tejaslamba.vanillacore.gui.feature;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.gui.AnvilInputGUI;
import com.tejaslamba.vanillacore.gui.ConfigMenuGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OnePlayerSleepConfigGUI extends ConfigMenuGUI {

    public OnePlayerSleepConfigGUI(Main plugin, Player player) {
        super(plugin, player, "features.one-player-sleep", 54, "§6One Player Sleep Config");
    }

    @Override
    protected void setupItems() {
        boolean enabled = (boolean) getPendingChange("enabled");
        inventory.setItem(4, createToggleItem(Material.RED_BED, "§9One Player Sleep", enabled,
            "§7Only one player needs to sleep",
            "§7to skip the night"));

        String sleepMessage = (String) getPendingChange("sleep-message");
        inventory.setItem(19, createValueItem(Material.WRITABLE_BOOK, "§eSleep Message", sleepMessage,
            "§7Message shown when a player",
            "§7starts sleeping",
            "",
            "§7Placeholders:",
            "§7{player} - Player name",
            "§7{displayname} - Display name"));

        String skipMessage = (String) getPendingChange("skip-message");
        inventory.setItem(21, createValueItem(Material.WRITABLE_BOOK, "§eSkip Message", skipMessage,
            "§7Message shown when night",
            "§7is skipped"));

        inventory.setItem(23, createItem(Material.BOOK, "§aHow It Works",
            "§7When enabled, only one player",
            "§7needs to sleep to skip the night",
            "",
            "§7This feature uses Minecraft's",
            "§7playersSleepingPercentage gamerule",
            "",
            "§7Set messages to empty (\"\") to",
            "§7disable that specific message"));

        inventory.setItem(45, createBackButton());
        inventory.setItem(49, createSaveButton());
        inventory.setItem(50, createCancelButton());
        inventory.setItem(53, createHelpItem());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 4) {
            boolean current = (boolean) getPendingChange("enabled");
            setPendingChange("enabled", !current);
        } else if (slot == 19) {
            openSleepMessageEditor();
        } else if (slot == 21) {
            openSkipMessageEditor();
        } else if (slot == 45) {
            openMainMenu();
        } else if (slot == 49 && isDirty) {
            saveChanges();
            openMainMenu();
        } else if (slot == 50 && isDirty) {
            discardChanges();
            openMainMenu();
        } else if (slot == 53) {
            player.sendMessage("§6§l=== One Player Sleep Help ===");
            player.sendMessage("");
            player.sendMessage("§7This feature allows any single player to skip");
            player.sendMessage("§7the night for the entire server.");
            player.sendMessage("");
            player.sendMessage("§eCustomization:");
            player.sendMessage("§7• Toggle the feature on/off");
            player.sendMessage("§7• Customize sleep and skip messages");
            player.sendMessage("§7• Use {player} placeholder in messages");
            player.sendMessage("");
            player.sendMessage("§7Documentation: §b/vanilla wiki");
        }
    }

    private void openSleepMessageEditor() {
        String current = (String) getPendingChange("sleep-message");
        new AnvilInputGUI.Builder(plugin, player)
            .prompt("Enter the sleep message (or 'empty' to disable):")
            .defaultValue(current)
            .onComplete((p, input) -> {
                String value = input.equalsIgnoreCase("empty") ? "" : input;
                setPendingChange("sleep-message", value);
                p.sendMessage("§a[Vanilla Core] Sleep message updated!");
                open();
            })
            .onCancel(this::open)
            .build()
            .open();
    }

    private void openSkipMessageEditor() {
        String current = (String) getPendingChange("skip-message");
        new AnvilInputGUI.Builder(plugin, player)
            .prompt("Enter the skip message (or 'empty' to disable):")
            .defaultValue(current)
            .onComplete((p, input) -> {
                String value = input.equalsIgnoreCase("empty") ? "" : input;
                setPendingChange("skip-message", value);
                p.sendMessage("§a[Vanilla Core] Skip message updated!");
                open();
            })
            .onCancel(this::open)
            .build()
            .open();
    }
}
