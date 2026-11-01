---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype1201:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype1201_intro/index.md
item_ids:
  - neoecoprototype1201:simplify_stonecutting_assembler
---

# L1 处理器装配室

**L1 处理器装配室**相当于绿色外壳的<ItemLink id="ae2:molecular_assembler" />，专门给还没压印器的存档用：处理器三件套在这里直接用样板做出来。它只跑本模组的处理器装配配方，普通分子装配样板会被拒收， 那类活请交给分子装配室。

<ItemGrid>
  <ItemIcon id="neoecoprototype1201:simplify_stonecutting_assembler" />
</ItemGrid>

装配室在**合成台**做：装配室 = 1 台分子装配室 + 4 只特质机壳 + 4 个绿晶晶格。

<Row>
  <RecipeFor id="neoecoprototype1201:simplify_stonecutting_assembler" />
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

硅、金锭、钻石都收标签，任意模组的对应物品都算。

三种处理器之外的其余处理器配方，从压印器的配方现场推导——别的模组给压印器加的配方不需要数据文件就能用。两个服务端配置键管这一条：

| 配置键 | 作用 |
|--------|------|
| `derive_processor_recipes_from_inscriber` | 关掉从压印器推导 |
| `disabled_processor_recipes` | 按输出排除某一种处理器 |

## 速度与批量

5 个卡槽只吃<ItemLink id="ae2:speed_card" />：不装卡一份要 100 刻，装满 5 张要 20 刻。

一次推送带几份完整材料就在**一个**周期里做完，产出按份数翻倍而耗时不变，所以想更快只能继续加卡。装配室的工作台只有 9 格，一批原料要按各自一组的数量摆进这 9 格里；**摆不下，这张样板就被拒收**，原料退回网络，日志写明是哪一条卡的；以处理器为例：三种原料、每种一组 64 个，3 × 64 = **192 倍**就是这一档的上限。一轮只有一组产出送到机器前面，交不下的那部分直接进网络，不会消失。

