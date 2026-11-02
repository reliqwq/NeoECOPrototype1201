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
---

# 支持的存储矩阵

这一页对比本模组在 **1.20.1 这条线**上注册的所有存储矩阵：先按"要存什么"选族，再比容量档位、能同时装
多少种东西、以及这一族需要什么模组。

## 各族一览

| 族 | 容量档位 | 同时可存种类 | 需要的模组 |
|------|----------------|----------|------------|
| 物品 | 1K、16K、64K、1M、4M | 256 | - |
| 流体 | 1K、16K、64K、1M、4M | 256 | - |
| 化学品 | 1K、16K、64K、1M、4M | 25 | Mekanism + Applied Mekanistics |
| FE（能量） | 1K、1M、4M | 1 | AppliedFlux |
| 无限混凝土 | 无限 | 16 | - |
| 猪咪 | 一档 2,560 字节 | 256 | - |
| 小宗（物品、流体） | 一档，每种字节近乎无限 | 3，装扩展卡 10 | MEGA Cells - 整页见[小宗存储矩阵](small_bulk_matrices.md) |

- 1K / 16K / 64K / 1M / 4M 说的是**字节**，不是资源数量。每个类型槽占整只矩阵的 1/256，所以 1K 那一档
  一个类型是 4 字节，4M 那一档同样一个类型是 16384 字节。一个字节折算多少资源由介质决定，这一条沿用
  AE2 自己的算法，我们没有改动。
- 化学品的 25 不是我们定的数字：eco 20.5.3 的 Mekanism 面板行就写 25，它自己的化学品矩阵也是 25。字节
  排布和表里其他族保持一致。
- FE 矩阵只存一种东西是设计如此——这一族的存在意义是把 Forge Energy 放进网格里，挨着 <ItemLink id="ae2:energy_cell" /> 放，而不是当抽屉用。AppliedFlux 自己还需要 **Glodium** 才能加载，所以只有同时装了这两者的整合包才会看到这一族。
- 无限混凝土矩阵只存一种资源，可以取 16 种原版混凝土颜色里的任意一种，而且取得完。它的外壳是混凝土的——
  沙子加碎石围着绿晶晶格——矩阵本体仍然是外壳 + 4M 元件。它是表里唯一**拆不回来**的矩阵：压缩艺术不是
  能解压回去的东西。
- 猪咪矩阵是一档 2,560 字节的物品矩阵，比 1K 还小，这是故意的。它的外壳是一把剑：有攻击伤害、剑的挥砍
  速度，并且比徒手多够两格。

## 怎么做出来

每一族都是**外壳 + 容量元件**，和 AE2 的形状一样：1K、16K、64K 用 AE2 自己的元件，1M 与 4M 用我们的
(<ItemLink id="neoecoprototype1201:simplify_storage_component_1m" /> 与
<ItemLink id="neoecoprototype1201:simplify_storage_component_4m" />)，因为 AE2 的阶梯只到 256k。
拆矩阵是**工作台**配方：把一只矩阵单独放进任意一格，就会吐回它的外壳和当初用的元件——除了无限混凝土，
表里每一只矩阵都有拆解配方。

空闲耗电是每 1M 字节 1 AE/t，所以同一条阶梯从 1K 的约 0.001 AE/t 一直到 4M 的 4 AE/t。无限混凝土矩阵
固定 8 AE/t，和 eco 自己的无限成员一样。

## 我们的 L1 驱动器挂载什么

驱动器只在**自己档位不低于这只盘**的时候才挂载它，所以比我们高一档的矩阵插得进去、面板上也会报容量，
但就是接不进网络。我们的 L1 在最低档，因此 eco 更高档的矩阵放进我们的驱动器只是占一格——这一档定价就在
量子以下，本来不该用来装后期矩阵。上面表里的六族全是我们的 L1，全都正常挂载。至于 eco 自己的驱动器收不
收我们的盘，那是它的规则。

存储侧的那台主机是 L1 存储控制器；另外两个子系统做什么，见 [L1 计算系统](computation_system.md) 与
[L1 合成系统](crafting_system.md)；AE 电力怎么存，见[盈能能源元件](energized_energy_cells.md)。
