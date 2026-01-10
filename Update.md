# Update Log

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