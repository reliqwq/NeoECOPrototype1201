---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype1201:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:backup_drive
  - neoecoprototype1201:cable_pattern_provider
  - ae2:silicon
  - neoecoae:crystal_matrix
  - neoecoae:superconducting_processor
  - neoecoae:energized_superconductive_ingot
  - neoecoprototype1201:ec9_processor_assembler
  - neoecoprototype1201:simplify_pattern_provider
  - neoecoprototype1201:simplify_stonecutting_assembler
  - neoecoprototype1201:simplify_structure_expansion_card
  - neoecoprototype1201:simplify_work_order_upgrade_computation_l9
  - neoecoprototype1201:simplify_work_order_upgrade_crafting_l9
  - neoecoprototype1201:simplify_work_order_upgrade_storage_l9
  - neoecoprototype1201:simplify_work_order_grow_computation_l4
  - neoecoprototype1201:simplify_work_order_grow_computation_l6
  - neoecoprototype1201:simplify_work_order_grow_computation_l9
  - neoecoprototype1201:simplify_work_order_grow_crafting_l4
  - neoecoprototype1201:simplify_work_order_grow_crafting_l6
  - neoecoprototype1201:simplify_work_order_grow_crafting_l9
  - neoecoprototype1201:simplify_work_order_grow_f1_crafting
  - neoecoprototype1201:simplify_work_order_grow_l1_storage
  - neoecoprototype1201:simplify_work_order_grow_storage_l4
  - neoecoprototype1201:simplify_work_order_grow_storage_l6
  - neoecoprototype1201:simplify_work_order_grow_storage_l9
---

# L1 处理器装配室

**L1 处理器装配室**相当于绿色外壳的<ItemLink id="ae2:molecular_assembler" />，只接受处理样板。它只跑本模组的处理器装配配方。它和下面的 L1 样板供应器搭配使用。

配方闸门与批量规则在本模组自己的方块实体中实现，不注入 AE2。

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype1201:simplify_pattern_provider" />
  <ItemIcon id="neoecoprototype1201:cable_pattern_provider" />
</ItemGrid>

样板供应器也能只挂在线缆的一面。两种形态在合成台里互换，一比一。

装配室与供应器都是**工作台合成配方**：装配室 = 1 台分子装配室 + 4 只特质机壳 + 4 个绿晶晶格；样板供应器 = 1 个 ME 样板供应器 + 4 块石英玻璃 + 4 个绿晶晶格，一次出 **2** 台。

<Row>
  <RecipeFor id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype1201:simplify_pattern_provider" />
</Row>

## 出厂配方

处理器三件套（EC9 还能从压印器推导更多配方，见下）：

| 产物 | 原料 |
|------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 运算处理器 | 硅 + 红石 + 赛特斯石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |

两种装配室都能制作超导处理器：

| 产物 | 原料 |
|------|------|
| <ItemLink id="neoecoae:superconducting_processor" /> | 水晶晶格 + 盈能超导锭 + 硅 |

其余 8 条：

| 产物 | 原料 |
|------|------|
| L1 供能接口 | 2 水晶谐振发电机 + 1 ME接口 + 1 ECO - SA 超导处理器 |
| ECOP - 结构扩容卡 | 内存卡 + 铝锭 + 绿晶晶格 |
| 盈能强化线程核心 | 2 ECO - CM4A 线程核心 + 1 ECO - C1 计算子结构外壳 |
| ECO - CE1R 闪存晶阵 | 2 ECO - CE1 闪存晶阵 + 1 ECO - L4 存储组件 |
| 雪球 / 细雪桶 | 4 雪块 → 4 雪球；3 雪球 + 1 桶 → 细雪桶 |
| reliqwq 的 Fumo 玩偶 | L1 存储主机 + C1 计算主机 + F1 合成主机 |
| Yang120 的 Fumo 玩偶 | eco 的 L4 存储主机 + C4 计算主机 + F4 合成主机 |
| kouooki 的 Fumo 玩偶 | 粉色染料 + 干海带 + 棕色蘑菇 |

硅、金锭、钻石接受标签。

只有 EC9 会从压印器的配方现场推导其余处理器配方——别的模组给压印器加的配方不需要数据文件就能用。

## 速度与批量

L1 有 5 个<ItemLink id="ae2:speed_card" />卡槽：不装卡一份要 100 刻，装满 5 张要 20 刻。它只接受 1× 样板，每次严格执行一份配方。

EC9 有 6 个加速卡槽，在供电充足的网络中装满 6 张时 **1 刻完成一次加工**。它接受不超过 `processor_assembler_max_runs_per_cycle` 的编码批量（默认 **64**，范围 **1–192**），但不会把 1× 样板自动扩成多份。批量超限，或整批原料摆不进 9 格工作台时，样板会被整张拒收，原料留在网络里。
