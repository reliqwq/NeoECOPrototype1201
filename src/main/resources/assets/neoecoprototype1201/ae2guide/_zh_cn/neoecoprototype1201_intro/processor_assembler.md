---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype1201:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_stonecutting_assembler
  - neoecoprototype1201:simplify_pattern_provider
  - neoecoprototype1201:cable_pattern_provider
---

# L1 处理器装配室

**L1 处理器装配室**相当于绿色外壳的<ItemLink id="ae2:molecular_assembler" />，只接受处理样板。它只跑本模组的处理器装配配方。它和下面的 L1 样板供应器搭配使用。

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype1201:simplify_pattern_provider" />
  <ItemIcon id="neoecoprototype1201:cable_pattern_provider" />
</ItemGrid>

样板供应器也能只挂在线缆的一面。两种形态在合成台里互换，一比一。

装配室与供应器都是**工作台合成配方**：装配室 = 1 台分子装配室 + 4 只特质机壳 + 4 个绿晶晶格；样板供应器 = 1 个 ME 样板供应器 + 4 块石英玻璃 + 4 个绿晶晶格，一次出 **2** 台。

<Row>
  <RecipeFor id="neoecoprototype1201:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype1201:simplify_pattern_provider" />
</Row>

## 出厂配方

处理器三件套（其余处理器由压印器推导，见下）：

| 产物 | 原料 |
|------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 运算处理器 | 硅 + 红石 + 赛特斯石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |

其余 8 条：

| 产物 | 原料 |
|------|------|
| L1 供能接口 | 2 水晶谐振发电机 + 1 ME接口 + 1 ECO - SA 超导处理器 |
| ECOP - 结构扩容卡 | 内存卡 + 铝锭 + 绿晶晶格 |
| 盈能强化线程核心 | 2 ECO - CM4A 线程核心 + 1 ECO - C1 计算子结构外壳 |
| ECO - CE1R 闪存晶阵 | 2 ECO - CE1 闪存晶阵 + 1 ECO - L4 存储组件 |
| 雪球 / 细雪桶 | 4 雪块 → 4 雪球；3 雪球 + 1 桶 → 细雪桶 |
| reliqwq 的 Fumo 玩偶 | L1 存储主机 + C1 计算主机 + F1 合成主机 |
| Yang120 的 Fumo 玩偶 | eco 的 L4 存储主机 + C4 计算主机 + F4 合成主机 |
| kouooki 的 Fumo 玩偶 | 粉色染料 + 干海带 + 棕色蘑菇 |

硅、金锭、钻石接受标签。

三种处理器之外的其余处理器配方，从压印器的配方现场推导——别的模组给压印器加的配方不需要数据文件就能用。

## 速度与批量

5 个卡槽只吃<ItemLink id="ae2:speed_card" />：不装卡一份要 100 刻，装满 5 张要 20 刻。

一次推送带几份完整材料就在**一个**周期里做完，产出按份数翻倍而耗时不变，所以想更快只能继续加卡。上限是工作台那 9 格：一批原料要按各自一组的数量摆进去，**摆不下这张样板就被整张拒收**、原料退回网络。以处理器为例，三种原料各一组 64 个，3 × 64 = **192 份**就是上限。一轮只有一组产出送到机器前面，交不下的那部分直接进网络，不会消失。

