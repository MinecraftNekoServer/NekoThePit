# NekoThePitPremium

这是一个Minecraft天坑乱斗（The Pit）模式的Bukkit插件，提供了丰富的游戏功能和系统。

# 注意事项！

> 注意：Java 9 及以上需要添加 JVM 参数，保证访问内部包，否则可能报错。

## 必须添加
```bash
--add-opens=java.base/java.net=ALL-UNNAMED
````

## 启动示例

```bash
# Windows / Linux 通用
java --add-opens=java.base/java.net=ALL-UNNAMED -Xms2G -Xmx2G -jar paper-1.12.2.jar
```

## 说明

* `--add-opens=java.base/java.net=ALL-UNNAMED`
  允许 JVM 模块访问 `java.net` 内部类，解决 Java 9+ 的访问限制问题
* `-Xms2G`：初始内存 2GB
* `-Xmx2G`：最大内存 2GB

## 注意

* Java 8 启动不需要 `--add-opens` 参数
* 推荐至少使用 Java 17 或以上，保证安全性和性能

## 命令列表

### 普通玩家命令

#### `/pit [subcommand]`
主命令，包含以下子命令：

- `/pit option` - 打开选项菜单（在5级或精通后解锁）
- `/pit view <player>` - 查看其他玩家档案（在70级或精通后解锁）
- `/pit events` - 预览活动（需要支持者权限或管理员权限）
- `/pit show` - 展示手持物品（需要支持者权限或管理员权限）
- `/pit tradeLimits` - 显示每日交易限制
- `/pit trade <player>` - 向指定玩家发起交易请求（60级解锁）
- `/pit spawn` - 传送回出生点
- `/pit killRecap` - 查看最近的死亡回放
- `/pit cdk <code>` - 使用CDK兑换码
- `/pit viewOffer <player>` - 查看来自指定玩家的交易报价
- `/pit offer <player> <price>` - 向指定玩家发起交易报价（60级解锁）
- `/pit AuctionGui` - 打开拍卖行界面（活动期间可用）
- `/pit cool` - 显示当前在线的精通玩家
- `/pit openKingsQuestUI` - 打开国王任务界面
- `/pit openBakeMaster` - 打开烘焙大师界面
- `/pit mail` - 打开邮件菜单（10级或精通后解锁）
- `/pit perk` - 打开天赋菜单（10级或精通后解锁）
- `/pit shop` - 打开商店菜单（10级或精通后解锁）
- `/pit spawn` - 传送回出生点（别名：/pit respawn, /pit home, /pit back）
- `/pit quest` - 打开任务菜单（30级或精通后解锁）
- `/pit prestige` - 打开精通菜单（120级或精通后解锁）
- `/pit demon` - 打开恶魔阵营菜单（无等级限制）
- `/pit leaderboard` - 打开排行榜助手菜单（无等级限制）
- `/pit stats` - 打开统计信息菜单（50级或精通后解锁）
- `/pit angel` - 打开天使阵营菜单（无等级限制）

### 管理员命令

#### `/pitadmin [subcommand]`
管理员命令，包含以下子命令：

- `/pitadmin gacha` - 抽奖功能
- `/pitadmin giveGacha <player> <amount>` - 给指定玩家抽奖券
- `/pitadmin gachaPreview` - 预览抽奖内容
- `/pitadmin checkGacha <player>` - 查看指定玩家抽奖券数量
- `/pitadmin setGacha <player> <amount>` - 设置指定玩家抽奖券数量
- `/pitadmin checkMaxHealth <player>` - 检查玩家额外血量
- `/pitadmin setLocCurrency <field>` - 设置配置位置字段
- `/pit give [player]` - 给予手持物品（别名命令）
- `/pitadmin addSpawn` - 添加出生点位置
- `/pitadmin loc` - 显示当前位置信息
- `/pitadmin hologramLoc` - 设置全息图位置
- `/pitadmin keeperLoc` - 设置退出NPC位置
- `/pitadmin mail` - 设置邮件NPC位置
- `/pitadmin genesisDemonLoc` - 设置恶魔阵营NPC位置
- `/pitadmin genesisAngelLoc` - 设置天使阵营NPC位置
- `/pitadmin changeItemInHand [lives|maxlive|tier] <amount>` - 修改手持物品属性
- `/pitadmin shopNpc` - 设置商店NPC位置
- `/pitadmin perkNpc` - 设置天赋NPC位置
- `/pitadmin LeaderNpc` - 设置排行榜NPC位置
- `/pitadmin prestigeNpc` - 设置精通NPC位置
- `/pitadmin statusNpc` - 设置状态NPC位置
- `/pitadmin debug <value>` - 调试命令
- `/pitadmin change [player] <type> <amount>` - 修改玩家数据（类型：coin, prestige, renown, streak, abounty, level, bounty, maxhealth）
- `/pitadmin edit` - 切换编辑模式（可自由破坏方块）
- `/pitadmin openMenu <menu>` - 打开指定菜单（shop, perkBuy, prestigePerkBuy, prestige, ench, quest, mail, cdk, allCdk）
- `/pitadmin mythicHologram` - 设置神话附魔台全息图位置
- `/pitadmin chestHologram` - 设置末影箱全息图位置
- `/pitadmin leaderHologram` - 设置排行榜全息图位置
- `/pitadmin helperHolo` - 设置帮助全息图位置
- `/pitadmin kaboom` - 对所有在线玩家使用爆炸效果
- `/pitadmin pitLoc <a|b>` - 设置天坑区域A或B的位置
- `/pitadmin event <name>` - 开启指定事件
- `/pitadmin setkothloc` - 设置占山为王位置
- `/pitadmin giveSupporter [player]` - 给予支持者权限
- `/pitadmin takeSupporter [player]` - 移除支持者权限
- `/pitadmin drop` - 删除数据库（需要3个管理员确认）
- `/pitadmin testSound [sound]` - 测试音效（默认为successfully）
- `/pitadmin quest` - 设置任务NPC位置
- `/pitadmin reloadnpc` - 重载所有NPC位置
- `/pitadmin table` - 设置附魔台位置
- `/pitadmin ench` - 打开附魔菜单
- `/pitadmin setegg` - 设置蛋位置
- `/pitadmin pi` - 打开物品管理菜单
- `/pitadmin pr` - 打开符文物品菜单
- `/pitadmin reboot [duration] [reason]` - 重启服务器（默认2分钟，原因：计划外重启）
- `/pitadmin forceTrade <player>` - 与指定玩家强制交易
- `/pitadmin wipe <player> <reason>` - 清档指定玩家
- `/pitadmin unwipe <player>` - 取消玩家清档状态
- `/pitadmin rollback <player>` - 回滚指定玩家背包备份
- `/pitadmin ham [a]` - 设置汉堡NPC位置
- `/pitadmin spire floor <number>` - 设置尖塔楼层位置
- `/pitadmin spire spawn` - 设置尖塔中心位置
- `/pitadmin forceSpawn [player]` - 强制传送玩家到随机出生点
- `/pitadmin disablePlugin <plugin>` - 禁用指定插件
- `/pitadmin refreshEvents` - 刷新事件
- `/pitadmin addAngelSpawns` - 添加天使阵营出生点
- `/pitadmin addDemonSpawns` - 添加恶魔阵营出生点
- `/pitadmin addPackagePoint` - 添加包裹点
- `/pitadmin addSewersPoint` - 添加下水道事件点
- `/pitadmin enchantrecords` - 检查手持物品附魔记录
- `/pitadmin addSquadsLoc` - 添加小队模式位置
- `/pitadmin addbhLoc` - 添加BlockHead事件位置
- `/pitadmin clearrecords` - 清除手持物品附魔记录
- `/pitadmin resetKingsQuests` - 重置国王任务标记
- `/pitadmin reload` - 重载配置文件
- `/pitadmin auction <price>` - 以指定价格发起自定义拍卖
- `/pitadmin rename <name>` - 重命名手持的神话物品

## 依赖

- ProtocolLib
- 可选依赖：WorldEdit, FastAsyncWorldEdit, PlayerPoints, LuckPerms

## 支持的游戏版本

- Minecraft 1.12.2

## 功能特色

- 丰富的玩家等级和精通系统
- 多种附魔和天赋系统
- 任务和事件系统
- 交易和拍卖功能
- NPC系统
- 自定义物品系统
- 活动和比赛模式