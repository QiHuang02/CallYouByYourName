# Update Log

[dev] Version 2101.2.0-build.25
refactor(network): 重构网络通信系统使用RPC框架

- 将原有的Payload系统替换为RPCPacket系统
- 创建新的CYRPCPacket类处理所有网络通信
- 更新CallYouNetwork类使用新的RPC框架初始化
- 移除所有旧的Payload相关类和处理方法
- 更新客户端和服务端通信方法调用

[dev] Version 2101.2.0-build.24
docs(readme): 更新项目文档并添加许可证文件

- 添加完整的 GNU GPL v3 许可证文件
- 移除 README.md 中的 emoji 图标保持简洁风格
- 扩展依赖项列表，增加 FTB Chunks 可选依赖支持
- 完善 @spot 功能说明，描述点击添加临时路点的能力
- 添加历史记录配置选项说明，包括服务器端存储设置