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
  - neoecoprototype1201:simplify_fe_cell_housing
  - neoecoprototype1201:simplify_concrete_storage_matrix_housing
  - neoecoprototype1201:pigmee_storage_matrix_housing
  - neoecoprototype1201:simplify_storage_component_1m
  - neoecoprototype1201:simplify_storage_component_4m
  - neoecoprototype1201:prototype_item_cell_1k
  - neoecoprototype1201:prototype_item_cell_16k
  - neoecoprototype1201:prototype_item_cell_64k
  - neoecoprototype1201:prototype_item_cell_1m
  - neoecoprototype1201:prototype_item_cell_4m
  - neoecoprototype1201:simplify_fluid_storage_cell_1k
  - neoecoprototype1201:simplify_fluid_storage_cell_16k
  - neoecoprototype1201:simplify_fluid_storage_cell_64k
  - neoecoprototype1201:simplify_fluid_storage_cell_1m
  - neoecoprototype1201:simplify_fluid_storage_cell_4m
  - neoecoprototype1201:simplify_chemical_storage_cell_1k
  - neoecoprototype1201:simplify_chemical_storage_cell_16k
  - neoecoprototype1201:simplify_chemical_storage_cell_64k
  - neoecoprototype1201:simplify_chemical_storage_cell_1m
  - neoecoprototype1201:simplify_chemical_storage_cell_4m
  - neoecoprototype1201:simplify_fe_storage_cell_1k
  - neoecoprototype1201:simplify_fe_storage_cell_1m
  - neoecoprototype1201:simplify_fe_storage_cell_4m
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
  - neoecoprototype1201:simplify_singularity_cell
  - neoecoprototype1201:simplify_sky_stone_dust_cell
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
| 奇点（自生长） | 无限字节 | 1 | — |
| 陨石尘（自生长） | 无限字节 | 1 | — |
| 魔源（自生长） | 无限字节 | 1 | Ars Énergistique（连带 Ars Nouveau） |
| 魔源 | 1K - 4M | 256 | Ars Énergistique（连带 Ars Nouveau） |

- 1K - 4M 说的是**字节**，不是资源数量：每个类型槽占整只矩阵的 1/256。
- 化学品的 25 沿用 eco 自己的面板行，不是我们定的。
- 无限混凝土只存 16 色、而且是表里唯一**拆不回来**的矩阵；猪咪是一档 2,560 字节的物品矩阵。
- 四支**全能家族**是本附属的高容量档，需要 AE2 Omni Cells：矩阵本体跑在 eco 自己的 omni 存储后端上——
- **奇点家族**与**魔源**是会自己生长的盘：奇点单元按 600 刻一批产出 120 个奇点，陨石尘盘产 AE2 的陨石粉，
  魔源生长盘产源质——都不吃任何输入，库存从放进驱动器那一刻起按世界时钟推算。五档魔源盘则用 AE2 自己的
  组件装配，256 类、每类 1/256 容量，与物品/流体阶梯逐字节对齐。魔源一族需要 Ars Énergistique（连带
  Ars Nouveau），没装的整合包看不到它们。
  与 eco 的 LE4/LE6/L9 全能矩阵同一套，所以没装那个模组的整合包根本看不到它们。全能矩阵一次可容 512 种，
  量子矩阵无类型上限——它的 1M 档刻意比全能 1M 贵一半。复合全能用类型预算换更大的 1M。乏核废料矩阵只收
  一种物质——Mekanism 的乏核废料——让这种副产物在 L1 也有容身之处；它额外要求 Mekanism 与 Applied
  Mekanistics。

## 怎么做出来

每一族都是**外壳 + 容量元件**，和 AE2 的形状一样：1K、16K、64K 用 AE2 自己的元件，1M 与 4M 用我们的
(<ItemLink id="neoecoprototype1201:simplify_storage_component_1m" /> 与
<ItemLink id="neoecoprototype1201:simplify_storage_component_4m" />)，因为 AE2 的阶梯只到 256k。
拆矩阵是**工作台**配方：把一只矩阵单独放进任意一格，就会吐回它的外壳和当初用的元件——除了无限混凝土，
表里每一只矩阵都有拆解配方。

空闲耗电是每 1M 字节 1 AE/t，所以同一条阶梯从 1K 的约 0.001 AE/t 一直到 4M 的 4 AE/t。无限混凝土矩阵
固定 8 AE/t，和 eco 自己的无限成员一样。

## 我们的 L1 驱动器挂载什么

驱动器只在**自己档位不低于这只盘**的时候才挂载它。我们的 L1 在最低档，所以 eco 更高档的矩阵插进我们这台会被拒收。上面表里的六族全是我们的 L1，全都正常挂载。至于 eco 自己的驱动器收不收我们的盘，那是它的规则。

存储侧的那台主机是 L1 存储控制器；另外两个子系统做什么，见 [L1 计算系统](computation_system.md) 与
[L1 合成系统](crafting_system.md)；AE 电力怎么存，见[盈能能源元件](energized_energy_cells.md)。
