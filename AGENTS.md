# 仓库约定（1.20.1 这条线）

> 面向 AI agent 与贡献者。这条线是 `F:/neoecoSimplify`（1.21.1 / NeoForge）的**裁剪移植**，两条线独立演化，
> 不是同一份代码的两个副本。**工程事实看 `README.md`**（工具链表、eco 20.5.3 的 API 差异、构建产物名）；
> 本文件只写"怎么协作、什么不能碰"。与 README 冲突时，**以实测重量为准并把两处一起改**。

---

## 1. 环境锁定（今天量过的，别再从记忆读）

MinecraftForge **47.4.20** / Minecraft **1.20.1** / **JDK 17**（`gradle.properties` 用绝对路径点名
`F:/jdk17`，因为日常 JDK 是 21）；AE2 **15.4.10**；eco **20.5.3**（Modrinth 上发布的 1.20.1 构建，
经 `fg.deobf`）—— **编译基线 = 玩家运行下限**，`mods.toml` 里就是 `[20.5.3,)`。
Gradle **8.12.1** + ForgeGradle **6.0.54**，`mappings channel: 'official'`。

dev 侧还有三颗"看得见"的依赖：Mekanism `10.4.16.80`、Applied Mekanistics、JEI `15.59.0.212` ——
全部 `compileOnly + runtimeOnly`，**没有一个进我们的 jar**。JEI 必须是
`u6dRKwZ:jei-1.20.1-forge-15.59.0.212:IrfJO7PN` 这种**点名文件**的 Ivy 坐标：Modrinth 的 maven 布局
把 `<slug>/<version>` 收敛到该版本任意一个文件，15.59.0.212 走 maven 拿到的是 1 826 038 B 的那份，
而 forge 构建是 1 849 781 B；MezzConfig 在 maven 上只有 1.19.2 的构建。

**eco 20.5.3 上"样板迁移"那道门的现状**（2026-11-02 逐条读字节码量的，不是从 1.21.1 推的）：
`ECOMachineInterfaceBlockEntity` 的搬运路径**已经在用** #136 那三条里的两条 ——
`PatternCatalog.containsPatternOtherThan(PatternStorageHost,int,AEItemKey,Object)`（匹配时排除来源那一格）
和 `insertPreparedPatternReporting(Object,ECOPreparedPattern)`（带来源槽的重载），并且
`$PatternTransferTask` 带 `releaseClaims()` 与 `skipCandidate(ECOPatternSourceSlot)`，
所以"候选被丢弃要还锁"也落了。**没落的只剩第三条**：这版 `ECOPatternInsertionResult` 只有 5 个常量
（`UNCHANGED` 是 21.2.11 才加的），也就是"失败要出声"缺、"吞样板"的两条机制成因已堵。
⇒ 开不开母线那道门仍然是一个要拍的产品决定，但代价比我原先记的小一档。

## 2. 最重要的一条：可选依赖不能出现在"会被加载"的位置

**Forge 1.20.1 在构建事件表时会强加载我们 jar 里的每一个类**（`EventSubclassTransformer` 逐个
`Class.forName`）。所以"把第三方引用关进方法体"这条规矩在这里**只挡方法体，不挡类定义**：

- ✅ 第三方名字只出现在方法体里，且被**同一个方法内**的 `ModList.get().isLoaded(...)` 包住 —— appmek / appflux 两族就是这个形状，实测能启动。
- ❌ **`extends` 上游某个第三方类** —— 父类链是 must-resolve，门在它之前不生效。现场：omni 那三只细胞继承 eco 的 `ECOUniversalStorageCellItem`，而它的父类就是 OmniCells 的 `AEUniversalCellItem`，于是 `Could not find parent … → FATAL`，**我们自己的代码一行都没跑到**。

判"某个可选 mod 的族能不能上编译类路径"就一条：**先量父类链，再谈别的**。量法——
`javap` 目标类看 `extends`，逐层向上，任何一层出现第三方命名空间就不能继承。

同类事实：eco 20.5.3 把 `ae2omnicells` 的可选下限写成 `[1.1.6,)`，而 1.20.1 上 OmniCells 最新是
**1.0.7** → 装上也起不来（FML 直接拒）。这条不是我们的接线问题。

**编译类路径也要过同样的闸**（2026-11-02 落地）：感应卡放行读的是
`com.glodblock.github.appflux.common.AFItemAndBlock.INDUCTION_CARD`，那是个字段读，方法体里出现没问题，
但**编译要它**，所以 appflux 在这里是 `compileOnly`。它**故意不配 `runtimeOnly`**：appflux 自己
`mods.toml` 把 `glodium [1.0,)` 写成 mandatory，只声明 appflux 会让 FML 直接拒起 dev 客户端。
两条一起加才成立（glodium 按文件钉：Modrinth `UhW5uCKw/eoUaDkZf`，sha1 `c7534db3…99a378`，
因为 Modrinth 的 maven 布局会把版本号解析成"该版本里随便一个文件"）。1.20.1 的卡片字段名跟 1.21 不同：
**这条线是 `AFItemAndBlock`，1.21 那边叫 `AFSingletons`**，照抄会编不过。

## 3. 资源：单向，从 1.21.1 流向这里

贴图/模型/blockstate/lang/guide 页**只从 `F:/neoecoSimplify` 抄过来**，这里改了不要指望回流——美术分叉是唯一事后合不回去的东西。

**一个必须活过每次同步的例外**：`models/block/storage_controller/controller_formed_base.json`
（与 `_mirrored` 孪生）里四个 `#body` 面带 `forge_data{block_light:15, sky_light:15}`，1.21.1 那份**没有**。
原因在 Forge 1.20.1 会先问上一格"这面能不能被裁掉"再选光照，而 eco 的成型外壳在 `INVISIBLE` 时答"能"，
于是那几格回退到邻居的 0 光、**机器顶面整片黑**。从 1.21.1 直接覆盖回来就会把黑顶带回来。

**发光面在这条线上有一个必须显式写的标记，eco 20.5.3 自己 830/830 条全写、0 条例外**：元素级 `"shade": false`。
它不是装饰——带 shade 的面要再乘一次朝向系数（侧面 0.6、底面 0.5），`forge_data` 把 `block_light/sky_light`
给满也照不回去，所以缺这一个键的"发光"面在 eco 家里就比邻居暗，读起来像"换了张贴图"
（2026-11-02 他就是这么看出来的："闪存晶阵在高等级主机上有别的贴图"）。本仓 205 条发光面里 14 条缺，
全在四只主控成型底板（`computation_cooling_controller/controller_formed_base{,_mirrored}.json` 各 6 颗元素、
`computation_controller/` 两只各 1 颗），2026-11-02 补齐 ⇒ 现量 **205/205 的元素都带 `"shade": false`**。

**`ambient_occlusion` 是另一回事，还没拍。** eco 20.5.3 也在 830/830 条发光面写 `false`，但它自己没有任何模型
写顶层 `ambientocclusion`，而顶层默认是 true ⇒ 在这条线上"不写"与"写 true"是同一件事。eco 21.2.13 反过来
818/854 条不写，我们 1.21.1 那份也是 324 条全不写。所以本仓现在的读数 `false 20 / true 114 / 不写 71`
（20 = 搬来的那对发光线缆 18 + 闪存晶阵两只面 2）**跟 1.21.1 同源、不跟 eco 20.5.3 同源**，要不要整仓压到
false 是他的拍；守卫把这条**印成读数**、不判红。

**批量改 JSON 不许走 `json.dumps` 回合。** 试过：199 个标记改动被 17041 行新增埋掉（每个文件的数组都被重排），
而且第一版行扫描写坏了 190 份模型（只留下元素行，`git checkout --` 整目录回滚）。现在的做法是**只动文本行**，
并且自检三件事：行数只能按插入数增长、原有每一行必须还在结果里、写完后仍能 parse。

**美术交付（TedXenon，`美术新资源/` 是 1.21.1 仓里的交付区）按包整合，不等 1.21.1 先行。**
11-18 那包（`新建文件夹/`）已在 2026-11-21 整合进本线：盈能能源元件改**分档面**（16g 五档静帧、512g 五档各 16×256
16 帧、尾帧 `{"index":15,"time":25}` 停留），blockstate 按 `fullness=N` 指到 `cell_<tier>_N`，物品模型指向空档
面（AE2 自家 energy_cell 同款形状），旧的一档一张脉冲贴图按 README 自己的要求删除。交付包里三张物品贴图
（pattern_generator/backup_drive/扩容卡）与两线现行逐字节相同（那包本来就是它们的出处）；**未迁内容的美术
（寒冰冰淇淋 ×3、工作站卡、工作站 colormap）留在交付包里不进 assets**，随各族代码迁移时再走（colormap 那张
还依赖 21.2.19 的 `WorkstationSpriteSource`，20.5.3 没有这个门，先做也没用）。1.21.1 那条线同样没整合过这包
的盈能分档面——那边动资产时照这份形状抄。坑：闭包遍历贴图要用**替换命名空间之前**的模型 JSON（替换后的
`neoecoprototype1201:` 前缀永远匹配不上 `neoecoprototype:` 的判定）。

