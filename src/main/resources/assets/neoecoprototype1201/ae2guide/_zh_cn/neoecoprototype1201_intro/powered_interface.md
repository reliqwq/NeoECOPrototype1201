---
navigation:
  title: L1 供能接口
  icon: neoecoprototype1201:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_powered_me_interface
---

# L1 供能接口

<Row>
  <BlockImage id="neoecoprototype1201:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

**L1 供能接口**就是 AE2 的<ItemLink id="ae2:interface" />多了一个本事：它给网络供电。分区槽与库存槽都是
9 个，界面也是 AE2 自己那一套，分区规则完全一致——凡是接受 ME 接口的地方都能用它。

它自己**不耗电**（空闲 0 AE/t）。通电且有频道时它输出 **200 AE/t**。AE2 每个网格只保留输出功率最高的那台
被动发电源，所以更强的源在场时它自动停止贡献。

卡片：它和 AE2 自己的接口一样收 **1 张合成卡 + 1 张模糊卡**；装了 AppliedFlux 之后还多收 **1 张 Induction
Card（感应卡）**——appflux 1.20.1 没有这份中文译名，界面上就是英文那张卡，作用是让 AE 设备能收也能发 FE。
卡片的提示会把它并进"接口"那一行，不会单独列出我们的名字。

这一条线没有线缆部件版；槽位停在 AE2 的 9 个也是故意的——这台机器开的是 AE2 自己的接口面板，只加槽位
不加面板的话，多出来的那几格根本看不见。

原料四件，在 [L1 处理器装配室](processor_assembler.md) 里组装：2 个<ItemLink id="ae2:crystal_resonance_generator" />、
1 个<ItemLink id="ae2:interface" />、1 个<ItemLink id="neoecoae:superconducting_processor" />。
四台它合成一台[盈能超导接口](superconductive_interface.md)。
