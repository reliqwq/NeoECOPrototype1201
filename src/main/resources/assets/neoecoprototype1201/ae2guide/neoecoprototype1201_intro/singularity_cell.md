---
navigation:
  title: L1 Singularity Cell
  icon: neoecoprototype1201:simplify_singularity_cell
  position: 22
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_singularity_cell
  - neoecoprototype1201:simplify_sky_stone_dust_cell
---

# L1 Singularity Cell

Once it sits in a drive it **produces 120 singularities every 30 seconds**. It needs no input and consumes nothing, and it is drawn from like any other storage cell - it is there for saves that want singularities before they build an <ItemLink id="ae2:condenser" />.

## How fast

One **batch** is one payout of 120: the stock - what this page calls the bank - jumps by that much each interval instead of trickling. The shipped interval is 600 ticks (30 s), which averages about 4 a second.

| Time in a drive | Ready |
|-----------------|-------|
| 30 s | 120 |
| 1 min | 240 |
| 5 min | 1,200 |
| 30 min | 7,200 |
| 1 hour | 14,400 |
| 24 hours | 345,600 |

| What you want it for | Needs | At the default rate |
|----------------------|-------|---------------------|
| This addon's three L1 interfaces | 6 | the first batch covers it (30 s) |
| one eco MEGA 4G matrix, or one infinite storage component | 128 | two batches, 60 s |
| eco's infinite storage unlock (64 components) | 8,192 | 69 batches, about 35 min |

## How far it can be tuned

On this line the pace is a constant, not a config entry: one batch per 600 ticks (30 s), 120 per batch, on all three members. A pack that wants a different pace builds its own member with the KubeJS storage-matrix builder's `grows(perBatch, ticksPerBatch)` - both numbers are required, and a scripted cell runs on its own figures instead of the shipped ones.

The stock figure itself is a `long`, and **9,223,372,036,854,775,807 is where it stops**: past that it saturates instead of wrapping. The world clock running out saturates the same way rather than turning the stock negative.

## The contents are computed, not saved

Nothing accumulates inside the cell: the stack stores two values - the world tick it started at and how much has been taken - and every query is computed as (current tick - start tick) / ticks_per_batch x amount_per_batch - taken. Four consequences follow. While the game is paused or the server is down world time does not advance, so the stock does not grow. The start tick is written the first time a drive or host queries the cell, so a cell in an inventory, a chest or as a dropped item has not started counting, while one that has started keeps growing when you take it out. Terminals read the server value; a client tooltip syncs once a second and can be up to a second behind.

## Limits

It holds one content type, so it cannot be partitioned in a cell workbench and cannot be stored inside another cell. It belongs to the L1 tier, so [the L1 storage system](storage_system.md) mounts it without a whitelist entry, and eco's higher tiers do too.

## Recipe

<RecipeFor id="neoecoprototype1201:simplify_singularity_cell" />

## The other member: sky stone dust cell

The same engine, the same tile and the same default pace (120 per 600 ticks) - it grows **sky stone dust** (`ae2:sky_dust`, AE2's own) instead of singularities.

<RecipeFor id="neoecoprototype1201:simplify_sky_stone_dust_cell" />

Its bill differs from the singularity cell's only in the last ingredient: infinite matrix housing + 64M ECO storage component + green crystal lattice + one sky stone dust. ## The third member: source cell

It needs Ars Énergistique (that mod supplies the source medium; Ars Nouveau is its own hard dependency). Same engine, same tile, same default pace - it grows **source** instead.

> This member is **only registered when Ars Énergistique is installed**, so this page carries no icon or recipe box for it: a tag that cannot resolve an item writes a line of red text onto the page. With Ars Énergistique installed it is in JEI.

Its bill is not the component one: infinite matrix housing + Alakarkinos token + source gem block + magebloom and three essences, via Ars Nouveau's enchanting apparatus. 
