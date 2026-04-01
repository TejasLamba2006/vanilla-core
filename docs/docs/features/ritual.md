---
sidebar_position: 14
---

# Ritual

Ritual lets players start a timed server-wide event with the item in their hand.

## Command

```bash
/ritual [start [time] [color]|status|cancel]
```

- `start`: starts a ritual using your held item
- Time examples: `30s`, `1m`, `1h`, `1d`, `1h30m`
- Color is optional and supports tab completion
- `status`: shows the active ritual status
- `cancel`: cancels the active ritual (requires ritual cancel permission)

## Configuration

```yaml
features:
  ritual:
    enabled: false
    duration-minutes: 1
    radius: 5
    particle-color: BLUE
    strike-lightning: true
    end-effect-beacon-beam: false
    beacon-beam-duration-seconds: 8
    end-effect-firework-burst: false
    drop-item-at-end: false
    play-start-sound: true
    start-sound: "ENTITY_WITHER_SPAWN"
```

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| enabled | boolean | false | Master toggle for Ritual |
| duration-minutes | integer | 1 | Total ritual duration in minutes |
| radius | integer | 5 | Visual radius value for ritual ring |
| particle-color | string | BLUE | Ritual circle color |
| strike-lightning | boolean | true | Plays lightning effect at ritual end |
| end-effect-beacon-beam | boolean | false | Shows a beacon-style beam at ritual end |
| beacon-beam-duration-seconds | integer | 8 | Duration of the beacon beam end effect |
| end-effect-firework-burst | boolean | false | Launches fireworks when ritual completes |
| drop-item-at-end | boolean | false | Drops a copy of held ritual item on completion |
| play-start-sound | boolean | true | Plays start sound globally |
| start-sound | string | ENTITY_WITHER_SPAWN | Bukkit sound played on start |

## Particle Colors

Supported values:

- BLUE
- YELLOW
- AQUA
- FUCHSIA
- LIME
- ORANGE
- PURPLE
- WHITE
- BLACK
- GRAY
- RED
- GREEN

## In-Game Menu

Ritual appears in the main `/smp` menu like any other feature toggle.

- Left-click: enable or disable Ritual
- Right-click: open the Ritual settings GUI
- The GUI lets you adjust duration, radius, particle color, start sound, and end effects

## What Happens On Success

When the timer reaches zero:

- A completion message is broadcast to all online players
- If `strike-lightning` is enabled, a lightning visual effect plays at the ritual center
- If `end-effect-beacon-beam` is enabled, a timed vertical beam appears at the ritual center
- If `end-effect-firework-burst` is enabled, fireworks launch and detonate at the ritual center
- If `drop-item-at-end` is enabled, a copy of the ritual item drops at the ritual center
- The ritual boss bar and ritual display entities are cleaned up
