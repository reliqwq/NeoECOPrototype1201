---
navigation:
  title: L1 Processor Assembler
  icon: neoecoprototype1201:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:backup_drive
  - neoecoprototype1201:cable_pattern_provider
  - ae2:silicon
  - neoecoae:crystal_matrix
  - neoecoae:superconducting_processor
  - neoecoae:energized_superconductive_ingot
  - neoecoprototype1201:ec9_processor_assembler
  - neoecoprototype1201:simplify_pattern_provider
  - neoecoprototype1201:simplify_stonecutting_assembler
  - neoecoprototype1201:simplify_structure_expansion_card
  - neoecoprototype1201:simplify_work_order_upgrade_computation_l9
  - neoecoprototype1201:simplify_work_order_upgrade_crafting_l9
  - neoecoprototype1201:simplify_work_order_upgrade_storage_l9
  - neoecoprototype1201:simplify_work_order_grow_computation_l4
  - neoecoprototype1201:simplify_work_order_grow_computation_l6
  - neoecoprototype1201:simplify_work_order_grow_computation_l9
  - neoecoprototype1201:simplify_work_order_grow_crafting_l4
  - neoecoprototype1201:simplify_work_order_grow_crafting_l6
  - neoecoprototype1201:simplify_work_order_grow_crafting_l9
  - neoecoprototype1201:simplify_work_order_grow_f1_crafting
  - neoecoprototype1201:simplify_work_order_grow_l1_storage
  - neoecoprototype1201:simplify_work_order_grow_storage_l4
  - neoecoprototype1201:simplify_work_order_grow_storage_l6
  - neoecoprototype1201:simplify_work_order_grow_storage_l9
---

# L1 Processor Assembler

The **L1 Processor Assembler** is a green-shell version of the <ItemLink id="ae2:molecular_assembler" /> built for saves that do not have an inscriber yet: it crafts the three processors straight from patterns. It runs only this mod's processor assembly recipes; ordinary molecular assembly patterns are refused, and that work belongs to a Molecular Assembler. It is used with the L1 Pattern Provider below.

Its recipe gate and batching rules live in this mod's own block entity; no AE2 mixin is used.

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype1201:simplify_pattern_provider" />
  <ItemIcon id="neoecoprototype1201:cable_pattern_provider" />
</ItemGrid>

The provider also mounts on one face of a cable bus. The two forms convert into each other in a crafting table, one for one.

Both are made in a **crafting table**: the assembler is 1 Molecular Assembler + 4 Trait Casings + 4 Green Crystal Matrices, and the provider is 1 ME Pattern Provider + 4 Quartz Glass + 4 Green Crystal Matrices, giving **2** per craft.

<Row>
  <RecipeFor id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype1201:simplify_pattern_provider" />
</Row>

## Built-in recipes

The three processors (EC9 can derive further recipes from the inscriber, see below):

| Output | Ingredients |
|--------|-------------|
| Logic Processor | silicon + redstone dust + gold ingot |
| Calculation Processor | silicon + redstone dust + certus quartz crystal |
| Engineering Processor | silicon + redstone dust + diamond |

Both machines also make the Superconducting Processor:

| Output | Ingredients |
|--------|-------------|
| <ItemLink id="neoecoae:superconducting_processor" /> | crystal matrix + energized superconductive ingot + silicon |

The other eight:

| Output | Ingredients |
|--------|-------------|
| L1 Powered ME Interface | 2 Crystal Resonance Generators + 1 ME Interface + 1 Superconducting Processor |
| ECOP - Structure Expansion Card | Memory Card + Aluminum Ingot + Green Crystal Matrix |
| Energized Reinforced Threading Core | 2 ECO - CM4A Threading Cores + 1 ECO - C1 Computation Substructure Casing |
| ECO - CE1R Flash Crystal Array | 2 ECO - CE1 Flash Crystal Arrays + 1 ECO - L4 Storage Component |
| Snowball / powder snow bucket | 4 snow blocks → 4 snowballs; 3 snowballs + 1 bucket → powder snow bucket |
| reliqwq's Fumo Doll | the L1 storage host + C1 computation host + F1 crafting host |
| Yang120's Fumo Doll | eco's L4 storage host + C4 computation host + F4 crafting host |
| kouooki's Fumo Doll | pink dye + dried kelp + brown mushroom |

Silicon, gold and diamond are taken by tag, so any mod's version counts.

Only EC9 derives further processor recipes from the inscriber in place - recipes other mods add there need no data file here.

## Speed and batching

L1 has five <ItemLink id="ae2:speed_card" /> slots: one craft takes 100 ticks empty and 20 ticks with five cards. It accepts only 1× patterns and always executes one recipe unit.

EC9 has six speed-card slots and takes **1 tick** with six cards in an adequately powered network. It accepts encoded batches up to `processor_assembler_max_runs_per_cycle` (default **64**, range **1–192**), but never expands a 1× pattern automatically. An oversized batch, or one whose ingredients do not fit the nine-slot grid, is refused whole and its materials stay in the network.
