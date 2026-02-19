# Limited Enchantment Slots

Limit how many of a specific item type can be enchanted server-wide on a first-come-first-serve basis.

## Overview

The Limited Enchantment Slots feature allows server administrators to restrict the number of specific items (by material type) that can have enchantments. For example, you can configure that only 3 maces in the entire server can be enchanted, or only 5 netherite swords.

This creates a competitive PvP environment where powerful enchanted items are scarce and valuable.

## Key Features

- **Material-based limits**: Set limits per item material (e.g., MACE, NETHERITE_SWORD)
- **First-come-first-serve**: First players to enchant get the slots
- **Dynamic slot release**: Slots reopen when items are disenchanted or destroyed
- **Automatic scanning**: Existing enchanted items are registered on startup
- **Full GUI management**: Configure limits and view registrations through GUI
- **Persistent tracking**: Item registrations persist across server restarts

## How It Works

### Enchanting

When a player tries to enchant an item at an enchanting table:

1. Plugin checks if the item's material has a limit configured
2. If limit reached, the enchantment is blocked with a message
3. If slots available, the item is registered and enchantment proceeds
4. Each registered item gets a unique UUID stored in its NBT data

### Slot Release

Slots are automatically released when:

- **Disenchanting**: Using a grindstone to remove all enchantments
- **Item destruction**: Item is destroyed (lava, void, despawn, etc.)
- **Manual clear**: Admin clears registrations via GUI

### On Join Scanning

When the feature is enabled, all online players' inventories are scanned to register existing enchanted items. This ensures that items enchanted before the feature was enabled count toward the limit.

## Configuration

### YAML Config

```yaml
features:
  limited-enchantment-slots:
    enabled: false
    limits:
      # Material name (uppercase): maximum count
      MACE: 3
      NETHERITE_SWORD: 5
      NETHERITE_AXE: 5
      DIAMOND_SWORD: 10
      TRIDENT: 4
```

### GUI Configuration

Access the GUI:

```
/vanilla menu → Limited Enchantment Slots → Right Click
```

#### Main GUI Options

1. **Configure Material Limits** - Set limits for specific materials
2. **View Registered Items** - See who owns registered enchanted items
3. **Scan All Online Players** - Manually trigger inventory scan

#### Configure Limits GUI

- **Left Click**: Increase limit by 1
- **Right Click**: Decrease limit by 1
- **Shift + Left**: Increase limit by 5
- **Shift + Right**: Decrease limit by 5
- **Middle Click**: Remove limit entirely
- **Drop Key (Q)**: View registered items for that material

#### View Registered Items

Shows all registered items for a specific material with:

- Player name who owns the item
- Registration timestamp
- Last seen timestamp
- Item UUID

**Clear All Registrations**: Removes all registrations for that material (frees up all slots)

## Data Persistence

Registrations are saved to `plugins/vanilla-core/limited-enchants-registry.json`:

```json
{
  "registrations": [
    {
      "itemUUID": "abc-123-def-456",
      "material": "MACE",
      "playerName": "PlayerName",
      "playerUUID": "player-uuid-here",
      "timestamp": 1708185600000,
      "lastSeen": 1708185600000
    }
  ]
}
```

## Use Cases

### PvP Server Balancing

Limit powerful weapons to create scarcity:

```yaml
limits:
  MACE: 3          # Only 3 maces can be enchanted
  NETHERITE_SWORD: 10
  NETHERITE_AXE: 8
  TRIDENT: 5
```

### Event Servers

Create special "legendary" items:

```yaml
limits:
  DIAMOND_SWORD: 1   # Only one "legendary" sword
  DIAMOND_AXE: 1
```

### Economy Servers

Make enchanted items more valuable by limiting supply.

## Player Messages

### Enchantment Blocked

```
§c[Vanilla Core] §7Cannot enchant MACE! Limit reached: §e3/3
```

### Registration Success

```
§a[Vanilla Core] §7Registered enchanted MACE (§e2/3§7)
```

### Slot Released

```
§e[Vanilla Core] §7Enchantment slot released for MACE
```

## Admin Commands

Currently, all management is done through the GUI. Future updates may add commands like:

- `/vanilla limitedenchants list <material>` - List registered items
- `/vanilla limitedenchants clear <material>` - Clear registrations
- `/vanilla limitedenchants scan` - Manually scan players

## Permissions

- `vanillacore.feature.limited-enchantment-slots` - Access to GUI configuration *(future)*
- Uses general `vanillacore.admin` for now

## Technical Details

### Item Tracking

Items are tracked using Minecraft's Persistent Data Container (PDC):

- Key: `vanillacore:limited_enchant_uuid`
- Type: String (UUID)
- Scope: ItemStack NBT data

This ensures:

- Items keep their registration across trades, storage, etc.
- Items can be uniquely identified even if renamed
- Works with all vanilla item types

### Event Handling

The feature listens to:

- `EnchantItemEvent` - Enchanting table enchantments
- `InventoryClickEvent` - Grindstone usage (disenchanting)
- `PlayerJoinEvent` - Scan player inventory on join
- `PlayerDeathEvent` - Track item drops for potential unregistration

### Performance Considerations

- Concurrent data structures for thread safety
- Lazy loading of registrations (only when feature enabled)
- Efficient UUID-based lookups
- Minimal event processing overhead

## Compatibility

- **Paper/Spigot**: 1.21.1+
- **Dependencies**: None required
- **Conflicts**: May conflict with other enchantment limiters

## FAQ

**Q: What happens if I lower the limit below current registrations?**  
A: Existing registrations remain valid. The limit only prevents NEW enchantments.

**Q: Can players trade registered items?**  
A: Yes! The UUID stays with the item through trades and storage.

**Q: What if a player stores a registered item in a chest?**  
A: The registration persists. The "lastSeen" timestamp tracks when it was last detected.

**Q: Can I limit enchantment books?**  
A: Not directly. This feature only prevents enchanting items at enchanting tables. Books from villagers, loot, etc. are not affected.

**Q: What happens if the registry file is deleted?**  
A: All registrations are lost. On next restart, items will be re-scanned and registered again (could exceed previous limits).

## Related Features

- [Enchantment Limiter](/docs/features/enchantment-limiter) - Limit enchantment levels
- [Item Limiter](/docs/features/item-limiter) - Limit item quantities in inventory
- [Mace Limiter](/docs/features/mace-limiter) - Limit total maces craftable

## Troubleshooting

### Slots not releasing after disenchanting

Make sure the item lost **all** enchantments. Partially enchanted items remain registered.

### Items re-registering after manual clear

This is expected if items still exist in the world. Clear registrations, then manually remove the items from players.

### Scan not finding existing items

The scan only checks online players' inventories. Items in chests, enderchests, or offline players are not scanned automatically.

## Examples

### Hardcore SMP Setup

```yaml
features:
  limited-enchantment-slots:
    enabled: true
    limits:
      MACE: 2                 # Only 2 maces
      NETHERITE_SWORD: 8
      NETHERITE_AXE: 8
      NETHERITE_PICKAXE: 15
      ELYTRA: 10              # Limited elytras!
      TRIDENT: 5
```

### Weapons-Only Restriction

```yaml
limits:
  DIAMOND_SWORD: 20
  DIAMOND_AXE: 20
  NETHERITE_SWORD: 10
  NETHERITE_AXE: 10
  BOW: 15
  CROSSBOW: 15
  TRIDENT: 8
```

---

For more information, see the [Configuration Guide](/docs/configuration) or ask on our [Discord](https://discord.gg/7fQPG4Grwt).