**计算主机那对线缆（`ComputationCableModels`）在这条线上换了拆法。** 1.21.1 用 eco 的**双参**查表
`getCableConnectedModel(IECOTier, Block)` 把"发光的 `_normal` 那对"注册到 eco 自己的 `COMPUTATION_DRIVE` 上，
我们自己的帧走**单参**查表拿朴素那对。20.5.3 只有单参版本，表里按档位一键，所以宿主分不出来——原来据此判定
"发光那对在这条线上无法落地"并留了 0 处引用。**那个判定错了，漏了一层**：我们自己的驱动器有我们自己的渲染器
（`SimplifyComputationDriveRenderer`，按 BE 类型注册，精确键优先），eco 的表**只有 eco 的渲染器会读**，
而 eco 的渲染器只画 eco 的驱动器。于是 `cable_l1r`/`cable_l1r_dis`（8 元素 14 发光面 / 3 元素 4 发光面）+ 两张
`*_normal` 贴图搬过来，注册进 eco 的表，我们自己的渲染器改成直接引用 `LOWER_*`——同一份可见结果，无 mixin。
细胞本体仍然**按物品**查（eco 两代都是这张表），所以 CE1/CE1R 在谁家里都是我们那两张 `computation_cell/*`；
我们 `cell_north_a.png` 相对 eco 原件只有 20/256 像素不同（把 LED 换成我们家族的绿），eco 自己 a/b/c 三张同样
只差这 20 个像素（蓝/橙/紫 = L4/L6/L9）。档位颜色跟**细胞**走、不跟宿主走，这条是 eco 的口径。
**仍未眼验**：这套改动要重启客户端才看得见（他 13:30 起那一轮在这之前）。

**指南里的 3D 场景（`ae2guide/scenes/*.nbt`）是这条线上自己产的东西。** 做法：世界里用结构方块存 →
文件落在 `run/saves/<world>/generated/minecraft/structures/<name>.nbt`（gzip NBT）→ 按内容改名拷进
`ae2guide/scenes/` → 在**两种语言**的页里各写一次
`<GameScene zoom="4" interactive={true}><ImportStructure src="../scenes/x.nbt"/><IsometricCamera yaw="45" pitch="30"/></GameScene>`。
守卫现在验五件事：`src` 指的文件在不在、**每个注册 id 被恰好一页声明**（`item_ids` 是 guideme 建"物品 → 页"索引的唯一来源，没页声明的物品在书里点不开；今天一次补进 31 个：所有矩阵细胞、整套存储成员、两只材料、计算收发器与 CE1 阵列）、**两种语言声明的集合相同**、**没有哪颗场景不被 import**、以及**每颗场景的 palette 里每个我们命名空间的方块都真的在这条线上注册**（判据：结构文件是 gzip NBT，方块名只存在于 `palette[]` 里 `Name` 这个 TAG_String 中，其前缀 `\x08\x00\x04Name` + uint16 长度足够定址，不必上完整解析器）。画面对不对仍然验不了——**朝向、成型与否、成员放对格，只有客户端翻开那页才看得见**。
三条读数（现量四颗，逐颗解 NBT）：新存的场景 `DataVersion=3465`（1.20.1 原生），从 1.21.1 抄来的那两颗是 `3955`；
1.20.1 的 `NbtUtils` 遇到本地方块没有的属性会跳过，所以**属性面**旧捕获仍能显示、只是拿默认状态。
**但这句话曾经盖住真正的坑**：那两颗 1.21.1 捕获的 palette 写的是 `neoecoprototype:`（另一条线的命名空间，
这条线一个都不注册），guideme 把未注册的 id 读成 air ⇒ 合成图 10 个 palette 里 10 个、计算图 9 个里 8 个
全部变成空气，**整台多方块的壳层被吞掉**（2026-11-02 他就是这么报的："多方块合成与计算的基础被吞了"）。
当天用 `C:/tmp/scene_namespace.py` 就地把 palette 换成我们的命名空间（逐颗先证明"解析→重序列化"与原文件**逐字节相同**
才敢写；写完再读一遍同样 round-trip 通过），并给守卫补上第五条。**没验的**：这两颗的摆法仍是 1.21.1 世界里的形状，
这条线的最小合法摆法若与它不同，图画是对的但摆法不对——要这条线的真相就得在世界里重存一遍（同 `c1_main_best` 的做法）。
另外 `l1_storage_main.nbt` 的 palette 是干净的，但它体内还留着**改名前**的方块实体数据（`id` 写作 `neoecoprototype1201:drive`
`energy_cell` `storage_host` `storage_vent` 等，都在 `blocks[].nbt` 的 `ForgeCaps`/`visual` 里，本条线不注册这些裸 id）——
不影响方块渲染（渲染只看 palette），**但会不会让预览报错误没量**。
另外结构方块文件里**不要留 `author`/`pos`/`name`/`time`/`metadata`**（现在四颗都干净，顶层只有 DataVersion/size/blocks/palette/entities，每颗 112 格＝整盒捕获），
那是会把玩家名和坐标打进 jar 的地方。

**L1 样板供应器（11-02 搬来）留下三条可复用的口径。** ① 这代的 `MenuTypeBuilder` 只有 `build(String)`，而它把名字过一遍 `AppEng.makeId` ⇒ 用它就会把菜单 id 写进 `ae2:`；我们照装配室的先例走 Forge 的 `IForgeMenuType.create` + `MenuOpener.addOpener`（`Prototype1201.registerProviderMenu`），id 才留在自己命名空间。② `openMenu` / `returnToMainMenu` 在 20.5.3 世代的 AE2 里是接口 `PatternProviderLogicHost` 的 **default 方法**、参数类型是 `MenuLocator`（1.21 叫 `MenuHostLocator`）⇒ **照抄 1.21 的覆写会静默变成重载**，`@Override` 编得过但界面还是 AE2 自己那个。③ 样式文档只能放在 **`assets/ae2/screens/…`**：`InitScreens.register(type, factory, stylePath)` 的路径是 AE2 的 `StyleManager` 按 ae2 命名空间解析的，而我们要 `includes` 它自己的 `pattern_provider.json`，所以我们的 json 与面板贴图必须写进它的文件夹（1.21.1 同一做法，不是我们的发明）。面板能换行的根据也量了：**AE2 15.4.10 的 `screens/schema.json` 的 grid 枚举里有 `BREAK_AFTER_9COLS`**（它自己的 storage_bus/io_bus/formation_plane/cell_workbench/player_inventory 都在用），`textureHeight` 同样支持；画不开只是 AE2 自己那份供应器样式选了 `HORIZONTAL`（它的逻辑本来就是 `this(node, host, 9)`，一行 9 格刚好 8+9×18=170 ≤ 176）。所以 27 格不是特供，是"自带一份把组换成 BREAK_AFTER_9COLS 的样式"。

**线缆零件那一刀没做，但不是因为注册不了。** 这代没有 `IPartHelper`，附属要建 part 只需把 `appeng.items.parts.PartItem<T>` 注册进自己的 `DeferredRegister<Item>`（`IPartItem.getNetworkId` 就是 `BuiltInRegistries.ITEM.getId`）；**唯一必须补的动作**是在模型冻结前调 `PartModels.registerModels(PartModelsHelper.createModels(我们的Part类))`，否则模型的 `part/…` 路径根本不进 AE2 的附加模型表（`InitAdditionalModels` 会 `freeze()`，之后调用直接抛 `Cannot register models after the pre-initialization phase!`）。
**重复摆法不进书。** 同一台机器转个朝向、或只差一个可替换成员的第二张图，**写成一句话而不是第二颗场景**
（2026-11-02 他点名删掉 `l1_compute_interface` 与 `l1_crafting_other_end` 两颗，字节相同的副本在
`_removed_sources/`，页里各留一句说明）。`l1_compute_energized` 这一格现在装的是世界里存的 `c1_main_best`
（1321 B / DataVersion 3465，盈能强化那对成员落在他要展示的那一行）；被它换掉的那颗也留在同一备份目录里。

## 4. 数据一致性：`tools/check_data_sync.py`

配方 ↔ lang ↔ 成就三方一致由它兜：它数的是"注册过的细胞 / 拆解表 / 成就节点引用的 id / 有配方来源的 id"，
任何一边缺件就红。**它同时读三类文件，所以按家族拆提交会让中间某条提交守卫红** —— 需要拆时先确认拆点不切断三方。
现量（`1f96399` 之后）：20 只细胞 / 19 份拆解表 / 14 个节点、65 已注册 id、126 有来源 id、引用全解析。
**无限混凝土矩阵故意没有拆解表** —— 那是"压缩艺术，不可拆卸"那句文案，不是漏了。

