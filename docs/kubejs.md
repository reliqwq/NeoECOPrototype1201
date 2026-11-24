# KubeJS support

Ready-to-copy script: [`docs/kubejs-example.js`](kubejs-example.js). It covers the startup matrix builders, recipe changes, and a debug command.

**目录**

- [配方修改与移除](#remove-an-addon-recipe)（[替换](#replace-an-ordinary-crafting-recipe) · [工作站配方](#add-an-integrated-working-station-recipe)）
- [KubeJS 能配什么、不能配什么](#what-kubejs-should-configure)
- [自定义无限存储矩阵](#自定义无限存储矩阵)
- [通用存储矩阵注册](#通用存储矩阵注册)
- [自产矩阵（Grows）](#自产矩阵-grows)
- [桥接 Eco 无限供给矩阵](#无限供给矩阵-eco_supply_cell)
- [Java 新矩阵注册 API](#java-新矩阵注册-api)

> 这一页移植自 1.21.1 线的同名文档。两处形状差：1.21.1 那份开头的「离线矩阵改色」
> 小节依赖 `tools/generate_matrix_textures.py`，那套工具只在那条线上，这里没有——
> 需要改色就去那条线跑，产物是普通 PNG，拷过来即可用。本线的 KubeJS 集成跑在
> **KubeJS 2001.6.x（Forge 1.20.1）** 上，插件 API 与 1.21.1 的 NeoForge 版不同，
> 但脚本面（下面所有示例）保持同一形状；个别方法名以本文为准。

配方口径统一是**外壳 + AE2 同级 `cell_component_<档>`**（化学品每一件额外带 `appmek` 加载条件）。

| 介质 | 有的档位 |
|---|---|
| 物品 | `1k / 16k / 64k / 1m / 4m` |
| 流体 | `1k / 16k / 64k / 1m / 4m` |
| 化学品 | `1k / 16k / 64k / 1m / 4m` |

**三档介质都没有 4k**：本线的成品细胞按 `1k / 16k / 64k / 1m / 4m` 五档注册，与 1.21.1 的口径一致。

KubeJS is optional. Neo ECO Prototype does not declare KubeJS as a runtime dependency and the mod continues to work without it.

Put scripts in the KubeJS `server_scripts` directory and reload recipes with `/reload` or restart the game.

## Remove an addon recipe

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype1201:simplify_storage_component_4m' })
  event.remove({ id: 'neoecoprototype1201:simplify_computation_casing' })
})
```

## Replace an ordinary crafting recipe

Vanilla shaped and shapeless recipes use the normal KubeJS recipe helpers.

```js
ServerEvents.recipes(event => {
  event.shaped('neoecoprototype1201:simplify_storage_component_4m', [
    'ADA',
    'BCB',
    'AEA'
  ], {
    A: 'neoecoae:energized_crystal_dust',
    B: 'neoecoprototype1201:simplify_storage_component_1m',
    C: 'ae2:quartz_glass',
    D: 'neoecoae:superconducting_processor',
    E: 'neoecoae:energized_superconductive_ingot'
  })
})
```

Remove the original recipe first when replacing an existing recipe ID:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype1201:simplify_storage_component_4m' })

  event.shaped('neoecoprototype1201:simplify_storage_component_4m', [
    'ADA',
    'BCB',
    'AEA'
  ], {
    A: 'minecraft:iron_ingot',
    B: 'neoecoprototype1201:simplify_storage_component_1m',
    C: 'ae2:quartz_glass',
    D: 'ae2:printed_engineering_processor',
    E: 'minecraft:gold_ingot'
  })
})
```

## Add an integrated working station recipe

Neo ECO AE Extension recipes can be added with KubeJS `event.custom`. The recipe type is supplied by the upstream mod.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'neoecoae:integrated_working_station',
    energy: 10000,
    inputItems: [
      { item: 'neoecoprototype1201:simplify_storage_component_4m', count: 3 },
      { item: 'neoecoae:energized_superconductive_ingot', count: 26 },
      { item: 'neoecoae:superconducting_processor', count: 1 },
      { item: 'neoecoae:crystal_ingot', count: 1 }
    ],
    itemOutput: {
      id: 'neoecoae:eco_cell_component_16m',
      count: 1
    }
  })
})
```

Use the recipe ID to remove a specific integrated working station recipe. For example:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype1201:integrated_working_station/simplify_storage_component_4m' })
})
```

The exact item IDs and recipe IDs are part of the addon data files. Check `src/main/resources/data/neoecoprototype1201/recipes` when adapting a script to a particular release.

KubeJS scripts can change addon recipes, but they do not change multiblock structure definitions or the behavior of the storage and computation controllers.

## What KubeJS should configure

KubeJS is an optional content-layer extension point, not a replacement for the Java AE2 integration layer.

Good candidates for KubeJS:

- storage matrix definitions, bound item/fluid/chemical, capacity tier, material family, and models;
- addon and upstream integrated-working-station recipes;
- pack-specific recipe removal, replacement, and balancing;
- future data-driven allowlists whose meaning is already defined by a stable Java API.

Keep these in Java and resources:

- AE2 menu semantics and the distinction between `CONFIG` marker slots and `STORAGE` output slots;
- the L1 interface's screen layout and slot repositioning;
- passive power generation, suppression behavior, network node flags, and block/Part host logic;
- AE2 upgrade-card associations, because they must be registered for the exact block item and Part item and must match the logic that actually supports the card;
- multiblock structure validation, controller behavior, and Part geometry.

This split is intentional. A script can safely choose what content exists and how it is crafted, while the Java layer guarantees that an interface slot, button, texture, network behavior, and upgrade inventory remain the same contract. KubeJS support is therefore useful, but exposing raw GUI coordinates or arbitrary AE2 card insertion through scripts is not recommended.

## 自定义无限存储矩阵

KubeJS 在场时可以通过自定义 builder 类型 `neoecoprototype:infinite_storage_matrix`
（别名 `neoecoprototype:infinite_storage_matrix`）创建任意「无限」矩阵：绑定的物品/流体存入即融入、无限取出、优先路由，
与无限混凝土矩阵同一套行为（L1 等级、8 AE/t 待机、不可拆卸）。

在 `kubejs/startup_scripts` 中注册（需重启游戏生效）：

```js
StartupEvents.registry('item', event => {
  // 绑定单个物品
  event.create('infinite_dirt', 'neoecoprototype:infinite_storage_matrix')
       .itemType('minecraft:dirt')
       .displayName('无限泥土存储矩阵')

  // 绑定单个流体
  event.create('infinite_water', 'neoecoprototype:infinite_storage_matrix')
       .fluidType('minecraft:water')
       .displayName('无限水存储矩阵')
})
```

可选方法：

- 物品栏：按类型自动选择（item/fluid/chemical/infinite/other），各用自己家族的外壳配色；
  也可以用 `.material(...)` 显式指定。
- 驱动器内：同样按类型自动选择（`storage_cell_l1_item` / `storage_cell_l1_fluid` /
  `storage_cell_l1_chemical` / `storage_cell_l1_concrete` / `storage_cell_l1_custom`）。
- `cellModel` 必须传**格子芯片规格**的模型，而不是普通整方块模型。模型通常约为
  6×12×2，并包含 `level` / `type` / `base` 贴图槽；例如
  `neoecoprototype1201:block/cell/storage_cell_l1_fluid`。
- `.displayName(...)`、`.tooltip(...)` 等 KubeJS 通用物品 builder 方法均可叠加。

物品栏模型按绑定类型自动选择（item/fluid/chemical 各用自己的外壳配色，无限矩阵用银灰配色），
脚本也可以用 `.inventoryModel(...)` 覆盖。物品/流体绑定类型延迟解析：注册时目标内容尚不存在
也不会报错，进世界首次使用时才求值。解析失败时，报错消息同时给出**注册它的脚本物品**
（`kubejs:<你写的名字>`）和**缺失的那个 id**，日志里也会留一行 —— 照着这两样去查冲突就行。

可选的统一参数方法（下面显式写出的是无限矩阵的默认配色，不写也一样）：

```js
StartupEvents.registry('item', event => {
  event.create('infinite_oak_log', 'neoecoprototype:infinite_storage_matrix')
       .itemType('minecraft:oak_log')
       .size('infinite')
       .tier('l1')
       .inventoryModel('neoecoprototype1201:item/simplify_concrete_storage_cell')
       .cellModel('neoecoprototype1201:block/cell/storage_cell_l1_concrete')
})
```

`.size('infinite')` 和 `.tier('l1')` 是当前无限矩阵实现支持的固定组合；传入其他值会
明确报错，而不是创建元数据与实际存储行为不一致的矩阵。

## 通用存储矩阵注册

使用 `neoecoprototype:storage_matrix` 可以注册新的物品、流体或化学品矩阵
（从 1.21.1 线的文档抄来的脚本写 `neoecoprototype:storage_matrix` 也照常工作：两种命名空间
形式都注册了；裸名 `storage_matrix` 同样可用——KubeJS 6 对 type 字符串是精确匹配）：


```js
StartupEvents.registry('item', event => {
  event.create('hydrogen_matrix_4k', 'neoecoprototype:storage_matrix')
       .chemicalType('mekanism:hydrogen')
       .size('4k')
       .displayName('4K 氢气存储矩阵')

  event.create('oak_matrix_16k', 'neoecoprototype:storage_matrix')
       .itemType('minecraft:oak_log')
       .size('16k')
       // 注意：这一档是通用物品矩阵，上面绑的 oak_log 不会生效；
       // 要一台只装 oak_log 的矩阵就别写 size，用默认的 infinite。

  event.create('water_matrix_1m', 'neoecoprototype:storage_matrix')
       .fluidType('minecraft:water')
       .size('1m')
})
```

可用方法：

- `.itemType('namespace:item')`：绑定一种物品。**只有 `size('infinite')` 那一档真的绑**；有限档是通用物品矩阵（默认 256 种），绑定的那一种被忽略。
- `.fluidType('namespace:fluid')`：同上，只有无限档真的绑。
- `.chemicalType('namespace:chemical')`：绑定一种 Mekanism 化学品，有限档也真的绑；需要 Mekanism 和 Applied Mekanistics，缺了脚本会收到这一句而不是找不到类。
- `.type('item'/'fluid'/'chemical')`：设置矩阵声明类型；绑定方法会自动设置对应类型。传家族名（`pigmee`/`universal`/`quantum`/`small_bulk`/`infinite`/`other`）只换材质，不换容量 —— 容量一律用 `.size(...)`。
- `.keyType(() => key)`：绑定**任意 AEKey**（高级用法），给魔源这类既不是物品也不是流体的键用。键对象要脚本自己提供（那个模组的 API / 它自己的键工厂），因为一个裸 id 没法让别的键类型知道怎么造；它**只对无限档和 `.grows(...)` 生效**，有限档会明确报错。物品/流体/化学品仍优先用 `itemType`/`fluidType`/`chemicalType`。**不要写成 `.type(() => key)`**：KubeJS 的 Rhino 会把箭头函数按文本传给 `type(...)`（读成 `Function`），那里只收类型名或材质名。
- `.size('infinite'/'1k'/'4k'/'16k'/'64k'/'1m'/'4m')`：设置容量，档位名就是它字面的 2 的幂 —— `1k` = 1024、`4k` = 4096、`16k` = 16384、`64k` = 65536、`1m` = 1048576、`4m` = 4194304 字节。本线成品物品/流体的五档就是这些裸 2 的幂，`.size(...)` 的档位与它们一致；**全能（universal）家族例外**——它的 1K 比名字多 25%（1280），要和成品全能矩阵一样大就写 `.bytes(1280)`；`.size(...)` 与 `.bytes(...)` 同时出现时以 `.bytes(...)` 为准。
- `.bytes(n)`：改用自定义字节数，**它会顶掉先前的或之后的 `size(...)` 档位**（两种顺序都一样）；`size('infinite')` 与 `.bytes(...)` 同时出现会明确报错。
- `.totalTypes(n)`：改类型数，默认 256（物品/流体）或 25（化学品）。
- 每类型成本**不给脚本设**：由 `(bytes, totalTypes)` 推，规则是「填满类型上限吃这只矩阵容量的 20%」（`bytes / (5 × totalTypes)`，最少 1 字节）。以前是 `bytes / totalTypes`，也就是填满类型即整仓占满 —— 一只装满 256 种的 1M 矩阵连一个物品都放不下。2026-11-15 按玩家反馈改的，与 eco 自己最低一档同级。
- `.grows(perBatch, ticksPerBatch)`：把它变成**自产矩阵**，见下一节。
- `.inventoryModel('namespace:item/model')`：覆盖物品栏模型。
- `.cellModel('namespace:block/model')`：覆盖驱动器内格子模型。
- `.parentModel(...)` / `.texture(...)`：完全接管物品栏模型；设置后不再套用默认模型。

`infinite` 使用现有无限矩阵 handler；有限 item/fluid 使用 eco 存储后端；有限 chemical
使用 AppMek 化学品后端（本线化学细胞就是按 `ChemicalKeyType` 走的普通细胞，每类型成本
是家族自己的 `bytes >> 8`，不是物品/流体那条 `typeFee`）。这个 builder **不需要装 AE2 Omni Cells**：
只有 `universal` 与 `quantum` 两个家族要它，缺了会在脚本注册那一项上报"需要 AE2 Omni Cells"。
所有脚本在 `startup_scripts` 中运行，修改后需要重启游戏。

**卡片（AE2 的升级卡）按后端归属走**，本线不改动 AE2 的卡↔元件关联表：

- 所有带分区表的矩阵（包括 `universal` / `quantum` 两个家族）**都认模糊卡与反转卡各一张**。这两张改的是分区表的匹配方式；其余卡不收 —— 容量、速度这类靠数字改机器行为的卡不动它们（它们的类型上限与字节由 Omni Cells 那侧决定，量子连上限都没有）。
- **溢出销毁卡除小宗矩阵外都接受** —— 它会删掉塞不下的东西，所以小宗那几只（3 类/10 类上限）不收：删掉的可能是玩家还看得见位置、只是被类型上限挡住的那部分货。其余矩阵（含 `universal` / `quantum`）收一张，和 Omni Cells 自己那族一致。
- 没有分区表的那几只（自产矩阵、无限混凝土矩阵、脚本自建无限矩阵）本来就收不了卡，元件工作台对它们是拒开的。

这些说的是 AE2 自己的关联表加上 eco/本线的细胞形状，不是我们另写的一层：装不装哪张卡由 AE2 的注册关系与细胞的分区表形状决定。

默认模型不需要脚本配置，按矩阵**类型**自动选择材质，不会所有矩阵都长一样。
材质定义集中在 `MatrixMaterials`，脚本不应自行乱放贴图：

| 类型 | 物品栏模型 | 驱动器内模型 | 外壳材质 |
|---|---|---|---|
| item 物品 | `item/simplify_item_storage_cell_1k` | `block/cell/storage_cell_l1_item` | 物品外壳 |
| fluid 流体 | `item/simplify_fluid_storage_cell_1k` | `block/cell/storage_cell_l1_fluid` | 流体外壳 |
| chemical 化学品 | `item/simplify_chemical_storage_cell_1k` | `block/cell/storage_cell_l1_chemical` | 化学品外壳 |
| infinite 无限 | `item/simplify_concrete_storage_cell` | `block/cell/storage_cell_l1_concrete` | 无限银灰 |
| grows 自产（不写 material 时） | `item/simplify_concrete_storage_cell` | `block/cell/storage_cell_l1_concrete` | 无限银灰（它属于无限家族，不跟随绑定的媒介） |
| other 其他 | `item/simplify_default_storage_cell` | `block/cell/storage_cell_l1_custom` | 原量子外壳 / 驱动器棕色 |

固定物品家族的材质（同样由 `MatrixMaterials` 记录，供对照）：

| 家族 | 物品栏外壳 |
|---|---|
| 小宗 small bulk | `item/storage_recolor/small_bulk_item_cell_housing`（深灰） |
| 猪咪 pigmee | `item/pigmee_storage_cell` |
| 全能 universal | eco `omni_cell_housing` |
| 量子 quantum | eco `quantum_omni_cell_housing` |

`.material('item'/'fluid'/'chemical'/'infinite'/'other')` 可以显式指定材质；不写时按类型自动决定。
写法保持一行一句即可，例如：

```js
StartupEvents.registry('item', event => {
  event.create('stone_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:stone').size('1k').displayName('石头存储矩阵')

  event.create('other_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:gold_ingot').size('1k').material('other').displayName('其他材质矩阵')
})
```

这些模型都会在客户端自动注册，因此脚本创建的矩阵不会再出现紫黑缺失贴图。
需要"外壳 + 类型贴图"拼装时可以自行设置 `texture`，例如（本线是 KubeJS 6，
多张贴图用 `.texture(槽位, id)` 逐张给，没有 1.21.1 的 `.textures({...})` 一口喂法）：

```js
StartupEvents.registry('item', event => {
  const matrix = event.create('custom_item_matrix_1k', 'neoecoprototype:storage_matrix')
  matrix.itemType('minecraft:stone')
  matrix.size('1k')
  matrix.parentModel('minecraft:item/generated')
  matrix.texture('layer0', 'neoecoprototype1201:item/storage_recolor/eco_lower_item_cell_housing')
  matrix.texture('layer1', 'neoecoprototype1201:item/storage_recolor/eco_cell_light_16m')
  matrix.texture('layer2', 'neoecoprototype1201:item/storage_recolor/eco_cell_status_light')
  matrix.displayName('自定义外观 1K 石头矩阵')
})
```

## 自产矩阵 grows

`.grows(单批产量, 间隔刻)` 造的是一只**什么都不吃、自己长货**的矩阵：每隔
`ticksPerBatch` 世界刻跳一次，每次给出 `perBatch` 个，内容就是
`itemType` / `fluidType` / `chemicalType` 绑的那一个 AE key。本模组的奇点元件
就是这一族里注册出来的那一只，`grows` 让整合包加自己的成员。

```js
StartupEvents.registry('item', event => {
  event.create('lava_grower', 'neoecoprototype:storage_matrix')
       .fluidType('minecraft:lava')
       .grows(128, 600)
       .displayName('熔岩自产矩阵')

  event.create('hydrogen_grower', 'neoecoprototype:storage_matrix')
       .chemicalType('mekanism:hydrogen')
       .grows(1, 1200)
       .displayName('氢气自产矩阵')

  // 非物品/流体键（例如魔源）：键对象由那个模组的 API 提供
  event.create('source_grower', 'neoecoprototype:storage_matrix')
       .keyType(() => sourceKey)
       .grows(100, 200)
       .displayName('魔源自产矩阵')
})
```

规则与限制，一条一句：

- 两个数都必须写、都必须为正；写 0 或负数当场报错，不做"缺省回配置"。
- 产量与间隔是这只矩阵自己的数字，**不读服务端配置**：改配置只影响注册的奇点、陨石粉与魔源单元。
- 脚本这条路只要求为正，没有配置那条 600 刻的地板；想要慢就直接写大间隔。
- 内容用哪个媒介由绑定的 key 决定，任意 mod 的物品、流体、化学品都可以；魔源这类非物品/流体键用 `.keyType(() => key)` 绑（键对象由脚本提供）。化学品照旧要 Mekanism 与 Applied Mekanistics。
- `perBatch` 的单位就是那个 key 自己的计量单位，AE2 终端怎么显示那个媒介，这里就按什么计。
- 往它推任何东西都是 0，只出不进。
- 插进驱动器、被网络问过库存才开始计时；**放在身上不算**（和奇点元件同一条规则）。
- 库存没有上限，长到 `long` 顶就饱和、不回绕；已经被取走的部分不会自己回来。
- 它属于**无限家族**：不写 `.material(...)` 时戴银灰混凝土外壳（和 `infinite` 档那几只同款），不跟随绑定的媒介 —— 一只没有字节预算的矩阵套上流体外壳会说法不一致。`.material('other')` 一类的显式指定照旧听脚本的。
- 它也进 `StorageMatrixRegistry`（按 `infinite` 定义），为的是驱动器那一格有模型可读；所以 `cellModel(...)` 可以用。`bytes(...)`、`totalTypes(...)`、`size(...)` 仍然拒绝 —— 它没有字节预算可描述。
- 不能借 `universal` / `quantum` 家族：那两个后端存的是网络推进来的东西。
- 待机耗电固定 1.0，与奇点元件一致；物品堆叠上限 1。
- 主机面板上它和奇点元件一样不占存储那一族的行；数量写在它自己的 tooltip 里，那一行会带上内容名。
- 注册表冻结之后不能再造物品，所以这类脚本只在启动脚本里跑，改完要重启游戏。
- 20.5.3 上没有 1.21.1 的 exact-amount 口子，所以"无限"的口径是"列出量按很大的有限数结算、按量付电"——它是创造便利工具，不是白嫖机；这一条与全物品创造元件共用同一句话。

驱动器格子里的外观与产量跳变要开一次存档眼验；本线没有 GameTest 套件，这一段没有自动断言。

## 无限供给矩阵 eco_supply_cell

`neoecoprototype:eco_supply_cell` 造的**不是本模组的无限矩阵**，而是 eco 那族无限基础资源存储矩阵：
引擎是 eco 的 `ECOInfiniteResourceCell`，参数（每类型成本、待机耗电、EPIC、堆叠 1）
全部取自 eco 的档位，只有「锁哪几个键」由脚本给。绑定的键由 ME 网络直接无限抽取；
推进来的同类资源被直接销毁；别的键一律不收。

需要 Mekanism 与 Applied Mekanistics。这个示例直接加载化学品 API 并注册，不做模组存在性或化学品存在性判断。
**Mekanism 10.4（本线版本）把化学品拆成四个子注册表**（gas/infusion/pigment/slurry），没有 1.21 那个
`MekanismAPI.CHEMICAL_REGISTRY`，而且 `ChemicalStack` 是抽象类——照下面这个形状写（antimatter 是 gas）：

```js
const MekanismAPI = Java.loadClass('mekanism.api.MekanismAPI')
const BuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
const MekanismKey = Java.loadClass('me.ramidzkh.mekae2.ae2.MekanismKey')

const gasRegistry = BuiltInRegistries.REGISTRY.get(MekanismAPI.GAS_REGISTRY_NAME)

StartupEvents.registry('item', event => {
  event.create('base_resources', 'neoecoprototype:eco_supply_cell')
    .supplies('minecraft:redstone')
    .suppliesFluid('minecraft:milk')
    .suppliesKey(() => MekanismKey.of(gasRegistry.get(
      ResourceLocation.parse('mekanism:antimatter')).getStack(1)))
    .displayName('基础资源无限供给矩阵')
})
```

> [!TIP]
> **id 延迟解析**：启动脚本跑在物品注册完成之前，所以 `.supplies(...)` /
> `.suppliesFluid(...)` 绑的 id 会留到第一次被读时才求值 —— 绑一个比脚本更晚注册的
> 物品/流体（另一个模组的、或同一轮启动里排在我们后面的）完全没问题。
> 解析失败时悬停不会崩：tooltip 变一行红字，日志里另留一行，**同时写明是哪个脚本
> 注册的哪个物品**（`kubejs:<你写的名字>`）**和缺的是哪个 id** —— 排查冲突先看那一行，
> 修好脚本再把它装上驱动器（绑不上的矩阵在网格上是坏的）。

可用方法：

- `.supplies('namespace:item')` / `.suppliesFluid('namespace:fluid')`：各绑一种键，
  可调用任意多次，**顺序就是 tooltip 里的顺序**；一个都不绑会在注册时直接报错，
  消息里带脚本 id。
- `.suppliesKey(() => key)`：绑定**任意 AEKey**（高级用法，给魔源这类既不是物品也不是流体的键）。
  键对象要脚本自己从那个模组的 API 取 —— 裸 id 造不出这类键。可以和上面两个混用，
  顺序同样就是 tooltip 的顺序。三种绑法在引擎眼里没有区别：`insert` / `extract` 只问
  「这个键是不是我绑的那几个」，列出库存时每个绑定的键都报「无限」，不按键的类型过滤。
- `.tier('l4'/'l6'/'l9')`：**eco 自己的档位枚举**（`ECOTier`，不是本模组的 L1…L9），
  默认 `l9`；只决定每类型成本与待机耗电，不改变"无限"这件事。
- `.inventoryModel(...)` / `.cellModel(...)`：覆盖物品栏模型与驱动器格子模型。

> [!IMPORTANT]
> 引擎的每类型成本与待机耗电是 **eco 自己的参数**，不是本模组的数字。1.21.1 那份文档
> 在 eco 21.2.16 上量到过 `l4=8 192 字节/16 AE、l6=16 384/64、l9=32 768/256` 这一组读数；
> 本线编译基线是 **eco 20.5.3**，没有对这张表逐项复核 —— 以游戏内悬停与主机面板的读数为准，
> 不要把它当成我们承诺的固定值。

规则与限制，一条一句：

- 总字节数是 `Long.MAX_VALUE`，没有上限；「每类型成本」只是存进一种类型时的记账价，撑不满。
- 分区表只能在绑定的键之间选：`isBlackListed` 拒绝其余一切，所以没有任何配置能把它撑宽。
- 它进不了 eco 的无限存储域 —— 那只域会把供给矩阵搬进去，从而抹掉「存着」和「永远都有」的区别。
- 升级卡槽与 eco 那只相同；这族不存东西，卡没有可作用的对象。
- 主机面板里它归 eco 的「物品」那一行（申报类型 `neoecoae:items`，写死在基类里），不自己占一行 ——
  绑流体的矩阵也一样：面板按申报类型分行，不按键。eco 自己那只「水 + 圆石 + 岩浆」的矩阵就是同一种形状。
- 默认外观是 eco 那只的整件模型 + L9 格子模型；eco 只出了一张 L9 的格模型
  （`neoecoae:block/cell/storage_cell_l9_infinite_resource`），选 `l4`/`l6` 时若不想用
  L9 外壳，自己给 `.cellModel(...)`。
- 脚本只在 `startup_scripts` 里跑，改完要重启游戏；物品 id 照旧是 `kubejs:<你写的名字>`。

这条入口在本模组：eco 自己的 KubeJS 支持只有配方 schema（冷却、工作站、大型工作站），
没有注册任何 builder 类型，所以脚本在 eco 那边只能写配方、造不出矩阵。引擎是它的，入口是我们的。

## Java 新矩阵注册 API

后续新增的有限矩阵可以使用 `StorageMatrixDefinition`、`StorageMatrixRegistration` 和
`StorageMatrixRegistry`。现有矩阵注册入口保持不变，不需要迁移旧代码。

```java
StorageMatrixDefinition definition = StorageMatrixDefinition.finite(
        ResourceLocation.fromNamespaceAndPath("neoecoprototype1201", "example_cell_1m"),
        StorageMatrixDefinition.MatrixType.ITEM,
        StorageMatrixDefinition.MatrixSize.M1,
        1L << 20,
        1 << 12,
        256,
        SimplifyTier.L1,
        inventoryModel,
        driveModel);

StorageMatrixRegistration.registerFinite(
        Registration.ITEMS,
        "example_cell_1m",
        definition,
        AEKeyType.items(),
        SimplifyStorageCellItem::getItemCellType);
```

定义包含类型、大小、容量、每类型容量、类型数量、等级、待机耗电以及物品栏/驱动器
模型。注册器会校验定义路径与物品注册名一致，并拒绝重复定义（1.21.1 那份还会校验
命名空间与注册器的归属，Forge 1.20.1 的 `DeferredRegister` 不公开自己的 modid，
那一半校验到这里没有着力点）。可用
`StorageMatrixRegistry.get(item)` 或 `get(resourceLocation)` 查询定义。

该 API 当前是新增矩阵的声明和索引入口；已有 finite、chemical、fluid、infinite 各族
仍使用原有 handler 和注册路径，避免改变现有存档行为。

### 可发现性与自动补全

- builder 的所有方法带 `@Info` 中文描述；整合包作者安装 **ProbeJS** 后，在
  VSCode 中编写脚本即可获得 `itemType` / `fluidType` / `cellModel` 等
  方法的自动补全与提示；
- 未安装 ProbeJS 时，以本文档与同目录的 `kubejs-example.js` 为准；
- `.cellModel(...)` 不调用时默认使用无限系列的银灰类型灯模型。
