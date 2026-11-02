---
navigation:
  title: Supported storage matrices
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
---

# Supported storage matrices

One page comparing every matrix this addon registers on the **1.20.1 line**: pick the family by what you
need to store, then compare the capacity steps, how many things fit at once, and which mod the family
requires.

## The families

| Family | Capacity steps | Things at once | Requires |
|--------|----------------|----------------|----------|
| Item | 1K, 16K, 64K, 1M, 4M | 256 | - |
| Fluid | 1K, 16K, 64K, 1M, 4M | 256 | - |
| Chemical | 1K, 16K, 64K, 1M, 4M | 25 | Mekanism + Applied Mekanistics |
| FE (Forge Energy) | 1K, 1M, 4M | 1 | AppliedFlux |
| Infinite concrete | unbounded | 16 | - |
| Pigmee | one 2,560-byte step | 256 | - |

- The 1K / 16K / 64K / 1M / 4M figures count **bytes**, not resources. Each type slot costs 1/256th of
  the cell, so a 1K matrix gives one thing 4 bytes and a 4M matrix gives the same thing 16384 bytes. How
  much of a resource a byte is worth depends on the medium: AE2's own numbers, unchanged here.
- A chemical matrix's 25 is eco's own number, not ours: 20.5.3 builds its Mekanism panel row with 25 types
  and its own chemical cells use the same. The byte layout stays the one this table's other families use.
- An FE matrix stores one resource by design - the family exists to park Forge Energy in the grid, next
  to the <ItemLink id="ae2:energy_cell" /> rather than next to a storage drawer. AppliedFlux also needs
  **Glodium** to load, so this family only appears in a pack that ships both.
- The infinite concrete matrix keeps one resource, in any of the 16 vanilla concrete colours, and never
  runs out. Its housing is the concrete one - sand and gravel around a green crystal matrix - and the cell
  is still that housing plus a 4M component. It is the one matrix in this table that **cannot be taken
  apart**: compression art is not a thing you un-compress.
- The Pigmee matrix is a 2,560-byte item cell - smaller than a 1K, on purpose. Its housing is a sword:
  it deals damage, swings at sword speed and reaches two blocks further than your hand.

## How they are built

Every family is **housing + capacity component**, the same shape AE2 uses: 1K, 16K and 64K take AE2's own
components, while 1M and 4M take ours (<ItemLink id="neoecoprototype1201:simplify_storage_component_1m" />
and <ItemLink id="neoecoprototype1201:simplify_storage_component_4m" />), because AE2's ladder stops at
256k. Taking a matrix apart is a crafting-table recipe: put the matrix alone in any slot and it returns
the housing and the components it was made from - every matrix here has one except the infinite concrete.

Idle draw is one AE/t per 1M of bytes, so the same ladder runs from about 0.001 AE/t at 1K to 4 AE/t at
4M. The infinite concrete matrix draws 8 AE/t, like eco's own infinite members.

## What our L1 drive mounts

A drive mounts a cell only while its own tier compares **at least** the cell's, so a matrix from a tier
above ours can be inserted, shows its capacity on the panel, and never reaches the network. Our L1 sits at
the bottom tier, so eco's higher-tier matrices are dead weight in one of our drives - this tier is priced
below quantum on purpose and is not meant to host late-game matrices. Every matrix in the table above is
ours and is L1, so all six mount. Whether eco's own drives accept ours is its rule to decide.

The storage side that hosts these matrices is the L1 storage controller; see the
[L1 Computation System](computation_system.md) and [L1 Crafting System](crafting_system.md) pages for
what the other two subsystems do, and [Energized Energy Cells](energized_energy_cells.md) for AE power.
