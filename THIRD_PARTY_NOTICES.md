# Third-Party Notices - 1.20.1 line

This project is an **addon** to Neo ECO AE Extension (eco) and Applied Energistics 2 (AE2). No
dependency JAR is tracked in this repository or bundled into a release: every project below is
resolved from its own public maven at build time, and users are pointed at the official pages.
The code of this addon is **GPL-3.0-only** (see `LICENSE`), which does not relicense anyone else's
content and does not cover the artwork - see "Artwork" below.

## What we build and run against

| Component | Mod id | Version on this line | Upstream license | How it is used here |
|---|---|---|---|---|
| MinecraftForge | `forge` | 47.4.20 (floor `[47.4.0,)`) | LGPL-2.1 | loader |
| Neo ECO AE Extension | `neoecoae` | **20.5.3** - the published 1.20.1 build, compile baseline **and** declared floor `[20.5.3,)` | **GPLv3** - its own `mods.toml` reads `license="GPLv3"` | the whole addon sits on its storage / computation / crafting / multiblock API |
| Applied Energistics 2 | `ae2` | 15.4.10 (floor `[15.4.10,)`) | its `mods.toml` says only `"See GitHub repository for details"`; the repository states code LGPL v3 / GPL v3 and **textures and models CC BY-NC-SA 3.0** - the asset notice is quoted verbatim under "Artwork" | grids, cells, patterns, `InterfaceLogic`, `MenuOpener`, the molecular assembler we extend |
| GuideME | `guideme` | 20.1.14 | its `mods.toml` says only `"See GitHub repository for details"` | the in-game guide book (`ae2guide/` pages); `compileOnly` uses the `:api` classifier |
| LDLib (v1) | `ldlib` | Curse file `7809449` | **GPL-3.0** - its own `mods.toml` reads `license = "GPL-3.0 license"` (note this is LDLib **v1**; the LDLib**2** artifact on the 1.21.1 line is a different project with a different notice) | **mandatory because eco declares it**; our computation block entities implement its sync interfaces, so it is `compileOnly` as well |
| Mekanism | `mekanism` | 1.20.1-10.4.16.80 | **MIT** (its own `mods.toml`) | dev only (`compileOnly` + `runtimeOnly`); required by Applied Mekanistics |
| Applied Mekanistics | `appmek` | Curse file `7244744` | its `mods.toml` says only `"See GitHub repository for details"` | **optional at runtime**: it is what gives the chemical matrix family a key type |
| AppliedFlux | `appflux` | Curse file `7651647` (= `1.20-1.3.7-forge`) | **LGPL-3.0** (its own `mods.toml`) | **optional at runtime**: the FE matrix family and the induction card. `compileOnly` because one allowance reads `AFItemAndBlock.INDUCTION_CARD` |
| Glodium | `glodium` | Modrinth file `eoUaDkZf`, `1.20-1.5-forge`, sha1 `c7534db3248adfcf39a6158b423dfc9bfc99a378` | **MIT** (its own `mods.toml`) | dev only; appflux declares it mandatory, so appflux cannot run without it |
| Just Enough Items | `jei` | 15.59.0.212 (Modrinth file `IrfJO7PN`) | MIT | our `integration/jei` plugin compiles against it; players are never required to install it |
| Jade | `jade` | 11.13.2+forge | `CC BY-NC-SA 4.0` (its own `mods.toml`) | dev only |
| Registrate | `registrate` | **not declared** | see upstream | eco 20.5.3 already embeds `Registrate-MC1.20-1.3.11` through jarJar; a second copy on the module path is a duplicate-package `ResolutionException` at boot |

Content families that exist on the 1.21.1 line and are **deliberately absent here**: Mega Cells (small
bulk), AE2 OmniCells (universal / complex / quantum), Beyond Dimensions, KubeJS custom matrices,
Data Energistics, `ae2lt`. The first two have no 1.20.1 build that fits this dependency graph, and a
local shelf copy of `ae2omnicells-1.0.7` is kept **outside** the repository for reading only: its cell
superclass chain reaches into OmniCells itself, and Forge 1.20.1 force-loads every class in our jar
while building the event handler table, so shipping it would crash any pack without that mod.

