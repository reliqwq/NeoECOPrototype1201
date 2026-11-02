---
navigation:
  title: L1 Interfaces
  icon: neoecoprototype1201:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_powered_me_interface
  - neoecoprototype1201:superconductive_interface
---

# L1 Interfaces

<Row>
  <BlockImage id="neoecoprototype1201:simplify_powered_me_interface" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype1201:superconductive_interface" scale="3"></BlockImage>
</Row>

Both interfaces are AE2's <ItemLink id="ae2:interface" /> with a power answer attached: same nine config
slots, same nine stock slots, the same GUI, the same partitioning rules. Anywhere an ME Interface works,
either of these works. They differ in two numbers.

| | L1 Powered ME Interface | Energized Superconductive Interface |
|---|---|---|
| Config / stock slots | 9 / 9 | 9 / 9 |
| One marker holds | AE2's own amount | **8,192** items - and for fluids and chemicals **128 times** AE2's own per-marker amount |
| Passive output | **200 AE/t** | **4,000 AE/t** |
| Idle draw | 0 AE/t | 0 AE/t |
| Built in | the [L1 Processor Assembler](processor_assembler.md) | eco's <ItemLink id="neoecoae:integrated_working_station" />, 10,000 FE |

The widening stops at the marker, not at the slot count, on purpose: this line opens AE2's own interface
panel, and widening the count without widening the panel would hide the extra slots.

Both are passive generators, and AE2 keeps only the highest-output generator per grid. 4,000 AE/t is
usually the strongest thing in an early network, so one superconductive interface takes over the grid's
passive generation and the powered ones stand down beside it.

Cards: each takes **one crafting card and one fuzzy card**, exactly like AE2's own interface, and with
AppliedFlux installed each also takes **one induction card** - appflux 1.20.1 has no Chinese name for it,
so the item reads as "Induction Card" in a zh client; its own tooltip says it lets AE devices accept or
send energy. AE2's card tooltip folds our two blocks into its interface line rather than naming them
separately.

Recipes, written out because both are custom recipe types that the recipe viewer cannot render here:
the powered interface is 2 <ItemLink id="ae2:crystal_resonance_generator" /> + 1
<ItemLink id="ae2:interface" /> + 1 <ItemLink id="neoecoae:superconducting_processor" /> in the assembler,
and four of those make one superconductive interface, which itself costs 1 Cryotheum Crystal,
9 Energized Superconductive Ingots and 4 Superconducting Processors on top of those four.