2026-11-02 加了第四类读数 `check_models()`，管模型这一族（它过去完全没有守卫面）：
①**每个带发光面的元素**必须写 `"shade": false`（§3 那条口径；AO 只**印读数**、不判红，理由见 §3 那一段）；
②元素不许**混**发光面与不发光面（shade 是元素级的，混了就至少有一面是错的）；
③`textures`/`parent` 里指我们自己命名空间的 id 必须有对应 png/json；
④`models/block/computation_cable/` 与 `computation_cell/` 里**文件名 ↔ java 里的 `id("…")` 字面量双向相等**
——这两族不是被游戏自己发现的资源，是代码点名的图，缺哪一边都是静默。
现量：205 条发光面全平光、4 颗线缆模型都被 java 点名、纹理与 parent 全解析。
**控制组建过并逐条红在预期的那句**（每步按原始字节还原、还原后全绿）：摘掉 `cell_l4_formed` 的 shade、
给 `cable_l1r_dis` 的 `light` 元素抹掉一个面的 `forge_data`（造出混面）、把纹理键指到不存在的 png、
把 parent 指到不存在的模型、往目录里扔一颗没人点名的 `cable_l1_orphan.json`（红在"没有 java 点名它"）、
以及把 `cable_l1r.json` 改名（红在"java 点名了但这份不发货"）。
**验不了的仍然验不了**：这四类都是形状读数，面到底亮不亮只有客户端看得见。
同一天给 `check_pages()` 补了第五条：**每颗场景 palette 里的方块必须在这条线注册**（§3 那条"壳层被吞"就是它漏的）。
三条控制组各自红在预期那句、按原始字节还原后全绿：①把 `l1_crafting_min.nbt` 换回 git 里那份 1.21.1 捕获（同时点亮
"另一条线的命名空间"与"没有我们方块"两句）；②把一颗 palette 名字改一个字母（`..._drive`→`..._xrive`，长度不变才敢直接写
gzip 体）；③把我们的命名空间前缀换成一个没人注册的 20 字节前缀。改动只在解压后的正文里做，重新 gzip(level 9)。

### 4.1 从 1.21.1 抄过来的 JSON，有四处在这条线上换了形状（全部静默）

| 东西 | 1.21.1 写法 | Forge 1.20.1 现实 | 抄错的后果 |
|---|---|---|---|
| 成就的条件 | 顶层 `"neoforge:conditions": [...]` | **只认包装形状** `{"advancements":[{"conditions":[…],"advancement":{…}}]}`（`ConditionalAdvancement.processConditional`） | 顶层键被当未知字段忽略 → 节点**无条件加载**，缺模组时判据点名的物品不存在，树里多一颗永远点不亮的洞，0 条日志抱怨 |
| 配方的条件 | `"neoforge:conditions"` | 裸键 `"conditions"` + `forge:mod_loaded` | 我们 18 份条件配方本来就是对的；**别把成就那套写法搬过来** |
| 成就的图标 | `{"id": "ns:item"}` | `{"item": "ns:item"}` | 守卫已拦（`display.icon still uses 1.21's 'id'`） |
| 配方的原料/产物 id | 一样 | **必须带命名空间** | 少写 `neoecoprototype1201:` 的那一格被读成 `minecraft:simplify_storage_controller` ⇒ `ShapedRecipe.itemFromJson` 抛 `Unknown item`，**整条配方被丢弃**，日志里只有一行 `Parsing error loading recipe …`（现量：玩偶那只就这么丢的，其余 4 条没事） |
| 模型的面级光照 | `"neoforge_data"` | `"forge_data"` | 发光层不再是全亮烘焙光 → 整张面板像被乘 0.05（见 §3 那条例外） |

守卫 `check_node` 现在两件事都做：认包装形状并递归查内层、见到 `neoforge:` 前缀键直接红。
**控制组建过**（临时塞一颗带 `neoforge:conditions` 的假节点 → 红的正是那句，摘掉后 14 颗全绿）。

2026-11-02 又补了一条 `check_data_ids()`：它走 `data/` 全部 JSON，凡是 `item` / `tag` / `name` / `id` / `items`
这几个键上的字符串**没有冒号**就判红。表里那一行**不是一条两代差异**（1.21.1 也要求带命名空间），它进表是因为
旧规则的口径漏：`member_ids()` 只挑"看起来已经是我们命名空间"的串，所以**漏命名空间的那一份恰好不在它的视野里**
——守卫当时报"all references resolve"，是服务端那行 `Parsing error` 把它揭出来的。**分母印在读数里**
（现量：427 条 id 被扫、0 条缺命名空间），因为"0 问题"和"扫到 0 条"是同一个数字；控制组是把 reliqwq 配方里
三格原料的命名空间抹掉一格 ⇒ 正好红 1 条、点名的就是那份文件，按原始字节还原后全绿。

机魂那次（2026-11-02 晚）再加一条 `check_blockstates()`：它走 `assets/<ns>/blockstates/`，①**每一条指向我们自己命名空间的 model 都必须有对应 json**（现量：916 行里 69 条这种），②并且**合成主机的每一"形状"必须交齐四行外观**（无脸 + mind_tier 0/1/2）。这条规则存在是因为 68 行的 multipart 里少一行完全不打日志——有脸的主机只会继续画那张没脸的面板，玩家报的是"表情没换"，不是"资源缺失"。现量读数：formed 的 16 个形状 × 48 行脸。**控制组两条各自红在预期那句**：删掉一行 `mind_tier=2` ⇒ 红的正是 `('north','false','false') shape offers ['0','1','absent']`；把一张脸指到不存在的 `controller_l1_formed_face_7` ⇒ 红的正是"ships no such model"；按原始字节还原后 916/69 与 16×48 复现。

### 4.2 Java 侧的形状差（玩偶与机魂那两次搬运，逐条 javap `forge-1.20.1-47.4.20_mapped_official_1.20.1.jar` 与两颗上游 jar）

这些不是"写法偏好"，是编不过 / 编得过但静默不对的分界：

