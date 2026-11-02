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

**L1 计算系统**是 eco 的 <ItemLink id="neoecoae:computation_system_l4" /> 的 L1 版本：它不存东西，只给 ME 网络的合成系统提供**合成线程与并行数**，线程越多、并行越大，同一时刻能开的合成就越多。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

同一格换成**普通计算接口**也能成型（上图那一格放的是网络版），两种成员的差别见下面"每台贡献多少"那张表。

## 每台贡献多少

| 成员 | 提供 |
|------|------|
| 计算主机 | `cpu_threads` 条合成线程（默认 **2**） |
| 并行核心 | 每块 **24** 并行 |
| 线程核心 | 每块 **2** 条合成线程 |
| 盈能强化计算机核心 | 每块 **+1,024** 并行，**只认控制器正后方那一格**（那一格本来是外壳） |
| 盈能强化线程核心 | 每块 **+16** 条合成线程，**只认离控制器最近的那一格** |

## 部件

除主机与计算接口那两格固定格外，其余成员各自吃 **2 AE/t** 空闲耗电。

## 搭建步骤

1. 放下主机，使其朝外，用外壳搭出周围框架。
2. 在主机**左后方**放计算网络接口。
3. 从主机**右侧**的外壳再往右开始，水平排列超导晶振传输总线。
4. 在传输总线正后方放线程核心，它的上下各放一排计算驱动器。
5. 在线程核心的上下各放一排并行核心。
6. 在传输总线那一排的末端放冷却系统控制器：站在结构右侧、面朝主机放置。
7. 用外壳补齐剩余位置。

加长时，传输总线、驱动器、线程核心与并行核心要一起加。也可以直接用主机界面里的**自动搭建面板**预览与补齐——它给出的就是这套结构的形状权威。

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

