# mathanim-desktop

JavaFX + Spring Boot 本地应用：**OpenAI 兼容 API** 生成 Manim CE 脚本 → `py_compile` → 子进程 **manim** 出片；SQLite 存任务与设置；可选 **一键配音**（云端 TTS / 本地 IndexTTS2）。

详细架构与配置见同目录 **[DESIGN.md](DESIGN.md)**。

## 构建与运行

```bash
cd mathanim-desktop
./mvnw.cmd -q javafx:run
```

打包：`./mvnw.cmd -DskipTests package`（入口 `com.mathanim.MathAnimLauncher`）。也可用 IDE 直接运行该类。

---

## 功能一览（面向使用者）

| 能力 | 说明 |
|------|------|
| **输入方式** | **直接描述**：一段话说明要画的动画/图；**题目+过程+答案**：分栏填写，合并后交给 AI 可视化 |
| **一键生成并渲染** | 入队 → AI 流水线 → Manim，一条流程完成 |
| **仅入队** | 只创建任务，不自动跑渲染（可再在高级里点「AI→渲染」） |
| **快捷键** | **Ctrl+Enter** = 一键生成（直出框或辅导三栏内均可） |
| **刷新任务** | **F5** 刷新下方历史列表 |
| **设置与配音…** | 跳到「设置」页配置 API、配音、保存 |
| **打开文件夹** | 预览区旁按钮：在资源管理器中打开**成片所在目录**（需任务已有输出路径） |
| **高级选项（折叠）** | 问题拆解、两阶段、参考图 URL、提示词模板/JSON、拆解预览、改代码、工具按钮 |
| **设置** | Base URL / Key / Model、修补轮次、Python、**测试连接**；**一键配音**（所选任务 mp4） |

---

## 关键路径（代码）

| 路径 | 说明 |
|------|------|
| `src/main/java/com/mathanim/` | 服务、领域、UI 控制器 |
| `src/main/resources/fxml/MainView.fxml` | 主界面 |
| `src/main/resources/application.yml` | 默认配置（`mathanim.*`） |
| `src/main/resources/prompts/` | 系统提示词、角色 md、`PIPELINE.md`、题材附录 txt |
| `examples/manim/` | 例题库参考脚本（与 `example-bank.json` 中路径一致，相对本目录） |
