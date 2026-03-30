---
sidebar_position: 13
---

# Faster Happy Ghasts

Increases flying speed for newly spawned happy ghasts.

## Configuration

```yaml
features:
  faster-happy-ghasts:
    enabled: false
    flying-speed: 0.10
```

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| enabled | boolean | false | Master toggle for Faster Happy Ghasts |
| flying-speed | decimal | 0.10 | Applied flying speed for newly spawned happy ghasts |

## Notes

- The speed is applied shortly after spawn.
- If a server or Minecraft version does not expose a happy ghast entity, the feature does nothing safely.
- Typical vanilla-like value is around 0.05. Higher values make ghasts more mobile.

## In-Game Usage

- Open `/vanilla` menu
- Find `Faster Happy Ghasts`
- Left-click to toggle
- Right-click to view status and configured speed
