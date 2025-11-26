# ThePitPremium

一个功能丰富的Minecraft空岛生存插件，提供了独特的空岛游戏体验。

## 依赖要求

### 必装依赖

以下插件必须安装，否则ThePitPremium无法正常运行：

- **ProtocolLib** (v5.3.0或更高版本) - 用于协议处理和数据包操作
- **Spigot/Paper** (1.8.8或兼容版本) - 插件运行的基础服务端

### 推荐安装

以下插件可以增强ThePitPremium的功能体验：

- **LuckPerms** - 权限管理
- **PlaceholderAPI** - 变量扩展支持
- **PlayerPoints** - 玩家积分系统
- **DecentHolograms** - 全息图支持
- **ViaVersion** - 版本兼容性支持
- **Redis** - 高性能数据缓存 (如果使用缓存功能)
- **WordEdit**
- **Citizens**

## 安装说明

1. 确保服务器运行兼容的Spigot/Paper版本
2. 安装所有必装依赖插件
3. 将ThePitPremium.jar放入服务器的plugins文件夹
4. 重启服务器以加载插件
5. 根据需要配置插件设置

## Java 21 运行说明

如果使用Java 21运行此插件，请在启动服务器时添加以下JVM参数以确保插件正常运行：

```
--add-opens=java.base/java.net=ALL-UNNAMED
```

例如：
```
java --add-opens=java.base/java.net=ALL-UNNAMED -jar paper.jar
```

这是由于Java 17+引入的模块系统限制，需要显式授权才能访问某些内部API。

## 注意事项

- 插件需要Java 17或更高版本运行
- 使用MongoDB时需要确保数据库连接正常
- 建议定期备份插件数据文件
- 某些功能可能需要额外的依赖插件才能完全发挥效果

## 特色功能

- 独特的空岛生存玩法
- 丰富的任务和活动系统
- 完整的经济和商店系统
- 多样的装备和附魔系统
- 玩家社交和团队功能
- 个性化徽章和宠物系统