| 1.21.1 那条线上 | 这条线的现实 | 踩错的后果 |
|---|---|---|
| `implements Equipable` + 无参 `getEquipmentSlot()` | **一模一样**（`ArmorItem` 也是 implements 它） | 不用改 |
| `Holder<MobEffect>`、`getEffect(Holder)` | `MobEffect` 裸类型（`MobEffects.NIGHT_VISION` 就是 `MobEffect`） | 编不过 |
| `getDefaultAttributeModifiers(ItemStack)` 返 `ItemAttributeModifiers`；`EquipmentSlotGroup.HEAD` | 两个类都不存在。装备扫描走的是 `ItemStack#getAttributeModifiers(EquipmentSlot)`，它的**末条 invoke 是 `Item#getAttributeModifiers(EquipmentSlot, ItemStack)`**（Forge 加的口子）⇒ 覆写这一条 | 覆写错的那条 = 护甲值永远不生效，客户端看不出来 |
| `AttributeModifier.Operation.ADD_VALUE` | 这条线叫 **`ADDITION`**（`ADD_VALUE` 是 1.20.5 起的改名） | 编不过 |
| `DataComponents.ITEM_NAME` 上色 | 覆写 `Item#getName(ItemStack)`；**不是** anvil 改名，所以不斜体、不算重命名 | 用 CUSTOM_NAME 那条等价物会让 tooltip 斜体并且算改名 |
| `DataComponents.ENCHANTMENT_GLINT_OVERRIDE` | 覆写 `Item#isFoil(ItemStack)`（现量：`ItemStack#hasFoil()` 就是 `getItem().isFoil(stack)`） | 光泽丢了 |
| `PlayerTickEvent.Post` + `getEntity()` | `TickEvent.PlayerTickEvent` + `phase == TickEvent.Phase.END` + public 字段 `player` | 编不过 |
| `Capabilities.EnergyStorage.ITEM.getCapability(stack, null)` | `stack.getCapability(ForgeCapabilities.ENERGY)`（`ItemStack extends CapabilityProvider`），返回 **LazyOptional** | 判 `!= null` 会恒真（空的是 empty()，不是 null） |
| `net.neoforged…client.model.data.ModelData` | `net.minecraftforge.client.model.data.ModelData`，**`EMPTY` 在**，七参 `renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int, ModelData, RenderType)` **逐字存在** | 只换 import 那一行 |
| `protected getRenderShape` / `rotate` / `mirror` | 这条线上三个都是 **public**，子类保留 `protected` 就是收窄 ⇒ 编不过（`createBlockStateDefinition` 仍是 protected，别一起摘） | 编不过 |
| 数值从 `ModConfigSpec` 现读 | **这条线 0 处配置系统**（全树 `ForgeConfigSpec` / `ModConfig` 命中 0）⇒ 玩偶充电速率落成了常量 100 AE/t，与 1.21.1 那份配置的默认值同数 | 想"照抄配置"就得先给这条线起一整套；已判定不值 |
| `PartModels.registerModels(...)` 放在客户端设置的 enqueueWork 里 | **那是竞态**：AE2 的表在 `InitAdditionalModels.init(ModelEvent$RegisterAdditional)` 与 `CableBusModel.resolveDependencies` 两处冻结，谁先到都算。2026-11-02 加完 Jade 插件之后同一行就晚了一步 ⇒ `IllegalStateException: Cannot register models after the pre-initialization phase!` 直接从 `DeferredWorkQueue` 里把启动打断 | **只在模组构造子里注册**（构造期没人能烘焙模型），并按 `FMLEnvironment.dist == Dist.CLIENT` 分支；1.21.1 那条线上是同一课（`NeoECOPrototypeClient#registerPartModels` 的注释记的是玩家报的启动崩溃） |
| Jade：注册 provider 不需要 lang 键 | 11.13.2 在 provider 注册时 **断言 `config.jade.plugin_<modid>.<uid path>` 存在**，且这条只在物理客户端走 | 缺键 = `AssertionError: Missing config translation: …`，崩在加载界面。已补两份 lang（现 **123 键**），守卫 `check_jade_keys()` 从 `integration/jade` 里读 uid 再查两份文件；控制组抹掉 en 那条 ⇒ 红的正是那句 |
| `BlockEntity#saveAdditional(CompoundTag)`（1.20.1 原版是 protected） | AE2 15.4.10 把它声明成 **public**（`javap appeng.blockentity.AEBaseBlockEntity`） | 子类写 protected 直接编不过：`正在尝试分配更低的访问权限; 以前为public` |
| 额外字段落盘走 `loadTag(tag, registries)` | 这条线是**单参** `loadTag(CompoundTag)`（`AEBaseBlockEntity#load` 依旧 final，路由点没变，变的是签名带不带 `HolderLookup.Provider`） | 编不过 |
| `getAdvancements().get(rl)` 返 `Optional<AdvancementHolder>`，`award(Holder, criterion)` | `ServerAdvancementManager#getAdvancement(rl)` 直接返 **可空的 `Advancement`**，`PlayerAdvancements#award(Advancement, String)` | 编不过；**别把 null 判断写成 orElseThrow**，那是这条路径唯一会说话的地方 |
| Jade：`@WailaPlugin`（1.21 无参） | `WailaPlugin.value()` 是**必填的 modid**（11.13.2），插件由 Jade 自己扫描 `Class.forName` 装载 | 少传 modid ⇒ 编不过；更关键的是**发现失败没有别的读数**，所以 `register()` 里打了一行 `neoeco1201-jade` 日志：玩家报"提示里没有机魂"时，那行能分开"Jade 没扫到我们"和"扫到了但内容不对" |
| 方块实体要进网格就得注册 `AECapabilities.IN_WORLD_GRID_NODE_HOST`（NeoForge 21 的 `RegisterCapabilitiesEvent`） | **这条线一行都不用写**：`GridHelper.getNodeHost(Level,BlockPos)` 先做 `blockEntity instanceof IInWorldGridNodeHost`（现量 offset 0-19），Forge capability 只是 offset 25 的兜底；而 AE2 自己的 `AENetworkBlockEntity → IGridConnectedBlockEntity` 已经 implements 那个接口 ⇒ 我们只要把 BE 类型交出去（`ModBlockEntities.wire → setBlockEntity(…, null, serverTicker)`，第 4 参才是服务端 ticker：`setBlockEntity` 的 `putfield` 顺序 + `getTicker` 读 `clientTicker` 只在 `isClientSide`） | 照抄那行会引一个这条线没有的类（`appeng.api.AECapabilities` 在 15.4.10 命中 0，capability 在 `appeng.capabilities.Capabilities`）⇒ 编不过；反过来"以为必须要注册"会把已经免费的通道做成一笔假成本（口算单元那次搬运实测就是零行，见 `b487edf`） |

**同一个方向的另一个坑：文本抄过来了，这条线上没有读者。** `ECOCellType.desc()` 在 20.5.3 全 jar 1847 个类里
只有一个读者 —— `HostText.storageTypeAccentColor`，而且只取 `desc().getStyle().getColor()`；
**没有任何类含 `eco_cell_type` 这个字符串**，所以 `eco_cell_type.<ns>.<name>` 那类 lang 键不是上游在翻译的行标签
（1.21.1 那条线上 eco 确实把 desc 当行标签用）。这条线上我们只有一颗自造行（无限混凝土，`visible=false` 且不注册），
所以那行文字在这条线**任何界面都画不出来**。**结论：键留着**（lang 属于单向文本流，删了就是在不该分叉的文件上分叉），
但别在文档/指南里声称"面板会显示这一行"，也别为它去做渲染。玩家看得到的名字是物品名。

### 4.3 小宗搬到这条线时换掉的六件事（11-02 夜，每件都有理由，不是随手改）

| 1.21.1 那边 | 这条线 | 为什么 |
|---|---|---|
| 元件工作台里 `ae2:storage_cell_upgrade` 做升级，另外退你一颗 1M 元件 | 工作台 shapeless，不退东西 | AE2 15.4.10 **没有这个配方类型**（1455 个类里 `storage_cell_upgrade` / `cell_upgrade` 0 命中）。自己造一个配方类型 + serializer + JEI 槽位是这项目第四次引擎级手写 |
| 只有流体与化学品基础盘有拆解表 | 物品 + 流体两只有；**两只扩展版故意没有** | 扩展版若能拆回"外壳 + 元件"就等于把扩展卡白送回去。守卫的 `UNDISASSEMBLABLY` 收了这两只并写明理由 |
| 化学品那一对 + 第三只外壳在这一族里 | **这条线还没有** | 它同时要 `megacells` 与 `appmek` 两道门，而守卫认的是"目录名＝那一个 modid"（`integration_registers()` 取 `integration/<modid>/`）；双门要先给守卫加规则，那一刀单独拍 |
| 流体外壳配方用 `neoecoae:mega_fluid_cell_housing` 且**没带条件** | 用 `megacells:mega_fluid_cell_housing`，九份配方**全部**带 `forge:mod_loaded` | 前者在 eco 20.5.3 上静态确认不了，后者是 MEGA 自己 lang 里就有的；"没条件"是 1.21.1 的缺陷（缺件时配方引用不存在的物品），不抄 |
| 盘的面板行靠继承的 `getConfigInventory` + DataComponent 读写 | 覆写 `getConfigInventory` 走 `CellConfig.create(filter, stack, n)` | eco 20.5.3 继承的那版调**两参** `CellConfig.create`，里面 `bipush 63` **硬编码 63 格** —— 3 标记的盘会在元件工作台里拿到 63 个格。三参版给数量，持久化还是 AE2 的，我们不写 tag |
| 内容交给标准 `ECOStorageCell`（外加一段 long-bulk 迁移） | 同样交标准盘：`bytes / bytesPerType` 里 `bytesPerType=1` 就是那格"近乎无限"。迁移不搬，这条线从没发过带旧 payload 的版本 | 我们一度改用 `ECOBulkStorageCell`（唯一按 `BigInteger` 记账的那支），**结果被 tooltip 炸穿**：`getTotalItemTypes` 把 stack 的物品强转 `ECOBulkStorageCellItem`，而那个类只服务物品介质——两个构造子都是 `(Properties, IECOTier, Supplier<ECOCellType>[, int])`、里面写死 `AEKeyType.items()`，扩展卡认死 `neoecoae:eco_mega_upgrade_card`，类型上限校 1..25。现场 2026-11-02 23:10:43 客户端崩溃：创造栏建搜索树时给每个 stack 生成 tooltip，`ClassCastException` 点名我们的类——**任何** Inventory 都会走到，流体盘更是根本用不了它 |

**这一族的闸是"注册或不注册"，跟 must-resolve 无关**：`SimplifySmallBulkCellItem` 的父类链到 `Item` 为止（eco 20.5.3 自己的 `ECOMegaItemStorageCellItem` 同形，它也只多覆写一个 `isBlackListed`）。门是为了 `mega_item` / `mega_fluid` 那两行——eco 20.5.3 只在装了 MEGA 时由 `MegaCellsIntegration` 注册它们（dev 日志现量：`Loading integration …megacells.MegaCellsIntegration`）。id 一律写成字面量放在 `integration/megacells/` 里，走 helper 传变量的话守卫认不到它属于哪个可选 mod，配方条件与标签那条"只在装了它时才注册"的规则就会漏判。

**装上游件仍然要先量命名通道**（§5 那条 AE2 的坑在这族上重演了一次）：MEGA Cells 这条线用的是 Modrinth release 的 `megacells-forge-2.4.6-1.20.1.jar`（sha1 回读一致），而不是任何 maven 件。

**一族新盘在这条线要接四处，只接物品面 = 插进驱动器什么都不画**（11-02 夜现场：c67acf0 发完，玩家报"没驱动器贴图"；6705e49 补齐）。四处各有各的下场：

