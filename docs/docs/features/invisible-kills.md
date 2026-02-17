---
sidebar_position: 9
---

# Invisible Kills

Hide killer identity in death messages when the attacker is invisible.

## Overview

When a player with invisibility kills another player, the normal death message reveals their identity (e.g., "Steve was slain by Alex"). Invisible Kills modifies these death messages to hide the killer's name, maintaining the mystery of stealth attacks.

## Why Use Invisible Kills

Use cases for this feature:

- **Assassin Gameplay**: Invisible attackers remain anonymous
- **Paranoia Element**: Players cannot know who killed them
- **Event Support**: Works well for manhunt or assassin game modes
- **Stealth Rewards**: Makes invisibility potions more useful

## Configuration

```yaml
features:
  invisible-kills:
    enabled: false
    death-message: "{victim} was killed by §k?????????"
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | false | Master toggle for the feature |
| `death-message` | string | (see above) | Custom death message with placeholder |

### Message Placeholder

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `{victim}` | Name of the player who died | `Steve` |

## How It Works

1. Player A (with invisibility effect) kills Player B
2. Plugin intercepts the death event before the message broadcasts
3. Checks if Player A has the invisibility potion effect
4. If yes, replaces the death message with the custom message
5. The `§k` formatting code creates scrambled text for the killer's name

### Example Messages

**Normal death message**:

```
Steve was slain by Alex
```

**With Invisible Kills enabled**:

```
Steve was killed by ????????
```

The `§k` magic text effect makes the question marks appear as rapidly changing random characters, adding to the mystery.

## Using the GUI

1. Run `/vanilla` to open the main menu
2. Click on Invisible Kills
3. **Left Click**: Toggle the feature on/off
4. **Right Click**: View current settings and death message format

## Message Customization

You can customize the death message in config.yml:

### Mysterious Message

```yaml
death-message: "{victim} died under mysterious circumstances..."
```

### Assassin Themed

```yaml
death-message: "§c[ASSASSIN] §7{victim} has been eliminated."
```

### Spooky Themed

```yaml
death-message: "§8§oA shadow claimed {victim}..."
```

### Magic Text (Default)

```yaml
death-message: "{victim} was killed by §k?????????"
```

The `§k` code creates the scrambled/obfuscated text effect.

## What Triggers the Feature

The invisibility check requires:

- Active `INVISIBILITY` potion effect on the killer
- Any duration (even 1 tick counts)
- Any amplifier level

### Death Causes Affected

This feature affects kills by:

- Direct melee attacks
- Projectiles (arrows, tridents)

Does NOT affect:

- Environmental deaths (fall, lava, drowning)
- Mob kills
- Deaths when killer has no invisibility

## Game Mode Ideas

### Assassin Game

1. Assign each player a target
2. Give invisibility potions as rewards
3. Use Invisible Kills to hide successful assassinations
4. Last player standing wins

### Manhunt Enhancement

1. Hunter has invisibility
2. Speedrunner cannot know when hunter is near
3. Death message hidden so teammates don't know who was killed

### Mystery Murder

1. One player is secretly the murderer
2. Murderer receives invisibility
3. Players must deduce who is killing them

## Troubleshooting

### Kill messages still showing

1. Verify `enabled: true` in config
2. Ensure killer actually has invisibility effect
3. Run `/vanilla reload` after config changes

### Placeholder not working

Ensure you're using `{victim}` with lowercase spelling exactly.
