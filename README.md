# Neo ECO Prototype - 1.20.1 line

A separate project for Minecraft **1.20.1 / MinecraftForge 47.4.20**. It is a trimmed port of the
1.21.1 addon in `F:/neoecoSimplify`, not a second copy of it: the two lines evolve independently.

## Toolchain

| | this line | the 1.21.1 line |
|---|---|---|
| Loader | MinecraftForge 47.4.20 | NeoForge 21.1.251 |
| Minecraft / Java | 1.20.1 / 17 | 1.21.1 / 21 |
| AE2 | 15.4.10 | 19.2.17 |
| eco (Neo ECO AE Extension) | 20.5.0-beta1, our build of its `1.20.1` branch | 21.2.13 (compile baseline = runtime floor) |
| GuideME | 20.1.14 | 21.1.1 |

Gradle 8.12.1 + ForgeGradle 6.0.54, `mappings channel: 'official'`. The JDK 17 lives outside the
checkout (`gradle.properties` points at it by absolute path), and no jar is tracked: `.gitignore`
drops every `*.jar` except the Gradle wrapper.

**`libs/neoecoae-20.5.0-beta1.jar` is the build this checkout compiles against**, produced from
upstream's `1.20.1` branch (`DancingSnow0517/NeoECOAEExtension`) with the toolchain set to 17. It is
compiled against official mappings and is *not* reobfuscated, which is what makes it usable here. It
is not an upstream artifact - do not publish it as one. Earlier builds are kept locally as
byte-for-byte copies under `旧jar/`; no jar is tracked, so a fresh clone has to rebuild this one from
the same branch before the first `./gradlew build`.

## What is deliberately not ported

Measured when this line was scoped (2026-09, against the 20.5.0 branch), 71 of the 86 eco types the
1.21.1 addon then referenced resolve. The 15 that do not are the scope boundary of this project:

- **Cut.** One-click builder (`MultiBlockBuildController`), the host panel's action layer
  (`StorageHostActionUI`), `CraftingUIHelper`, `ClientUIBridge`, `ServerTaskUtil`,
  `MultiBlockInfoWrapper`, and the whole small-bulk family (`IECOBulkMarkableCellItem` - MegaCells has
  no 1.20.1 build, and neither does ae2omnicells, so the universal/quantum families go too).
- **Rewrite.** The three multiblock interface screens: 1.20.1 has `gui/ldlib/state/NE*InterfaceUiState`
  (LDLib v1 state objects) where 1.21.1 has `*InterfaceUI` classes, and the drive's cell rendering hook
  moved to `api.rendering.IFixedBlockEntityRenderer`.
- **Re-hook.** Fast path: 1.20.1 exposes `crafting/execution/fastpath/*` instead of
  `api.me.ECOFastPathFacade`.
- **Rename only.** `gui.theme.NEStyleSheets` to `gui.NEStyleSheets`.

## Confirmed API differences (measured, not guessed)

`Prototype1201.java` is the evidence: it fails to compile against anything else.

- **`ECOCellType`**: 20.5.0 is `record(ResourceLocation id, Component desc, int typeCount)` with a
  builder of `id / desc / typeCount`. The 1.21.1 line is `record(desc, typeCount, visible)` with
  `desc / typeCount / visible`. So on 1.20.1 the row carries its own id and **there is no way to
  register a hidden cell type** - every registration shows up in the host panel.
- **`ECOStorageCellItem`**: 20.5.0 only has the eager `AEKeyType` constructors, and capacity is
  `(long bytes, int bytesPerType, int totalTypes, double idleDrain)`. 1.21.1 additionally has a
  `Supplier<AEKeyType>` overload and `(bytes, bytesPerType, idleDrain)` with `totalTypes` overridden by
  a getter. Consequence for third-party key types (Mekanism gas, Data Energistics): on 1.20.1 the key
  type must be resolvable **before** item registration, or the cell subclass must override
  `getKeyType()` - the lazy-supplier trick the 1.21.1 addon uses is not available.

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

`src/main/java/.../Prototype1201.java` is a compile probe, not content: it only builds if eco's
`ECOCellType.builder()` chain and AE2's `AEKeyType` surface still match what the 1.21.1 addon calls.