## Artwork

**Every one of the 213 PNG files under `src/main/resources/assets/neoecoprototype1201/textures` is
byte-identical (md5-compared) to the file at the same path in the 1.21.1 project, `F:/neoecoSimplify`.**
Artwork is copied one way, from that project to this one, and nothing here was authored, recoloured
or re-derived on this line. The provenance record therefore belongs to that project and is unchanged
here: original artwork by **`Neo-TiX`** (lead artist) and **`kyln默寒寒冰`**, both **All Rights Reserved**
and not to be extracted, reused or redistributed separately; a group adapted from eco's cell art under
GPL-3.0; and a group adapted from AE2 under its asset notice, verbatim:

> *"Textures and Models (c) 2020, Ridanisaurus Rid, (c) 2013 - 2020 AlgorithmX2 et al / License: CC BY-NC-SA 3.0"*

That license carries a non-commercial term and a share-alike term, and this addon makes no ARR claim
over those files: anyone reusing them follows AE2's terms rather than ours. See
`F:/neoecoSimplify/THIRD_PARTY_NOTICES.md` for the per-group counts and the content test that produced
them (file md5 against whole-jar indexes, plus the "same pixel layout, different palette" comparison).

The JSON side of the assets is **not** a verbatim copy: of 188 model files, 39 are identical to their
1.21.1 twin once the namespace is normalised, and the rest differ because Forge 47 spells the same
things differently - face-level `"neoforge_data"` is `"forge_data"` here, and the formed controller
carries four `block_light/sky_light: 15` face overrides that the 1.21.1 model does not need (see
`README.md`, "Art asset policy": without them the top of the machine renders black on this engine).

## Inlined upstream geometry, credited

`models/block/simplify_casing.json` (13 elements) carries the geometry of eco's
`assets/neoecoae/models/block/casing_base.json` with our own texture slots - the same decision the
1.21.1 line records, and byte-identical to that file after namespace normalisation. It is inlined
rather than referenced because this repository's CI rejects any asset that points into the `neoecoae`
resource namespace (measured 0 hits before every release): an upstream rename would otherwise turn our
casing into a silent missing model. Geometry only; the surface artwork is the artists' own.

## Player data and skins

None. The doll / player-skin content of the 1.21.1 line is not ported to 1.20.1, so this project ships
no player skin and no name-bearing texture.

## Distribution rule

Do not copy dependency JARs into this repository or bundle them into a release without checking each
upstream project's current redistribution terms; link to the official pages instead. The local
`参考模组/`, `libs/` and `旧jar/` shelves exist for development only, are excluded by `.gitignore`
(`*.jar`, plus a CI gate that fails if any of those directories is ever tracked), and none of them
reaches a published artifact. Note also that `旧jar/neoecoae-20.5.0-beta1.jar` was built locally from
upstream's `1.20.1` branch and is **not** an upstream artifact - it must never be published as one.

Upstream projects: <https://github.com/DancingSnow0517/NeoECOAEExtension> ·
<https://github.com/AppliedEnergistics/Applied-Energistics-2> ·
<https://github.com/AppliedEnergistics/GuideME> ·
<https://github.com/Low-Drag-MC/LDLib> ·
<https://github.com/mezz/JustEnoughItems> ·
<https://github.com/Snownee/Jade> ·
<https://github.com/mekanism/Mekanism> ·
<https://github.com/glodblock/AppliedMekanistics> ·
<https://github.com/glodblock/AppliedFlux>

eco's own metadata lists its authors as DancingSnow, ZhuRuoLing and Yang120231; thanks to `Yang120`
for substantial help from the original Neo ECO AE Extension team, as recorded on the 1.21.1 line.
