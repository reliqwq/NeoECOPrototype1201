---
navigation:
  title: L1 Crafting System
  icon: neoecoprototype1201:simplify_crafting_system
  position: 60
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_crafting_system
  - neoecoprototype1201:simplify_crafting_casing
  - neoecoprototype1201:simplify_crafting_parallel_core
  - neoecoprototype1201:simplify_crafting_pattern_bus
  - neoecoprototype1201:simplify_crafting_worker
  - neoecoprototype1201:simplify_crafting_vent
  - neoecoprototype1201:simplify_crafting_interface
  - neoecoprototype1201:simplify_crafting_network_interface
  - neoecoprototype1201:simplify_fluid_input_hatch
  - neoecoprototype1201:simplify_fluid_output_hatch
---

# L1 Crafting System

The **L1 Crafting System** is the L1 version of eco's <ItemLink id="neoecoae:crafting_system_l4" />: put patterns in the bus and the worker cores execute the jobs the network hands over. It idles at **1/8** of eco's L4 equivalent. This machine executes; the computation system decides how many threads exist - **the two numbers are counted separately and neither adds to the other**. The image above is the smallest working shape.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

The same build turned the other way - same members, same counts, only the host faces west instead of
south (`mirrored` is false in both pictures):

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_other_end.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| Aspect | Value |
|--------|-------|
| Parallelism | **16** |
| Overclocked | **32** |
| Against the computation side | counted separately: this machine supplies 16 / 32, the computation system supplies threads and its own accelerators |

## Parts

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype1201:simplify_crafting_network_interface" />
  <ItemIcon id="neoecoprototype1201:simplify_fluid_input_hatch" />
  <ItemIcon id="neoecoprototype1201:simplify_fluid_output_hatch" />
</ItemGrid>

| Part | Holds | What it does | Idle draw | What its absence costs |
|------|-------|--------------|-----------|------------------------|
| Crafting host | - | Fixes the tier at L1; its front face is the control face | 4 AE/t | The structure never forms |
| F1 Worker Core | - | The slot that actually performs a craft | 4 AE/t | One lane less |
| Parallel core | - | Supplies parallel lanes | 4 AE/t | Fewer lanes |
| F1 smart pattern bus | crafting patterns | Where patterns live; the network reads patterns from here | 2 AE/t | That column has nothing to run |
| Crafting vent | - | Appearance and an idle draw, that is all - **there is no heat or shutdown mechanic** | 4 AE/t | 4 AE/t less, no function lost |
| F1 crafting fluid input hatch | coolant | Fixed slot: **takes in the fluid active cooling needs** - not recipe fluids | 2 AE/t | The structure never forms |
| F1 crafting fluid output hatch | - | Fixed slot: drains the spent coolant out | 2 AE/t | The structure never forms |
| Crafting interface | - | Fixed slot: invisible once formed, hands the cluster to outside ME cables | 2 AE/t | The structure never forms |
| Crafting network interface | - | The one the player opens, with eco's interface UI | 2 AE/t | No GUI; nothing else changes |
| Crafting casing | - | Fills out the frame | 2 AE/t | The structure never forms |

## Building it

1. Place the host facing out, then frame the rest with casings.
2. Put the crafting network interface - the one you can open - **behind and to the left of the host**, with the fluid input hatch above it and the output hatch below.
3. Starting one block right of the casing on the host's right side, lay F1 Worker Cores horizontally.
4. Put a row of parallel cores above and below those workers, and the crafting vent directly behind them.
5. Put a row of smart pattern buses above and below that vent.
6. Fill the remaining positions with casings.

Lengthening the structure means adding workers, parallel cores, pattern buses and vents together. If you would rather not place blocks one by one, use the **auto-build panel** in the host's GUI - what it plans is the authoritative shape, and those six steps are the order it lays them out.

## Overclocking and cooling

The overclock and active-cooling switches, and what coolant tier they demand, are eco's own rules - see eco's crafting system page. This tier's two numbers are **16** (overclock off) and **32** (on). The coolant level has one extra reader on our side: the crafting host's **machine mind** uses it to decide how much idle power to save, so a fuller tank changes the standby figure, not whether overclocking is allowed.