1. `ECOCellModels.register(item, 模型id)`（`Prototype1201Client` 客户端装配那段）——`SimplifyDriveRenderer:63` 走 `getModelLocation(cellStack.getItem())`，**答 null 就静默不画，日志 0 条**；
2. `ModelEvent.RegisterAdditional` 里的 `everyCellModel()`——没有别处点名的盘模型**根本不会被 bake**，所以第 1 处接了、第 2 处漏了同样是空；
3. `onRegisterItemColors` 的 `tinted` 列表——eco 20.5.3 给的是染色函数本体，这条线**每个盘都得自己交名字**，漏了的那族状态灯带永远是白的（不是崩，是"看起来像没做的"）；
4. 四张 rung 模型 + 外壳图，按 §4.1 的三处转换从 1.21 抄（`neoforge_data`→`forge_data`、`cutout`→`cutout_mipped`、去 `format_version`）。

**守卫看不见第 1 与第 3 处**：`check_data_sync.py` 的模型扫是"模型文件里的纹理与 parent 解析不解析"＋"java 点名的 4 张缆模型在不在文件里"，方向是 java→文件；**"某个已注册的盘物品没有任何一处 java 认它"这条没有规则**，因为盘模型名与物品 id 不成派生关系（`simplify_small_bulk_storage_cell` ↔ `storage_cell_l1_small_bulk_item`），照 id 猜就是把守卫写成假绿。所以这四处按家族逐条对，别只加物品侧。

**它带进来的上游噪声，别判成我们的**：装上 MEGA 之后 dev 日志固定有 1 条 `Couldn't parse element loot_tables:megacells:blocks/mega_emc_interface`、1 条 `Parsing error loading recipe megacells:cells/standard/radioactive_chemical_cell`（`Unknown item mekanismgenerators:reactor_glass`）和 25 条 `Skipping loading recipe megacells:…`——全是 MEGA 在向这包没装的 mana / source / EMC 模组要东西。**所以 §5 那句"0 条 `Parsing error loading recipe`"的口径从今往后按"0 条我们命名空间里的"判。**

### 4.4 omni 四支与魔源的移植规格（11-21 侦察；判死已翻案，跟随 eco）

**omni 四支可移，跟随 eco 自己的集成机制。** 11-21 晚把"不可移"的旧结论翻了案：旧判据是"Forge 1.20.1 强载全 jar ⇒ 父链够到 OmniCells 就崩"，但 eco 20.5.3/20.5.9 自己在 1.20.1 上注册 omni 盘（`eco_omni_cell_*`，父链同样经 `ECOUniversalStorageCellItem` 直通 OmniCells），且 `ae2omnicells` 是它的 optional 依赖——活得好好的，判死不成立。真实机制（javap 现量，20.5.9）：

1. 集成类标注 eco 的 `@Integration("ae2omnicells")`（`api/integration`，CLASS 保留注解，value = modid）；
2. Forge 的 mod 扫描（`ModFileScanData.getAnnotations()`）覆盖**全包每个 mod jar**——我们的 jar 也在内，扫描只读字节码不加载类；
3. eco 构造器里 `IntegrationManager.compileContent()` 建表、`loadAllIntegrations()` 按 modid 门后 `newInstance()` + 调 `apply()`/`applyClient()`（dev 日志 "Loading …integration … for …" 的出处）；加载序 eco 先于我们（我们 load-after eco），apply() 里往 `Registration.ITEMS` 添加的条目赶得上我们自己的 `ITEMS.register(modBus)`；
4. 没装 OmniCells：集成类不实例化 ⇒ 物品类不加载 ⇒ 父链永不解析 ⇒ 不崩。旧会话量到的崩溃多半出在没门或静态引用，等价于"无条件注册"的错，不是父链本身。

形状照抄 `AE2OmniCellsIntegration`：public 无参构造 + `apply()`（注册物品，往 `Registration.ITEMS`）+ `applyClient()`；本线客户端接线保持自家形状（Client 类摸 `Integration.CELLS` 列表，没装时列表为空）。乏核的三重门保持 1.21.1 形状（方法体内 mekanism+appmek 双查）。

**魔源一族可移**（父链停在我们自己的 `PrototypeCellItem`，与 FE 家族同形；`Integration/arseng/` 门 = arseng）。规格：

- arseng 1.20.1：同走 `@Integration("arseng")`；Modrinth IJI3QuK8，forge 1.20.1 最新 `arseng-2.1.1-beta.jar`（123 229 B，sha1 e46e7cf5c4fc5f48d1b1ea025eb59c484811b2c6，2026-11-21 已下载进 libs/ 未接线）；`gripe._90.arseng.me.key.SourceKey/SourceKeyType` 类路径与 1.21.1 一致（jar tf 现量）；硬依赖 Ars Nouveau（TKB6INcv）+ AE2，dev 跑要连 Ars Nouveau 一起进 run/mods。
- eco 20.5.3 有 L4/L6/L9 魔源盘 ⇒ `neoecoae:source` cell type 在；`EcoCellTypes.require("source")` 问的是 eco 自己的 `NERegistries.CELL_TYPE`，同一口可用。
- 前置件：`SimplifySingularityCellItem`（470 行）+ `SingularityCellHandler`（218 行）必须先搬——魔源的五档盘、growing cell、面板行三处都踩在这两个类上。1.21.1 的 Bank 走 **DataComponent**，1.20.1 没有这条机制 ⇒ 改物品 stack NBT（两个 long），`SINGULARITY_CELL_BANK` 的注册不存在也不需要。
- AE2 15.4.10 差异两处未验：`StorageCellTooltipComponent`（预览图那条）与 `AEConfig.isTooltipShowCellContent()` 方法名，javap 后照实改；没有就把 `getTooltipImage` 答 empty，预览是便宜的一半。
- config 六项：`singularity_cell` 一对 + `sky_stone_dust_cell` 一对 + `source_cell` 一对（魔源的 SelfGrow 走 source 那对）。
- 内容 7 件：source 五档盘（PrototypeCellItem 五参构造子正合适，bytesPerType=bytes>>8 恰好是 256 档的价）+ housing + growing cell。
- 资产：类型条纹用 `cell_type_lower` 的 y12-15 轨（本线 FE 占到 y9-12，12-15 空，javap/Pillow 现量），盘模型 ×5（l0_source_1k/16k/64k、l1_source、l1r_source）+ 外壳贴图 + item 模型；照 §4.1 三转换 + §4.3 四处接线（每家族逐条对，守卫看不见第 1 与第 3 处）。
- 配方 8 份从 1.21.1 转换（`neoforge:conditions`→`forge:conditions`、result `id`→`item`、命名空间 1201），lang 双文件 7 键，`matrix_types.md` 补 source 段并 claim 全部 id（`check_data_sync.py` 的认领闸会拦）。
- 奇点单元自己的 lang 键与 `eco_cell_type.neoecoprototype.singularity/sky_stone_dust/source` 三条一起进。

### 4.5 EC9 与工作站卡的移植侦察（11-21，未开工——这是一张设计单，不是机械移植单）

**机器模型不同源，不是家族拆分小手术。** 1.20.1 的装配室 BE 只有 166 行（旧轻量形状，`resolve(level, pattern, output)` 三参签名），而 1.21.1 的独立机器引擎（638 行：pattern push、批量周期、流体产出、升级栏）整个建在 AE2 19.2 的 API 上（IUpgradeableObject / IGridTickable / ICraftingMachine）。搬 EC9 的家族拆分 = 先搞清 1.20.1 装配室靠什么驱动（eco 加工路径？AE2 压印路径？），再把"家族/批量/派生"三个概念适配到那套模型上。三把前置量：

1. 1.20.1 BE 的驱动源：它实现什么接口、谁调它的 tick（读 166 行全文）；
2. `ProcessorAssemblerRecipes` 的派生机制现状（有 `InscriberRecipe` import，但 11-09 的 count==1 修复在不在、resolve 有没有 includeDerived 形状）；
3. 工作站卡的两扇门（`ECOWorkstationModules.register` / `ECOWorkstationRecipeAdapters.register`）在 eco 20.5.3 上存不存在（javap `cn.dancingsnow.neoecoae.api.workstation.*`），以及 Forge 1.20.1 的 SizedIngredient/流体成分类形状（NeoForge 包名不同）。

EC9 的常量（本线无 config）：batch cap 64、升级栏 6 格、`l1Name` 绿名照 1.21.1。资产四件（blockstate/model/贴图 `ec9_processor_assembler.png`/配方）照 §4.1 三转换从 1.21.1 抄。JEI：本线已有 jei 包，加第二 catalyst 一处。

**工作站卡**（170 行）：机制 = eco 大型工作站两扇门 + 把我们的配方类型从 eco 的 catch-all（OTHER 分类）里摘出来。上有双前置：eco 20.5.3 的两扇门 API 存在性 + Forge 成分类适配。门不存在则卡片降级为"注册但不进栏"（grette 先例）或跳过。

