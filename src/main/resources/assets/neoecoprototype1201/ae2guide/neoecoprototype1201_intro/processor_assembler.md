---
navigation:
  title: L1 Processor Assembler
  icon: neoecoprototype1201:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:backup_drive
  - neoecoprototype1201:cable_pattern_provider
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

The three processors (the rest are derived from the inscriber, see below):

| Output | Ingredients |
|--------|-------------|
| Logic Processor | silicon + redstone dust + gold ingot |
| Calculation Processor | silicon + redstone dust + certus quartz crystal |
| Engineering Processor | silicon + redstone dust + diamond |

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


## Speed and batching

5 card slots take nothing but the <ItemLink id="ae2:speed_card" />: one craft costs 100 ticks empty and 20 ticks with five cards in.

However many complete sets a push carries, they finish in a **single** cycle - output scales with the multiplier, time does not - so more cards are the only way to go faster. The assembler has a nine-slot grid, and one batch has to fit into those nine slots at its own stack sizes; a pattern that needs **more than nine slots** is refused - the materials go back to the network and the log names the offending line. For a processor the ceiling is 3 ingredients x 64 per stack = **192×**. One stack of the output reaches the machine per round; the rest is inserted into the network rather than lost.

## The EC9: the family's volume half

Additionally supports recipes derived from the inscriber.

It is built from a basic assembler, six green crystal matrices and a CE1R flash crystal array.

<RecipeFor id="neoecoprototype1201:ec9_processor_assembler" />
