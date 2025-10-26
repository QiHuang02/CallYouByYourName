# Call You By Your Name 🎯

> A Forge 1.20.1 chat enhancement mod that makes @-mentions loud, clear, and friendly. 💬

![Hero image](https://example.com/path/to/cover-image.png)

## Feature Highlights ✨
- **Clickable @-pings:** Mention a teammate with `@playerName` to deliver a highlighted notification that includes a one-click reply suggestion and optional bell sound.
- **Ready-made group calls:** Rally everyone in your dimension with `@here` or nearby allies with `@near`; more groups can be added by other mods via the built-in registry hook.
- **Show-and-tell items:** Drop `@item` into your message to share the item in your main hand as a hoverable tooltip, or get a friendly warning if your hand is empty.
- **Client-side clarity:** Color-coded highlighting and smart autocompletion work together to keep mentions readable and fast to type, without interfering with slash commands.
- **Spam control & alerts:** A configurable cooldown prevents mention spam, while optional sounds can call players back to the action even when chat is closed.

## Requirements 📦
- **Minecraft:** Java Edition 1.20.1
- **Loader:** Forge `47.4.10` or newer on both client and server

## Installation 🛠️
1. Install a compatible Forge profile for Minecraft 1.20.1.
2. Place the CallYouByYourName JAR in your `mods/` folder.
3. Restart Minecraft (or your dedicated server) to load the mod.

## Using Mentions 📚
| Token | What it does | Notes |
| --- | --- | --- |
| `@playerName` | Pings a single player with a golden reminder and clickable reply prompt. | You cannot ping yourself; duplicates are ignored politely. |
| `@here` | Notifies every other player in your current dimension. | Always available to all players. |
| `@near` | Calls allies within a ~32 block radius. | Range checks run per dimension to avoid cross-world noise. |
| `@item` | Shares your held item as a hoverable tooltip. | Fails gracefully if your hand is empty. |

> 💡 Tip: The chat will tell you how many seconds remain if you hit the mention cooldown.

![Chat demo placeholder](https://example.com/path/to/chat-demo.png)

## Client Experience 🌈
- Mention colors adapt to players, groups, and items so you can scan conversations at a glance.
- Autocomplete surfaces both group tokens and online players, but hides automatically while you type commands that start with `/`.
- Mention tokens from servers (or other mods) are synchronized automatically when you join.

## Configuration ⚙️
The first launch creates `config/CallYouByYourName-common.toml`. Tweak the following options:
- `mentionCooldownMs` – Minimum delay between mentions sent by the same player (default `5000`). Set to `0` to disable the check.
- `enableMentionSound` – Whether to play the bell sound when a player is pinged (default `true`).

Reload the config or restart Minecraft to apply changes.

## Extending the Mod 🧩
Developers can add custom tokens by implementing `MentionGroupProvider` and registering via Java's `ServiceLoader`, instantly syncing those tokens to connected clients.

## Troubleshooting 🧰
- **"I can't use a group mention"** – Some third-party groups may require extra permissions; you'll receive a red warning if access is denied.
- **"Why don't I see suggestions?"** – Mention autocomplete only appears in standard chat and hides during command input.
- **"Item sharing failed"** – Make sure something is in your main hand before typing `@item`; the mod cancels the message otherwise.

## License 📜
Released under the **Apache License 2.0**. Contributions are welcome!

Have fun calling your friends by their names! 🎉