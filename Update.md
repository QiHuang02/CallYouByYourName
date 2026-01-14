# Update Log

[dev] Version 2101.2.0-build.37
refactor(core): 拆分提及执行流程为权限/组装/派发模块，并独立历史记录器

- 提及消息由 MentionMessageComposer 重建，并为可回复提及增加悬停提示
- 服务端历史记录保存格式化后的消息组件，统一由 MentionHistoryRecorder 写入
- MentionExecutor 仅负责流程编排，降低耦合

feat(client): 提及历史界面重做，支持回复与标记已读

- 新增 MentionHistoryRow：渲染完整消息组件与动态操作按钮，并支持快速回复
- 支持从记录中创建 FTB 临时传送点，替代坐标复制
- 支持物品提及图标内嵌显示

refactor(storage,network): 历史记录改用唯一ID并扩展操作

- MentionRecord 新增 historyId，移除 senderName/location 与遗留字段
- 日志操作改用 historyId，新增单条标记已读

chore(build): 更新构建配置与元数据生成

- 升级到 NeoForge moddev 插件并调整依赖作用域
- 引入 mods.toml 模板生成，Gradle wrapper 升级到 9.2.1

refactor(core): Refactor mention execution into permission, composition, and dispatch modules with a dedicated history recorder

- Messages are rebuilt by MentionMessageComposer with reply hover hints for replyable mentions
- Server-side history now stores formatted message components via MentionHistoryRecorder
- MentionExecutor now orchestrates only, reducing coupling

feat(client): Reworked mention history UI with reply and mark-read

- New MentionHistoryRow renders full message components, dynamic actions, and quick reply
- Create FTB transient waypoints from history instead of copying coords
- Inline item icon rendering for item mentions

refactor(storage,network): History records now use unique IDs and expanded actions

- MentionRecord adds historyId and drops senderName/location and legacy fields
- Log actions now use historyId with a new single-record mark-read action

chore(build): Update build config and metadata generation

- Move to NeoForge moddev plugin and adjust dependency scopes
- Add templated mods.toml generation and update Gradle wrapper to 9.2.1

[dev] Version 2101.2.0-build.36
refactor(storage): 迁移数据存储系统使用原版Codec序列化

- 更新 MentionSavedData 的 NBT 存储格式，从映射结构改为列表结构
- 保留对旧版存档格式的兼容性支持，实现数据迁移逻辑

chore(build): 更新依赖版本并添加忽略文件

- 更新 neo_version 从 21.1.216 到 21.1.218
- 更新 ftb_teams_version 从 2101.1.7 到 2101.1.9
- 更新 ftb_library_version 从 2101.1.28 到 2101.1.30
- 更新 ldlib2_version 从 2.1.5.b 到 2.1.7

[release] Version 2101.2.0-build.35 -- fix
refactor(network): 迁移网络通信至 NeoForge Payload 系统

- 移除基于 LowDragLib2 的 RPC 网络实现 (CYRPCPacket)
- 引入标准的 NeoForge Payload 数据包系统
- 重构网络处理逻辑，新增 NetworkHandler 统一处理数据包
- 实现了一系列 Payload 类用于处理配置同步、日志请求和 Toast 通知

fix(client): 优化提及配置界面的交互逻辑

- 修复提及类型主开关与通知开关的联动逻辑，确保禁用时正确锁定子选项
- 优化全局设置与特定类型设置的生效优先级判定
- 调整群发提及开关的显示状态，现在会正确响应全局开关的变化

chore(ci): 优化发布工作流

- 在构建前添加 runData 步骤，确保生成的数据包被正确打包

[release] Version 2101.2.0-build.34

refactor(compat): 使用反射调用 FTB API 避免硬依赖

- 使用反射调用方式添加临时路标点并返回路标名称
- 通过反射获取FTB Teams团队成员列表
- 统一异常处理为反射操作异常

[release] Version 2101.2.0-build.33
fix(mixin): 修复与 ChatHeads 模组的兼容问题

[dev] Version 2101.2.0-build.32
refactor(core): 优化提及记录的数据结构和存储方式

- 修改 MentionRecord 类以支持多个目标 ID 和对应的读取状态
- 更新 MentionSavedData 存储逻辑，统一管理所有提及记录而非按玩家分组
- 实现遗留数据迁移功能，确保旧版本记录能够正确转换
- 优化提及记录的读取标记和删除操作，提升性能表现
- 添加记录规范化处理，确保数据一致性和完整性

[dev] Version 2101.2.0-build.31
refactor(core): 重构组件包结构并优化数据序列化

- 将 notify 包重命名为 notifier 并更新所有相关引用
- 将 target 包重命名为 targetProvider 并更新所有相关引用
- 为 MentionRules 和 MentionType 类实现 IPersistedSerializable 接口
- 重构 MentionSavedData 的序列化逻辑以使用 PersistedParser
- 更新 CallYouNetwork 中的网络编解码器注册逻辑
- 重新排列 MentionPreferences 中 TypePreference 类的位置

chore(build): 更新模组许可证配置

- 将模组许可证从 All Rights Reserved 修改为 GPL-3.0 LICENSE

[dev] Version 2101.2.0-build.30
refactor(core): 重构核心包结构以更好地组织提及功能组件

- 将 components 包重命名为 mention.components 以明确其职责
- 移动网络相关类到 core.network 包下
- 更新客户端相关类到 core.client 包下
- 修改聊天相关的类到 core.client.chat 包下
- 调整屏幕界面相关的类到 core.client.screen 包下
- 修正注册表中的命名以匹配新的架构设计

[dev] Version 2101.2.0-build.29
refactor(api): 重构API组件为可分发组件架构

- 将 MentionRules 从 api 包移动到 api.components 子包
- 更新MentionType中的字段名称从 notification 改为 notifier
- 统一所有组件的编解码器实现方式为基于 IDispatchedComponent 的通用实现

[dev] Version 2101.2.0-build.28
refactor(core): 将 MentionRecord 和 MentionSavedData 从 storage 包移动到 saveddata 包

[dev] Version 2101.2.0-build.27
feat(lang): 添加位置标签翻译并更新格式化器

- 将硬编码的 Spot 文本替换为可翻译的组件
- 更新了位置文本格式化器以使用翻译文本

[dev] Version 2101.2.0-build.26
refactor(api): 将组件接口移至专用包并更新序列化实现

- 将 Notifier、TargetProvider 和 TextFormatter 接口移动到 cn.qihuang02.callyou.api.components 包
- 简化网络数据传输的数据编解码器实现

[dev] Version 2101.2.0-build.25
refactor(network): 重构网络通信系统使用RPC框架

- 将原有的 Payload系统替换为 RPCPacket 系统
- 创建新的 CYRPCPacket 类处理所有网络通信
- 更新 CallYouNetwork 类使用新的RPC框架初始化
- 移除所有旧的 Payload 相关类和处理方法
- 更新客户端和服务端通信方法调用

[dev] Version 2101.2.0-build.24
docs(readme): 更新项目文档并添加许可证文件

- 添加完整的 GNU GPL v3 许可证文件
- 移除 README.md 中的 emoji 图标保持简洁风格
- 扩展依赖项列表，增加 FTB Chunks 可选依赖支持
- 完善 @spot 功能说明，描述点击添加临时路点的能力
- 添加历史记录配置选项说明，包括服务器端存储设置
