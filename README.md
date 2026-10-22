# ArcanumCraft 魔导工匠

一款融合魔法与科技的综合性 Paper/Spigot 服务端插件，包含 6 大核心系统。

## 📦 编译

```bash
# 依赖: Maven + JDK 17/21
chmod +x build.sh
./build.sh
```

编译后产物：
- `target/ArcanumCraft.jar` - 插件 JAR 包
- `ArcanumCraft-source.tar.gz` - 源码包

## 🚀 安装

1. 将 `target/ArcanumCraft.jar` 复制到服务器的 `plugins/` 目录
2. 启动服务器，插件自动生成配置文件
3. 编辑 `plugins/ArcanumCraft/config.yml` 调整参数
4. 使用 `/arcanumcraft reload` 重载配置

## 🎮 命令列表

### 🔮 魔晶系统
| 命令 | 权限 | 说明 |
|------|------|------|
| `/crystal` | `arcanumcraft.crystal.use` | 查看余额 |
| `/crystal pay <玩家> <数量>` | `arcanumcraft.crystal.use` | 转账 |
| `/crystal top` | `arcanumcraft.crystal.use` | 排行榜 |

### 🌀 传送门系统
| 命令 | 权限 | 说明 |
|------|------|------|
| `/portal set <名称>` | `arcanumcraft.portal.use` | 建立传送门（消耗50魔晶） |
| `/portal goto <名称>` | `arcanumcraft.portal.use` | 传送到目标 |
| `/portal list` | `arcanumcraft.portal.use` | 查看所有传送门 |
| `/portal remove <名称>` | `arcanumcraft.portal.use` | 删除传送门 |

### ⚒️ 熔炉系统
| 命令 | 权限 | 说明 |
|------|------|------|
| `/forge status` | `arcanumcraft.forge.use` | 查看周围熔炉状态 |
| `/forge mine` | `arcanumcraft.forge.use` | 破坏周围熔炉 |

### ✨ 附魔系统
| 命令 | 权限 | 说明 |
|------|------|------|
| `/enchant info` | `arcanumcraft.enchant.use` | 查看可附魔列表 |
| `/enchant upgrade` | `arcanumcraft.enchant.use` | 查看手持物品信息 |
| `/enchant imbue <附魔> <等级>` | `arcanumcraft.enchant.use` | 自定义附魔 |

### 🛠️ 管理命令
| 命令 | 权限 | 说明 |
|------|------|------|
| `/arcanumcraft reload` | `arcanumcraft.admin` | 重载配置 |
| `/arcanumcraft help` | - | 显示帮助 |

## 🧩 核心系统

### 1. 魔晶经济系统
- 每日魔晶上限（可配置）
- 玩家间转账
- 挖矿/钓鱼获取魔晶
- 加入奖励
- 全服排行榜

### 2. 传送门网络
- 玩家自定义命名传送门
- 传送冷却、保护时间
- 强制加载远端区块
- 可视化传送门列表

### 3. 智能熔炉
- 4 种熔炉类型（熔炼炉/烟熏炉/高炉/坩埚）
- 自动加工配方
- 耐久系统
- 破坏回收

### 4. 魔导祭坛
- 5 种自定义附魔：
  - 灵魂烈焰（火属性攻击增强）
  - 虚空之触（击退）
  - 奥术精通（对不死生物伤害）
  - 时移（横扫伤害）
  - 元素共鸣（火焰保护）
- 费用倍率可配置

### 5. 魔晶矿脉
- 自然生成紫水晶块（模拟魔晶矿石）
- 高度范围：Y10~Y40
- 每区块最多 3 个矿脉
- 30% 概率掉落 1-3 个魔晶

### 6. 数据统计
- 玩家行为追踪
- 持久化存储
- 成就系统基础框架

## ⚙️ 配置项

### 魔晶系统
```yaml
crystal:
  ore-generation:
    enabled: true
    max-per-chunk: 3
    min-height: 10
    max-height: 40
  daily-limit: 100
  join-bonus: 10
  min-transfer: 1
```

### 传送门系统
```yaml
portal:
  cost: 50
  teleport-delay: 0
  protection-seconds: 2
  force-chunk-load: true
  cooldown-seconds: 30
```

### 熔炉系统
```yaml
forge:
  costs:
    smelting-furnace: 30
    smoker: 40
    blast-furnace: 50
    cauldron: 20
  work-interval: 5
```

### 附魔系统
```yaml
enchantment:
  cost-multiplier: 2.0
  max-level: 10
  custom-enchantments:
    - soul-fire
    - void-touch
    - arcane-mastery
    - temporal-shift
    - elemental-resonance
```

## 📝 开发规范

- 包名：`github.xhserver123.arcanumcraft`
- 主类：`ArcanumCraftPlugin`
- 构建工具：Maven（JDK 17/21）
- 语言文件：`src/main/resources/lang/{zh_cn,en_us}.yml`
- 无 Lombok、无硬编码敏感信息

## 📄 许可证

MIT License - 见 [LICENSE](LICENSE)