### 4.6 扩容卡系统：剩余工作清单（11-21 侦察——比预想小得多）

**卡片本体已在本线**：`items/SimplifyStructureExpansionCardItem.java` 997 行（1.21.1 为 1333 行），grow/upgradeTier/swapInPlace/工单票据系统/handOverTicket 全在——上一轮移植迁移驱动器时把基类完整带过来了。两个 lookup（growWorkOrder/upgradeWorkOrder）**故意答 null**，javadoc 写明"this line does not ship the card family"。⑦ 剩的全是注册面：

1. `BackupDriveItem.java`（1.21.1 为 251 行）——载物大概走 DataComponent，1.20.1 要改 NBT 或能力（先读再定）；
2. 注册四件：卡片（带 `nameColor` 染名的构造子）、backup drive、12 张工单（plain Item，stacksTo(16)）——1.21.1 的注册块在 ModRegistration 1183-1250，逐行可抄；
3. 两个 lookup 接线：映射到本线存在的定义——`SimplifyStorageDefinition.L1`/`SimplifyCraftingDefinition.L1`（本线有）+ `NEMultiBlocks.STORAGE_SYSTEM_L4/L6/L9`、`COMPUTATION_SYSTEM_L4/L6/L9`、`CRAFTING_SYSTEM_L4/L6/L9`（eco 20.5.3 有，javap 待验）。**Trinity 行跳过**（本线未移植 Trinity）；
4. 配方两份：`structure_expansion_card.json` + `structure_expansion_card_from_backup_drive.json`（1.21.1 原文转换）；
5. lang 双文件 16 键（卡+desc、backup_drive+desc、12 工单）+ `matrix_types.md` 或 assembler 页 claim + 创造栏 accepts；
6. 工单的 12 键名照 1.21.1（扩容/升级 + 上游措辞）。

**核对过的既有事实**：卡片 useOn→grow/upgradeTier 的三参形状与 1.21.1 一致；`MigrationDriveItem extends SimplifyStructureExpansionCardItem` 在两线同构；本线无 config（1.21.1 的 CARD_CONSUMES_ITEMS 配置项 → 常量 true，字段已在）。### 4.7 杂项（⑧）的移植面量化（11-21 夜；创意件已落地）

- **KubeJS 集成：已落地（11-21 夜）**。移植面：存储矩阵三件（`api/StorageMatrixDefinition` record 照搬 / `api/StorageMatrixRegistry` 静态表 / `items/SimplifyStorageCellItem` extends eco 的 `ECOStorageCellItem`，20.5.3 构造子同形——`PrototypeCellItem` 已证）+ `MatrixMaterials`（命名空间 1201）+ `items/CustomInfiniteCellItem`(+`Handler`) + `items/EcoSupplyCellItem`（20.5.3 的 `ECOInfiniteResourceCellItem` 五参构造子已 javap 证实）+ `integration/kubejs/` 七件（插件/三 builder/两 Support/schema）+ `kubejs.plugins.txt` + `docs/kubejs.md`/`kubejs-example.js`（按本线改写：离线改色节没搬——工具只在那条线；Mekanism 示例换成 10.4 形状；档位/材质表按本线事实改写）。**量过的形状差，逐条对过 jar**：① 插件是类（`extends KubeJSPlugin`），builder 类型注册在 `init()` 里调 `RegistryInfo.of(Registries.ITEM, Item.class).addType(...)`（BuiltinKubeJSPlugin.init 的字节码同型）；② schema 走 `registerRecipeSchemas(RegisterRecipeSchemasEvent)`，组件名是 `ItemComponents.INPUT_ARRAY/OUTPUT`（`InputItem[]`/`OutputItem`，不是 1.21.1 的 Ingredient/ItemStack 键），unique 走 `uniqueOutputId`——`uniqueId(RecipeKey)` 重载不存在；③ 模型钩子是 builder 覆写 `generateAssetJsons(AssetJsonGenerator)`（1.21.1 是 `generateItemModels(KubeAssetGenerator)`），`ModelGenerator.parent` 收 **String**；KubeJS 6 没有 `.textures({...})` 一口喂法，用 `.texture(槽位, id)`；④ builder 类型引用（`RegistryInfo` implements rhino 的 TypeWrapperFactory）逼着 compileOnly 也要带 rhino（`libs/rhino-forge-2001.2.3-build.10.jar`，Modrinth sha1 已验）。⑤ **Mekanism 10.4**：`MekanismAPI.CHEMICAL_REGISTRY` 不存在（是四个子注册表的 ResourceKey），`ChemicalStack` 抽象——`resolve` 探四个子注册表 + `chemical.getStack(1)` 喂 `MekanismKey.of`；本线化学细胞是普通 `PrototypeCellItem`（keyType=`ChemicalKeyType`），每类型成本是家族自己的 `bytes >> 8`，不是物品/流体的 `typeFee`。⑥ 本线 omni 细胞的 `TOTAL_TYPES=512`（1.21.1 是 256），OmniMatrixSupport 的定义读数跟着走。⑦ Forge 的 `DeferredRegister` 不公开 modid，`StorageMatrixRegistration` 的命名空间半边校验没有着力点，只留路径半边（javadoc 写明）。**发货形状 = eco 自己那个先例**：插件类只被 `kubejs.plugins.txt` 引用，我们自己的代码 0 处无条件点名它；生产包 F:/1201prod 跑 eco（同样 `extends` 缺席的 KubeJS 父类）无 KubeJS 照常启动。依赖是 `compileOnly files(libs/kubejs…26.jar)`，**故意不配 runtimeOnly**：无 KubeJS 的 dev 启动正是 §2 闸要验的那条路。**还欠的读数**：~~无 KubeJS 冷启~~**已过（11-21 12:58，主菜单到达，本 mod COMMON_SET，插件类无人强加载）**；装 kubejs/rhino/architectury 到 run/mods 再起一次（脚本面眼验）仍欠。**首启热修（11-21 12:40→12:58 三轮）**：① 首启崩在 **arseng 自己**（`NoClassDefFoundError: arsnouveau/…/ISourceTile`，事件表强加载先于它的依赖屏幕——与本线 11-02 的 omni FATAL 同机制，这次在它那边）；它 mods.toml 虽声明 ars_nouveau mandatory 但 dev 里没有 Ars Nouveau 就是起不来 ⇒ **arseng 撤 runtimeOnly 留 compileOnly**（appflux 先例，源家族在 dev 缺席、isLoaded 闸兜住），原注释里"Ars Nouveau 已在 runtime"那句被证伪删掉。② 首启日志 34 条缺模型 = **上一轮 omni/魔源/EC9/扩容卡/工单移植全没过客户端**：23 只（EC9 物品、三张升级卡、扩容卡、backup_drive、12 工单、omni 1m/1k）1.21.1 有 ⇒ 已按单向流拷入 + 6 张贴图（卡片五张带一张 mcmeta）；`singularity_cell_housing.png`（16×192 十二帧）上一轮只拷 PNG 漏了 mcmeta ⇒ 陨石粉/奇点自产两件"挤成一张图"已补 mcmeta。③ **11 只 `l0_/l1_*`（omni/源的驱动器芯片名）两线都没有**——1.21.1 客户端的梯子表也点这些名字，本线照抄 ⇒ omni/源细胞在驱动器里的芯片两侧同形地走 eco 兜底，是两侧共同的既知缺口，不是本线退化；谁先做模型都行，做时按 `storage_cell_l1_<family>` 命名。④ 日志噪声两条与本线无关：LDLib 自己的 TestJEIPlugin 在 JEI 注册期抛 NPE；`megacells:mega_emc_interface`/`mekanismgenerators:reactor_glass` 是 JEI 侧对缺席 mod 物品的抱怨。**模型闭包的坑记一笔**：拷模型要"先闭包贴图、再做命名空间替换"——替换后的 JSON 前缀是 `neoecoprototype1201:`，拿它找 `neoecoprototype:` 前缀的贴图引用永远不命中（这次 23 只模型第一轮就栽在这，靠数据闸抓回来）。
- **创意三件：两件已落地（11-21），一件推退**。`all_items_creative_cell` + `pattern_generator` 按 1.21.1 注册点直移（Registration 的 concrete 元件段 + 创造栏两项 + `AllItemsCreativeCellHandler::register` 走 `enqueueWork`，与 InfiniteConcreteCellHandler 同位）。本线量的形状差，逐条对过 jar：1.20.1 无 `RecipeHolder`（配方自己带 `getId`）；AE2 15.4.10 的 `encodeProcessingPattern` 收 **GenericStack 数组**不是 List；eco 20.5.3 的工作站配方读**自己的** `platform.recipe.SizedIngredient`（本线不得引 Forge 的同名类）；`LargeIntegratedWorkingStationRecipe` 没有 `id()` 分量——它的 `getId` 继承自 `Recipe` 接口，小型那族才覆写 `id()`；20.5.3 的 `IECOStorageCell` 上**没有** 1.21.1 的 `visitExactAmounts`/`hasSaturatingListing`/`ExactAmountSource`，所以全物品元件的"无限"只到列出量为止，eco 按"很大的有限数"计费（指南页写明）。描述行走本线的 per-item `appendHoverText` 形状（扩容卡同款），不走 1.21.1 的 tooltip 事件。指南新增 `creative_helpers.md` 双语页，认领两只 id；数据闸 123→**125 of 125**。**`workstation_preview` 推退**：它唯一的存在理由是给工作站卡的指南页展示"装上后的外观"，而工作站卡在本线未移植（§4.5——20.5.3 缺两个 addon 口子），先没东西可展示；跟工作站卡一起开。
- **成就族：不需要移植（11-21 核对，推翻本节原判断）。** 原记录说"1.21.1 的 `assembler_built` 等十个新 id 是新内容"——那是把 1.21.1 的成就目录 `ls | head` 截在十个的误读。实量：1.21.1 共 **16** 个成就 id，1.20.1 有 **19** 个，是 1.21.1 的**超集**（多 `fluid_matrix`/`infinite_matrix`/`pigmee_matrix` 三件本线独有）；16 个共享 id 一份不少，且 1.20.1 的文件已经换过形状（`icon.id`→`icon.item`、命名空间 1201、触发物改成本线已注册的 id——`migration_drive` 触发四只梯级、`chemical_matrix` 触发 appmek 细胞、`fe_matrix` 触发 appflux 细胞），lang 齐、数据闸绿。触发物核对也过：EC9（`simplify_stonecutting_assembler` 本线 id）与迁移驱动在列；omni/魔源/扩容卡没有成就节点，本线与 1.21.1 都没有，不算缺。**这条线的成就面无事可做，除非 1.21.1 将来新增成就 id。**

