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

## 2. 最重要的一条：可选依赖不能出现在"会被加载"的位置

**Forge 1.20.1 在构建事件表时会强加载我们 jar 里的每一个类**（`EventSubclassTransformer` 逐个
`Class.forName`）。所以"把第三方引用关进方法体"这条规矩在这里**只挡方法体，不挡类定义**：

- ✅ 第三方名字只出现在方法体里，且被**同一个方法内**的 `ModList.get().isLoaded(...)` 包住 —— appmek / appflux 两族就是这个形状，实测能启动。
- ❌ **`extends` 上游某个第三方类** —— 父类链是 must-resolve，门在它之前不生效。现场：omni 那三只细胞继承 eco 的 `ECOUniversalStorageCellItem`，而它的父类就是 OmniCells 的 `AEUniversalCellItem`，于是 `Could not find parent … → FATAL`，**我们自己的代码一行都没跑到**。

判"某个可选 mod 的族能不能上编译类路径"就一条：**先量父类链，再谈别的**。量法——
`javap` 目标类看 `extends`，逐层向上，任何一层出现第三方命名空间就不能继承。

同类事实：eco 20.5.3 把 `ae2omnicells` 的可选下限写成 `[1.1.6,)`，而 1.20.1 上 OmniCells 最新是
**1.0.7** → 装上也起不来（FML 直接拒）。这条不是我们的接线问题。

## 3. 资源：单向，从 1.21.1 流向这里

贴图/模型/blockstate/lang/guide 页**只从 `F:/neoecoSimplify` 抄过来**，这里改了不要指望回流——美术分叉是唯一事后合不回去的东西。

**一个必须活过每次同步的例外**：`models/block/storage_controller/controller_formed_base.json`
（与 `_mirrored` 孪生）里四个 `#body` 面带 `forge_data{block_light:15, sky_light:15}`，1.21.1 那份**没有**。
原因在 Forge 1.20.1 会先问上一格"这面能不能被裁掉"再选光照，而 eco 的成型外壳在 `INVISIBLE` 时答"能"，
于是那几格回退到邻居的 0 光、**机器顶面整片黑**。从 1.21.1 直接覆盖回来就会把黑顶带回来。

## 4. 数据一致性：`tools/check_data_sync.py`

配方 ↔ lang ↔ 成就三方一致由它兜：它数的是"注册过的细胞 / 拆解表 / 成就节点引用的 id / 有配方来源的 id"，
任何一边缺件就红。**它同时读三类文件，所以按家族拆提交会让中间某条提交守卫红** —— 需要拆时先确认拆点不切断三方。
今天（`e3d8ebf`）读数：20 只细胞 / 19 份拆解表 / 10 个节点、65 已注册 id、126 有来源 id、引用全解析。
**无限混凝土矩阵故意没有拆解表** —— 那是"压缩艺术，不可拆卸"那句文案，不是漏了。

## 5. 测试与验证

- `./gradlew compileJava` 一次约 **24 s**（`--no-daemon` 更稳：这台机器上 daemon 池会互相挤，
  2026-11-01 有一次 `compileJava` 排了 26 分钟没启动）。**别拿 `build/classes` 的旧 class 当"我编过了"。**
- **`Loaded N recipes` 那行不是我们的配方数**：三次启动都是 43，而我们仓库里有 88 份配方文件；
  它是客户端侧 RecipeManager 的计数，口径未查清。**判"配方加载对不对"要看 0 条
  `Parsing error loading recipe` + 在 JEI 里看到那一页**，不要看 43。
- 游戏内读数（成型外观、面板、渲染器、状态灯染色）**套件永远看不见**，只能启动客户端点。
  两颗探针（潜行右键合成主机 / 计算主机）就是为这个插的，**0 读数通常意味着没人点过**，不是功能坏了。
  注意日志会被下次启动覆盖：`run/logs/latest.log` 只保最近一次，要留读数就当时抓走。

## 6. 本机工具坑

- `core.autocrlf=true` ⇒ **`git diff --shortstat` 的行数不是改动规模**（一次提交报 +16002/−14375，
  其实是整份 JSON 重写 + 行尾）；`git diff --check` 在已推送历史里报出 **~998 处 "trailing whitespace"**，
  性质是行尾/尾随空格，不是内容错误。**新提交别引这个噪声**：python 写文件用 `newline=""` 并保持 LF。
- PowerShell 脚本块会被 Git Bash 吃掉 `$_` → 写成 `.ps1` 用 `-File`。
- 像素/贴图结论一律用 Pillow；`javap` 在 `F:/java/bin`（JDK17 那份在 `F:/jdk17/bin`）。
- 推 GitHub 偶发连不上（本机有代理改 hosts/自签证书），试 `git -c http.version=HTTP/1.1 push`。

## 7. 目录与提交

- 不跟踪任何 jar；`libs/`、`run/`、`_removed_sources/`、`旧jar/`、`备份_*/`、`参考模组/` 都在 `.gitignore`。
  **删除的东西一律逐字节备份进 `_removed_sources/` 并保持相对路径**，但它是**本机副本**，真出处是删除前的那个提交——
  不许拿"备份里还有一份"当"删了没事"的理由。
- 提交前缀小写英文（`feat:` / `fix:` / `build:` / `chore:` / `docs:`），一条提交一件事，**边界是"每条都能独立编译且守卫能过"**。
- **本条线目前没有 tag、没有 CHANGELOG、没有发版流程**（`git tag --list` 为空）。第一次发布前要先决定：
  是沿用 1.21.1 那套（三处号→现在只有一处、CHANGELOG 中文段、轻量 tag、`gh release` 挂 jar、下载核对 sha256），
  还是这条线单独定一套。别在没定之前先中门。
