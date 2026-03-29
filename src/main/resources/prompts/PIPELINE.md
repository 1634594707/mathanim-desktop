# MathAnim 提示词与「智能体」编排说明

本文借鉴 **golutra** 的思路：*保留单一工程能力，把不同职责拆成可编排的「成员」；API 层像 golutra 对接多种 CLI 一样，只换接入点而不改流水线内核。*

## 1. API 接入（兼容层）

- 配置项：`mathanim.ai.base-url`、`api-key`、`model`（见 `application.yml` 与桌面「API」页）。
- **任意 OpenAI 兼容** HTTP 端点均可：官方 OpenAI、Azure OpenAI、本地 vLLM/Ollama（OpenAI 兼容模式）、OneAPI 等。
- 与 golutra「不换 CLI、只加编排层」类比：MathAnim **不换 Manim / py_compile**，只换 **Chat Completions 上游**。

### DeepSeek（默认联调）

- **Base URL**：`https://api.deepseek.com/v1`（须含 `/v1`，客户端会请求 `{base}/chat/completions`）。
- **Model**：例如 `deepseek-chat`（与官方控制台一致即可）。
- **Key**：在 [DeepSeek API 平台](https://platform.deepseek.com/) 创建后填入「API」页或 `application.yml` 的 `api-key`。
- 桌面「API」页可点 **测试连接** 发极小探测对话；通过后「创作」一键生成再走完整 AI → Manim 流水线。

## 2. 流水线中的逻辑 Agent（角色键名）

任务字段 `promptOverridesJson` 为 JSON，结构为 ManimCat 风格：

```json
{
  "roles": {
    "problemFraming": { "system": "…", "user": "…" },
    "solutionAnalysis": { "system": "…", "user": "…" },
    "conceptDesigner": { "system": "…", "user": "…" },
    "codeGeneration": { "system": "…", "user": "…" },
    "codeRetry": { "system": "…", "user": "…" }
  }
}
```

| 键（常量见 `PromptRoleKeys`） | 典型阶段 | 默认资源文件 |
|-------------------------------|----------|----------------|
| `problemFraming` | 问题拆解 | `roles/problem-framing.system.md` |
| `solutionAnalysis` | 两阶段之「解题 + 可视规划」（在分镜前，日志可见①） | `roles/solution-analysis.system.md` |
| `conceptDesigner` | 两阶段之概念设计 / 分镜 | `roles/concept-designer.system.md` |
| `codeGeneration` | 代码生成（或两阶段之代码） | `roles/code-generation.system.md` |
| `codeRetry` | py_compile / Manim 失败后的修补 | `manim-scene-repair.txt` 等 + 覆盖 |

未在 JSON 中覆盖的字段使用上述内置资源。

## 3. Handoff（与 golutra 成员协作类比）

```
用户概念 (+ 可选参考图)
    → [problemFraming] 规划 JSON（可选）
    → 合并为 enriched concept
    → [solutionAnalysis]（若勾选「两阶段」：先解题与可视规划，日志为 ①）
    → [conceptDesigner]（若两阶段：英文分镜，日志为 ②）
    → [codeGeneration]（日志为 ③）
    → 静态检查 / Manim
    → 失败则 [codeRetry] 循环直至上限
```

桌面端「一键生成」即触发整条链；仅改提示词时编辑 JSON 或替换 `resources/prompts/roles/*.md`。

### 桌面端：可选角色模板（自动生成 JSON）

在 **高级 → OVERRIDES** 中先选 **大类**（数学 / 物理 / 通用），再选 **模板**，然后 **填入 JSON**（替换文本框内容）或 **合并到当前**（与现有 JSON 按角色合并，`system`/`user` 追加在后方）。模板由 `PromptOverrideTemplateService` 与 `prompts/templates/*.txt` 组装；大类见 `com.mathanim.prompt.PromptTemplateCategory`，具体项见 `PromptTemplateKind`。
