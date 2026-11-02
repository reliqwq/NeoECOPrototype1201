---
navigation:
  title: Neo ECO Prototype
  position: 200
item_ids:
  - neoecoprototype1201:simplify_storage_controller
  - neoecoprototype1201:simplify_storage_casing
  - neoecoprototype1201:simplify_storage_vent
  - neoecoprototype1201:simplify_drive
  - neoecoprototype1201:simplify_energy_cell
  - neoecoprototype1201:simplify_storage_interface
  - neoecoprototype1201:simplify_storage_network_interface
  - neoecoprototype1201:simplify_green_crystal_matrix
  - neoecoprototype1201:simplify_green_aluminum_casing
---

# Neo ECO Prototype

Neo ECO Prototype 把 Neo ECO AE Extension 的后期内容带到 **L1 前期**。三大系统（存储 / 计算 / 合成）的耗电是 eco L4 同级的 **1/8**。

没玩过 eco 也可以读：这三套东西就是**能建得更长、自带能量与冷却的 AE2 版本**——存储对应<ItemLink id="ae2:drive" />，合成对应合成 CPU 与样板供应器，计算负责给自动合成提供线程与并行。

一台成型的 L1 存储子系统长这样：一头是控制器，同排挂着驱动器、能量单元、散热口与一只接口，其余位置都是外壳。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_main.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 想做什么 | 看哪一页 |
|----------|----------|
| 用样板自动化处理器 | [L1 处理器装配室](processor_assembler.md)（前期没有压印器也能做出处理器） |
| 哪种矩阵存什么 | [支持的存储矩阵](matrix_types.md) |
| 把一整块电量存进网格 | [盈能能源元件](energized_energy_cells.md) |
| 一台既分区又供电的接口 | [L1 接口](interfaces.md)（供能与超导两台写在同一页） |
| 提升合成吞吐 | [L1 计算系统](computation_system.md) 与 [L1 合成系统](crafting_system.md) |

两台接口都自带被动发电，但 AE2 每个网格只保留输出功率最高的那台——细节见 [L1 接口](interfaces.md) 那一页。

三合一联合体仍在早期开发，未收进本指南。
