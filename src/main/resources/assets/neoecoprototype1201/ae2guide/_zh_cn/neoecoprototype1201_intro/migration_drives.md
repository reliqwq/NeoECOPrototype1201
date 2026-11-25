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

一张盘把**一台已经成型、且已经撑到满配边长的主机**在原位升一档。对着主机方块【潜行 + 右键】。
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

## 换档

无论是否返还旧方块，机器内的存储矩阵与样板都会保留：矩阵回到原来的驱动槽位，样板回到原来的样板总线槽位。

换下的方块**默认不返还**。在服务端配置中启用 `migration_drive.refund_replaced_blocks` 后，旧外壳、驱动、通风器、元件与接口会进入你的背包；背包放不下时掉在地上。成功提示会说明本次使用的是哪一种行为。

换档会清空子系统接口的设置，需要重新配置；成功提示也会明确说明这一点。

驱动本身被吃掉，换回来的是一张空盘——<ItemLink id="neoecoprototype1201:migration_drive_empty" />。

## 怎么拿到

四种驱动都能在 eco 的整合工作站里合成：先用扩容卡和 AE2 1K 元件做空盘，再逐档升级；创造档驱动合并前三张。也可以从创造栏或管理员命令直接获得。

<RecipeFor id="neoecoprototype1201:migration_drive_empty" />
<RecipeFor id="neoecoprototype1201:migration_drive_1_4" />
<RecipeFor id="neoecoprototype1201:migration_drive_4_6" />
<RecipeFor id="neoecoprototype1201:migration_drive_6_9" />
<RecipeFor id="neoecoprototype1201:migration_drive_creative" />

机器必须已成型且达到满配边长。其他情况——包括只差一格没到上限的主机——都会被拒收，并打一行说清是哪一条。
