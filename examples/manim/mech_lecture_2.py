# example-bank id: mech-lecture-2
# 水平连接体：题设数值 → 装置图 → 隔离体受力图（标数字）
"""
渲染:
  python -m manim render examples/manim/mech_lecture_2.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        F_N = 12.0
        m1_kg = 3.0
        m2_kg = 2.0
        mu = 0.25
        g_val = 10.0
        m1g = m1_kg * g_val
        m2g = m2_kg * g_val
        f_max = mu * m1g

        title = VGroup(
            Text("连接体与轻绳（受力 + 数值）", font_size=34, color="#263238"),
            Text("水平桌面：m₁ 受拉力 F，绳连 m₂；m₂ 光滑、m₁ 与桌面有摩擦", font_size=22, color="#546e7a"),
        ).arrange(DOWN, buff=0.3)
        spec = VGroup(
            Text(f"题设数值：F = {F_N:g} N，m₁ = {m1_kg:g} kg，m₂ = {m2_kg:g} kg，μ = {mu:g}，g = {g_val:g} m/s²", font_size=22, color="#1565c0"),
            Text(f"（m₁g = {m1g:g} N，m₂g = {m2g:g} N；m₁ 最大静摩擦约 f_max = μN₁ = {f_max:g} N）", font_size=20, color="#546e7a"),
        ).arrange(DOWN, buff=0.15)

        self.play(Write(title), run_time=1.8)
        self.play(FadeIn(spec, shift=DOWN * 0.12), run_time=1.2)
        self.wait(2)
        self.play(
            title.animate.scale(0.52).to_edge(UP, buff=0.15),
            spec.animate.scale(0.92).next_to(title, DOWN, buff=0.2),
            run_time=1,
        )
        self.wait(0.8)

        table_top = Line(LEFT * 6 + DOWN * 0.85, RIGHT * 6 + DOWN * 0.85, color=GREY_B, stroke_width=4)

        m1 = Rectangle(width=1.0, height=0.65, fill_color=BLUE_E, fill_opacity=1, stroke_color=BLUE_D)
        m2 = Rectangle(width=1.0, height=0.65, fill_color=GREEN_E, fill_opacity=1, stroke_color=GREEN_D)
        m1.move_to(LEFT * 2.0 + DOWN * 0.52)
        m2.move_to(RIGHT * 1.8 + DOWN * 0.52)

        rope = Line(m1.get_right() + UP * 0.06, m2.get_left() + UP * 0.06, color=GREY_D, stroke_width=4)
        lbl1 = Text("m₁", font_size=26).move_to(m1.get_center())
        lbl2 = Text("m₂", font_size=26).move_to(m2.get_center())

        self.play(Create(table_top), run_time=0.8)
        self.play(FadeIn(m1), FadeIn(m2), Write(lbl1), Write(lbl2), run_time=1.2)
        self.play(Create(rope), run_time=0.9)
        self.wait(0.8)

        c1, c2 = m1.get_center(), m2.get_center()
        scale_h = 0.022

        F_arr = Arrow(
            m1.get_left() + UP * 0.03,
            m1.get_left() + LEFT * 1.35 + UP * 0.03,
            color=RED_C,
            stroke_width=6,
            buff=0,
        )
        Fl = VGroup(Text("F", font_size=28, color=RED_C), Text(f"{F_N:g} N", font_size=20, color=RED_C)).arrange(
            DOWN, buff=0.04
        ).next_to(F_arr, UP, buff=0.08)

        T1 = Arrow(
            m1.get_right() + UP * 0.1,
            m1.get_center() + RIGHT * 0.38 + UP * 0.1,
            color=ORANGE,
            stroke_width=4,
            buff=0,
        )
        T2 = Arrow(
            m2.get_left() + UP * 0.1,
            m2.get_center() + LEFT * 0.38 + UP * 0.1,
            color=ORANGE,
            stroke_width=4,
            buff=0,
        )
        Tl = Text("同一绳：张力 T 大小相等", font_size=22, color=ORANGE).next_to(rope, UP, buff=0.38)

        self.play(GrowArrow(F_arr), Write(Fl), run_time=1.1)
        self.wait(1)
        self.play(GrowArrow(T1), GrowArrow(T2), FadeIn(Tl), run_time=1.3)
        self.wait(1.2)

        # m₁：竖直方向 N₁ = m₁g，水平 f
        mg1 = Arrow(c1, c1 + DOWN * m1g * scale_h, color=PURPLE_B, stroke_width=5, buff=0)
        N1 = Arrow(c1, c1 + UP * m1g * scale_h, color=GREEN_C, stroke_width=5, buff=0)
        f_arr = Arrow(c1, c1 + LEFT * 1.0, color=RED_D, stroke_width=5, buff=0)
        mg1_l = VGroup(Text("m₁g", font_size=20, color=PURPLE_B), Text(f"{m1g:g} N", font_size=18, color=PURPLE_B)).arrange(
            DOWN, buff=0.03
        ).next_to(mg1, DOWN, buff=0.06)
        N1_l = VGroup(Text("N₁", font_size=20, color=GREEN_C), Text(f"{m1g:g} N", font_size=18, color=GREEN_C)).arrange(
            DOWN, buff=0.03
        ).next_to(N1, UP, buff=0.06)
        f_l = VGroup(
            Text("f（静摩擦）", font_size=18, color=RED_D),
            Text(f"≤ {f_max:g} N", font_size=17, color=RED_D),
        ).arrange(DOWN, buff=0.03).next_to(f_arr, LEFT, buff=0.08)

        self.play(GrowArrow(mg1), GrowArrow(N1), Write(mg1_l), Write(N1_l), run_time=1.2)
        self.wait(0.8)
        self.play(GrowArrow(f_arr), Write(f_l), run_time=1)
        self.wait(1.5)

        # m₂：光滑，竖直平衡
        mg2 = Arrow(c2, c2 + DOWN * m2g * scale_h, color=PURPLE_B, stroke_width=5, buff=0)
        N2 = Arrow(c2, c2 + UP * m2g * scale_h, color=GREEN_C, stroke_width=5, buff=0)
        mg2_l = VGroup(Text("m₂g", font_size=20, color=PURPLE_B), Text(f"{m2g:g} N", font_size=18, color=PURPLE_B)).arrange(
            DOWN, buff=0.03
        ).next_to(mg2, DOWN, buff=0.06)
        N2_l = VGroup(Text("N₂", font_size=20, color=GREEN_C), Text(f"{m2g:g} N", font_size=18, color=GREEN_C)).arrange(
            DOWN, buff=0.03
        ).next_to(N2, UP, buff=0.06)

        self.play(GrowArrow(mg2), GrowArrow(N2), Write(mg2_l), Write(N2_l), run_time=1.2)
        self.wait(2)

        eq = VGroup(
            Text("列式（水平）", font_size=26, color="#263238"),
            Text(f"• m₁：F − T − f = m₁a（f 方向与相对运动趋势相反）", font_size=20, color="#455a64"),
            Text(f"• m₂：T = m₂a（光滑，无摩擦）", font_size=20, color="#455a64"),
            Text("• 联立可得 a、T；需满足 |f| ≤ μN₁ 才未打滑", font_size=20, color="#455a64"),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.22)
        eq.to_edge(DOWN, buff=0.35)
        self.play(FadeIn(eq, shift=UP * 0.15), run_time=1.8)
        self.wait(3.5)

        self.play(
            Indicate(m1, color=BLUE_C, scale_factor=1.05),
            Indicate(m2, color=GREEN_C, scale_factor=1.05),
            run_time=1.5,
        )
        self.wait(3)
