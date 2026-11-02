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

Neo ECO Prototype brings Neo ECO AE Extension's late-game content forward to **L1 early game**. The three systems (storage / computation / crafting) idle at **1/8** of eco's L4 equivalent.

If you have never used eco: think of these as **longer-buildable AE2 versions with their own power and cooling** - the storage side stands where an <ItemLink id="ae2:drive" /> would, the crafting side is a crafting CPU plus its pattern providers, and the computation side supplies the threads and parallelism autocrafting runs on.

A formed L1 storage subsystem, as one shape: controller at one end, drive, energy cell, vent and one interface along the row, casing everywhere else.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_main.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| What you want | Where to read |
|---------------|----------------|
| Automating processors from patterns | [L1 Processor Assembler](processor_assembler.md) - processors before you have an inscriber |
| Which matrix stores what | [Supported storage matrices](matrix_types.md) |
| Parking a block of charge in the grid | [Energized Energy Cells](energized_energy_cells.md) |
| An interface that powers the grid | [L1 Interfaces](interfaces.md) - powered and superconductive, on one page |
| More crafting throughput | [L1 Computation System](computation_system.md) and [L1 Crafting System](crafting_system.md) |

Both interfaces generate power passively, but AE2 keeps only the highest-output generator per grid - see the [L1 Interfaces](interfaces.md) page.

The combined three-in-one machine is still in early development and is not covered here.
