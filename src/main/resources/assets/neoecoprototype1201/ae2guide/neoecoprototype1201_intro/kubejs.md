---
navigation:
  title: KubeJS support
  icon: neoecoprototype1201:simplify_small_bulk_expansion_card
  position: 40
  parent: neoecoprototype1201_intro/index.md
---

# KubeJS support

KubeJS is optional; these exist only when it is installed.

| What you can do | Where |
|-----------------|-------|
| Register custom storage matrices | Startup scripts, two builder types: `neoecoprototype:storage_matrix` and `neoecoprototype:infinite_storage_matrix` - capacity, type limit, material and bound resource are all settable |
| Make a cell that grows its own stock | Same builder, `.grows(perBatch, ticksPerBatch)`: it hands out the bound item/fluid/chemical every `ticksPerBatch` world ticks and takes nothing in |
| Add, change or remove this addon's recipes | Server scripts; the processor assembler's recipes can be removed by field or added as raw JSON |

The two script kinds run at different moments: **startup scripts** run when the game starts, and registering a matrix has to happen there (before the registries freeze); **server scripts** re-run every time the server loads, which is what recipe changes use. Full examples live in this mod's repository under `docs/kubejs.md` and `docs/kubejs-example.js`.
