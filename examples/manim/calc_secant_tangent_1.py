# example-bank id: calc-lecture-1
# 导数几何：割线趋近切线（y=x²，典型可渲染参考）
"""
渲染:
  python -m manim render examples/manim/calc_secant_tangent_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        title = VGroup(
            Text("导数的几何意义：割线 → 切线", font_size=32, color="#263238"),
            Text("曲线取 y = x² 上一点附近（示意）", font_size=24, color="#546e7a"),
        ).arrange(DOWN, buff=0.28)
        self.play(Write(title), run_time=1.5)
        self.wait(1)
        self.play(title.animate.scale(0.52).to_edge(UP, buff=0.18), run_time=0.8)
        self.wait(0.5)

        axes = Axes(
            x_range=[-0.3, 2.4, 0.5],
            y_range=[-0.3, 4.2, 1],
            x_length=7,
            y_length=5.2,
            axis_config={"include_numbers": False, "color": GREY_B},
        ).shift(DOWN * 0.25)

        graph = axes.plot(lambda x: x * x, color=BLUE_D, stroke_width=4)
        a = 1.0
        p_fix = axes.c2p(a, a * a)
        dot_fix = Dot(p_fix, radius=0.09, color=RED_C)

        h = ValueTracker(1.35)

        def p_mov():
            x2 = h.get_value()
            return axes.c2p(x2, x2 * x2)

        dot_mov = always_redraw(lambda: Dot(p_mov(), radius=0.09, color=ORANGE))

        secant = always_redraw(
            lambda: Line(p_fix, p_mov(), color=YELLOW_D, stroke_width=4)
        )

        self.play(Create(axes), run_time=0.9)
        self.play(Create(graph), run_time=1)
        self.play(FadeIn(dot_fix), FadeIn(dot_mov), run_time=0.6)
        self.play(Create(secant), run_time=0.5)
        self.wait(0.8)

        hint = Text("移动右端点：割线斜率 → 切线斜率 2a", font_size=22, color="#37474f").to_edge(DOWN, buff=0.5)
        self.play(FadeIn(hint), run_time=0.8)
        self.wait(0.5)

        self.play(h.animate.set_value(a + 0.08), run_time=4, rate_func=smooth)
        self.wait(0.6)

        slope = 2 * a
        tan_line = axes.plot(lambda x: slope * (x - a) + a * a, x_range=[0.2, 1.85], color=GREEN_C, stroke_width=4)
        self.play(Create(tan_line), run_time=1)
        self.play(Indicate(dot_fix, color=RED_C, scale_factor=1.2), run_time=0.9)
        self.wait(1.2)

        res = Text(f"在 x = {a:g} 处，切线斜率 ≈ {slope:g}", font_size=26, color="#1565c0").to_edge(DOWN, buff=0.38)
        self.play(FadeOut(hint), FadeIn(res), run_time=1)
        self.wait(3)
