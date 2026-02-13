---
sidebar_position: 12
---

# Server Restart

Schedule and manage server restarts with countdown warnings, multiple notification types, and automatic scheduling.

## Overview

The Server Restart feature provides a comprehensive restart management system with:

- **Instant Restart**: Immediate server restart with safety confirmation
- **Countdown System**: Configurable countdown with player notifications
- **Scheduled Restarts**: Automatic restarts at configured times
- **Multiple Notifications**: Chat, ActionBar, BossBar, and Title messages
- **Pre-Restart Commands**: Execute commands before shutdown

## Configuration

```yaml
features:
  server-restart:
    enabled: false
    countdown-time: 60
    countdown-announcements:
      - 300
      - 120
      - 60
      - 30
      - 10
      - 5
      - 4
      - 3
      - 2
      - 1
    notification-types:
      - chat
      - bossbar
    bossbar-color: RED
    timezone: ""
    scheduled-restarts-enabled: false
    scheduled-times:
      - "04:00:00"
    execute-pre-commands: true
    pre-restart-commands:
      - "save-all"
    sound:
      enabled: true
      sound: "BLOCK_NOTE_BLOCK_PLING"
      volume: 1.0
      pitch: 1.0
    messages:
      countdown-message: "§c⚠ Server restarting in §e{time} §cseconds!"
      actionbar-message: "§cRestarting in {time}s"
      bossbar-message: "§cServer Restart: {time}s"
      title-message: "§c⚠ Server Restart"
      subtitle-message: "§e{time} seconds remaining"
      restart-now-message: "§c⚠ Server restart initiated by {player}!"
      restart-done-message: "§c⚠ Server is restarting now!"
      cancelled-message: "§a✔ Server restart has been cancelled."
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `false` | Enable/disable the feature |
| `countdown-time` | integer | `60` | Default countdown duration in seconds |
| `countdown-announcements` | list | `[300, 120, ...]` | Seconds at which to show countdown warnings |
| `notification-types` | list | `[chat, bossbar]` | Types of notifications to show |
| `bossbar-color` | string | `RED` | BossBar color (RED, BLUE, GREEN, YELLOW, PURPLE, PINK, WHITE) |
| `timezone` | string | `""` | Timezone for scheduled restarts (empty = server default) |
| `scheduled-restarts-enabled` | boolean | `false` | Enable automatic scheduled restarts |
| `scheduled-times` | list | `["04:00:00"]` | List of scheduled restart times |
| `execute-pre-commands` | boolean | `true` | Run pre-restart commands before shutdown |
| `pre-restart-commands` | list | `["save-all"]` | Commands to run before restart |

## Notification Types

You can enable multiple notification types simultaneously:

| Type | Description |
|------|-------------|
| `chat` | Broadcast message in chat (only at announcement times) |
| `actionbar` | Persistent message above hotbar (every second) |
| `bossbar` | Progress bar at top of screen (updates every second) |
| `title` | Large centered text (only at announcement times) |

## Schedule Formats

The server restart supports three schedule formats:

### Daily Restart

```yaml
scheduled-times:
  - "04:00:00"  # Every day at 4:00 AM
  - "16:00:00"  # Every day at 4:00 PM
```

### Weekly Restart

```yaml
scheduled-times:
  - "SUN 03:00:00"  # Every Sunday at 3:00 AM
  - "WED 04:00:00"  # Every Wednesday at 4:00 AM
```

Supported days: `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`

### One-Time Restart

```yaml
scheduled-times:
  - "2024-12-25 06:00:00"  # Christmas Day at 6:00 AM
```

:::tip Mixed Schedules
You can mix different formats in the same list:

```yaml
scheduled-times:
  - "04:00:00"              # Daily at 4 AM
  - "SUN 02:00:00"          # Extra restart on Sundays
  - "2024-12-31 23:59:00"   # New Year's Eve
```

:::

## GUI Configuration

Access the Server Restart GUI by:

1. Opening the main menu (`/vanilla menu`)
2. Finding the **Server Restart** feature (clock icon)
3. **Right-click** to open settings

### Main Settings GUI

| Slot | Item | Function |
|------|------|----------|
| 10 | Redstone Block | **Restart Now** - Shift+Click to confirm immediate restart |
| 12 | Clock | **Start Countdown** - Left-click for default time, right-click for custom |
| 14 | Barrier | **Cancel Restart** - Stop active countdown |
| 16 | Command Block | **Scheduled Restarts** - Open schedule management |
| 28 | Bell | **Notification Types** - Cycle through notification options |
| 30 | Compass | **Countdown Time** - Adjust default countdown duration |
| 32 | Command Block | **Pre-Restart Commands** - Toggle command execution |
| 34 | Dye | **BossBar Color** - Cycle through colors |
| 40 | Arrow | **Back** - Return to main menu |

### Schedule Management GUI

| Slot | Item | Function |
|------|------|----------|
| 4 | Lime/Gray Dye | **Toggle Scheduled Restarts** - Enable/disable scheduling |
| 19-25 | Paper | **Schedule Entries** - Shift+Click to remove |
| 36 | Arrow | **Back** - Return to restart settings |
| 40 | Emerald | **Add Schedule** - Add new schedule via chat |

## Commands

The Server Restart feature integrates with the main menu and doesn't have standalone commands. Use the GUI for all configuration.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `vanillacore.menu` | Access main menu (required for restart GUI) | `op` |

## How It Works

### Countdown Process

1. When a countdown starts (manually or scheduled):
   - A background task runs every second
   - At announcement times, notifications are sent based on enabled types
   - BossBar progress decreases as time passes
   - Warning sounds play at announcement times (if enabled)

2. When countdown reaches 0:
   - Pre-restart commands execute (if enabled)
   - Final "restarting now" message broadcasts
   - Server shuts down

### Scheduled Restart Detection

The plugin checks every second if a scheduled restart time has been reached:

- The check accounts for `countdown-time` to start the countdown before the scheduled time
- For example: if scheduled for `04:00:00` with 60s countdown, countdown starts at `03:59:00`

## Best Practices

### Recommended Setup

```yaml
features:
  server-restart:
    enabled: true
    countdown-time: 300  # 5 minute warning
    countdown-announcements:
      - 300
      - 180
      - 120
      - 60
      - 30
      - 10
      - 5
      - 4
      - 3
      - 2
      - 1
    notification-types:
      - chat
      - bossbar
      - title
    bossbar-color: RED
    scheduled-restarts-enabled: true
    scheduled-times:
      - "06:00:00"  # Morning restart
      - "18:00:00"  # Evening restart
    execute-pre-commands: true
    pre-restart-commands:
      - "save-all"
      - "broadcast §eSaving world data..."
```

### Tips

- **Long Countdown for Scheduled**: Use 5+ minute countdowns for scheduled restarts so players have time to prepare
- **Multiple Notifications**: Use both BossBar (constant visibility) and Title (attention-grabbing at key moments)
- **Pre-Commands**: Always include `save-all` to ensure world data is saved
- **Timezone**: Set timezone explicitly if your server has players from different regions

## Example Use Cases

### PvP Server (Frequent Restarts)

```yaml
scheduled-times:
  - "00:00:00"
  - "06:00:00"
  - "12:00:00"
  - "18:00:00"
countdown-time: 120  # 2 minute warning
```

### Survival Server (Minimal Disruption)

```yaml
scheduled-times:
  - "04:00:00"  # Once daily at low-traffic time
countdown-time: 600  # 10 minute warning
```

### Event Server (Manual Control Only)

```yaml
scheduled-restarts-enabled: false
# Use GUI to manually trigger restarts as needed
```
