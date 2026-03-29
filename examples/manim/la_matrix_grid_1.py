# example-bank id: la-matrix-grid-1
# 对齐 3b1b OpeningManimExample 第一段：矩阵作用于平面网格（剪切，Manim CE）
"""
渲染:
  python -m manim render examples/manim/la_matrix_grid_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        intro = Text(
            "线性变换：矩阵把平面上的点（与网格）一起变换",
            font_size=30,
            color="#263238",
        ).to_edge(UP, buff=0.35)
        self.play(FadeIn(intro), run_time=1)
        self.wait(1.2)

        # 与 OpeningManimExample 相同的剪切矩阵 [[1,1],[0,1]]
        mat2 = np.array([[1.0, 1.0], [0.0, 1.0]])
        mat3 = np.block([[mat2, np.zeros((2, 1))], [np.zeros((1, 2)), np.ones((1, 1))]])

        plane = NumberPlane(
            x_range=(-8, 8, 1),
            y_range=(-5, 5, 1),
            x_length=12,
            y_length=7,
            background_line_style={"stroke_color": BLUE_E, "stroke_width": 1, "stroke_opacity": 0.35},
            axis_config={"stroke_color": GREY_B, "include_numbers": False},
        )
        plane.set_color(GREY_B)

        matrix_mob = IntegerMatrix(mat2.astype(int), include_background_rectangle=True)
        matrix_mob.set_color("#263238")
        cap = VGroup(
            Text("剪切变换", font_size=26, color="#455a64"),
            matrix_mob,
        ).arrange(DOWN, buff=0.35)
        cap.to_edge(UP, buff=0.15)

        self.play(
            intro.animate.scale(0.45).to_corner(UL, buff=0.2),
            FadeIn(cap, shift=DOWN * 0.2),
            run_time=0.9,
        )
        self.wait(0.6)

        self.play(Create(plane), run_time=1.8)
        self.wait(0.8)

        hint = Text(
            "x' = x + y，y' = y  （竖直方向不变，水平方向随 y 剪切）",
            font_size=22,
            color="#90a4ae",
        ).to_edge(DOWN, buff=0.45)
        self.play(FadeIn(hint), run_time=0.8)
        self.wait(1)

        self.play(
            plane.animate.apply_matrix(mat3, about_point=ORIGIN),
            run_time=3.5,
            rate_func=smooth,
        )
        self.wait(1.5)

        self.play(Indicate(matrix_mob, color=YELLOW_C, scale_factor=1.05), run_time=1.2)
        self.wait(2)

        out = Text(
            "面积：det = 1（剪切不改变面积）",
            font_size=24,
            color="#81c784",
        ).next_to(hint, UP, buff=0.35)
        self.play(FadeIn(out), run_time=0.9)
        self.wait(2.5)
