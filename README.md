# Neo ECO Prototype - 1.20.1 line

A separate project for Minecraft **1.20.1 / MinecraftForge 47.4.20**. It is a trimmed port of the
1.21.1 addon in `F:/neoecoSimplify`, not a second copy of it: the two lines evolve independently.

## Toolchain

| | this line | the 1.21.1 line |
|---|---|---|
| Loader | MinecraftForge 47.4.20 | NeoForge 21.1.251 |
| Minecraft / Java | 1.20.1 / 17 | 1.21.1 / 21 |
| AE2 | 15.4.10 | 19.2.17 |
| eco (Neo ECO AE Extension) | 20.5.3, the published 1.20.1 build (Modrinth) | 21.2.13 (compile baseline = runtime floor) |
| GuideME | 20.1.14 | 21.1.1 |

Gradle 8.12.1 + ForgeGradle 6.0.54, `mappings channel: 'official'`. The JDK 17 lives outside the
checkout (`gradle.properties` points at it by absolute path), and no jar is tracked: `.gitignore`
drops every `*.jar` except the Gradle wrapper.

**This line compiles against eco's published 1.20.1 build**, `maven.modrinth:neoecoae:20.5.3`,
resolved by Gradle and passed through `fg.deobf` because the published jar is reobfuscated for
production. `mods.toml` floors eco at `[20.5.3,)`: the compile baseline is also what players must
have. A fresh clone needs no manual jar step.

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

Six storage-matrix families ship: item, fluid, chemical (only with Applied Mekanistics), FE (only
with Applied Flux), the infinite concrete matrix (vanilla-only, through a stateless cell handler)
and the Pigmee matrix. Measured from `lang` and the data guard: 36 blocks and 34 items named, 88 recipe JSONs (69 crafting
and machine recipes plus 19 disassembly tables), 14 advancement nodes, seven guide pages in each of
English and Chinese, and four scene NBTs - every one of the 65 named ids is claimed by exactly one page,
so the book's item index opens a page for all of them.

Still not ported - zero references in our own bytecode, checked class by class: the one-click
builder (`MultiBlockBuildController`), the host panel's action layer (`StorageHostActionUI`,
`ClientUIBridge`, `ServerTaskUtil`), bulk marking (`IECOBulkMarkableCellItem`) and eco's fast path
(`ECOFastPathFacade`).

That is a scoping decision, not an API wall. The 2026-09 survey that scoped this line was taken
against the 20.5.0 branch; the published 20.5.3 build has since grown most of those surfaces -
`MultiBlockBuildController`, `StorageHostActionUI`, `ClientUIBridge`, `ServerTaskUtil`,
`MultiBlockInfoWrapper`, `IECOBulkMarkableCellItem`, `ECOFastPathFacade` and 44
`crafting/execution/fastpath` classes are all in the jar, and the interface screens now use the
`gui/computation/ComputationInterfaceUI` / `gui/crafting/CraftingInterfaceUI` shape instead of the
old `gui/ldlib/state/NE*InterfaceUiState` objects. What is still absent is `CraftingUIHelper`.
`MultiBlockInfoWrapper` is the one of those we already use: our three definitions are handed to
eco's multiblock JEI page through it.

The line references **78 eco classes** today - every constant-pool `Class` entry across our 89
compiled classes that lives under `cn/dancingsnow/neoecoae/` - and all of them resolve in 20.5.3;
a green `compileJava` is the evidence.

## Confirmed API differences (measured on the 20.5.3 jar)

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

`./gradlew build` produces `build/libs/neoecoprototype1201-0.1.0.jar`. The entry point
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
