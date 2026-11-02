---
navigation:
  title: 盈能超导接口
  icon: neoecoprototype1201:superconductive_interface
  position: 90
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:superconductive_interface
---

# 盈能超导接口

<Row>
  <BlockImage id="neoecoprototype1201:superconductive_interface" scale="3"></BlockImage>
</Row>

**盈能超导接口**是 [L1 供能接口](powered_interface.md) 的高配版：布局一样，分区槽与库存槽同样是 AE2 自己的
9 个，界面也是它那套，但**单个分区能存 8,192 个物品**；流体与化学品则把 AE2 的每格上限**放大 128 倍**
（AE2 把物品分区限死在一组 64 个，8192 就是同一条放大比例）。分区与设置方式和原版接口一样。

它自己**不耗电**（空闲 0 AE/t）。4,000 AE/t 通常高过网络中其他被动发电源，所以一台它就能接管整个网络的
被动发电。

卡片：与供能接口同一套放行——**1 张合成卡、1 张模糊卡**，装了 AppliedFlux 之后再加 **1 张 Induction Card
（感应卡）**。

它在 eco 的 <ItemLink id="neoecoae:integrated_working_station" /> 里组装：1 个天外寒冰 + 4 台
[L1 供能接口](powered_interface.md) + 9 个盈能超导锭 + 4 枚超导处理器，耗能 10,000 FE。
