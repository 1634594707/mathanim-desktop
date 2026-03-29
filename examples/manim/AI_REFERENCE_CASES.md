# 例题库与 AI 生成参考（MathAnim）

本目录脚本与 `mathanim-desktop` 中 `example-bank.json` 一一对应，设计时对齐 **Math-To-Manim** 思路：**分镜清晰、可渲染优先、中文用 `Text`、公式尽量短或避免无 LaTeX 环境卡死**。

## 案例矩阵（典型题类）

| id | 文件 | 学科与题型 | AI 生成关键词 |
|----|------|------------|----------------|
| triple-lecture-1 | triple_lecture_1.py | 三重积分 / 直角坐标累次积分 | 四面体、投影、先 z 后 y 后 x、固定画面侧栏 |
| triple-lecture-2 | triple_lecture_2.py | 三重积分 / 柱坐标 | 圆柱、r θ z、dV=r dr dθ dz、对称性 2π |
| triple-direct-1 | triple_direct_1.py | 球坐标与体积元 | ThreeDScene、Surface、慢节奏、环境旋转 |
| calc-lecture-1 | calc_secant_tangent_1.py | 导数几何 | 割线、切线、y=x²、ValueTracker、Indicate |
| la-lecture-1 | la_projection_1.py | 向量投影 | 正交分解、虚线、投影箭头、数乘 |
| la-matrix-grid-1 | la_matrix_grid_1.py | 矩阵×网格（剪切） | NumberPlane、`apply_matrix`、IntegerMatrix、对齐 OpeningManimExample |
| mech-lecture-1 | mech_lecture_1.py | 斜面静摩擦 | 受力箭头、数值、μ≥tanθ |
| mech-lecture-2 | mech_lecture_2.py | 连接体 | 隔离体、T、F、N、mg、多物体 |
| mech-circular-1 | mech_direct_1.py | 水平圆周 | 俯视图、向心力、mω²r、Updater 随动 |
| mech-direct-1 | mech_projectile_1.py | 平抛 | 轨迹、vₓ vᵧ、Axes、MoveAlongPath |

## 导出参考图（最后一帧 PNG）

仓库根目录执行：

```powershell
.\examples\manim\render_reference_frames.ps1
```

输出目录：`examples/manim/reference_frames/`。用于 **RAG / few-shot 截图对齐** 或人工检查构图。

注意：`triple_lecture_2.py` 中相邻两段 `MathTex` 字符串拼接时，`\quad` 与下一行开头的 `V` 曾误连成 `\quadV` 导致 LaTeX 报错；已改为 `\quad `（末尾空格）再拼 `V:\ ...`。

## 与 Math-To-Manim 的对应关系

- **管线**：其仓库侧重多 agent（叙事、知识、代码）；本桌面端用 **固定例题 + 提示词模板** 收敛输出。
- **资产**：其 `public/`、`giffolder/` 多为高质量 GIF；本仓库用 **可维护的短 Manim 脚本 + 批量 PNG** 便于版本控制。
- **代码规范**：遵守 `mathanim-desktop` 内 `manim-scene-generation.txt`（`GeneratedScene`、`Axes` 关数字、`MathTex` 不用全角标点等）。

## 与本地 `manim/`（3Blue1Brown 官方 manimgl）范例

仓库内 **`manim/example_scenes.py`** 含 **OpeningManimExample**（网格矩阵变换、复平方映射）、**GraphExample**（曲线与 ValueTracker）、**CoordinateSystemExample**（`c2p` 与追踪线）、**TexTransformExample**（公式子串变换）等，是分镜与技法的优质参考。

本桌面工程使用 **Manim CE**，API 与 `manimlib` 不完全相同。已整理 **manimgl → CE** 的对照与可迁移手法，见同目录 **`MANIM_3B1B_REFERENCE.md`**。
