---
navigation:
  title: L1 接口
  icon: neoecoprototype1201:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_powered_me_interface
  - neoecoprototype1201:superconductive_interface
---

# L1 接口

<Row>
  <BlockImage id="neoecoprototype1201:simplify_powered_me_interface" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype1201:superconductive_interface" scale="3"></BlockImage>
</Row>

两台接口都是 AE2 的<ItemLink id="ae2:interface" />多了一个供电答案：分区槽与库存槽都是 9 个，界面也是它
那一套，分区规则完全一致——凡是接受 ME 接口的地方，这两台都能替。两者只差两个数字。

| | L1 供能接口 | 盈能超导接口 |
|---|---|---|
| 分区槽 / 库存槽 | 9 / 9 | 9 / 9 |
| 单个分区能存 | AE2 自己的量 | **8,192** 个物品；流体与化学品是 AE2 每格上限的 **128 倍** |
| 被动输出 | **200 AE/t** | **4,000 AE/t** |
| 空闲耗电 | 0 AE/t | 0 AE/t |
| 在哪里做 | [L1 处理器装配室](processor_assembler.md) | eco 的 <ItemLink id="neoecoae:integrated_working_station" />，10,000 FE |

放大只做到"每个分区"、没有做到"槽位数"，这是故意的：这条线开的是 AE2 自己的接口面板，只加槽位不加面板
的话，多出来的那几格根本看不见。

两台都是被动发电源，而 AE2 每个网格只保留输出功率最高的那一台。4,000 AE/t 通常高过前期网络里的其他被动
发电源，所以一台盈能超导接口就会接管整个网络的被动发电，旁边的供能接口自动停下。

卡片：两台都收 **1 张合成卡 + 1 张模糊卡**，跟 AE2 自己的接口一样；装了 AppliedFlux 之后各再多收
**1 张 Induction Card（感应卡）**——appflux 1.20.1 没有这份中文译名，中文客户端里它就是英文名，它自己的
提示写的是"让 AE 设备能收也能发能量"。AE2 的卡片提示会把我们这两台并进"接口"那一行，不单独列名字。

配方直接写在下面，因为这两条都是自定义配方类型、这里的配方查看器画不出来：供能接口 = 2 个
<ItemLink id="ae2:crystal_resonance_generator" /> + 1 个<ItemLink id="ae2:interface" /> + 1 个
<ItemLink id="neoecoae:superconducting_processor" />，在装配室里做；四台供能接口再做一台盈能超导接口，
那一台另外要 1 个天外寒冰 + 9 个盈能超导锭 + 4 枚超导处理器。
