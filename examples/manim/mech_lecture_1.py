# example-bank id: mech-lecture-1
# 斜面静摩擦：题设数值 → 受力图（标数字）→ 平衡与 μ≥tanθ
"""
渲染:
  python -m manim render examples/manim/mech_lecture_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE
        theta = 30 * DEGREES
        m_kg = 2.0
        g_val = 10.0
        mg_N = m_kg * g_val
        f_N = mg_N * np.sin(float(theta))
        N_N = mg_N * np.cos(float(theta))
        mu_min = float(np.tan(float(theta)))

        problem = VGroup(
            Text("题目：物块在倾角 θ 斜面上静止", font_size=30, color="#263238"),
            Text("求静摩擦因数 μ 需满足的条件", font_size=28, color="#455a64"),
        ).arrange(DOWN, buff=0.35)
        self.play(Write(problem), run_time=2)
        self.wait(2)
        nums = VGroup(
            Text(f"题设数值：m = {m_kg:g} kg，g = {g_val:g} m/s²，θ = 30°", font_size=24, color="#1565c0"),
            Text(
                f"则 mg = {mg_N:g} N（用于标度受力箭头）",
                font_size=22,
                color="#546e7a",
            ),
        ).arrange(DOWN, buff=0.2)
        self.play(FadeIn(nums, shift=DOWN * 0.15), run_time=1.2)
        self.wait(2)
        self.play(
            problem.animate.scale(0.58).to_edge(UP, buff=0.18),
            nums.animate.scale(0.85).next_to(problem, DOWN, buff=0.25),
            run_time=1,
        )
        self.wait(1.2)

        plane = Line(LEFT * 4.2 + DOWN * 1.15, RIGHT * 3.2 + UP * 0.95, color=GREY_B, stroke_width=4)
        ground = Line(LEFT * 5.5 + DOWN * 1.15, RIGHT * 5.5 + DOWN * 1.15, color=GREY_C, stroke_width=2)

        block = Rectangle(width=1.05, height=0.68, color=BLUE_D, fill_color=BLUE_E, fill_opacity=0.92)
        block.move_to(ORIGIN)
        block.rotate(theta)
        block.shift(UP * 0.06 + LEFT * 0.25)

        pivot = plane.get_start()
        angle_arc = Arc(
            radius=0.6,
            start_angle=plane.get_angle(),
            angle=theta,
            arc_center=pivot,
            color=GREY_A,
            stroke_width=3,
        )
        angle_label = Text("30°", font_size=32, color="#37474f").next_to(angle_arc, RIGHT, buff=0.1).shift(UP * 0.08)

        self.play(Create(ground), Create(plane), run_time=1.2)
        self.wait(0.8)
        self.play(Create(angle_arc), Write(angle_label), run_time=1)
        self.play(FadeIn(block, shift=UP * 0.1), run_time=0.9)
        self.wait(1.5)

        cm = block.get_center()
        mg = Arrow(
            cm,
            cm + DOWN * 1.55,
            color=PURPLE_B,
            stroke_width=5,
            buff=0,
            max_tip_length_to_length_ratio=0.12,
        )
        mg_l = VGroup(
            Text("mg", font_size=24, color=PURPLE_B),
            Text(f"= {mg_N:g} N", font_size=20, color=PURPLE_B),
        ).arrange(DOWN, buff=0.06).next_to(mg, LEFT, buff=0.12)

        n_dir = np.array([-np.sin(theta), np.cos(theta), 0.0])
        along = np.array([np.cos(theta), np.sin(theta), 0.0])
        N = Arrow(cm, cm + n_dir * 1.4, color=GREEN_C, stroke_width=5, buff=0, max_tip_length_to_length_ratio=0.12)
        N_l = VGroup(
            Text("N", font_size=24, color=GREEN_C),
            MathTex("=" + rf" {N_N:.2f}" + r"\,\mathrm{N}", font_size=20, color=GREEN_C),
        ).arrange(DOWN, buff=0.05).next_to(N.get_end(), UR, buff=0.06)

        f_arr = Arrow(
            cm,
            cm + along * 1.15,
            color=RED_D,
            stroke_width=5,
            buff=0,
            max_tip_length_to_length_ratio=0.12,
        )
        f_l = VGroup(
            Text("f（静摩擦）", font_size=20, color=RED_D),
            Text(f"= {f_N:g} N", font_size=20, color=RED_D),
        ).arrange(DOWN, buff=0.05).next_to(f_arr.get_end(), DR, buff=0.06)

        self.play(GrowArrow(mg), Write(mg_l), run_time=1.2)
        self.wait(1.5)
        self.play(GrowArrow(N), Write(N_l), run_time=1.1)
        self.wait(1.2)
        self.play(GrowArrow(f_arr), Write(f_l), run_time=1.1)
        self.wait(2)

        step1 = Text(
            f"沿斜面：f = mg sin θ = {f_N:g} N；垂直斜面：N = mg cos θ ≈ {N_N:.2f} N",
            font_size=22,
            color="#37474f",
        ).to_edge(DOWN, buff=1.45)
        self.play(FadeIn(step1), run_time=1.2)
        self.wait(2.5)

        step2 = Text("静摩擦约束：f ≤ μN  →  mg sin θ ≤ μ · mg cos θ", font_size=24, color="#263238").next_to(
            step1, UP, buff=0.22
        )
        self.play(FadeIn(step2, shift=UP * 0.15), run_time=1.5)
        self.wait(2)

        res = MathTex(
            rf"\mu \geq \tan 30^\circ \approx {mu_min:.3f}",
            font_size=44,
            color="#1565c0",
        ).to_edge(DOWN, buff=0.42)
        self.play(Write(res), run_time=1.8)
        self.play(Indicate(res, color="#ff9800", scale_factor=1.05), run_time=1.5)
        self.wait(4)
