# Spring AI Tool Adapter — Codex Prompt 工程

## 文件结构

```
codex-prompts/
├── codex-loader.md              ← Codex 入口文件（告诉 Codex 怎么读这套体系）
├── spring-ai-tool-adapter-prompts.yaml  ← 主提示词链（6 个 Phase 完整定义）
├── convergence-criteria.yaml     ← 每个 Phase 的收敛判定标准
└── README.md                    ← 本文件
```

## 三个文件各管什么

| 文件 | 角色 | 谁读它 |
|------|------|--------|
| `codex-loader.md` | 操作手册 — Codex 启动时读，知道"我是谁、怎么读 YAML、怎么输出" | Codex |
| `spring-ai-tool-adapter-prompts.yaml` | 核心提示词 — 6 个 Phase 的完整定义（输入/产出/自检/Loop 规则） | Codex |
| `convergence-criteria.yaml` | 验收标准 — 每个 Phase 做到什么程度算"完成" | Codex + 你 |

## 工作流程

```
你 → 告诉 Codex："按 phase-1 生成代码"
        ↓
Codex 读 codex-loader.md（知道规则）
        ↓
Codex 读 prompts.yaml 的 phase-1（知道要做什么）
        ↓
Codex 读 convergence-criteria.yaml 的 phase-1（知道做到什么程度算完）
        ↓
Codex 生成代码 + 跑自检 + 输出 CONTRACT_SUMMARY
        ↓
你 review → 通过 → 进入 phase-2
```

## 怎么指挥 Codex（示例话术）

```
"加载 codex-loader.md，按 phase-1 生成 ToolRegistry 和 AiToolRegistrar 代码，
 完成后对照 convergence-criteria.yaml 的 phase-1 做自检，输出 CONTRACT_SUMMARY。"

"继续 phase-2，输入契约来自上一轮的 CONTRACT_SUMMARY，
 生成所有 ToolSchemaConverter 实现。"

"phase-3 发现审计性能问题，进入 loop_next 分支：审计异步化改造。"
```

## 你（人类）在每个 Phase 结束后要做的

1. ✅ 检查 Codex 输出的 CONTRACT_SUMMARY
2. ✅ 验证 assumptions 是否合理
3. ✅ 确认 risks 是否可接受
4. ✅ 决定是否进入下一轮迭代 / 下一 Phase
5. ✅ 把通过的代码 commit，把契约摘要存档

## 设计原则

- **你 = 架构师 + Prompt 总控**（定义规则、判断收敛）
- **Codex = 代码执行单元**（读规则、生成代码、自检、输出契约）
- **YAML = 唯一真相源**（所有约束、契约、判定标准都在 YAML 里，可版本管理）
- **Loop = 自动进化**（每轮自检 → 发现假设 → 下一轮验证 → 收敛）

## 提示

- 每次让 Codex 生成前，先确认它已加载 `codex-loader.md`
- 每个 Phase 的 CONTRACT_SUMMARY 务必存档（它是下一 Phase 的输入）
- 如果某个 Phase 三次迭代都没收敛 → 不是 Codex 的问题，是约束太紧或设计有误 → 回来改 YAML
- YAML 本身就是可版本管理的文档，建议和代码一起 commit
