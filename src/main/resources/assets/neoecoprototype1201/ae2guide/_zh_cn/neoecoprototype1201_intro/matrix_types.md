---
navigation:
  title: 支持的存储矩阵
  icon: neoecoprototype1201:prototype_item_cell_1m
  position: 20
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_item_storage_matrix_housing
  - neoecoprototype1201:simplify_fluid_storage_matrix_housing
  - neoecoprototype1201:simplify_chemical_storage_matrix_housing
  - neoecoprototype1201:simplify_concrete_storage_matrix_housing
  - neoecoprototype1201:pigmee_storage_matrix_housing
  - neoecoprototype1201:simplify_storage_component_1m
  - neoecoprototype1201:simplify_storage_component_4m
  - neoecoprototype1201:pigmee_storage_cell
  - neoecoprototype1201:simplify_concrete_storage_cell
  - neoecoprototype1201:simplify_universal_storage_matrix_housing
  - neoecoprototype1201:simplify_universal_storage_cell_1k
  - neoecoprototype1201:simplify_universal_storage_cell_1m
  - neoecoprototype1201:simplify_quantum_storage_matrix_housing
  - neoecoprototype1201:simplify_quantum_storage_cell_1k
  - neoecoprototype1201:simplify_quantum_storage_cell_1m
  - neoecoprototype1201:simplify_complex_universal_storage_matrix_housing
  - neoecoprototype1201:simplify_complex_universal_storage_cell_1k
  - neoecoprototype1201:simplify_complex_universal_storage_cell_1m
  - neoecoprototype1201:simplify_source_growing_cell
  - neoecoprototype1201:simplify_source_storage_matrix_housing
  - neoecoprototype1201:simplify_source_storage_cell_1k
  - neoecoprototype1201:simplify_source_storage_cell_16k
  - neoecoprototype1201:simplify_source_storage_cell_64k
  - neoecoprototype1201:simplify_source_storage_cell_1m
  - neoecoprototype1201:simplify_source_storage_cell_4m
---

# 支持的存储矩阵

这一页对比本模组在 **1.20.1 这条线**上注册的所有存储矩阵：先按"要存什么"选族，再比容量档位、能同时装
多少种东西、以及这一族需要什么模组。

## 目前支持的

| 族 | 容量 | 可存种类 | 前置 |
|------|--------|----------|------------|
| 物品 | 1K - 4M | 256 | — |
| 流体 | 1K - 4M | 256 | — |
| 化学品 | 1K - 4M | 25 | Mekanism + Applied Mekanistics |
| FE | 1K / 1M / 4M | 1 | AppliedFlux + Glodium |
| 无限混凝土 | 无限 | 16 | — |
| 猪咪 | 2,560 字节 | 256 | — |
| MEGA | [小宗存储矩阵](small_bulk_matrices.md)（物品、流体） | 3，装卡 10 | MEGA Cells |
| 全能（omni） | 1K, 1M | 512 | AE2 Omni Cells |
| 量子 | 1K, 1M | 无限 | AE2 Omni Cells |
| 复合全能 | 1K, 1M | 1024 | AE2 Omni Cells |
| 乏核废料 | 1K, 1M | 1 | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| 奇点（自产） | 无限字节 | 1 | — |
| 陨石粉（自产） | 无限字节 | 1 | — |
| 魔源（自产） | 无限字节 | 1 | Ars Énergistique（连带 Ars Nouveau） |
| 魔源 | 1K - 4M | 256 | Ars Énergistique（连带 Ars Nouveau） |

## 怎么做出来

每一族都是**外壳 + 容量元件**
(<ItemLink id="neoecoprototype1201:simplify_storage_component_1m" /> 与
<ItemLink id="neoecoprototype1201:simplify_storage_component_4m" />)，因为 AE2 的阶梯只到 256k。
拆矩阵是**工作台**配方：把一只矩阵单独放进任意一格，就会吐回它的外壳和当初用的元件——除了无限家族，
表里每一只矩阵都有拆解配方。

空闲耗电是每 1M 字节 1 AE/t，所以同一条阶梯从 1K 的约 0.001 AE/t 一直到 4M 的 4 AE/t。无限混凝土矩阵
固定 8 AE/t，和 eco 自己的无限成员一样。

## 我们的 L1 驱动器挂载什么

 L1 存储主机不支持 eco 更高档的矩阵。eco存储主机可以支持L1矩阵。

存储侧的那台主机是 L1 存储控制器；另外两个子系统做什么，见 [L1 计算系统](computation_system.md) 与
[L1 合成系统](crafting_system.md)；AE 电力怎么存，见[盈能能源元件](energized_energy_cells.md)。
