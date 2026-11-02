---
navigation:
  title: L1 合成系统
  icon: neoecoprototype1201:simplify_crafting_system
  position: 60
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_crafting_system
  - neoecoprototype1201:simplify_crafting_casing
  - neoecoprototype1201:simplify_crafting_parallel_core
  - neoecoprototype1201:simplify_crafting_pattern_bus
  - neoecoprototype1201:simplify_crafting_worker
  - neoecoprototype1201:simplify_crafting_vent
  - neoecoprototype1201:simplify_crafting_interface
  - neoecoprototype1201:simplify_crafting_network_interface
  - neoecoprototype1201:simplify_fluid_input_hatch
  - neoecoprototype1201:simplify_fluid_output_hatch
---

# L1 合成系统

**L1 合成系统**是 eco 的 <ItemLink id="neoecoae:crafting_system_l4" /> 的 L1 版本：把合成样板装进总线，工作核心就并行执行网络交来的合成任务，耗电是 eco L4 同级的 **1/8**。它管执行，计算系统管能给多少线程——**两边的数字各算各的，谁也不给对面加数**。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

同一台机器换个朝向还是它：成员、数量都不变，只是主机的 `facing` 从 south 变成 west（存出来的结构里 `mirrored` 两种摆法都是 false）。

| 项目 | 数值 |
|------|------|
| 并行 | **16** |
| 开超频后 | **32** |
| 与计算侧的关系 | 各算各的：这一台给 16 / 32，计算系统给线程数与它的加速器数 |

## 部件

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_network_interface" />
  <ItemIcon id="neoecoprototype1201:simplify_fluid_input_hatch" />
  <ItemIcon id="neoecoprototype1201:simplify_fluid_output_hatch" />
</ItemGrid>

| 部件 | 空闲耗电 |
|------|----------|
| 合成主机 | 4 AE/t |
| F1 工作核心 | 4 AE/t |
| 并行核心 | 4 AE/t |
| 智能样板总线 | 2 AE/t |
| 合成通风口 | 4 AE/t |
| 合成流体输入仓 | 2 AE/t |
| 合成流体输出仓 | 2 AE/t |
| 合成接口 | 2 AE/t |
| 合成网络接口 | 2 AE/t |
| 合成外壳 | 2 AE/t |

## 搭建步骤

1. 放下主机，使其朝外，用外壳搭出周围框架。
2. 在主机**左后方**放合成网络接口（玩家能打开的那一块），它上方放流体输入仓、下方放流体输出仓。
3. 从主机**右侧**的外壳再往右开始，水平排列 F1 工作核心。
4. 在工作核心的上下各放一排并行核心，正后方放合成通风口。
5. 在通风口的上下各放一排智能样板总线。
6. 用外壳补齐剩余结构。

加长时，工作核心、并行核心、样板总线与通风口要一起加。不想逐块摆就用主机界面里的**自动搭建面板**：它给出的就是这套结构的形状权威，上面六步就是它排出来的顺序。

## 超频与冷却

超频与主动冷却的开关、冷却液等级要求都沿用 eco 那一套，规则看 eco 自己的合成系统页；这一档的两个数是 **16**（不开超频）与 **32**（开超频）。冷却液位在我们这里还有一层用处：合成主机的**机魂**读它来决定空闲耗电省多少（装了 Jade 时，有脸的主机会把心情与空闲耗电直接写在提示里），所以冷却液满不满会改变待机数字，但不改变能不能超频——后者归 eco 的规则管。