**顺序建议（11-21 更新）**：~~创意三件~~已结（两落地一推退）→ ~~成就十个~~已结（无事可做）→ ~~KubeJS~~已落地（`compileOnly` 对 Modrinth 最新 Forge 件 `2001.6.5-build.26`，sha1 `8b0cc979…012ae`；真实整合包不在本机，包里若不是这颗 build 号，回来对一眼 API 面即可——2001.6.x 一代内形状稳定）。**剩两条客户端读数**（无 KubeJS 冷启 + 装 KubeJS 眼验脚本面，见上）。

## 5. 测试与验证

- `./gradlew compileJava` 一次约 **24 s**（`--no-daemon` 更稳：这台机器上 daemon 池会互相挤，
  2026-11-01 有一次 `compileJava` 排了 26 分钟没启动）。**别拿 `build/classes` 的旧 class 当"我编过了"。**
- **`Loaded N recipes` 那行不是我们的配方数**：三次启动都是 43，而我们仓库里有 88 份配方文件；
  它是客户端侧 RecipeManager 的计数，口径未查清。**判"配方加载对不对"要看 0 条
  `Parsing error loading recipe` + 在 JEI 里看到那一页**，不要看 43。
- 游戏内读数（成型外观、面板、渲染器、状态灯染色）**套件永远看不见**，只能启动客户端点。
  注意日志会被下次启动覆盖：`run/logs/latest.log` 只保最近一次，要留读数就当时抓走。
- **探针"0 读数"不等于"没人点"**。三颗潜行右键诊断（存储 / 计算 / 合成主机）里，计算那颗今天
  在 15:15 / 21:09 / 23:53 抛了 **12 次** `Failed to handle packet`：`ICraftingCPU.getName()` 会返回
  null，而我们在 `use` 里直接 `.getString()`。存储那颗更早（13:29）抛过 `ItemStack.getDisplayName()`
  一次，被当天那次重写顺手带走了。**修法已落地**：body 抽成 `diagnose(...)`，外面 `try/catch
  RuntimeException` 并 `LOGGER.warn("… diagnose at {} failed", pos, e)` —— 诊断工具不许成为故障源，
  因为它读的全是上游可能给 null 的对象（`getName()`、`getStackInSlot()` 都是实例）。新增诊断照这个形状。
- **这三颗诊断只写日志，屏幕上只留一句**（11-02 夜改）。撤掉的是 8 条 `displayClientMessage(Component.literal(...))`
  —— 那批文本的键名（`total= avail= busy= status=`）从来没翻译，中文包里就是调试文本刷聊天栏；读数本身照旧走
  各自的 `LOGGER.info`，屏幕上只发一条 action bar 的 `diagnose.<ns>.log_written`（两份语言都补了键）。闸多一道
  `MachineDiagnostics.askedByBuilder` = `isCreative() || hasPermissions(2)`：原来那三处命中就无条件 `CONSUME`，
  等于把"生存玩家空手潜行右键=我要贴着这台机器放方块"吃掉了。**"干脆删掉"不是选项**：这条线 `@GameTest`
  命中 0 处（`grep -rl GameTest src/main/java` 空），成型、挂载、CPU 列表、样板索引进度这四类数只能从世界里读，
  而那 12 次包崩与 `count=1 / <unnamed>` 恰恰是这三颗探针给的。
- **读日志要按签名扫，不要只 grep 你以为的那一条**：我 21:09 那轮只查了 recipe category，
  就把同一份日志里已经躺着的 8 条 packet NPE 漏了一个小时。口径是：`Failed to handle packet` /
  `Exception` / `ERROR` 全扫一遍再下结论。
- **原版食谱书那条 `Unknown recipe category: <type>/<id>` 的机制**（消掉我们 7 条时量的）：
  `ClientRecipeBook.getCategory` 走完原版类型后调 `RecipeBookManager.findCategories`，null 就打一条
  WARN、把配方塞进 `RecipeBookCategories.UNKNOWN`；分类在 `setupCollections` 遍历**全部**配方时发生，
  不看界面不看菜单，所以每次进世界每个未登记类型每颗配方一条。正门是 mod bus 上的
  `RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder`。**坑**：原版按分类分页、
  **不按 RecipeType 过滤**，所以答 `CRAFTING_MISC` 会把"合成台里做不出来"的配方画进那张页 ——
  我们这种没有食谱书面板的机器只能答 `UNKNOWN`（`UNKNOWN` 不在任何 `RecipeBookType` 的列表里）。
  剩下的 5 条（3 条 AE2 + 2 条 appflux 塞进 `ae2:charger` 的）不是我们的账。
- **发布 jar 走的这条路 dev 永远验不到**：mixin 在发布件里靠 manifest 的 `MixinConfigs`，
  dev 靠 `--mixin.config`（两边都得有）；`reobfJar` 会把我们对 MC 成员的引用换成 SRG 名
  （现量：产物 89 个 class 里 **333 处 SRG 形式**，我们那颗 mixin 只碰 AE2 成员且 `remap=false`，
  所以 jar 里**没有 refmap 也不影响它** —— 但以后谁加一条碰 MC 成员的 mixin，就没有 refmap 承载它，
  mixin AP 在这条线上没接）。
- **手装的 server 实例是通的。上一轮死在拼装里不是拼装法错了，是拿错了 AE2 的件（2026-11-02 夜追出来）。**
  当时崩在 `ae2.mixins.json:spatial.MinecraftServerMixin` → `@Shadow field levels was not located …
  Using refmap ae2.mixins.refmap`，把我们整颗 jar 移出 `mods/` 崩得一模一样，于是就判成"拼装环境不代表真实
  环境"停手了。真相是命名通道：maven 的 `appeng:appliedenergistics2-forge:15.4.10`（8 417 759 B，
  md5 `f73a2cc3a1f5`）是**官方名通道的 dev 件**——同一把尺扫 160 颗类得 **0 条 `f_/m_` 对 648 条 Mojmap 成员**；
  而 installer 装出来的 `server-1.20.1-20230612.114412-srg.jar` 里 `MinecraftServer` 的成员是
  `f_129762_ / m_129783_`（SRG 通道）⇒ AE2 按 `levels` 找的那个字段在这份运行时根本不存在，谁的 mixin 都注不进去。
  AE2 的 GitHub release（tag `forge/v15.4.10`，同名文件 8 495 956 B，md5 `69e47ebd8018`）才是生产件：190 SRG 对 15 Mojmap。
  **所以：往生产实例里放件之前，先按这条尺量一遍通道，别按文件名猜。**
- 换上生产 AE2 之后 `F:/1201prod` 起来了：`DedicatedServer: Done (12.723s)`，我们四行 INFO 全在（代表物品、
  管道能力表已绑、两颗菜单登记、**`L1 interface accessor is mixed into appeng.helpers.InterfaceLogic`**
  ——最后这条就是 `build.gradle` 讲的"dev 加载类、发布 jar 才走 manifest `MixinConfigs`"那个通道，第一次在
  生产加载器下落到），0 条 `Parsing error loading recipe`，唯一 ERROR 是首启没有 `server.properties`（随即自建）。
  同批五颗件的通道核对：AE2 release=SRG、GuideME 20.1.14=SRG(53/4)、LDLib=SRG(512/18)、eco 20.5.3=SRG(1333/5)、
  **我们的 1.0.0=SRG(263/18)** ⇒ `reobfJar` 这条通道没问题，发布包与上游同一命名道。
