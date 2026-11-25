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

## Production speed

Production advances in batches from world time, not as a continuous trickle. The server config sets the amount and interval independently for each family member; the default is 120 every 600 ticks (30 s). Stock tops out at **9,223,372,036,854,775,807**, then saturates instead of wrapping during normal production.

## Stock is computed

Counting starts the first time a drive or host queries the cell. Sitting in an inventory, chest or item drop does not start it. Paused game time or a stopped server does not grow the stock, and a cell that has started keeps growing after you take it out.

## Limits

It refuses all inserted content, including singularities, and only holds and yields the one thing it produces itself. It cannot be partitioned in a cell workbench or stored inside another cell. It belongs to the L1 tier, so [the L1 storage system](storage_system.md) mounts it directly and is also compatible with eco hosts.

## Recipe

<RecipeFor id="neoecoprototype1201:simplify_singularity_cell" />

## The other member: sky stone dust cell

<ItemLink id="neoecoprototype1201:simplify_sky_stone_dust_cell" /> produces sky stone dust (`ae2:sky_dust`): by default it produces **120 every 30 seconds**, and its stock grows according to world time.

<RecipeFor id="neoecoprototype1201:simplify_sky_stone_dust_cell" />

Recipe: infinite matrix housing + 64M ECO storage component + green crystal lattice + one sky stone dust.

## Ars Nouveau tie-in: source matrix

It produces source: **120 every 30 seconds** by default.

It is **only registered when Ars Énergistique is installed**, along with its required dependency Ars Nouveau.

Recipe: infinite matrix housing + Alakarkinos token + source gem block + magebloom and three essences, via Ars Nouveau's enchanting apparatus.
