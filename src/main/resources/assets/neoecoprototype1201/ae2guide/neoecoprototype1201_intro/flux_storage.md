---
navigation:
  title: FE Storage Matrices
  icon: neoecoprototype1201:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_fe_cell_housing
  - neoecoprototype1201:simplify_fe_storage_cell_1k
  - neoecoprototype1201:simplify_fe_storage_cell_1m
  - neoecoprototype1201:simplify_fe_storage_cell_4m
---

# FE Storage Matrices

FE storage matrices store **Forge Energy (FE) as a resource in the ME network** - they behave like the item and fluid families, only the contents are power. They need **AppliedFlux**.

## Capacity

Capacity is counted in **bytes** - AE2's storage allowance per cell, not the 8-bit kind. What a byte is worth in FE is AppliedFlux's own `flux_cell.amount`, 1,048,576 FE per byte out of the box.

| Matrix | Bytes | At the default rate |
| --- | --- | --- |
| ECO - L1 1K Storage Matrix (FE) | 1,024 | about 1.07 billion FE |
| ECO - L1 1M Storage Matrix (FE) | 1,048,576 | about 1.1 trillion FE |
| ECO - L1 4M Storage Matrix (FE) | 4,194,304 | about 4.4 trillion FE |

Raising `flux_cell.amount` scales every FE cell in the game up in proportion, ours included, and costs no materials.

## Recipe

> This family **is only registered when AppliedFlux is installed**, so this page carries neither icons nor recipe boxes for it: a tag that cannot resolve an item does not leave a gap, it writes a line of red text onto the page. With AppliedFlux installed all four are in JEI.

- **ECO - L1 Storage Matrix Housing (FE)**: shaped `ABA / B B / CCC` - 2 Green Crystal Matrix + 3 Redstone + 3 `appflux:harden_insulating_resin`.
- **ECO - L1 1K Storage Matrix (FE)**: the housing plus AE2's 1k component, shapeless.
- **ECO - L1 1M Storage Matrix (FE)**: the housing plus an ECO - L1 Storage Component, shapeless.
- **ECO - L1 4M Storage Matrix (FE)**: the housing plus an ECO - L4 Storage Component, shapeless.

A disassembly recipe gives the housing and the component back.

## Where it fits

They belong to the L1 tier, so [the L1 storage system](storage_system.md) mounts them without any whitelist entry, and eco's higher tiers do too - the tier is what decides which host accepts a cell. In the L1 storage host panel each gets its own row, named after eco's own FE cell type ("energy") - the same name it carries inside eco's own hosts.
