# Neo ECO Prototype - 1.20.1 line

A separate project for Minecraft **1.20.1 / MinecraftForge 47.4.20**. It is a trimmed port of the
1.21.1 addon in `F:/neoecoSimplify`, not a second copy of it: the two lines evolve independently.

## Toolchain

| | this line | the 1.21.1 line |
|---|---|---|
| Loader | MinecraftForge 47.4.20 | NeoForge 21.1.251 |
| Minecraft / Java | 1.20.1 / 17 | 1.21.1 / 21 |
| AE2 | 15.4.10 | 19.2.17 |
| eco (Neo ECO AE Extension) | 20.5.10, the published 1.20.1 build (Modrinth) | 21.2.13 (compile baseline = runtime floor) |
| GuideME | 20.1.14 | 21.1.1 |

Gradle 8.12.1 + ForgeGradle 6.0.54, `mappings channel: 'official'`. The JDK 17 lives outside the
checkout (`gradle.properties` points at it by absolute path), and no jar is tracked: `.gitignore`
drops every `*.jar` except the Gradle wrapper.

**This line compiles against eco's published 1.20.1 build**, `maven.modrinth:neoecoae:20.5.10`
(bumped from 20.5.3 on 2026-11-21 - the whole line recompiled clean against it and the dev client
boots to the main menu on it), resolved by Gradle and passed through `fg.deobf` because the
published jar is reobfuscated for production. `mods.toml` floors eco at `[20.5.10,)`: the compile
baseline is also what players must have. A fresh clone needs no manual jar step.

Two details of that baseline: eco's own `mods.toml` makes LDLib v1 mandatory, so LDLib is a
runtime-only dependency here; and Registrate must **not** be added - the published eco jar already
embeds `Registrate-MC1.20-1.3.11` through jarJar, and a second copy on the module path is a
duplicate-package `ResolutionException` at boot.

Jars from before this switch are kept locally, byte-for-byte, under `旧jar/`; nothing in the build
reads them. That includes the old `20.5.0-beta1`, produced from upstream's `1.20.1` branch - it is
not an upstream artifact, so do not publish it as one.

## Scope

All three L1 subsystems are ported. **Storage**: controller, interface, network interface, casing,
vent, drive, energy cell. **Computation**: system, casing, threading and parallel cores, cooling
controller, transmitter, drive, interface, network interface, and the two energized members.
**Crafting**: system, casing, vent, worker, parallel core, pattern bus, both fluid hatches, the
powered and superconductive interfaces, and the processor assembler. Each subsystem carries its own
cluster, calculator and `MultiBlockDefinition`; the calculators go in through
`NEBlockEntity.registerCalculatorFactory`, for every storage member and not only the hosts, because
eco's constructors install eco's own calculator otherwise and a scan starting from a drive would
validate our shell against eco's L4 geometry.

Storage-matrix coverage now includes item, fluid, chemical (only with Applied Mekanistics), FE
(only with Applied Flux), infinite concrete and Pigmee matrices, small-bulk item/fluid cells (with
MEGA Cells), the Universal / Quantum / Complex Universal OMNI families (with AE2 OmniCells), and the
singularity, sky-stone dust and Ars Energistique source families. KubeJS can additionally register
finite, infinite and self-growing matrices, plus eco's infinite-supply cell. Alongside the three
subsystems: the L1 pattern provider in two forms (a block and a cable part, 27 pattern slots), the
EC9 processor assembler, the structure expansion card with its backup drive and twelve work orders,
five dolls plus the plushie crafting CPU, and two creative helpers. Measured by `lang` and the data
guard: 145 registered ids are claimed by exactly one GuideME page, with 35 storage cells, 32
disassembly tables, 19 advancement nodes and four scene NBTs.

The structure expansion card is registered: it can grow a formed machine along its edge or swap it
up a tier in place, using a backup drive and work orders; migration drives remain the quick tier-swap
path. Still not ported - zero references in our own bytecode, checked class by class: the host panel's
action layer (`StorageHostActionUI`, `ClientUIBridge`, `ServerTaskUtil`), bulk marking
(`IECOBulkMarkableCellItem`) and eco's fast path (`ECOFastPathFacade`). The workstation card is also
not ported: eco 20.5.10 still hard-codes its workstation-module and recipe-adapter gates. The
unfinished Grette crystal items are registered but hidden from the creative tab, and the cryotheum
ores are registered as reserved content but currently generate nowhere.

