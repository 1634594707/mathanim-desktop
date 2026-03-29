# example-bank id: mech-direct-1
# 平抛：轨迹、速度矢量与水平/竖直分量（标数值，供 AI/课堂参考）
"""
渲染:
  python -m manim render examples/manim/mech_projectile_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        v0 = 8.0
        g = 10.0
        t_hit = 1.0

        title = VGroup(
            Text("平抛运动（水平初速、重力竖直向下）", font_size=32, color="#263238"),
            Text(f"数值：v₀ = {v0:g} m/s，g = {g:g} m/s²（示意）", font_size=22, color="#546e7a"),
        ).arrange(DOWN, buff=0.25)
        self.play(Write(title), run_time=1.5)
        self.wait(1.2)
        self.play(title.animate.scale(0.55).to_edge(UP, buff=0.2), run_time=0.8)
        self.wait(0.5)

        axes = Axes(
            x_range=[0, 10, 2],
            y_range=[-6, 2, 2],
            x_length=9,
            y_length=5,
            axis_config={"include_numbers": False, "color": GREY_B},
        ).shift(LEFT * 0.3 + DOWN * 0.35)

        def x_of(t):
            return v0 * t

        def y_of(t):
            return -0.5 * g * t * t

        traj = axes.plot(
            lambda x: y_of(x / v0) if x > 1e-6 else 0,
            x_range=[0, x_of(t_hit)],
            color=BLUE_D,
            stroke_width=4,
        )

        self.play(Create(axes), run_time=1)
        self.play(Create(traj), run_time=1.2)
        self.wait(0.6)

        lbl_y = Text("y", font_size=22, color=GREY_B).next_to(axes.y_axis, UP, buff=0.05)
        lbl_x = Text("x", font_size=22, color=GREY_B).next_to(axes.x_axis, RIGHT, buff=0.05)
        self.play(FadeIn(lbl_x), FadeIn(lbl_y), run_time=0.5)

        path = ParametricFunction(
            lambda u: axes.c2p(x_of(u), y_of(u)),
            t_range=[0, t_hit],
        )

        dot = Dot(color=RED_C, radius=0.1).move_to(path.point_from_proportion(0))
        self.play(FadeIn(dot), run_time=0.4)
        self.play(MoveAlongPath(dot, path, rate_func=linear), run_time=4)
        self.wait(0.5)

        t0 = 0.55
        p0 = axes.c2p(x_of(t0), y_of(t0))
        vx = v0
        vy = -g * t0
        scale = 0.14
        p1 = p0 + np.array([vx * scale, vy * scale, 0.0])
        ph = p0 + np.array([vx * scale, 0.0, 0.0])
        pv = p0 + np.array([0.0, vy * scale, 0.0])

        v_arrow = Arrow(p0, p1, color=PURPLE_B, stroke_width=5, buff=0)
        ah = Arrow(p0, ph, color=ORANGE, stroke_width=4, buff=0)
        av = Arrow(p0, pv, color=GREEN_C, stroke_width=4, buff=0)

        self.play(dot.animate.move_to(p0), run_time=0.5)
        self.play(GrowArrow(v_arrow), run_time=0.7)
        self.wait(0.3)
        self.play(GrowArrow(ah), GrowArrow(av), run_time=0.85)

        v_txt = Text("v（合速度）", font_size=20, color=PURPLE_B).to_edge(RIGHT, buff=0.35).shift(UP * 1.0)
        vx_txt = Text(f"vₓ = v₀ = {v0:g} m/s", font_size=20, color=ORANGE).next_to(v_txt, DOWN, aligned_edge=LEFT, buff=0.32)
        vy_txt = Text(f"vᵧ = −g t ≈ {vy:.1f} m/s", font_size=20, color=GREEN_C).next_to(vx_txt, DOWN, aligned_edge=LEFT, buff=0.22)
        self.play(FadeIn(v_txt), FadeIn(vx_txt), FadeIn(vy_txt), run_time=1)
        self.wait(1.2)

        foot = Text(
            "水平方向匀速；竖直方向匀加速（与自由落体相同）",
            font_size=22,
            color="#37474f",
        ).to_edge(DOWN, buff=0.45)
        self.play(FadeIn(foot), run_time=1)
        self.wait(2.5)
