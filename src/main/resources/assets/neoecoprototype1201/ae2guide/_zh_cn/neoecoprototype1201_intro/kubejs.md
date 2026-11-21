---
navigation:
  title: KubeJS 支持
  icon: neoecoprototype1201:simplify_small_bulk_expansion_card
  position: 40
  parent: neoecoprototype1201_intro/index.md
---

# KubeJS 支持

KubeJS 是可选的；装了才有这些。

| 能做什么 | 在哪做 |
|-----------------|-------|
| 注册自定义存储矩阵 | 启动脚本，两个 builder 类型：`neoecoprototype1201:storage_matrix` 与 `neoecoprototype1201:infinite_storage_matrix`——容量、类型上限、材质、绑定的资源都能设 |
| 造一只自己长货的元件 | 同一个 builder，`.grows(perBatch, ticksPerBatch)`：每 `ticksPerBatch` 世界刻给出绑定的物品/流体/化学品，什么都不收 |
| 增改删本模组配方 | 服务端脚本；处理器装配室的配方可以按字段删、或以原生 JSON 加 |

两种脚本时机不同：**启动脚本**在游戏启动时跑，注册矩阵必须在那一档（注册表冻结之前）；**服务端脚本**每次服务器加载都重跑，改配方用它。完整示例在本模组仓库的 `docs/kubejs.md` 与 `docs/kubejs-example.js`。
