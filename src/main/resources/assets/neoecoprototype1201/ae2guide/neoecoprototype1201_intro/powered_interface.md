---
navigation:
  title: L1 Powered ME Interface
  icon: neoecoprototype1201:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_powered_me_interface
---

# L1 Powered ME Interface

<Row>
  <BlockImage id="neoecoprototype1201:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

The **L1 Powered ME Interface** is AE2's <ItemLink id="ae2:interface" /> with one addition: it feeds the
network. Same nine config slots and nine stock slots, the same GUI, the same partitioning rules - anywhere
an ME Interface works, this one works.

It idles at **0 AE/t**. Given power and a channel it outputs **200 AE/t**. AE2 keeps only the
highest-output passive generator per grid, so it stands down while a stronger source is present.

Cards: it takes **one crafting card and one fuzzy card**, like AE2's own interface, and with AppliedFlux
installed it also takes **one induction card**, which is what lets it draw FE from a pipe instead of only
AE. AE2's card tooltip groups it with the interfaces rather than naming it separately.

On this line it has no cable-part twin, and the marker count stays at AE2's nine on purpose: the machine
opens AE2's own interface panel, and widening the count without widening the panel would hide the extra
slots.

Four ingredients, assembled in the [L1 Processor Assembler](processor_assembler.md): two
<ItemLink id="ae2:crystal_resonance_generator" />, one <ItemLink id="ae2:interface" /> and one
<ItemLink id="neoecoae:superconducting_processor" />. Four of these craft one
[Energized Superconductive Interface](superconductive_interface.md).
