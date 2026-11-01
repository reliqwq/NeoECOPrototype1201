---
navigation:
  title: Energized Energy Cells
  icon: neoecoprototype1201:energized_energy_cell_16g
  position: 23
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:energized_energy_cell_16g
  - neoecoprototype1201:energized_energy_cell_512g
---

# Energized Energy Cells

<Row>
  <BlockImage id="neoecoprototype1201:energized_energy_cell_16g" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype1201:energized_energy_cell_512g" scale="3"></BlockImage>
</Row>

The **Energized Energy Cells** are two standalone batteries. Like eco's <ItemLink id="neoecoae:energy_cell_l4" /> family they add stored power to the network, but they are not a member of any multiblock: place one and it is a storage node on the grid, idling at 0, charging and discharging.

Touching is enough - no cable has to run through it. Two of them side by side form a grid of their own, which is what the game test measures.

| Cell | Capacity (AE) | Ingredients |
|------|---------------|-----------|
| <ItemLink id="neoecoprototype1201:energized_energy_cell_16g" /> | 16,000,000,000 | 8 ECO - LT9 High Density Energy Cells + 32 Energized Superconductive Blocks + 64 Green Crystal Matrices |
| <ItemLink id="neoecoprototype1201:energized_energy_cell_512g" /> | 256,000,000,000 | 8 of the 16G cells + 1 Energized Superconductive Block |

Both are made in eco's <ItemLink id="neoecoae:integrated_working_station" /> and each costs 100,000 FE. Station recipes cannot be drawn on this page, so the ingredients are in the table.

Breaking one carries its charge with it - the "Stored Energy" line on its tooltip is that figure - and placing it again resumes from there.

The second cell's item id still reads `energized_energy_cell_512g` while its capacity is 256G, which matters for any script that looks it up by id.
