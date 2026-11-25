---
navigation:
  title: L1 奇点元件
  icon: neoecoprototype1201:simplify_singularity_cell
  position: 22
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_singularity_cell
  - neoecoprototype1201:simplify_sky_stone_dust_cell
---

# L1 奇点元件

插进驱动器之后，**每 30 秒产出 120 个奇点**：它不需要任何输入，也不消耗任何东西，取用方式与普通存储元件相同。

## 多快

一**批** = 一次到账的一份产出，默认 120 个：默认间隔 600 tick（30 秒），平均约每秒 4 个。

| 简单估计 | 可产出 |
|----------------|--------|
| 30 秒 | 120 |
| 1 分钟 | 240 |
| 5 分钟 | 1,200 |
| 30 分钟 | 7,200 |
| 1 小时 | 14,400 |
| 24 小时 | 345,600 |

| 用途 | 需要 | 默认速率下 |
|------|------|------------|
| 本模组三台 L1 接口 | 6 | 第一批就到（30 秒） |
| eco 一只 MEGA 4G 矩阵，或一个无限存储组件 | 128 | 两批，60 秒 |
| eco 的无限存储解锁（64 个组件） | 8,192 | 69 批，约 35 分钟 |

## 产出速度

按世界时间分批产出，不持续滴落。服务端可分别配置每批数量与间隔，默认每 600 tick（30 秒）产出 120 个。库存最多为 **9,223,372,036,854,775,807**，达到后饱和，不会因正常产出回绕。

## 库存是算出来的

第一次被驱动器或主机查询时开始计数；只放在背包、箱子或掉落物里不会开始。暂停或关服时世界时间不走，库存不涨；已经开始计数的元件取出后仍会继续。

## 限制

不接收任何输入（包括奇点），只保存并输出自己产出的那一种内容。不能在元件工作台分区，也不能装进另一只存储单元。属于 L1 这一级，[L1 存储系统](storage_system.md) 直接收，兼容 eco 主机。

## 配方

<RecipeFor id="neoecoprototype1201:simplify_singularity_cell" />

## 同一族的另一台：陨石粉矩阵

<ItemLink id="neoecoprototype1201:simplify_sky_stone_dust_cell" /> 产出陨石粉（`ae2:sky_dust`）：默认也是**每 30 秒产出 120 个**，库存按世界时间自己长。

<RecipeFor id="neoecoprototype1201:simplify_sky_stone_dust_cell" />

配方：无限矩阵外壳 + 64M ECO 存储组件 + 绿晶矩阵 + 一个陨石粉。

## 新生魔艺联动：魔源矩阵

产出的是魔源：默认同样**每 30 秒产出 120**。

它**只在装了 Ars Energistique（`arseng`，以及它的前置 Ars Nouveau）时才会注册**。

合成方式：无限矩阵外壳 + Alakarkinos 神像 + 魔源宝石块 + 魔源花与三种精华，走 Ars Nouveau 的附魔装置合成。
