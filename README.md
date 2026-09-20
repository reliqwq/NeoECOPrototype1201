# Neo ECO Prototype - 1.20.1 line

A separate project for Minecraft **1.20.1 / MinecraftForge 47.4.20**. It is a trimmed port of the
1.21.1 addon in `F:/neoecoSimplify`, not a second copy of it: the two lines evolve independently.

## Toolchain

| | this line | the 1.21.1 line |
|---|---|---|
| Loader | MinecraftForge 47.4.20 | NeoForge 21.1.233 |
| Minecraft / Java | 1.20.1 / 17 | 1.21.1 / 21 |
| AE2 | 15.4.10 | 19.2.17 |
| eco (Neo ECO AE Extension) | 20.5.0, built from its `1.20.1` branch | 21.2.0-beta4 (published) |
| GuideME | 20.1.14 | 21.1.x |

Gradle 8.12.1 + ForgeGradle 6.0.54, `mappings channel: 'official'`. The JDK 17 and the eco jar live
outside the repository; `gradle.properties` points at the JDK by absolute path.

**`libs/neoecoae-20.5.0.jar` is our own build**, produced from upstream's `1.20.1` branch
(`DancingSnow0517/NeoECOAEExtension`) with the toolchain set to 17. It is compiled against official
mappings and is *not* reobfuscated, which is what makes it usable here. It is not an upstream
artifact - do not publish it as one.

## What is deliberately not ported

Measured against this jar, 71 of the 86 eco types the 1.21.1 addon references resolve. The 15 that do
not are the scope boundary of this project:

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

## Build

```
./gradlew build
```

`src/main/java/.../Prototype1201.java` is a compile probe, not content: it only builds if eco's
`ECOCellType.builder()` chain and AE2's `AEKeyType` surface still match what the 1.21.1 addon calls.
