---
navigation:
  title: L1 Storage System
  icon: neoecoprototype1201:simplify_storage_controller
  position: 10
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_storage_controller
  - neoecoprototype1201:simplify_drive
  - neoecoprototype1201:simplify_energy_cell
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
---

# L1 Storage System

The L1 Storage System is an extensible multiblock that does the job of AE2's <ItemLink id="ae2:drive" /> - it mounts storage matrices into the ME network - but it carries its own energy buffer, and it brings eco's L4 shape forward to the L1 tier. It idles at **1/8** of what eco's L4 equivalent costs.

Which matrix families fit in this drive, and what each one's capacity and type limit is, is laid out on one page: [Supported storage matrices](matrix_types.md).

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_main.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Parts

| Part | What it does | Idle draw |
|------|--------------|-----------|
| Storage host | Fixes the tier at L1; **its front face is the control face** | 32.25 AE/t |
| Storage matrix drive | one storage matrix; insert a cell by right-clicking, take it out sneak-right-clicking empty-handed | 32 AE/t each |
| Energy cell | Buffers **100,000 AE** for the structure and shares it with the network | 0 |
| Storage interface | Turns invisible once formed and hands the cluster's grid node to outside ME cables | 2 AE/t |
| Storage network interface | The one the player opens: same cluster behaviour plus eco's interface UI | 2 AE/t |
| Storage vent | Appearance and an idle draw, that is all | 4 AE/t |
| Storage casing | Fills out the frame | 2 AE/t |

Members not named above draw **2 AE/t** - that is eco's shared 16 AE/t component baseline, scaled to L1. The host's **32.25 AE/t** is not a typo: eco's baseline for a storage host is 258 AE/t, and L1 scales it by 1/8.

## Building it

Same shape as eco's own storage system - frame the host, mount drives in rows, keep an energy cell in the structure, fill the rest with casings.

## Which matrix

| What to store | Matrix family | Needs |
|---------------|---------------|-------|
| Items, fluids, chemicals (1K - 4M) | item / fluid / chemical storage matrices | chemicals also need Mekanism and Applied Mekanistics |
| A few resources, each nearly unlimited | [Small Bulk Matrices](small_bulk_matrices.md) | MEGA Cells |
| Many resources at once | universal / quantum / complex universal matrices (1K / 1M) | AE2 Omni Cells |
| One item, infinitely | infinite matrix (`concrete`) / KubeJS infinite matrix | - |
| FE | FE storage matrices - on the [matrix page](matrix_types.md) | AppliedFlux |
| Singularities, no Matter Condenser build | [L1 Singularity Cell](singularity_cell.md) | - |

Partition a matrix in AE2's cell workbench to give it a whitelist. The singularity cell is the exception: it holds only what it grows itself and cannot be partitioned.

## Other mods' cells

Mounting follows the tier: a drive mounts cells whose tier is at or below the host's own. Everything here is L1, so eco's higher-tier cells insert but never mount - their capacity would show on the panel and nothing would reach the network.
