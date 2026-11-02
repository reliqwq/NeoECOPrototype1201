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

**指南里的 3D 场景（`ae2guide/scenes/*.nbt`）是这条线上自己产的东西。** 做法：世界里用结构方块存 →
文件落在 `run/saves/<world>/generated/minecraft/structures/<name>.nbt`（gzip NBT）→ 按内容改名拷进
`ae2guide/scenes/` → 在**两种语言**的页里各写一次
`<GameScene zoom="4" interactive={true}><ImportStructure src="../scenes/x.nbt"/><IsometricCamera yaw="45" pitch="30"/></GameScene>`。
守卫只验"这个 src 指的文件在不在"，不验画面对不对——**朝向、成型与否、成员放对格，只有客户端翻开那页才看得见**。
两条读数：新存的场景 `DataVersion=3465`（1.20.1 原生），从 1.21.1 抄来的那三颗是 `3955`，
1.20.1 的 `NbtUtils` 遇到本地方块没有的属性会跳过，所以旧捕获仍能显示、只是拿默认状态；
另外结构方块文件里**不要留 `author`/`pos`/`time`**（现在六颗都干净，顶层只有 DataVersion/size/blocks/palette/entities），
那是会把玩家名和坐标打进 jar 的地方。

## 4. 数据一致性：`tools/check_data_sync.py`

配方 ↔ lang ↔ 成就三方一致由它兜：它数的是"注册过的细胞 / 拆解表 / 成就节点引用的 id / 有配方来源的 id"，
任何一边缺件就红。**它同时读三类文件，所以按家族拆提交会让中间某条提交守卫红** —— 需要拆时先确认拆点不切断三方。
现量（`1f96399` 之后）：20 只细胞 / 19 份拆解表 / 14 个节点、65 已注册 id、126 有来源 id、引用全解析。
**无限混凝土矩阵故意没有拆解表** —— 那是"压缩艺术，不可拆卸"那句文案，不是漏了。

### 4.1 从 1.21.1 抄过来的 JSON，有四处在这条线上换了形状（全部静默）

| 东西 | 1.21.1 写法 | Forge 1.20.1 现实 | 抄错的后果 |
|---|---|---|---|
| 成就的条件 | 顶层 `"neoforge:conditions": [...]` | **只认包装形状** `{"advancements":[{"conditions":[…],"advancement":{…}}]}`（`ConditionalAdvancement.processConditional`） | 顶层键被当未知字段忽略 → 节点**无条件加载**，缺模组时判据点名的物品不存在，树里多一颗永远点不亮的洞，0 条日志抱怨 |
| 配方的条件 | `"neoforge:conditions"` | 裸键 `"conditions"` + `forge:mod_loaded` | 我们 18 份条件配方本来就是对的；**别把成就那套写法搬过来** |
| 成就的图标 | `{"id": "ns:item"}` | `{"item": "ns:item"}` | 守卫已拦（`display.icon still uses 1.21's 'id'`） |
| 模型的面级光照 | `"neoforge_data"` | `"forge_data"` | 发光层不再是全亮烘焙光 → 整张面板像被乘 0.05（见 §3 那条例外） |

守卫 `check_node` 现在两件事都做：认包装形状并递归查内层、见到 `neoforge:` 前缀键直接红。
**控制组建过**（临时塞一颗带 `neoforge:conditions` 的假节点 → 红的正是那句，摘掉后 14 颗全绿）。

**同一个方向的另一个坑：文本抄过来了，这条线上没有读者。** `ECOCellType.desc()` 在 20.5.3 全 jar 1847 个类里
只有一个读者 —— `HostText.storageTypeAccentColor`，而且只取 `desc().getStyle().getColor()`；
**没有任何类含 `eco_cell_type` 这个字符串**，所以 `eco_cell_type.<ns>.<name>` 那类 lang 键不是上游在翻译的行标签
（1.21.1 那条线上 eco 确实把 desc 当行标签用）。这条线上我们只有一颗自造行（无限混凝土，`visible=false` 且不注册），
所以那行文字在这条线**任何界面都画不出来**。**结论：键留着**（lang 属于单向文本流，删了就是在不该分叉的文件上分叉），
但别在文档/指南里声称"面板会显示这一行"，也别为它去做渲染。玩家看得到的名字是物品名。

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
- **手装 Forge 实例这条冒烟路在这台机器上不通，别再走第二遍（2026-11-02）。** installer 的 server 装能装成
  （卡住的四个件从本机 Gradle 缓存 / Maven Central 补，sha1 与 installer 期望值逐字符相同），
  但**拼出来的 server 死在 AE2 自己的 mixin 上**：`ae2.mixins.json:spatial.MinecraftServerMixin` →
  `@Shadow field levels was not located … Using refmap ae2.mixins.refmap`；**把我们整颗 jar 移出 `mods/`
  崩溃一模一样** ⇒ 这是拼装环境不代表真实发布环境，它在我们的代码之前就炸，继续分辨只会得到"我的拼装坏了"
  而不是"模组坏了"的读数。`--installClient` 两次都在 socket 上卡死（7 分钟 0 输出；forge/creeperhost
  实测 7 KB/s 且反复 Read timed out）。**正确做法：拿一个真启动器里的 Forge 1.20.1 实例**（真素材、真名通道）
  丢五颗 jar 跑：我们的 `build/libs/*.jar` + AE2 15.4.10 + eco 20.5.3 + GuideME 20.1.14 + LDLib（eco 强制），
  判据三条 = 进得了主菜单、那颗 mixin 打"8192 可用"而不是 ClassCastException、注册与配方无异常。
  这次拼装已经验到并且算数的部分：FML 在**生产启动器**下读我们的 `mods.toml` 并按
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
- **本条线仍然没有 tag、没有 CHANGELOG、没有发版流程**。第一次发布前要先定四件事：
  ① 号段（建议从 `0.2.0` / `0.1.0-preview1` 起，**别用 1.0.0** —— 缺一键建造 / 主机动作层 / 大宗标记 /
  快速路径，还带"接口面闪烁"那条未结缺陷）；② 渠道（私有 GitHub Release 还是 Modrinth/Curse，
  以及命名要一直带 `(1.20.1)`）；③ 先做一次**生产实例冒烟**（上一条）；④ CHANGELOG 与 release 正文
  用哪套写法（1.21.1 的 §16 那套形状可以直接沿用：重点 / 升级前必读 / 新增·变更·修复·移除 / 依赖）。
  **别在没定之前先中门。**
