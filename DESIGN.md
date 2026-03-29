# MathAnim Desktop（mathanim-desktop）实现说明

本文档描述本目录 **mathanim-desktop** 桌面应用；与 ManimCat Web 栈（Express / Redis / React）无对应实现，不再展开上游架构。

模块入口与构建命令见 **[README.md](README.md)**。

---

## 一、定位与能力

- **本地单机**：JavaFX 界面 + 内嵌 Spring Boot，数据在 **SQLite**（路径由 `mathanim.data-dir` 决定）。
- **AI**：任意 **OpenAI 兼容** Chat Completions（`/v1/chat/completions` 等），支持 **参考图 URL** 多模态。
- **渲染**：本机 **Manim Community Edition**，子进程调用，可取消；**py_compile** 作为静态检查；失败则 **AI 修补** 循环（次数上限可配）。
- **输出**：任务级 **视频 mp4** 或 **单帧 png**；可选复制到 **导出目录** 便于查找。
- **扩展**：**提示词 JSON** 覆盖各逻辑角色；**大类 + 模板** 一键生成/合并 JSON；**一键配音**（云端语音 API / 本地 IndexTTS2 + ffmpeg）。

---

## 二、技术栈

| 层级 | 技术 |
|------|------|
| UI | JavaFX 21、FXML、`mathanim-theme.css` |
| 应用 | Spring Boot、嵌入式 Tomcat 不用于对外 HTTP（仅 IoC / 配置） |
| 持久化 | SQLite + Spring Data JPA |
| AI | `OpenAiChatClient`（HTTP Java 11+） |
| 渲染 | 进程：`ManimRenderService` + `ManimProcessRegistry` |

---

## 三、包与职责（`com.mathanim`）

| 包 / 类 | 职责 |
|---------|------|
| `MathAnimFx` / `MathAnimApplication` | JavaFX 启动与 Spring 上下文 |
| `ui.MainViewController` | 主界面逻辑、预览、入队、设置、配音按钮 |
| `service.WorkflowPipelineService` | 拆解 → 两阶段/单阶段 → py_compile → manim → 修补循环 |
| `service.ManimAiCodeService` | 代码生成 / 编辑 / 修补提示词 |
| `service.ManimRenderService` | manim CLI 组装、找产物、超时、导出复制 |
| `service.RenderJobService` | 任务 CRUD、异步流水线入口、内置测试、取消 |
| `service.ProblemFramingService` | 问题拆解 |
| `service.PythonStaticCheckService` | `python -m py_compile` |
| `service.AiEffectiveService` | 合并 `application.yml` 与 `AppSettings` |
| `service.DubbingService` | TTS + ffmpeg 混流 |
| `service.PromptOverrideTemplateService` | 模板 → `PromptOverridesDto` JSON |
| `prompt.*` | `PromptRoleKeys`、`PromptTemplateCategory/Kind`、JSON DTO |
| `domain.*` | `RenderJob`、`AppSettings`、枚举（画质、阶段、配音模式等） |
| `config.*` | `AiProperties`、`ManimProperties`、`DubbingProperties` 等 |

---

## 四、数据与目录

- **数据库**：`<data-dir>/mathanim.db`（`AppSettings`、`RenderJob` 等）。
- **任务工作目录**：`<data-dir>/jobs/<jobId>/`（`generated_scene.py`、`media/`）。
- **导出目录**：`mathanim.manim.export-media-dir`（渲染成功后复制 `{jobId}.mp4` 等）。
- **内置 Manim 参考脚本**：`examples/manim/`（与 `example-bank.json` 中 `manimExamplePath` 一致，均相对 **本目录 mathanim-desktop**）。

---

## 五、AI → Manim 流水线（概念顺序）

1. 可选 **问题拆解**（`ProblemFramingService`），规划可合并进概念。
2. **单阶段**：直接按 `manim-scene-generation.txt` 生成代码。  
   **两阶段**：解题与可视规划 → 英文分镜（`concept-designer.system.md`）→ 代码（`code-generation.system.md`）。
3. **`py_compile`** 失败或 **manim** 非零退出 → 用 `manim-scene-repair.txt`（及可选 `codeRetry` 覆盖）修补，直至达到 **`max-retry-passes`**。
4. 成功则更新任务状态、输出路径，并尝试 **导出目录复制**。

进程内异步执行（非 Redis 队列）；**取消** 通过 `ManimProcessRegistry` 终止子进程。

---

## 六、配置要点（`application.yml`）

| 前缀 | 含义 |
|------|------|
| `mathanim.data-dir` | 数据根（库、jobs、cache） |
| `mathanim.ai.*` | base-url、api-key、model、python、max-retry-passes、max-completion-tokens |
| `mathanim.manim.*` | command-prefix、fps、renderer、timeout-seconds、export-media-dir |
| `mathanim.dubbing.*` | 云端 TTS、本地 IndexTTS2 URL、参考音、ffmpeg |

UI「API」页与 SQLite **可覆盖** YAML 默认值（`AiEffectiveService` / 配音相关 `AppSettingsService`）。

---

## 七、界面功能（摘要）

- **创作**：**输入方式**（直出描述 / 题目+过程+答案 分栏）、输出模式、画质、语言；**Ctrl+Enter** 一键生成；**F5** 刷新任务列表；可选 **问题拆解**、**两阶段 AI**；参考图 URL；**大类 + 模板** 与 **填入 JSON / 合并**；**一键生成** / 仅入队 / **检测 Manim** / **设置与配音…**；**仅预览拆解**。
- **任务列表**：选中加载脚本、规划、拆解结果；**AI → 渲染**、**内置测试**、**取消**、**删除**；**查看/复制题干**。
- **输出区**：视频/图片预览、**打开文件夹**（成片所在目录）、全屏。
- **设置**：API 与 Python、**测试连接**；**配音**（云端/本地、一键配音）。

---

## 八、提示词与模板

- **内置资源**：`resources/prompts/`（`manim-scene-generation.txt`、`manim-scene-repair.txt`、`roles/*.md`、`templates/*.txt`）。
- **角色键名**：`PromptRoleKeys`（如 `problemFraming`、`codeGeneration`、`codeRetry`）；**编排说明**：`prompts/PIPELINE.md`。
- **模板**：`PromptTemplateCategory` × `PromptTemplateKind` → `PromptOverrideTemplateService` 生成 JSON；题材附录见 `prompts/templates/appendix-*.txt`（含傅里叶·吉布斯、物理示意、工程安全等）。

---

## 九、一键配音

- **模式**：`DubbingMode.API`（OpenAI 兼容 `POST {base}/audio/speech`）或 `LOCAL`（IndexTTS2 `POST /tts`）。
- **混流**：ffmpeg 将音轨与 mp4 封装，输出同目录 **`{原文件名}_dubbed.mp4`**。
- **Key**：可单独配置配音 Key；未配置时可回退与聊天共用的 Key（见 `DubbingService`）。

---

## 十、与 ManimCat 的关系（简述）

桌面端在**产品语义**上对齐 ManimCat 的 **Workflow**：概念 → 可选拆解 → 单/两阶段代码 → 静态检查 → manim → 修补。  
**未实现** ManimCat 的 Web 队列、Redis、React 前端、Studio Agent、SSE 等；若需对照上游，请直接查阅 ManimCat 仓库，本文档不再维护上游索引。

---

*文档版本：以本目录 `mathanim-desktop` 源码为准。*
