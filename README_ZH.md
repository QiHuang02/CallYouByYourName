# Call You By Your Name 🎯

> 一款适用于 NeoForge 1.21.1 的聊天增强模组，让 @ 提醒更加醒目、友好且可控。💬

![Call You By Your Name 模组截图](docs/callyoubyyourname.png)

## 功能亮点 ✨
- **即时 @ 提醒：** 输入 `@玩家名` 发送带有高亮与点击回复按钮的提示，支持声音提醒。
- **分享手中物品：** 使用 `@item` 广播主手物品，保留稀有度颜色、悬浮提示，并支持在聊天中直接显示物品图标。
- **标注所在位置：** 使用 `@spot` 插入绿色的 “Spot” 标记，悬停即可查看发送时的坐标。
- **群体召集：** 用 `@here` 通知同维度玩家，`@near` 召集附近队友，或用 `@team` 呼叫 FTB 队伍成员。
- **隐私与控制：** 内置 GUI 界面，允许你拉黑特定玩家、禁用特定类型的 @ 通知或静音。
- **客户端优化：** 智能自动补全、颜色高亮，且不干扰指令输入。

![点击回复演示](docs/reply.webp)

## 必要条件 📦
- **Minecraft：** Java 版 1.21.1
- **加载器：** NeoForge `21.1` 或更高版本
- **前置/依赖：**
    - [LDLib2](https://www.curseforge.com/minecraft/mc-mods/ldlib) (必须安装)
    - [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge) (可选，启用 `@team` 功能)

## 安装步骤 🛠️
1. 安装适配 Minecraft 1.21.1 的 NeoForge。
2. 将 `CallYouByYourName` 和 `LDLib2` 的 JAR 文件放入 `mods/` 文件夹。
3. 重启游戏或服务器。

## @ 语法速查 📚
| 语法 | 功能 | 备注 |
| --- | --- | --- |
| `@玩家名` | 向单个玩家发送金色提醒与回复按钮。 | 无法 @ 自己，重复目标会被忽略。 |
| `@here` | 通知当前维度的所有玩家。 | 全员可用。 |
| `@near` | 召集半径约 32 格内的玩家。 | 仅限同一维度。 |
| `@team` | 呼叫你的 FTB 队伍成员。 | 需要安装 **FTB Teams** 模组。 |
| `@item` | 展示主手物品信息与图标。 | 手持空物时会提示错误。 |
| `@spot` | 插入高亮坐标标记。 | 悬停查看 XYZ。 |

<p align="center">
  <img src="docs/@item.png" alt="@item 悬浮提示预览" width="45%" />
  <img src="docs/@spot.png" alt="@spot 坐标标记预览" width="45%" />
</p>

## 偏好设置与隐私 🛡️
在游戏中按 **`M`** 键（可在按键设置中修改）打开 **@ 偏好设置** 界面：
- **通用设置：** 一键开启/关闭所有通知，或单独禁用某种类型（如不想接收 `@here` 骚扰）。
- **屏蔽列表：** 搜索在线玩家并将其加入黑名单，拒收来自他们的 @ 提醒。
- **同步：** 你的设置会同步到服务器，黑名单随身携带。

## 配置项 ⚙️
配置文件位于 `config/callyou-common.toml`，主要选项包括：

- **限制与冷却：**
    - `maxMentionsPerMessage`：单条消息最大有效 @ 数量（默认 `5`）。
    - `maxTargetsPerMention`：单次 @ 最大涉及人数（默认 `16`）。
    - `globalCooldownTicks`：发送 @ 消息的全局冷却（默认 `20` tick / 1秒）。
    - `perTargetCooldownTicks`：对同一目标的连续 @ 冷却（默认 `40` tick / 2秒）。

- **视觉效果：**
    - `renderItemIconAndPlaceholder`：是否在 `@item` 后渲染物品图标（默认 `true`）。

## 常见问题 🧰
- **“`@team` 没反应？”** 请确认服务器已安装 `FTB Teams` 模组。
- **“打不开设置菜单？”** 请检查按键绑定，确认 `Mention Preferences` 是否冲突。
- **“`@item` 发送失败？”** 请确认主手确实持有物品。

## 授权协议 📜
本模组以 **Apache License 2.0** 发布。

祝你在聊天中随叫随到，畅玩愉快！🎉
