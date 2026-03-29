# 3Blue1Brown 官方 `example_scenes` 与 Manim CE 对照

本仓库根目录下的 `manim/` 为 **3b1b/manim**（manimgl / `manimlib`）。经典范例集中在：

`manim/example_scenes.py`

运行方式（需已安装 manimgl 环境）：

```text
manimgl example_scenes.py OpeningManimExample
```

下列「优秀案例」适合作为 **分镜与动画手法** 的参考；**生成 mathanim-desktop 代码时请使用 Manim Community Edition**（`from manim import *`），API 与 manimgl 并不完全一致。

---

## 范例速览（按教学价值）

| 类名 | 亮点 | 迁移到 CE 时的注意点 |
|------|------|----------------------|
| **OpeningManimExample** | 开篇叙事；`NumberPlane` + 矩阵线性变换；`ComplexPlane` + `apply_complex_function`（z→z²） | CE 有 `NumberPlane`、`ComplexPlane`；复变换需查 CE 是否同名或自写 `apply_complex_function` |
| **AnimatingMethods** | `.animate` 链式、颜色渐变、`apply_complex_function` / `apply_function` | CE 同样支持 `mob.animate`；复杂形变 API 名称可能不同 |
| **TextExample** | `Text` 与 `t2c` / `t2f` 着色与字体 | CE 的 `Text` 也支持 `t2c` 等（见 CE 文档） |
| **TexTransformExample** | **TransformMatchingStrings**：公式变形对齐子串 | CE 中为 **`TransformMatchingTex`** 等，用法接近；适合做「恒等变形」板书 |
| **TexIndexing** | `Tex` 按子串索引、`FlashAround`、`Indicate` | CE 的 `MathTex` 索引规则类似，但无 LaTeX 时优先 `Text` |
| **UpdatersExample** | **`always_redraw`**、`f_always`、数值随图形变 | CE 有 **`always_redraw`**、`add_updater`；与当前 `mech_direct_1` 中箭头随动一致 |
| **CoordinateSystemExample** | `Axes.c2p` / `Axes.get_h_line` + `always_redraw` 追踪竖线水平线 | CE：`axes.c2p`，辅助线可用 `always_redraw(lambda: DashedLine(...))` |
| **GraphExample** | **`axes.get_graph`**、多曲线 `ReplacementTransform`、**`ValueTracker` + dot 在曲线上移动** | CE：`axes.plot`；点上移动用 `dot.add_updater` 或 `x_tracker` 模式（与 `calc_secant_tangent_1` 同源思想） |
| **TexAndNumbersExample** | 方程中数字与几何量联动 | CE 可用 `DecimalNumber` + `add_updater`，或纯 `Text` 更新 |
| **SurfaceExample** | 三维纹理面、相机、`increment_phi` / 环境旋转 | CE：`ThreeDScene`、`Surface`、`begin_ambient_camera_rotation`（与 `triple_*` 例题一致） |

---

## 建议在 AI 提示词中强调的「手法」（来自上述范例）

1. **先建立坐标系/区域，再动点**：`CoordinateSystemExample`、`GraphExample`。  
2. **用 `always_redraw` 绑辅助线**（垂线、水平线、括号），避免一帧帧手算。  
3. **公式变形**用「匹配子串的 Transform」，而不是硬切 `ReplacementTransform` 整行（见 `TexTransformExample`）。  
4. **复杂平面映射**（矩阵、z²）适合作为 **高阶扩展例题**，需单独验证 CE API。  
5. **`set_backstroke`**（描边防压线）在 manimgl 常用；CE 可用 `Text` 的描边类参数或背景 `Rectangle` 模拟。

---

## 与当前 `examples/manim/` 的对应关系

| 3b1b 范例思想 | 本仓库最接近的脚本 |
|---------------|-------------------|
| GraphExample 点在抛物线上移动 | `calc_secant_tangent_1.py` |
| CoordinateSystemExample 辅助线 | 可在后续高数题中复用 `always_redraw` + `axes.get_vertical_line`（CE 名称略不同） |
| UpdatersExample 宽度与标签 | `mech_direct_1.py`（矢量随点动） |
| OpeningManimExample 网格变换 | **`la_matrix_grid_1.py`**（剪切矩阵 [[1,1],[0,1]] + `NumberPlane.animate.apply_matrix`） |

---

## 延伸阅读

- 官方文档（在线）：[Example scenes 说明](https://3b1b.github.io/manim/getting_started/example_scenes.html)  
- 大量历史视频源码：`3b1b/videos` 仓库（与本 manim 版本可能不完全兼容，仅作叙事参考）