- 一个新读数的坑：server 侧 `RecipeManager: Loaded 17 recipes`（dev 客户端三次都是 43）。**这两个数都不是我们的
  配方数**，旁边那行 `Loaded 1808 advancements` 说明 reload 是全量跑完的，所以 17 是这条日志自己的口径，未查清；
  判据照旧 = `Parsing error loading recipe` 0 条 + 客户端里逐条点开看。
- 复用件：`F:/1201prod`（installer 装好的 `libraries/` + `win_args.txt` + `run.bat`，`eula.txt` 已 true）留着，
  起法 = `cd F:/1201prod && F:/jdk17/bin/java @user_jvm_args.txt @libraries/net/minecraftforge/forge/1.20.1-47.4.20/win_args.txt nogui`。
  `--installClient` 仍然别碰：客户端要微软登录，脚本起不来，跟速度无关。
- 这条拼装已经验到并且算数的另一部分：FML 在生产启动器下读我们的 `mods.toml` 并按
  `ae2 [15.4.10,)` / `neoecoae [20.5.3,)` 拒启动（缺依赖屏原文如此）。
  素材仓库顺带记一笔：`F:/gradle-cache/caches/forge_gradle/assets` 620 MB 里 520 MB 是 `.ogg`、
  贴图只占 11.3 MB，dev 与 prod 共用一份，所以 prod 客户端不需要重新下几百 MB。
- **但"服务端侧"不需要真实例：headless `./gradlew runServer --offline` 在这台机器上是通的**
  （2026-11-02；早前有一轮把它记成"卡在 Forge 在线版本检查、跑不到加载配方"，那是错的——
  版本检查失败在这条线上只是一条 WARN，不拦启动）。FG6 的 server run 配置就在
  `run/`（`--nogui`，`eula.txt` 已存在），走到 `DedicatedServer: Done (12.623s)`，
  **0 条 `Parsing error loading recipe`**，我们三行 INFO 全在（mixin 已织入 / 管道能力表已绑 /
  装配室菜单已登记），eco 的服务也在同一轮起来。⇒ 注册、配方、mixin 这三类判据以后走这条路，
  只有真视觉（面板、渲染器、状态灯）仍要客户端。
- **`Registry minecraft:item: Object did not get ID it asked for`（13 条）与
  `ERROR Unidentified mapping from registry neoecoae:cell_type`（2 条）是旧存档的账，不是 jar 的账。**
  A/B 只有一个变量＝世界目录：带着 `run/world`（本次新增物品之前的旧 dev 服务端存档）跑 = 13 + 2；
  把它改名让服务器新建一份 = **0 / 0**，其余读数不变。这条线还没发布过 ⇒ 现实里只有 dev 存档会念这几句；
  将来玩家跨版本升级会同样看到一次这种 id 提示，**别把它当注册缺陷**（判据同上：换一个变量跑一遍）。
- `Loaded 43 recipes` 在服务端也是 43 ⇒ 上面那句"这行不数我们的配方文件"两侧都验过了。

## 6. 本机工具坑

- `core.autocrlf=true` ⇒ **`git diff --shortstat` 的行数不是改动规模**（一次提交报 +16002/−14375，
  其实是整份 JSON 重写 + 行尾）；`git diff --check` 在已推送历史里报出 **~998 处 "trailing whitespace"**，
  性质是行尾/尾随空格，不是内容错误。**新提交别引这个噪声**：python 写文件用 `newline=""` 并保持 LF。
- PowerShell 脚本块会被 Git Bash 吃掉 `$_` → 写成 `.ps1` 用 `-File`。
- 像素/贴图结论一律用 Pillow；`javap` 在 `F:/java/bin`（JDK17 那份在 `F:/jdk17/bin`）。
- 推 GitHub 偶发连不上（本机有代理改 hosts/自签证书），试 `git -c http.version=HTTP/1.1 push`。
- **curl 在这台机器上默认 TLS 校验会死在吊销检查**：`schannel: CRYPT_E_REVOCATION_OFFLINE` 不是证书错，
  是吊销服务器连不上。加 `--ssl-no-revoke` 就通（2026-11-02 验 maven.minecraftforge.net 时撞到）。
  那个源实测只有 **7 KB/s**，长文件要 `-C -` 续传循环，单次 `--max-time` 到点必断。
- **ForgeGradle 的启动自检会挂住整条构建，而且挂在 daemon 里看不出来**：`UserDevPlugin.apply` 调
  `EnvironmentChecks.testServerConnection`，对 `https://maven.minecraftforge.net/` 与
  `https://libraries.minecraft.net/` 各发一次 HEAD。2026-11-02 两轮分别白等 13 min 与 10 min，
  `jstack` 里 `Daemon worker` 是 RUNNABLE 停在 TLS 握手 `SocketDispatcher.read0`、十分钟只吃 2.1 s CPU；
  同两个 URL 用 `curl --ssl-no-revoke` 1.2 s 就回 200/404 ⇒ 坏的是 JVM 这条路，不是网断了。
  绕法两件事都要：**`--no-daemon`**（命令行上的 `-D` 进不到执行构建的那颗 daemon JVM，第一次就是这么等的）
  加 **`-Dnet.minecraftforge.gradle.check.certs=false`**（`EnvironmentFlag` 只认
  `net.minecraftforge.gradle.<key>` / `fg.<key>`，key 全仓三个：`check.certs`、`check.gradle`、`check.java`）。
  两条都带上 `build -x test` **9 s** 跑完。顺带：两条会话共用一台机器会攒出五颗 busy daemon
  （日志原话 `5 busy Daemons could not be reused`），所以 `--no-daemon` 在这里既绕自检也绕排队。
- `javap` 读产物字节码只认 `-classpath <解出来的目录> + 点号类名`：`path/Foo.class` 和斜杠类名都报
  "找不到类"。JDK21 那份的报错是 GBK 中文，加 `-J-Duser.language=en` 才看得清。

## 7. 目录与提交

- 不跟踪任何 jar；`libs/`、`run/`、`_removed_sources/`、`旧jar/`、`备份_*/`、`参考模组/` 都在 `.gitignore`。
  **删除的东西一律逐字节备份进 `_removed_sources/` 并保持相对路径**，但它是**本机副本**，真出处是删除前的那个提交——
  不许拿"备份里还有一份"当"删了没事"的理由。第三方 jar 放在 `参考模组/` 只给读，不进门禁例外。
- 提交前缀小写英文（`feat:` / `fix:` / `build:` / `chore:` / `docs:`），一条提交一件事，**边界是"每条都能独立编译且守卫能过"**。
- **版本号现在只有一处**：`gradle.properties` 的 `mod_version`。`mods.toml` 写 `${mod_version}`，
  由 `processResources` 展开（和 1.21.1 那条线的规矩一致）。**每次出包后要把 jar 里的
  `META-INF/mods.toml` 读回来核对**（`version = "0.1.0"` 而不是 `${mod_version}`），这是那条注释存在的理由。
- `LICENSE`（GPL-3.0 全文）与 `THIRD_PARTY_NOTICES.md` 已于 2026-11-02 补齐。后者每个许可证字段都是从
  **那颗 jar 自己的 `mods.toml` 读出来的**，不是抄 1.21 的表；LDLib **v1**（这条线）自报 `GPL-3.0 license`，
  和 1.21 那条线的 LDLib**2** 不是一个东西。美术侧：这条线的 **213 张 PNG 全部与 1.21.1 同路径文件逐字节相同**
  （md5 全量对过），所以美术出处记录在那边，这里不改写。
- **发版四件事里三件已经拍了（2026-11-02 夜）。**
  ① 号段 = **1.0.0**。这里原来写的是"别用 1.0.0，因为缺一键建造 / 主机动作层 / 大宗标记 / 快速路径，还带着
  接口面闪烁那条未结缺陷"——**这条被否了**，理由记下来：这条线是独立的一条移植线，第一版就给 1.0.0 更好认，
  缺的那些在 CHANGELOG 的"升级前必读"里逐条写明，不用号段去暗示。③ 生产实例冒烟**已做**（21:31，
  `F:/1201prod`，`Done (12.723s)` + mixin 在生产通道落到），④ CHANGELOG 已按 1.21.1 §16 那套形状写了第一版。
  **还没定的只有一条**：② 渠道——GitHub Release 已经算定（仓 PUBLIC），Modrinth/Curse 上不上没拍；
  以及 §17 那条"接口每个面闪烁"仍未结，CHANGELOG 里没有为它写任何话。
