# MathAnim Desktop

`MathAnim Desktop` 是一个本地桌面工具，用来把“题目 / 教学思路 / 动画描述”转换成可渲染的 Manim 场景，并输出视频或图片。

核心链路是：

`用户输入 -> AI 生成或修改 Manim 脚本 -> py_compile 检查 -> Manim 渲染 -> 本地预览 / 导出`

项目技术栈：

- `JavaFX`
- `Spring Boot`
- `SQLite`
- `Manim Community`

更完整的架构说明见 [DESIGN.md](DESIGN.md)。

## 适合什么场景

- 数学、物理、线代、力学等课程的动画备课
- 把题目解析转换成讲解型动画
- 快速生成教学用 Manim 草稿
- 基于已有脚本继续修改、修补、重渲染
- 结合 GeoGebra 辅助生成几何指令

## 当前主要能力

### 1. 双输入模式

- `直接描述`
  适合“我想做一个什么动画”的场景
- `题干 + 解析 + 答案`
  适合讲题、备课、把解题过程转成动画

### 2. AI 到 Manim 工作流

- 单阶段代码生成
- 多阶段生成：解题/可视目标 -> 分镜 -> 代码
- 可选问题拆解预览
- 支持参考图 URL
- 自动静态检查 `py_compile`
- 渲染失败后自动修补重试
- 多轮失败后可切换保底模式

### 3. 输出与预览

- 输出 `mp4` 视频或 `png` 图片
- 内置视频预览、进度拖动、全屏预览
- 任务日志面板
- 失败诊断卡片
- 右侧状态栏显示草稿 / 排队 / 处理中 / 保底模式 / 已就绪 / 失败

### 4. 任务管理

- 立即生成并执行
- 仅保存草稿任务
- 执行选中任务
- 修改脚本后重新执行
- 删除任务
- 取消任务
- 查看 / 复制任务内容
- 失败后直接重试
- 失败后强制保底重试

### 5. 扩展能力

- Prompt 覆盖 JSON
- 内置示例库
- GeoGebra 指令生成
- TTS / 配音流程接入

## 最近完成的产品优化

这几轮主要围绕“复杂任务失败时更容易理解和处理”做了优化：

- 新增任务日志面板
- 新增失败诊断卡片
- 新增保底模式醒目标识
- 保底任务在历史列表里高亮
- 失败任务在历史列表里高亮
- 任务状态改成人话显示
- 增加“重试选中任务”
- 增加“保底重试”
- 增强失败诊断与修补提示

## 运行方式

### 方式 1：开发启动脚本

```powershell
cd mathanim-desktop
.\run-dev.ps1
```

### 方式 2：直接使用 Maven Wrapper

```powershell
cd mathanim-desktop
.\mvnw.cmd spring-boot:run
```

### 运行测试

```powershell
.\mvnw.cmd test
```

### 打包

```powershell
.\mvnw.cmd -DskipTests package
```

## Release 下载与启动

已发布版本：[`v0.1.0`](https://github.com/1634594707/mathanim-desktop/releases/tag/v0.1.0)

### 下载

在 Release 页面下载：

- `mathanim-desktop-0.1.0-SNAPSHOT.jar`

### 启动

在已安装 JDK 17+ 的环境中执行：

```powershell
java -jar .\mathanim-desktop-0.1.0-SNAPSHOT.jar
```

若你本机已配置 Python / Manim，首次启动后按“设置”页提示完成 `Base URL / API Key / Model / Python` 配置即可使用。

应用入口类：

- `com.mathanim.MathAnimLauncher`

## 首次使用建议

第一次启动后，建议按这个顺序检查：

1. 打开“设置”页
2. 配置 `Base URL / API Key / Model`
3. 配置 `Python`
4. 点击“测试 AI 连接”
5. 点击“检测 Manim”
6. 回到“创作”页执行一个简单任务

如果你还需要配音，再额外配置：

- TTS API 参数，或
- 本地 IndexTTS2 地址
- `ffmpeg`

## 依赖要求

### 必需

- JDK 17+
- Python 环境
- Manim Community Edition
- 可用的 OpenAI 兼容 Chat Completions 接口

### 可选

- `ffmpeg`
- IndexTTS2

## 配置说明

默认配置文件：

- `src/main/resources/application.yml`

常见配置前缀：

- `mathanim.data-dir`
  数据目录，包含 SQLite、jobs、cache
- `mathanim.ai.*`
  AI 接口与重试参数
- `mathanim.manim.*`
  Manim 命令、帧率、超时、输出目录等
- `mathanim.dubbing.*`
  配音相关配置

界面“设置”页中的值会覆盖部分 YAML 默认值，并持久化到 SQLite。

## 关键目录

### 源码

- `src/main/java/com/mathanim/`
- `src/main/resources/fxml/MainView.fxml`
- `src/main/resources/css/mathanim-theme.css`
- `src/main/resources/prompts/`

### 数据与输出

- `.mathanim/mathanim.db`
- `.mathanim/jobs/<jobId>/`
- `generated-videos/`

### 示例

- `examples/manim/`
- `src/main/resources/examples/example-bank.json`

## 典型工作流

### 普通生成

1. 输入创作描述，或题目与解析
2. 可选执行问题拆解
3. AI 生成初始 Manim 脚本
4. 运行 `py_compile`
5. 如失败则进入修补轮次
6. 调用 `manim` 渲染
7. 在右侧预览并查看日志 / 失败诊断

### 修改脚本

`已有脚本 + 编辑说明 -> AI 修改 -> 检查 -> 渲染`

### 失败后恢复

- 直接点击 `重试选中任务`
- 或点击 `保底重试`

保底重试会强制进入更保守的生成策略，优先提高成功率。

## 重要类

- `MathAnimFx`
  JavaFX 启动与窗口装配
- `ui.MainViewController`
  主界面交互、预览、任务操作、设置、GeoGebra
- `service.RenderJobService`
  任务创建、异步执行、删除、重试
- `service.WorkflowPipelineService`
  AI 到 Manim 的核心工作流
- `service.ManimRenderService`
  Manim CLI 调用与结果处理
- `service.ManimAiCodeService`
  代码生成、修改、修补
- `service.ProblemFramingService`
  问题拆解

## 已知注意点

- “保存草稿”和“立即执行”是两种不同操作
- 如果模型不支持视觉输入，参考图相关能力可能退化
- JavaFX 对部分视频编码的兼容性有限
- Manim 与 Python 环境最好保持一致
- 复杂任务不一定是模型太差，很多时候是镜头过多、布局过重、API 过于脆弱

## 开发建议

如果你继续扩展这个项目，当前最值得优先推进的方向是：

- 左侧操作区语义重构
- 任务重试来源标记
- 历史任务筛选与分组
- 控制器拆分
- UI 状态逻辑测试补强

## 相关文档

- [DESIGN.md](DESIGN.md)
- [PRODUCT_OPTIMIZATION_PLAN.md](PRODUCT_OPTIMIZATION_PLAN.md)
