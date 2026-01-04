# Call You By Your Name 🎯

> A NeoForge 1.21.1 chat enhancement mod that makes @-mentions loud, clear, and friendly. 💬

![Call You By Your Name hero screenshot](docs/callyoubyyourname.png)

## Feature Highlights ✨
- **Ping teammates instantly:** Mention a player with `@playerName` to deliver a highlighted notification and a click-to-reply suggestion that pre-fills chat with their name, plus an optional bell sound.
- **Share what you're holding:** Drop `@item` into your message to broadcast your main-hand item with its rarity color, hover tooltip, and (optionally) an inline icon rendered directly in chat.
- **Point friends to your location:** Use `@spot` to insert a green "Spot" marker; hovering shows the XYZ of where you were standing when you sent the message.
- **Ready-made group calls:** Rally everyone in your dimension with `@here`, nearby allies with `@near`, or your FTB Team with `@team`.
- **Privacy & Control:** A built-in GUI lets you block specific players, disable certain mention types, or mute notifications.
- **Client-side clarity:** Color-coded highlighting for players, groups, and special tokens pairs with smart autocompletion.

![Clickable reply suggestion in chat](docs/reply.webp)

## Requirements 📦
- **Minecraft:** Java Edition 1.21.1
- **Loader:** NeoForge `21.1` or newer
- **Dependencies:**
    - [LDLib2](https://www.curseforge.com/minecraft/mc-mods/ldlib) (Required)
    - [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge) (Optional, enables `@team`)

## Installation 🛠️
1. Install a compatible NeoForge profile for Minecraft 1.21.1.
2. Place the `CallYouByYourName` JAR and `LDLib2` JAR in your `mods/` folder.
3. Restart Minecraft (or your dedicated server) to load the mod.

## Using Mentions 📚
| Token | What it does | Notes |
| --- | --- | --- |
| `@playerName` | Pings a single player with a golden reminder and clickable reply prompt. | You cannot ping yourself; duplicates are ignored politely. |
| `@here` | Notifies every other player in your current dimension. | Always available to all players. |
| `@near` | Calls allies within a ~32 block radius. | Range checks run per dimension. |
| `@team` | Pings all members of your FTB Team. | Requires **FTB Teams** mod installed. |
| `@item` | Shares your held item with tooltip info and inline icon. | Warns you if your hand is empty. |
| `@spot` | Inserts a highlighted "Spot" link that shows your XYZ on hover. | Great for pointing friends to your current location. |

<p align="center">
  <img src="docs/@item.png" alt="@item tooltip preview" width="45%" />
  <img src="docs/@spot.png" alt="@spot waypoint preview" width="45%" />
</p>

## Preferences & Privacy 🛡️
Press **`M`** (configurable) in-game to open the **Mention Preferences** screen.
- **General Tab:** Toggle global mention permissions or disable specific types (e.g., mute `@here` or `@near`).
- **Blocked Players:** Search for an online player and block them from mentioning you.
- **Sync:** Your preferences are synchronized with the server, so your blocklist follows you.

## Configuration ⚙️
The config file is located at `config/callyou-common.toml`. Key options include:

- **Limits & Cooldowns:**
    - `maxMentionsPerMessage`: Max valid mentions per chat message (default `5`).
    - `maxTargetsPerMention`: Max players pinged by a single mention (default `16`).
    - `globalCooldownTicks`: Cooldown between messages with mentions (default `20` ticks / 1s).
    - `perTargetCooldownTicks`: Cooldown for pinging the same player again (default `40` ticks / 2s).

- **Visuals:**
    - `renderItemIconAndPlaceholder`: Toggle rendering of inline item icons for `@item` (default `true`).

## Troubleshooting 🧰
- **"@team doesn't work"** – Ensure `FTB Teams` is installed on the server.
- **"I can't see the menu"** – Check your keybinds settings to ensure `Mention Preferences` is bound to `M`.
- **"Item sharing failed"** – Make sure you are holding an item in your **main hand**.

## License 📜
Released under the **Apache License 2.0**. Contributions are welcome!

Have fun calling your friends by their names! 🎉
