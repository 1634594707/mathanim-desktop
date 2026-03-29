# example-bank id: triple-lecture-2
# 圆柱域上 z(x^2+y^2) 的三重积分：柱坐标、对称性、逐步计算（加长版）
"""
渲染:
  python -m manim render examples/manim/triple_lecture_2.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(ThreeDScene):
    def construct(self):
        self.camera.background_color = WHITE
        R = 1.1
        H = 1.35

        # —— 题设
        prob = VGroup(
            Text("柱坐标下的三重积分", font_size=36, color="#263238"),
            MathTex(
                r"\iiint_V z\,(x^2+y^2)\,\mathrm{d}V,\quad "
                r"V:\ x^2+y^2\leq R^2,\ 0\leq z\leq h",
                font_size=26,
                color="#263238",
            ),
        ).arrange(DOWN, buff=0.4)
        self.play(Write(prob), run_time=2)
        self.wait(2.5)
        self.play(FadeOut(prob), run_time=0.8)
        self.wait(0.5)

        self.set_camera_orientation(
            phi=60 * DEGREES,
            theta=-52 * DEGREES,
            gamma=0,
            zoom=0.58,
        )
        shift = LEFT * 1.35 + DOWN * 0.15
        axes = ThreeDAxes(
            x_range=[-1.5, 1.5, 0.5],
            y_range=[-1.5, 1.5, 0.5],
            z_range=[0, H + 0.4, 0.4],
            axis_config={"include_numbers": False, "color": "#546e7a", "stroke_width": 2},
        ).shift(shift)

        def cyl_surf(u, v):
            return axes.c2p(R * np.cos(u), R * np.sin(u), v)

        surf = Surface(
            cyl_surf,
            u_range=(0, TAU),
            v_range=(0, H),
            resolution=(34, 14),
            fill_color="#4fc3f7",
            fill_opacity=0.32,
            stroke_color="#0277bd",
            stroke_width=1.0,
        )
        disk0 = Surface(
            lambda u, v: axes.c2p(v * R * np.cos(u), v * R * np.sin(u), 0),
            u_range=(0, TAU),
            v_range=(0, 1),
            resolution=(28, 8),
            fill_color="#81d4fa",
            fill_opacity=0.38,
            stroke_color="#01579b",
        )
        disk1 = Surface(
            lambda u, v: axes.c2p(v * R * np.cos(u), v * R * np.sin(u), H),
            u_range=(0, TAU),
            v_range=(0, 1),
            resolution=(28, 8),
            fill_color="#b3e5fc",
            fill_opacity=0.32,
            stroke_color="#01579b",
        )

        geo_note = VGroup(
            Text("积分区域：实心圆柱", font_size=24, color="#37474f"),
            Text("侧壁 r=R，下底 z=0，上底 z=h", font_size=22, color="#546e7a"),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.2)
        geo_note.to_edge(RIGHT, buff=0.2).shift(UP * 0.5)
        for m in geo_note:
            self.add_fixed_in_frame_mobjects(m)

        self.play(Create(axes), run_time=1.2)
        self.play(FadeIn(geo_note, shift=LEFT * 0.15), run_time=1)
        self.wait(1.5)
        self.play(FadeIn(disk0), FadeIn(disk1), Create(surf), run_time=2)
        self.play(Indicate(surf, color="#ff9800", scale_factor=1.02), run_time=1.5)
        self.wait(2)

        self.begin_ambient_camera_rotation(rate=0.12, about="theta")
        self.wait(5)
        self.stop_ambient_camera_rotation()
        self.wait(1)

        self.play(FadeOut(geo_note), run_time=0.6)

        # —— 柱坐标与体积元（分步出现）
        s1 = VGroup(
            Text("① 换元", font_size=26, color="#1565c0"),
            MathTex(r"x=r\cos\theta,\ y=r\sin\theta,\ z=z", font_size=28, color="#263238"),
            MathTex(r"x^2+y^2=r^2", font_size=28, color="#263238"),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.28)
        s2 = MathTex(
            r"\mathrm{d}V=r\,\mathrm{d}r\,\mathrm{d}\theta\,\mathrm{d}z",
            font_size=30,
            color="#c62828",
        )
        s3 = MathTex(
            r"z(x^2+y^2)\,\mathrm{d}V=z\,r^2\cdot r\,\mathrm{d}r\,\mathrm{d}\theta\,\mathrm{d}z"
            r"=z\,r^3\,\mathrm{d}r\,\mathrm{d}\theta\,\mathrm{d}z",
            font_size=22,
            color="#263238",
        )
        step_col = VGroup(s1, s2, s3).arrange(DOWN, buff=0.35, aligned_edge=LEFT)
        step_col.scale(0.78).to_edge(RIGHT, buff=0.15).shift(UP * 0.1)
        for m in step_col:
            self.add_fixed_in_frame_mobjects(m)

        self.play(FadeIn(s1, shift=LEFT * 0.2), run_time=1.4)
        self.wait(2)
        self.play(Write(s2), run_time=1.2)
        self.wait(2)
        self.play(Write(s3), run_time=1.6)
        self.wait(2.5)

        # —— 积分限与对称性
        s4 = VGroup(
            Text("② 积分限与对称", font_size=26, color="#2e7d32"),
            MathTex(
                r"0\leq\theta\leq2\pi,\quad 0\leq r\leq R,\quad 0\leq z\leq h",
                font_size=22,
                color="#263238",
            ),
            MathTex(
                r"\Rightarrow\ \int_0^{2\pi}\mathrm{d}\theta=2\pi",
                font_size=26,
                color="#263238",
            ),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.25)
        s4.scale(0.78).to_edge(RIGHT, buff=0.15).shift(DOWN * 0.85)
        for m in s4:
            self.add_fixed_in_frame_mobjects(m)
        self.play(FadeOut(step_col), run_time=0.5)
        self.play(FadeIn(s4, shift=LEFT * 0.15), run_time=1.5)
        self.wait(2.5)

        # —— 拆成两个定积分
        s5 = MathTex(
            r"I=2\pi\int_0^R r^3\,\mathrm{d}r\int_0^h z\,\mathrm{d}z",
            font_size=28,
            color="#263238",
        )
        s6 = MathTex(
            r"=2\pi\cdot\frac{R^4}{4}\cdot\frac{h^2}{2}"
            r"=\frac{\pi R^4 h^2}{4}",
            font_size=28,
            color="#1565c0",
        )
        fin = VGroup(s5, s6).arrange(DOWN, buff=0.45, aligned_edge=LEFT)
        fin.scale(0.82).to_edge(RIGHT, buff=0.12)
        for m in fin:
            self.add_fixed_in_frame_mobjects(m)
        self.play(FadeOut(s4), run_time=0.4)
        self.play(Write(s5), run_time=2)
        self.wait(2)
        self.play(Write(s6), run_time=2)
        self.play(Indicate(s6, color="#ff7043", scale_factor=1.03), run_time=1.5)
        self.wait(3)

        self.play(
            *[FadeOut(m) for m in [axes, surf, disk0, disk1, s5, s6]],
            run_time=1,
        )
        out = MathTex(
            r"\displaystyle \iiint_V z(x^2+y^2)\,\mathrm{d}V=\frac{\pi R^4 h^2}{4}",
            font_size=40,
            color="#0d47a1",
        )
        self.add_fixed_in_frame_mobjects(out)
        self.play(Write(out), run_time=2)
        self.wait(4)
