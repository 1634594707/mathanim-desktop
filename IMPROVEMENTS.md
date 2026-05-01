# MathAnim 提升任务清单

## 一、渲染进度感知

- [x] 在 `ManimRenderService` 中解析 Manim 子进程的 stderr 输出，提取进度百分比
- [x] 定义进度回调接口 `Consumer<Integer>`，从渲染服务传递到 UI 层
- [x] 在 FXML 输出状态栏新增 `ProgressBar` 组件
- [x] 在 `MainViewController` 中绑定进度回调，实时更新进度条
- [x] 渲染超时时显示"可能卡住"提示，提供继续等待或取消的选项

## 二、任务列表分页

- [x] `RenderJobRepository` 新增分页查询方法（`Pageable` 参数）
- [x] `RenderJobService` 修改列表查询逻辑，支持分页加载
- [x] `MainViewController` 中 ListView 改为虚拟化渲染，只加载可视区域
- [x] `jobLogBuffers` 增加 LRU 缓存策略，最大容量 50，淘汰最久未查看的任务日志
- [x] 搜索和筛选改用数据库层面的分页查询（`LIMIT/OFFSET`）

## 三、错误恢复智能优化

- [x] 定义错误指纹算法（hash 错误关键信息，忽略行号等易变内容）
- [x] `WorkflowPipelineService` 中记录每轮错误指纹，比对连续两轮是否相同
- [x] 连续两轮相同错误时提前终止重试，提示"AI 无法自动修复此错误"
- [x] `ReliableGenerationHints` 增加"连续相同错误"的特殊提示，引导 AI 换思路
- [x] 达到最大轮次后，比对首尾轮错误，相同则提示"可能需要手动修改脚本"

## 四、activePipeline 竞态修复

- [x] `activePipeline` 类型从 `Map` 改为 `ConcurrentHashMap`
- [x] `poll` lambda 中改为每次从数据库重新查询 job 状态，不使用捕获的旧引用
- [x] 取消任务时通过标志位通知 poll 循环优雅退出
- [x] 考虑用 `ScheduledExecutorService.scheduleWithFixedDelay` 替代手写 poll + retry
- [x] 消除 `immediatePollDebounce` 的竞态窗口

## 五、GeoGebra 功能增强

- [ ] GeoGebra 标签页增加命令手动编辑区域，支持微调 AI 生成的指令
- [ ] 增加"导出为图片"按钮，调用 GeoGebra `exportPNG` API 截图
- [ ] 导出的图片自动作为参考图传入 Manim 生成流程
- [ ] 增加 GeoGebra 命令的历史记录功能（本地持久化）
- [ ] 增加 GeoGebra 命令的收藏功能

## 六、数据库 Schema 管理

- [x] 引入 Flyway 依赖（`pom.xml` 添加 `flyway-core`，使用其内置 SQLite 支持）
- [x] 创建 `src/main/resources/db/migration/` 目录，编写 V1 基线迁移脚本
- [x] `application.yml` 中将 `ddl-auto` 改为 `validate`，由 Flyway 管理 schema
- [x] 编写清理迁移：删除 `app_settings` 表中已废弃的 dubbing 相关列
- [x] 增加启动时自动清理逻辑（如删除超过 90 天的失败任务）
- [x] 增加数据库备份功能（复制 SQLite 文件到备份目录）

## 七、MainViewController 拆分

- [x] 提取 `StudioController` — 左栏创作区（概念输入、选项、生成按钮）
- [x] 提取 `OutputController` — 右栏输出区（预览、日志、诊断卡片）
- [x] 提取 `GeogebraController` — GeoGebra 标签页全部逻辑
- [x] 提取 `HistoryController` — 任务历史列表（搜索、筛选、批量操作）
- [x] 创建 `SharedState` bean 管理跨控制器共享状态（当前选中任务、日志缓冲等）
- [x] 拆分 `MainView.fxml` 为多个子 FXML，使用 `fx:include` 引入
- [x] 确保各控制器之间通过事件或共享状态通信，无直接依赖

***

共 **7 大项、38 个子任务**。已完成 **33/38**（一、二、三、四、六、七已完成）。
