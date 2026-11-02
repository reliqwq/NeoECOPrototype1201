---
navigation:
  title: Small Bulk Matrices
  icon: neoecoprototype1201:simplify_small_bulk_storage_cell
  position: 20
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_small_bulk_storage_cell
  - neoecoprototype1201:simplify_small_bulk_storage_cell_expanded
  - neoecoprototype1201:simplify_small_bulk_fluid_storage_cell
  - neoecoprototype1201:simplify_small_bulk_fluid_storage_cell_expanded
  - neoecoprototype1201:simplify_small_bulk_expansion_card
  - neoecoprototype1201:simplify_small_bulk_storage_matrix_housing
  - neoecoprototype1201:simplify_small_bulk_fluid_storage_matrix_housing
---

# Small Bulk Matrices

The **Small Bulk Matrix** is the extreme member of the <ItemLink id="ae2:drive" /> family: an ordinary matrix holds many types in limited amounts, this one holds very few types in nearly unlimited amounts. **3** types by default, **10** after an expansion card.

| Aspect | Value |
|--------|-------|
| Type limit | 3, or 10 with the expansion card |
| Amount per type | the cell reports **9,223,372,036,854,775,807** bytes - a long at its top, shown as 9.2E in a terminal - and you will not reach it |
| Needs | MEGA Cells (2.4.6 is the 1.20.1 build measured on this line). The chemical variant is not on this line yet |

## Partitioning

Partitioning is a cell whitelist: only the types you allow enter the cell, everything else passes by. Edit it in AE2's cell workbench - a base cell shows 3 slots there, an expanded one 10 - and the fluid cell partitions against the fluid key type.

## Going to 10 kinds

Put the cell and the **expansion card** together in a crafting table and the expanded cell comes out. Nothing is returned alongside it: the card is spent, and the 1M component stays inside the cell. The card itself is also a crafting-table recipe: 4 Green Crystal Matrices + 4 MEGA Cells 1M components + 1 Energized Superconductive Ingot.

## Taking it apart

Both base cells - item and fluid - disassemble in a crafting table into their own housing plus one MEGA Cells 1M component.
> **Warning:** **neither expanded cell has a disassembly recipe.** Once the card is spent the cell cannot be taken back apart into housing and component, so decide before you craft it.

## What belongs to this family

> These items **only exist when MEGA Cells is installed**. Without it they are never registered, so this page lists names instead of icons: an icon tag that cannot resolve an item does not render blank, it writes a line of red text onto the page. With MEGA Cells installed, the icons and recipes of this family are all in JEI.

| Name | Which one it is |
| --- | --- |
| ECO - L1 Small Bulk Storage Cell (3 Types) | item cell, base |
| ECO - L1 Small Bulk Storage Cell (10 Types) | item cell, the card's result |
| ECO - L1 Small Bulk Fluid Storage Matrix (3 Types) | fluid cell, base |
| ECO - L1 Small Bulk Fluid Storage Matrix (10 Types) | fluid cell, the card's result |
| L1 Small Bulk Expansion Card | takes 3 types to 10; recipe in the section above |
| ECO - Small Bulk Storage Matrix Housing | housing of the item cell |
| ECO - L1 Small Bulk Fluid Storage Matrix Housing | housing of the fluid cell |
