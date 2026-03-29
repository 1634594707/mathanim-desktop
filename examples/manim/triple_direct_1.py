# example-bank id: triple-direct-1
# 球坐标 (r,θ,φ)、与直角坐标关系、体积元示意（加长、慢节奏）
"""
渲染:
  python -m manim render examples/manim/triple_direct_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(ThreeDScene):
    def construct(self):
        self.camera.background_color = "#f5f5f5"

        banner = Text(
            "球坐标与体积元（课堂慢节奏示意）",
            font_size=34,
            color="#263238",
        ).to_edge(UP, buff=0.3)
        self.add_fixed_in_frame_mobjects(banner)
        self.play(FadeIn(banner), run_time=1)
        self.wait(2)

        self.set_camera_orientation(phi=68 * DEGREES, theta=-42 * DEGREES, gamma=0, zoom=0.72)
        axes = ThreeDAxes(
            x_range=[-2.2, 2.2, 1],
            y_range=[-2.2, 2.2, 1],
            z_range=[-1.8, 1.8, 1],
            axis_config={"include_numbers": False, "color": GREY_B},
        )

        sphere = Surface(
            lambda u, v: axes.c2p(
                1.35 * np.cos(u) * np.cos(v),
                1.35 * np.sin(u) * np.cos(v),
                1.35 * np.sin(v),
            ),
            u_range=(0, TAU),
            v_range=(-PI / 2 + 0.08, PI / 2 - 0.08),
            resolution=(28, 16),
            fill_color=BLUE_E,
            fill_opacity=0.12,
            stroke_color=BLUE_D,
            stroke_width=0.7,
        )

        p_surf = axes.c2p(0.9, 0.7, 0.55)
        origin = axes.c2p(0, 0, 0)
        r_vec = Arrow(origin, p_surf, color="#1565c0", stroke_width=4, buff=0.1)
        r_lbl = Text("r", font_size=28, color="#1565c0")
        r_lbl.next_to(r_vec.get_center(), UR, buff=0.05)
        self.add_fixed_orientation_mobjects(r_lbl)

        self.play(Create(axes), run_time=1.2)
        self.play(FadeIn(sphere), run_time=1.5)
        self.wait(1.5)
        self.play(GrowArrow(r_vec), Write(r_lbl), run_time=1.2)
        self.wait(2.5)

        col1 = VGroup(
            Text("直角 ↔ 球坐标（常用约定）", font_size=26, color="#37474f"),
            MathTex(
                r"x=r\sin\varphi\cos\theta,\ "
                r"y=r\sin\varphi\sin\theta,\ "
                r"z=r\cos\varphi",
                font_size=22,
                color="#263238",
            ),
        ).arrange(DOWN, buff=0.3, aligned_edge=LEFT)
        col1.to_edge(LEFT, buff=0.25).shift(UP * 0.35)
        col1.scale(0.88)
        for m in col1:
            self.add_fixed_in_frame_mobjects(m)
        self.play(FadeIn(col1, shift=RIGHT * 0.2), run_time=1.4)
        self.wait(3)

        explain = VGroup(
            Text("r：到原点距离", font_size=26, color="#1565c0"),
            Text("θ：在 xy 平面内从 x 轴转过的角", font_size=24, color="#2e7d32"),
            Text("φ：与 +z 轴夹角（极角，0→z 正向）", font_size=24, color="#e65100"),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.32)
        explain.to_edge(RIGHT, buff=0.22).shift(UP * 0.15)
        for m in explain:
            self.add_fixed_in_frame_mobjects(m)
        self.play(FadeIn(explain, shift=LEFT * 0.15), run_time=1.5)
        self.wait(3)
        self.play(Indicate(r_vec, color="#90caf9"), run_time=1.2)
        self.wait(2)

        self.play(FadeOut(col1), FadeOut(explain), run_time=0.6)
        vol = VGroup(
            Text("体积元（记忆与量纲）", font_size=28, color="#37474f"),
            MathTex(
                r"\mathrm{d}V=r^2\sin\varphi\,\mathrm{d}r\,\mathrm{d}\varphi\,\mathrm{d}\theta",
                font_size=30,
                color="#c62828",
            ),
            Text(
                "小盒：沿 r、φ、θ 各走一小步，体积近似 r² sin φ · Δr Δφ Δθ",
                font_size=22,
                color="#546e7a",
            ),
        ).arrange(DOWN, buff=0.35, aligned_edge=LEFT)
        vol.scale(0.82).to_edge(DOWN, buff=0.35)
        for m in vol:
            self.add_fixed_in_frame_mobjects(m)
        self.play(FadeIn(vol, shift=UP * 0.2), run_time=1.8)
        self.wait(3.5)

        self.begin_ambient_camera_rotation(rate=0.08, about="theta")
        self.wait(6)
        self.stop_ambient_camera_rotation()

        self.play(
            *[FadeOut(m) for m in [banner, sphere, axes, r_vec, r_lbl, vol]],
            run_time=1,
        )
        end = Text("可与直角坐标互化，按区域选择坐标系", font_size=30, color="#455a64")
        self.add_fixed_in_frame_mobjects(end)
        self.play(FadeIn(end), run_time=1.2)
        self.wait(3)
