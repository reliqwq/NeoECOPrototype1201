---
navigation:
  title: L1 计算系统
  icon: neoecoprototype1201:simplify_computation_system
  position: 50
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_computation_system
  - neoecoprototype1201:simplify_computation_casing
  - neoecoprototype1201:simplify_computation_parallel_core
  - neoecoprototype1201:simplify_computation_threading_core
  - neoecoprototype1201:simplify_computation_cooling_controller
  - neoecoprototype1201:simplify_computation_drive
  - neoecoprototype1201:simplify_computation_interface
  - neoecoprototype1201:simplify_computation_network_interface
  - neoecoprototype1201:energized_computation_core
  - neoecoprototype1201:energized_computation_threading_core
  - neoecoprototype1201:energized_computation_cell_4m
  - neoecoprototype1201:simplify_computation_transmitter
  - neoecoprototype1201:simplify_computation_cell_1m
---

# L1 计算系统

**L1 计算系统**是 eco 的 <ItemLink id="neoecoae:computation_system_l4" /> 的 L1 版本：提供计算并行与计算存储。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>


## 每台作用

| 成员 | 提供 |
|------|------|
| 计算主机 | `cpu_threads` 条合成线程（默认 **2**） |
| 并行核心 | 每块 **24** 并行 |
| 线程核心 | 每块 **2** 条合成线程 |
| 盈能强化计算机核心 | 每块 **+1,024** 并行，**只认控制器正后方那一格** |
| 盈能强化线程核心 | 每块 **+16** 条合成线程，**只认离控制器最近的那一格** |

## 部件

其余成员 **2 AE/t** 空闲耗电。

## 搭建步骤

用主机界面里的**自动搭建面板**预览与补齐。

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_computation_system" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_casing" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_parallel_core" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_threading_core" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_cooling_controller" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_drive" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_interface" />
  <ItemIcon id="neoecoprototype1201:simplify_computation_network_interface" />
</ItemGrid>

## 两件盈能强化成员

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_energized.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 物品 | 在哪合成 | 原料 |
|------|----------|------|
| 盈能强化计算机核心 | eco 的集成工作站 | 3 × ECO - C4 可拓展计算子系统主机、8 × 绿晶晶格、4 × 没那么神秘的方块，耗能 66,600 FE |

另外两件（**盈能强化线程核心**、**ECO - CE1R 闪存晶阵**）的原料写在 [L1 处理器装配室](processor_assembler.md) 的出厂配方表里，这里不再重复。

