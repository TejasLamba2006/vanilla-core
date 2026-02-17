package com.tejaslamba.vanillacore.gui.feature;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.gui.AnvilInputGUI;
import com.tejaslamba.vanillacore.gui.ConfigMenuGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DimensionLockConfigGUI extends ConfigMenuGUI {

    private final String dimension;
    private final Material iconMaterial;

    public DimensionLockConfigGUI(Main plugin, Player player, String dimension) {
        super(plugin, player, "features.dimension-lock-" + dimension, 27, 
              "§6" + capitalize(dimension) + " Lock Config");
        this.dimension = dimension;
        this.iconMaterial = dimension.equals("end") ? Material.END_PORTAL_FRAME : Material.NETHERRACK;
    }

    @Override
    protected void setupItems() {
        boolean locked = (boolean) getPendingChange("enabled");
        
        inventory.setItem(10, createToggleItem(iconMaterial, 
            "§5Lock " + capitalize(dimension), locked,
            "§7When locked, players cannot",
            "§7enter the " + capitalize(dimension) + " dimension",
            "",
            locked ? "§cCurrently: LOCKED" : "§aCurrently: OPEN"));

        String lockedMessage = (String) getPendingChange("locked-message");
        inventory.setItem(12, createValueItem(Material.WRITABLE_BOOK, 
            "§eLocked Message", lockedMessage,
            "§7Message shown to players",
            "§7when they try to enter",
            "§7the locked dimension"));

        inventory.setItem(14, createItem(Material.BOOK, "§aHow It Works",
            "§7When enabled:",
            "§a• §7Players cannot use portals",
            "§a• §7Teleports to " + dimension + " are blocked",
            "§a• §7Custom message is shown",
            "",
            "§7When disabled:",
            "§a• §7Normal access to " + dimension,
            "§a• §7All portals work normally"));

        inventory.setItem(18, createBackButton());
        inventory.setItem(22, createSaveButton());
        inventory.setItem(26, createHelpItem());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 10) {
            boolean current = (boolean) getPendingChange("enabled");
            setPendingChange("enabled", !current);
            setPendingChange("locked", !current);
        } else if (slot == 12) {
            openMessageEditor();
        } else if (slot == 18) {
            openMainMenu();
        } else if (slot == 22 && isDirty) {
            saveChanges();
            openMainMenu();
        } else if (slot == 26) {
            player.sendMessage("§6§l=== " + capitalize(dimension) + " Lock Help ===");
            player.sendMessage("");
            player.sendMessage("§7This feature allows you to lock access");
            player.sendMessage("§7to the " + capitalize(dimension) + " dimension.");
            player.sendMessage("");
            player.sendMessage("§eWhen to use:");
            player.sendMessage("§7• Prevent early-game " + dimension + " access");
            player.sendMessage("§7• Event preparation (close " + dimension + ")");
            player.sendMessage("§7• Economy control (limit resources)");
            player.sendMessage("§7• Server maintenance");
            player.sendMessage("");
            player.sendMessage("§7Customize the locked message to explain");
            player.sendMessage("§7why the dimension is currently closed.");
        }
    }

    private void openMessageEditor() {
        String current = (String) getPendingChange("locked-message");
        new AnvilInputGUI.Builder(plugin, player)
            .prompt("Enter the locked message:")
            .defaultValue(current)
            .withNonEmptyValidator()
            .onComplete((p, input) -> {
                setPendingChange("locked-message", input);
                p.sendMessage("§a[Vanilla Core] Locked message updated!");
                open();
            })
            .onCancel(this::open)
            .build()
            .open();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