That is a scoping decision, not an API wall. The line compiles against eco's published 20.5.10
build. `MultiBlockBuildController`, `StorageHostActionUI`, `ClientUIBridge`, `ServerTaskUtil`,
`MultiBlockInfoWrapper`, `IECOBulkMarkableCellItem`, `ECOFastPathFacade` and the interface-screen
surfaces are present in that jar; `CraftingUIHelper` is still absent. `MultiBlockInfoWrapper` is the
one of those we already use: our three definitions are handed to eco's multiblock JEI page through
it. A green `compileJava` is the API evidence.

## Confirmed API differences (measured on the 20.5.10 baseline)

The ported source is the evidence: it only compiles against this shape.

- **`ECOCellType`**: `record(Component desc, int typeCount, boolean visible)` with a
  `desc / typeCount / visible` builder. The registry key is the row's id, and `visible` exists, so a
  hidden cell type is possible; this line keeps the default.
- **`ECOStorageCellItem`**: capacity is
  `(Properties, IECOTier, AEKeyType, Supplier<ECOCellType>, long bytes, int bytesPerType, double idleDrain)`,
  with `Supplier<AEKeyType>` overloads and short forms that omit capacity. `totalTypes` is not a
  constructor argument any more - it travels in the cell type. The lazy key-type overloads also mean
  a third-party key type (Mekanism gas, Data Energistics) can be resolved when the item is created.
- **`ECOStorageCellItem.Handler.INSTANCE`** handles every `ECOStorageCellItem` subclass and releases
  through `ECOStorageRepository`. An addon must not register a handler of its own: the interface's
  `releaseCellInventory` default is a no-op, so the second registration shadows repository release.
- **`NEBlockEntity`** no longer has `writeUiSyncTag` / `readUiSyncTag`; client state rides on
  `getUpdateTag()` / `handleUpdateTag(CompoundTag)`.
- **`NEClusterCalculator`** requires `casing()`, and `validateBlockLine` returns `Optional<BlockPos>`.
- **`NEItemColors`** exposes `getCellColor(ItemStack, int)`; register it against
  `RegisterColorHandlersEvent.Item`.
- **`IFixedBlockEntityRenderer`** lives in `client.rendering`; `tessellateModel` is the
  `(PoseStack, MultiBufferSource, ResourceLocation, packedLight, packedOverlay)` overload - the
  variant that took the block entity is gone.

## Art asset policy

Textures, models, blockstates, lang and GuideME pages are copied **one way, from the 1.21.1 project to
this one**. Do not edit art here and expect it back; divergent art is the one thing that cannot be
merged afterwards.

**One deliberate exception, and it must survive every art sync:** in
`models/block/storage_controller/controller_formed_base.json` (and its `_mirrored` twin) four `#body`
faces carry `forge_data{block_light:15, sky_light:15}` that the 1.21.1 model does not have - the `up`
and `down` of `back`, the `down` of the lower `front_a`, the `up` of the upper `front_a`. Forge 1.20.1
asks the neighbour above whether a face may be culled before it picks a light, and eco's formed
casing answers yes while it is `INVISIBLE`; those faces then fell back to the neighbour's (zero)
light and the top of the machine rendered black. 1.21 draws the same model without the four overrides
and is fine, so a straight copy from 1.21 will reintroduce the black top.

## Build

```
./gradlew build
```

`./gradlew build` produces `build/libs/neoecoprototype1201-1.1.1.jar` - the number is
`gradle.properties`' `mod_version`, so this sentence has to move with every bump. The entry point
`Prototype1201.java` is registration plus the four hand-overs AE2 and eco offer no slot for: the
calculator factory per member type, our block entity types in eco's pipe-capability table, our menu
type in AE2's `MenuOpener` table (`MenuTypeBuilder` can only name entries inside `ae2:` on this
line), and the infinite matrix's cell handler. Everything else the mod needs is reached through a
public API, so a broken eco API shows up at compile time rather than in a world.

**One place for the version number**: `gradle.properties`' `mod_version`. `mods.toml` reads
`${mod_version}` and `processResources` expands it, so the jar name and the mod's own metadata
cannot drift; after a build, read `META-INF/mods.toml` back out of the jar to prove it. The README's
jar path above is the only other copy and it is prose, not data.

The code is GPL-3.0-only (`LICENSE`). `THIRD_PARTY_NOTICES.md` records what we build against and
what we did not write: eco, AE2, GuideME, LDLib v1 and the four optional families, each with the
license line read from that jar's own metadata, plus the artwork provenance - all 213 PNGs here are
byte-identical to their 1.21.1 twins, so the artists' ARR terms and AE2's CC BY-NC-SA asset notice
carry over unchanged.
