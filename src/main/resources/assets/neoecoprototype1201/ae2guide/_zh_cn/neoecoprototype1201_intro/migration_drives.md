---
navigation:
  title: 升格迁移驱动
  icon: neoecoprototype1201:migration_drive_1_4
  position: 95
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:migration_drive_1_4
  - neoecoprototype1201:migration_drive_4_6
  - neoecoprototype1201:migration_drive_6_9
  - neoecoprototype1201:migration_drive_creative
  - neoecoprototype1201:migration_drive_empty
---

# 升格迁移驱动

一张盘把**一台已经成型、且已经撑到满配边长的主机**在原位升一档。对着主机方块【潜行 + 右键】。它不是建造器：
机器必须先成型、必须先满长，升档不改变它的形状。

<Row>
  <ItemImage id="neoecoprototype1201:migration_drive_1_4" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_4_6" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_6_9" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_creative" scale="3"></ItemImage>
</Row>

| 驱动 | 从 | 到 | 名字颜色 |
|------|----|----|----------|
| <ItemLink id="neoecoprototype1201:migration_drive_1_4" /> | 本模组的 L1 存储 / 计算 / 合成主机 | eco 同族的 L4 | 绿，L1 那一档 |
| <ItemLink id="neoecoprototype1201:migration_drive_4_6" /> | eco L4 | eco L6 | 蓝，L4 那一档 |
| <ItemLink id="neoecoprototype1201:migration_drive_6_9" /> | eco L6 | eco L9 | 橙，L6 那一档 |
| <ItemLink id="neoecoprototype1201:migration_drive_creative" /> | L1、L4 或 L6 | 一步到同族 eco L9 | 紫，L9 那一档 |

## 换档怎么处理你手上的东西

被换下来的每一格方块都**退回你的背包**——旧机壳、驱动器、通风口、元件、接口都交还给你，不会消失在新机器的墙里。
插着的东西留在原来那一格：存储矩阵回到它自己那只驱动器的同一格，样板回到它自己那条总线的同一个槽。

唯一留不住的是**接口自己那份配置**：eco 只把接口槽开放成只读，所以换完档要重设一次。成功提示当场就会把这句说出来。
这是认下来的代价，不是等着再修的 bug。

驱动本身被吃掉，换回来的是一张空盘——<ItemLink id="neoecoprototype1201:migration_drive_empty" />，
它故意不上色，免得被人当成还能用的盘。

## 怎么拿到

**这一条线上四张盘都没有配方**，只有创造栏与管理员指令能拿到。这是刻意的，不是没做完：一档的价格还没设计，
而一张价格靠猜的收费盘放进整合包，比一张暂时做不出来的盘更糟。

`/give @p neoecoprototype1201:migration_drive_1_4`，另外三张是 `migration_drive_4_6`、`migration_drive_6_9`、
`migration_drive_creative`。

机器必须是三族里的一台主机、已经成型、并且已经满长。其他情况——包括只差一格没到上限的主机——都会被拒收，
并打一行说清是哪一条。
