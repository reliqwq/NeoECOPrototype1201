---
navigation:
  title: Neo ECO Prototype
  position: 200
item_ids:
  - neoecoprototype1201:simplify_storage_casing
  - neoecoprototype1201:simplify_storage_vent
  - neoecoprototype1201:simplify_storage_interface
  - neoecoprototype1201:simplify_storage_network_interface
  - neoecoprototype1201:simplify_green_crystal_matrix
  - neoecoprototype1201:simplify_green_aluminum_casing
  - neoecoprototype1201:chipped_budding_grette
  - neoecoprototype1201:cryotheum_ice_cream_bucket
  - neoecoprototype1201:damaged_budding_grette
  - neoecoprototype1201:dissolution_bucket
  - neoecoprototype1201:flawed_budding_grette
  - neoecoprototype1201:flawless_budding_grette
  - neoecoprototype1201:fumo_doll_kouooki
  - neoecoprototype1201:fumo_doll_reliqwq
  - neoecoprototype1201:fumo_doll_tedxenon
  - neoecoprototype1201:fumo_doll_yang120
  - neoecoprototype1201:fumo_doll_yang120_cpu
  - neoecoprototype1201:fumo_doll_yang120_giant
  - neoecoprototype1201:grette_cluster
  - neoecoprototype1201:grette_crystal
  - neoecoprototype1201:grette_crystal_block
  - neoecoprototype1201:grette_crystal_dust
  - neoecoprototype1201:large_grette_bud
  - neoecoprototype1201:medium_grette_bud
  - neoecoprototype1201:small_grette_bud
  - neoecoprototype1201:cryotheum_ice_cream
  - neoecoprototype1201:cryotheum_ice_cream_block
  - neoecoprototype1201:cryotheum_ore
  - neoecoprototype1201:end_cryotheum_ore
  - neoecoprototype1201:nether_cryotheum_ore
---

# Neo ECO Prototype

Neo ECO Prototype brings Neo ECO AE Extension's late-game content forward to **L1 early game**. The three systems (storage / computation / crafting) idle at **1/8** of eco's L4 equivalent.

If you have never used eco: think of these as **more powerful AE2 versions with built-in energy buffers** - the storage side stands where an <ItemLink id="ae2:drive" /> would, the computation side supplies crafting CPUs and threads, and the crafting side corresponds to the molecular assembler.

A formed L1 storage subsystem, as one shape: controller at one end, drive, energy cell, vent and one interface along the row, casing everywhere else.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_main.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| What you want | Where to read |
|---------------|----------------|
| Automating processors from patterns | [L1 Processor Assembler](processor_assembler.md) - processors before you have an inscriber |
| Which matrix stores what | [Supported storage matrices](matrix_types.md) |
| The full storage multiblock | [L1 Storage System](storage_system.md) - parts, idle draw, build order |
| Grow singularities without IO | [L1 Singularity Cell](singularity_cell.md) - it grows its own stock |
| Few types, nearly unlimited amounts | [Small Bulk Matrices](small_bulk_matrices.md) - needs MEGA Cells, 3 types or 10 |
| Upgrade AE2 blocks to L1 in place | [AE2 to L1 Upgrade Cards](ae2_upgrade_cards.md) - patterns come along |
| Store large amounts of AE power | [Energized Energy Cells](energized_energy_cells.md) |
| An ME interface that partitions items and powers the grid | [L1 Interfaces](interfaces.md) - powered and superconductive, on one page |
| Increase computation parallelism and thread count | [L1 Computation System](computation_system.md) and [L1 Crafting System](crafting_system.md) |
| Upgrade a formed L1 host to an eco version | [Migration Drives](migration_drives.md) |
| Test items | [Creative Helpers](creative_helpers.md) - an endless cell and the pattern generator |

Both interfaces generate power passively, but AE2 keeps only the highest-output generator per grid - see the [L1 Interfaces](interfaces.md) page.

The combined three-in-one machine is still in early development and is not covered here.
