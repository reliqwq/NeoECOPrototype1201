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

Every block the swap replaces goes **back into your inventory** - the old casings, drives, vents, cells and
interfaces are handed to you, not consumed inside the new walls. Whatever is mounted stays where it was: a
storage matrix returns to the same drive slot, a pattern to the same bus and the same slot.

The one thing that does not survive is **the interface's own configuration**: eco only exposes its interface
slots for reading, so after a swap that cell has to be set up again. The success message says so on the spot.
This is an accepted cost, not a bug to re-fix.

The drive itself is consumed and comes back as the empty shell - <ItemImage id="neoecoprototype1201:migration_drive_empty" />,
which has no colour on purpose, so an empty disk cannot be read as a usable one.

## How you get one

There is **no recipe on this line, in any of the four**, and the creative tab plus an admin command is the
only route. That is deliberate rather than unfinished: the price of a rung has not been designed, and a
priced disk whose price is a guess is worse in a pack than one that is simply not craftable yet.

`/give @p neoecoprototype1201:migration_drive_1_4` - and the same for `migration_drive_4_6`, `migration_drive_6_9` and `migration_drive_creative`.

The machine must be a host of one of the three families, formed, and at its full length. Anything else -
including a host that is one cell short of the ceiling - is refused with a line saying why.
