---
sidebar_position: 30
---

# Social Engine

SMP Watchdog includes a social engine for chat and player messaging workflows.

## Included Features

- Persistent player preferences (chat, PM, mentions, social spy)
- Group-based chat formatting
- URL formatting in chat and PMs
- Mentions with notification sound
- Private messaging and reply
- Social spy
- Chat/PM/mention toggles
- Player blocking and blocked list
- Configurable command enable/disable and cooldowns
- Announcement engine with interval broadcasting

## Commands

- `/smp msg <player> <message>`
- `/smp reply <message>`
- `/smp socialspy`
- `/smp togglechat`
- `/smp togglepm`
- `/smp togglementions`
- `/smp block <player>`
- `/smp unblock <player>`
- `/smp blocked`
- `/smp announcements reload`

## Configuration Sections

- `database.*`
- `social.announcements.*`
- `social.chat.*`
- `social.pm.*`
- `social.commands.*`
- `social.lifecycle.*`
- `social.spawn.*`

## Notes

- Social preferences and block lists are stored in SQLite (`database.path`).
- Social spy visibility also requires `smp.socialspy` permission.
- URL formatting and mention styling are MiniMessage-based.
