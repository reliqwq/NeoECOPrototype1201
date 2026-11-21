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
  - neoecoprototype1201:simplify_universal_storage_matrix_housing
  - neoecoprototype1201:simplify_universal_storage_cell_1k
  - neoecoprototype1201:simplify_universal_storage_cell_1m
  - neoecoprototype1201:simplify_quantum_storage_matrix_housing
  - neoecoprototype1201:simplify_quantum_storage_cell_1k
  - neoecoprototype1201:simplify_quantum_storage_cell_1m
  - neoecoprototype1201:simplify_complex_universal_storage_matrix_housing
  - neoecoprototype1201:simplify_complex_universal_storage_cell_1k
  - neoecoprototype1201:simplify_complex_universal_storage_cell_1m
  - neoecoprototype1201:simplify_singularity_cell
  - neoecoprototype1201:simplify_sky_stone_dust_cell
  - neoecoprototype1201:simplify_source_growing_cell
  - neoecoprototype1201:simplify_source_storage_matrix_housing
  - neoecoprototype1201:simplify_source_storage_cell_1k
  - neoecoprototype1201:simplify_source_storage_cell_16k
  - neoecoprototype1201:simplify_source_storage_cell_64k
  - neoecoprototype1201:simplify_source_storage_cell_1m
  - neoecoprototype1201:simplify_source_storage_cell_4m
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
| Small bulk (item, fluid) | one rung, nearly unlimited bytes per type | 3, or 10 with the expansion card | MEGA Cells - [the whole page](small_bulk_matrices.md) |
| Universal (omni) | 1K, 1M | 512 | AE2 Omni Cells |
| Quantum | 1K, 1M | unlimited | AE2 Omni Cells |
| Complex universal | 1K, 1M | 1024 | AE2 Omni Cells |
| Spent nuclear waste | 1K, 1M | 1 | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| Singularity (self-growing) | unbounded bytes | 1 | - |
| Sky stone dust (self-growing) | unbounded bytes | 1 | - |
| Source (self-growing) | unbounded bytes | 1 | Ars Énergistique (pulls Ars Nouveau) |
| Source | 1K, 16K, 64K, 1M, 4M | 256 | Ars Énergistique (pulls Ars Nouveau) |

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
- The four **omni families** are this addon's high-capacity step, and they need AE2 Omni Cells: the
- The **singularity family** and **source** are cells that grow their own stock: the singularity cell
  produces 120 singularities per 600 ticks, the sky-stone-dust cell grows AE2's sky dust, and the source
  growing cell produces source - none of them take any input, and the stock is computed from the world
  clock from the moment the cell is mounted. The five source rungs assemble with AE2's own components -
  256 types at 1/256 of the capacity each, byte-for-byte the item and fluid ladders. The source family
  needs Ars Énergistique (which pulls Ars Nouveau); a pack without it never sees these.
  matrices themselves run on eco's own omni storage backend, exactly like eco's LE4/LE6/L9 omni
  matrices, so a pack without that mod simply never sees them. The universal matrix holds 512 things
  at once and the quantum one has no type limit at all - its 1M rung is worth half again the universal
  1M on purpose. The complex universal trades type budget for a bigger 1M. The spent nuclear waste
  matrix holds exactly one substance - Mekanism's spent nuclear waste - and exists so that byproduct
  has somewhere to live at L1; it needs both Mekanism and Applied Mekanistics next to Omni Cells.

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
