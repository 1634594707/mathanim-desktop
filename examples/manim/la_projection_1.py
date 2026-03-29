# example-bank id: la-lecture-1
# 向量投影：直角分解与数乘（2D，典型参考）
"""
渲染:
  python -m manim render examples/manim/la_projection_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        title = VGroup(
            Text("向量在方向上的投影（2D）", font_size=32, color="#263238"),
            Text("proj_u(v) = (v·u) u，其中 u 为单位向量", font_size=22, color="#546e7a"),
        ).arrange(DOWN, buff=0.28)
        self.play(Write(title), run_time=1.5)
        self.wait(1)
        self.play(title.animate.scale(0.52).to_edge(UP, buff=0.2), run_time=0.8)

        o = ORIGIN + LEFT * 2.2 + DOWN * 0.6
        v = np.array([3.2, 1.8, 0.0])
        ux = np.array([1.0, 0.0, 0.0])
        proj = float(np.dot(v, ux)) * ux

        arr_v = Arrow(o, o + v, color=BLUE_D, stroke_width=6, buff=0)
        arr_x = Arrow(o, o + ux * 2.8, color=GREY_B, stroke_width=3, buff=0)
        arr_p = Arrow(o, o + proj, color=ORANGE, stroke_width=6, buff=0)

        drop = DashedLine(o + proj, o + v, color=GREEN_C, stroke_width=2)

        lv = Text("v", font_size=28, color=BLUE_D).next_to(arr_v, UP, buff=0.08)
        lx = Text("u：x 轴单位向量", font_size=22, color=GREY_B).next_to(arr_x, DOWN, buff=0.1)
        lp = Text("投影", font_size=22, color=ORANGE).next_to(arr_p, DOWN, buff=0.08)

        self.play(Create(arr_x), Write(lx), run_time=0.8)
        self.wait(0.4)
        self.play(GrowArrow(arr_v), Write(lv), run_time=0.9)
        self.wait(0.6)
        self.play(GrowArrow(arr_p), Write(lp), run_time=0.9)
        self.play(Create(drop), run_time=0.7)
        self.wait(1)

        nums = Text(
            f"例：|vₓ| = {proj[0]:.1f}（与 x 轴单位向量点乘）",
            font_size=24,
            color="#37474f",
        ).to_edge(DOWN, buff=0.55)
        self.play(FadeIn(nums), run_time=0.8)
        self.wait(2)
        self.play(Indicate(arr_p, color=YELLOW_C, scale_factor=1.05), run_time=1)
        self.wait(2.5)
