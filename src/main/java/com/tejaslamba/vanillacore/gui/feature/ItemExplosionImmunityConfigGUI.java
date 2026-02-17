package com.tejaslamba.vanillacore.gui.feature;

import com.tejaslamba.vanillacore.Main;
import com.tejaslamba.vanillacore.gui.ConfigMenuGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ItemExplosionImmunityConfigGUI extends ConfigMenuGUI {

    public ItemExplosionImmunityConfigGUI(Main plugin, Player player) {
        super(plugin, player, "features.item-explosion-immunity", 27, "§6Item Explosion Immunity");
    }

    @Override
    protected void setupItems() {
        boolean enabled = (boolean) getPendingChange("enabled");
        
        inventory.setItem(13, createToggleItem(Material.TNT, "§5Item Explosion Immunity", enabled,
            "§7Dropped items will be protected",
            "§7from all types of explosions:",
            "",
            "§a• §7TNT explosions",
            "§a• §7Creeper explosions",
            "§a• §7End crystal explosions",
            "§a• §7Bed explosions in nether/end",
            "§a• §7Respawn anchor explosions",
            "",
            "§7This prevents item loss during",
            "§7PvP fights or griefing attempts"));

        inventory.setItem(18, createBackButton());
        inventory.setItem(22, createSaveButton());
        inventory.setItem(26, createHelpItem());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 13) {
            boolean current = (boolean) getPendingChange("enabled");
            setPendingChange("enabled", !current);
        } else if (slot == 18) {
            openMainMenu();
        } else if (slot == 22 && isDirty) {
            saveChanges();
            openMainMenu();
        } else if (slot == 26) {
            player.sendMessage("§6§l=== Item Explosion Immunity Help ===");
            player.sendMessage("");
            player.sendMessage("§7When enabled, dropped items on the ground");
            player.sendMessage("§7will be immune to all explosion damage.");
            player.sendMessage("");
            player.sendMessage("§ePerfect for:");
            player.sendMessage("§7• PvP servers (prevent item loss)");
            player.sendMessage("§7• Protection from griefers");
            player.sendMessage("§7• Accidental TNT mishaps");
            player.sendMessage("");
            player.sendMessage("§7This is a simple on/off feature");
            player.sendMessage("§7with no additional configuration.");
        }
    }
}
