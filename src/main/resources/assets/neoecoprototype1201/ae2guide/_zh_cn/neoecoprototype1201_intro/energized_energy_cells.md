---
navigation:
  title: 盈能能源元件
  icon: neoecoprototype1201:energized_energy_cell_16g
  position: 23
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:energized_energy_cell_16g
  - neoecoprototype1201:energized_energy_cell_512g
---

# 盈能能源元件

<Row>
  <BlockImage id="neoecoprototype1201:energized_energy_cell_16g" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype1201:energized_energy_cell_512g" scale="3"></BlockImage>
</Row>

**盈能能源元件**是两块独立电池：与 eco 的<ItemLink id="neoecoae:energy_cell_l4" />那一族一样往网络里存电，但它不是任何多方块的成员——放下去自己就是网格上的一个储能节点，空闲耗电 0，网络既能往里灌也能从里取。

接入方式是**贴着的就算**：不需要线缆穿过它，两台并排放下就会连成同一张网（这一条是套件实测的读数）。

| 元件 | 容量（AE） | 合成原料 |
|------|------------|----------|
| <ItemLink id="neoecoprototype1201:energized_energy_cell_16g" /> | 16,000,000,000 | 8 只 ECO - LT9 高密度能量元件 + 32 个盈能超导块 + 64 个绿晶晶格 |
| <ItemLink id="neoecoprototype1201:energized_energy_cell_512g" /> | 256,000,000,000 | 8 只 16G + 1 个盈能超导块 |

两台都在 eco 的 <ItemLink id="neoecoae:integrated_working_station" /> 里做，各耗能 100,000 FE；工作站的配方图画不到这一页上，所以原料写在表里。

拆下来会把当时的电量带走——悬浮提示里"已存储能源"那一行就是它，重新放下接着用。

第二只的物品 id 仍写作 `energized_energy_cell_512g`，而它的容量是 256G——按 id 抓东西的脚本要知道这一条。
