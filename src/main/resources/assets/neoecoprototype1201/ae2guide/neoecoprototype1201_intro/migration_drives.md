---
navigation:
  title: Migration Drives
  icon: neoecoprototype1201:migration_drive_1_4
  position: 95
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:migration_drive_1_4
  - neoecoprototype1201:migration_drive_4_6
  - neoecoprototype1201:migration_drive_6_9
  - neoecoprototype1201:migration_drive_creative
  - neoecoprototype1201:migration_drive_empty
---

# Migration Drives

A drive takes **one finished host that already spans its full length** up a rung of the tier ladder, where
it stands. Shift-right-click the host block. It is not a builder: the machine has to be formed and full
length first, and nothing about its shape changes.

<Row>
  <ItemImage id="neoecoprototype1201:migration_drive_1_4" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_4_6" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_6_9" scale="3"></ItemImage>
  <ItemImage id="neoecoprototype1201:migration_drive_creative" scale="3"></ItemImage>
</Row>

| Drive | From | To | Name colour |
|-------|------|----|-------------|
| <ItemLink id="neoecoprototype1201:migration_drive_1_4" /> | this addon's L1 storage / computation / crafting host | eco's L4 of the same family | green, the L1 band |
| <ItemLink id="neoecoprototype1201:migration_drive_4_6" /> | eco L4 | eco L6 | blue, the L4 band |
| <ItemLink id="neoecoprototype1201:migration_drive_6_9" /> | eco L6 | eco L9 | orange, the L6 band |
| <ItemLink id="neoecoprototype1201:migration_drive_creative" /> | L1, L4 or L6 | eco L9 of that family in one step | purple, the L9 band |

## What the swap does with your stuff

Mounted storage matrices and stored patterns are preserved either way: a matrix returns to the same drive
slot, and a pattern to the same bus and slot. This never changes when block refunds are disabled.

Replaced blocks are **not returned by default**. Enable `migration_drive.refund_replaced_blocks` in the
server config to collect the old casings, drives, vents, cells and interfaces in your inventory, or on the
floor if it is full. The success message states which behavior is active.

The one thing that does not survive is **the interface's own configuration**: eco only exposes its interface
slots for reading, so after a swap that cell has to be set up again. The success message says so on the spot.
This is an accepted cost, not a bug to re-fix.

The drive itself is consumed and comes back as the empty shell - <ItemImage id="neoecoprototype1201:migration_drive_empty" />,
which has no colour on purpose, so an empty disk cannot be read as a usable one.

## How you get one

All four drives are crafted in eco's integrated working station: make the empty shell from a structure
expansion card and an AE2 1K component, upgrade it one rung at a time, and combine the first three drives
into the creative drive. The creative tab and admin commands also provide them directly.

<RecipeFor id="neoecoprototype1201:migration_drive_empty" />
<RecipeFor id="neoecoprototype1201:migration_drive_1_4" />
<RecipeFor id="neoecoprototype1201:migration_drive_4_6" />
<RecipeFor id="neoecoprototype1201:migration_drive_6_9" />
<RecipeFor id="neoecoprototype1201:migration_drive_creative" />

The machine must be a host of one of the three families, formed, and at its full length. Anything else -
including a host that is one cell short of the ceiling - is refused with a line saying why.
