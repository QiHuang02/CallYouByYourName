# Call You By Your Name

> A NeoForge 1.21.1 chat enhancement mod that makes @-mentions loud, clear, and friendly. ??

![Call You By Your Name hero screenshot](docs/callyoubyyourname.png)

## Feature Highlights
- **Ping teammates instantly:** Mention a player with `@playerName` to deliver a highlighted notification and a click-to-reply suggestion that pre-fills chat with their name, plus an optional bell sound.
- **Share what you're holding:** Drop `@item` into your message to broadcast your main-hand item with its rarity color, hover tooltip, and (optionally) an inline icon rendered directly in chat.
- **Point friends to your location:** Use `@spot` to insert a green "Spot" marker; hovering shows the XYZ of where you were standing when you sent the message. With **FTB Chunks** installed, clicking it drops a temporary waypoint on your map.
- **Ready-made group calls:** Rally everyone in your dimension with `@here`, nearby allies with `@near`, or your FTB Team with `@team`.
- **Backend-ready preferences:** Synced preference, blocklist, and history data remain in place for future integrations, even though this build ships without an in-game management screen.
- **Persistent unread tracking:** Mentions are stored server-side by default so unread counts survive relogs.
- **Client-side clarity:** Color-coded highlighting for players, groups, and special tokens pairs with smart autocompletion.

![Clickable reply suggestion in chat](docs/reply.webp)

## Requirements
- **Minecraft:** Java Edition 1.21.1
- **Loader:** NeoForge `21.1` or newer
- **Dependencies:**
    - [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge) (Optional, enables `@team`)
    - [FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) (Optional, makes `@spot` clickable to add a temporary waypoint)

## Installation
1. Install a compatible NeoForge profile for Minecraft 1.21.1.
2. Place the `CallYouByYourName` JAR in your `mods/` folder.
3. Restart Minecraft (or your dedicated server) to load the mod.

## Using Mentions
| Token | What it does | Notes |
| --- | --- | --- |
| `@playerName` | Pings a single player with a golden reminder and clickable reply prompt. | You cannot ping yourself; duplicates are ignored politely. |
| `@here` | Notifies every other player in your current dimension. | Always available to all players. |
| `@near` | Calls allies within a ~32 block radius. | Range checks run per dimension. |
| `@team` | Pings all members of your FTB Team. | Requires **FTB Teams** mod installed. |
| `@item` | Shares your held item with tooltip info and inline icon. | Warns you if your hand is empty. |
| `@spot` | Inserts a highlighted "Spot" link that shows your XYZ on hover. | With **FTB Chunks** installed, clicking drops a temporary waypoint named after the sender. |

<p align="center">
  <img src="docs/@item.png" alt="@item tooltip preview" width="45%" />
  <img src="docs/@spot.png" alt="@spot waypoint preview" width="45%" />
</p>

## Preferences & History Backend
This build does not include an in-game preferences screen or keybind.
- **Sync:** Player preference, blocklist, and history payloads still synchronize with the server.
- **Unread notice:** If server-side history is enabled, you still receive an unread mention count on login.
- **Future integrations:** The retained backend data paths make it possible to add a different client entry point later without changing save formats.

## Configuration
The config file is located at `config/callyou-common.toml`. Key options include:

- **Limits & Cooldowns:**
    - `maxMentionsPerMessage`: Max valid mentions per chat message (default `5`).
    - `maxTargetsPerMention`: Max players pinged by a single mention (default `16`).
    - `globalCooldownTicks`: Cooldown between messages with mentions (default `20` ticks / 1s).
    - `perTargetCooldownTicks`: Cooldown for pinging the same player again (default `40` ticks / 2s).

- **Visuals:**
    - `renderItemIconAndPlaceholder`: Toggle rendering of inline item icons for `@item` (default `true`).
- **History & Storage:**
    - `enableServerSideHistory`: Store mention history on the server for later review (default `true`).
    - `historyRetentionDays`: Days to keep history before pruning (default `7`, `0` = never expire).
    - `maxHistoryPerPlayer`: Maximum records kept per player (default `50`, `0` = no limit).

## Troubleshooting
- **"@team doesn't work"**: Ensure `FTB Teams` is installed on the server.
- **"Item sharing failed"**: Make sure you are holding an item in your **main hand**.
- **"Why can't I open a settings menu?"**: This build intentionally ships without the old in-game preferences UI.
- **"Clicking @spot doesn't add a waypoint"**: Install **FTB Chunks** (client + server as needed) to enable temporary waypoints.

## License
Released under the **Apache License 2.0**. Contributions are welcome!

Have fun calling your friends by their names! ??
