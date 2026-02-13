---
sidebar_position: 11
---

# GUI Reference

This page provides a complete guide to using the in-game configuration GUI. The GUI allows you to manage all Vanilla Core features without editing configuration files.

## Opening the Main Menu

To open the main configuration menu, run the following command:

```
/smp
```

**Required Permission:** `smpcore.admin`

When you run this command, a chest-like inventory window opens displaying all available features as clickable items.

<!-- Add screenshot here: main-menu.gif showing the menu opening -->

## Main Menu Layout

The main menu is organized as a 54-slot chest inventory (6 rows of 9 slots). Features are displayed in the top portion, with navigation and utility buttons at the bottom.

### Row 1-2: Item and Combat Features

The first two rows contain features related to items and combat balance:

| Slot | Feature | Item Display |
|------|---------|--------------|
| 1 | Enchantment Limiter | Enchanted Book |
| 2 | Item Limiter | Chest |
| 3 | Mace Limiter | Mace |
| 4 | Netherite Disabler | Netherite Ingot |

### Row 3: Dimension and World Features

| Slot | Feature | Item Display |
|------|---------|--------------|
| 1 | Nether Lock | Netherrack |
| 2 | End Lock | End Stone |
| 3 | Mob Manager | Spawner |
| 4 | One Player Sleep | Bed |

### Row 4: Miscellaneous Features

| Slot | Feature | Item Display |
|------|---------|--------------|
| 1 | Infinite Restock | Emerald |
| 2 | Invisible Kills | Potion of Invisibility |
| 3 | Item Explosion Immunity | TNT |
| 4 | Minimap Control | Map |
| 5 | Server Restart | Clock |

### Bottom Row: Utility Buttons

| Slot | Button | Function |
|------|--------|----------|
| 49 | Reload | Reloads all configuration files |
| 53 | Close | Closes the menu |

## Feature Status Indicators

Each feature item in the menu has visual indicators showing its current status:

### Enabled Features

- **Glow Effect:** The item has an enchantment glow
- **Lore Text:** Shows "Status: Enabled" in green
- **Item Name:** Displayed in green color

### Disabled Features

- **No Glow:** The item appears without enchantment glow
- **Lore Text:** Shows "Status: Disabled" in red
- **Item Name:** Displayed in red color

<!-- Add screenshot here: feature-status.png showing enabled vs disabled -->

## Interacting with Features

### Left Click - Open Settings

Left-clicking on a feature item opens that feature's dedicated settings menu. This is where you can configure specific options for that feature.

### Right Click - Quick Toggle

Right-clicking on a feature item toggles it on or off immediately without opening the settings menu. This is useful for quickly enabling or disabling features.

### Shift + Left Click - Reset to Default

Shift + left-clicking on a feature resets all of its settings to the default values. A confirmation message appears in chat before the reset is applied.

:::warning Reset Confirmation
When you shift-click to reset a feature, all custom settings for that feature are lost. Make sure you want to reset before confirming.
:::

## Feature Settings Menus

Each feature has its own settings menu that opens when you left-click on it in the main menu.

### Common Elements

All feature settings menus share these common elements:

**Header (Slot 4):** Displays the feature name and current status

**Toggle Button (Slot 49):** Enables or disables the feature

**Back Button (Slot 45):** Returns to the main menu

**Settings Area (Slots 10-44):** Contains feature-specific settings

### Item Limiter Settings Menu

The Item Limiter settings menu displays all currently configured item limits and allows you to add new ones.

<!-- Add screenshot here: item-limiter-menu.gif showing adding a limit -->

**Current Limits Display:**
Each limited item is shown with its material icon and the current limit number in the item's lore.

**Adding a New Limit:**

1. Click the "Add New Limit" button (Lime Wool)
2. The menu closes and a prompt appears in chat
3. Type the item name (for example: `totem_of_undying`)
4. Type the limit number (for example: `2`)
5. The limit is added and you return to the menu

**Editing an Existing Limit:**

1. Click on the item you want to edit
2. Type the new limit number in chat
3. The limit is updated

**Removing a Limit:**

1. Shift + click on the item you want to remove
2. The limit is deleted

### Enchantment Limiter Settings Menu

This menu displays all enchantments and their current maximum levels.

<!-- Add screenshot here: enchant-limiter-menu.png -->

**Enchantment Display:**
Each enchantment is shown as an enchanted book with the enchantment name and current cap level in the lore.

**Modifying a Cap:**

1. Click on the enchantment you want to modify
2. Type the new maximum level in chat (0 to disable the enchantment entirely)
3. The cap is updated

### Dimension Lock Settings Menus

The Nether Lock and End Lock menus are simple toggle menus.

<!-- Add screenshot here: dimension-lock-menu.png -->

**Status Display:**
Shows whether the dimension is currently locked or unlocked.

**Toggle Button:**
Click to lock or unlock the dimension. Changes take effect immediately.

**Custom Message:**
Click the paper item to set a custom message shown to players who try to enter a locked dimension.

## Chat Input System

Several settings require you to type values in chat rather than clicking in the menu.

### How Chat Input Works

1. Click on a setting that requires text input
2. The menu closes automatically
3. A prompt message appears in chat explaining what to enter
4. Type your value and press Enter
5. The value is saved and you return to the menu

### Canceling Input

To cancel a chat input prompt without making changes:

- Type `cancel` and press Enter
- Or wait 30 seconds for the prompt to expire

:::tip Quick Return
If you accidentally close the menu or cancel input, you can always run `/smp` again to return to the main menu.
:::

## Settings Menu

Access global plugin settings by clicking the Settings button in the main menu.

<!-- Add screenshot here: settings-menu.png -->

| Setting | Description |
|---------|-------------|
| Debug Mode | Enables verbose logging for troubleshooting |
| Update Checks | Checks for new versions on startup |
| Metrics | Sends anonymous usage statistics |

## Reload Button

The Reload button in the main menu reloads all configuration files from disk. Use this after manually editing `config.yml`.

:::danger Manual Edits
If you make manual edits to `config.yml` while the server is running, those changes are not applied until you click Reload or run `/smp reload`. Changes made in the GUI overwrite manual edits if you don't reload first.
:::

## Troubleshooting

### Menu Not Opening

If the menu doesn't open when you run `/smp`:

1. Check that you have the `smpcore.admin` permission
2. Verify the plugin is loaded with `/plugins`
3. Check the console for error messages

### Items Missing from Menu

If some feature items are missing:

1. The feature may be disabled in the config
2. A required dependency (like WorldGuard) may be missing
3. Try running `/smp reload` to refresh the menu

### Changes Not Saving

If your changes don't persist after server restart:

1. Ensure the server shuts down properly (use `/stop`, not kill)
2. Check that the config file is not read-only
3. Look for error messages in the console during shutdown
