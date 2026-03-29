# 预览脚本：刚体箱中碰撞（manim-physics / pymunk）
# 未加入 example-bank；效果好再由你决定入库。
# 依赖: pip install manim-physics pymunk
# 参考: manim-physics tests/test_rigid_mechanics.py、docs TwoObjectsFalling
#
# 渲染（建议提高帧率减轻穿模，按机器调整）:
#   python -m manim render examples/manim/physics_rigid_box_1.py GeneratedScene -qm --fps 60
#
from manim_physics import *


class GeneratedScene(SpaceScene):
    """封闭矩形边界内的刚体下落与碰撞（与官方测试同源逻辑）。"""

    def construct(self):
        self.camera.background_color = "#0f172a"

        title = Text("刚体箱中碰撞（pymunk）", font_size=36, color="#e2e8f0").to_edge(UP, buff=0.35)
        sub = Text("红球与黄方块 · 四壁静止", font_size=22, color="#94a3b8").next_to(title, DOWN, buff=0.12)
        self.add(title, sub)

        # 与 test_rigid_mechanics 一致的三条边，再加顶边形成封闭「箱」
        x0, x1 = -4.0, 4.0
        y0, y1 = -3.5, 3.5
        wall_kw = dict(color=GREY_B, stroke_width=5)
        ground = Line([x0, y0, 0], [x1, y0, 0], **wall_kw)
        wall_l = Line([x0, y0, 0], [x0, y1, 0], **wall_kw)
        wall_r = Line([x1, y0, 0], [x1, y1, 0], **wall_kw)
        ceiling = Line([x0, y1, 0], [x1, y1, 0], **wall_kw)
        walls = VGroup(ground, wall_l, wall_r, ceiling)
        self.add(walls)

        # 与官方测试相同的两个刚体初始布局（圆 + 倾斜小方块）
        circle = Circle(radius=0.35, color=RED_C, fill_color=RED_E, fill_opacity=1)
        circle.shift(UP)
        circle.shift(DOWN + RIGHT)

        rect = Square(side_length=0.65, color=YELLOW_A, fill_color=YELLOW_A, fill_opacity=1)
        rect.rotate(PI / 4)
        rect.shift(UP * 2)
        rect.scale(0.5)

        self.play(
            DrawBorderThenFill(circle),
            DrawBorderThenFill(rect),
            run_time=1.2,
        )
        # 略提高弹性，便于观察箱内多次碰撞
        self.make_rigid_body(rect, circle, elasticity=0.92, density=1.0, friction=0.75)
        self.make_static_body(walls, elasticity=0.95, friction=0.85)
        self.wait(16)
