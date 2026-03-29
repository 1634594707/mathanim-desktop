# example-bank id: mech-direct-1
# 水平面匀速圆周：题设数值 → 俯视图受力（向心力标数字）
"""
渲染:
  python -m manim render examples/manim/mech_direct_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE

        m_kg = 0.5
        r_m = 2.0
        omega = 3.0
        F_n = m_kg * omega**2 * r_m

        title = VGroup(
            Text("水平面匀速圆周运动（俯视图）", font_size=34, color="#263238"),
            Text("向心力由静摩擦力提供", font_size=24, color="#546e7a"),
        ).arrange(DOWN, buff=0.3)
        spec = VGroup(
            Text(
                f"题设数值：m = {m_kg:g} kg，r = {r_m:g} m，ω = {omega:g} rad/s",
                font_size=22,
                color="#1565c0",
            ),
            Text(
                f"则所需向心力大小 Fₙ = mω²r = {F_n:g} N（由 fₛ 提供）",
                font_size=22,
                color="#546e7a",
            ),
        ).arrange(DOWN, buff=0.15)

        self.play(Write(title), run_time=1.8)
        self.play(FadeIn(spec, shift=DOWN * 0.12), run_time=1.2)
        self.wait(1.8)
        self.play(
            title.animate.scale(0.52).to_edge(UP, buff=0.12),
            spec.animate.scale(0.95).next_to(title, DOWN, buff=0.18),
            run_time=1,
        )
        self.wait(0.8)

        R = 2.2
        center = np.array([0.0, -0.35, 0.0])
        circle = Circle(radius=R, color=GREY_B, stroke_width=3).move_to(center)
        dot_path = Dot(center, radius=0.04, color=GREY_B)

        block = Dot(center + RIGHT * R, radius=0.12, color=BLUE_E)
        block_lbl = VGroup(
            Text("m", font_size=24, color=BLUE_E),
            Text(f"= {m_kg:g} kg", font_size=18, color=BLUE_E),
        ).arrange(DOWN, buff=0.02)
        block_lbl.next_to(block, UP, buff=0.1)

        def upd_block_lbl(m):
            m.next_to(block, UP, buff=0.1)

        block_lbl.add_updater(upd_block_lbl)

        self.play(Create(circle), FadeIn(dot_path), run_time=1.2)
        self.wait(0.6)
        self.play(FadeIn(block), Write(block_lbl), run_time=1)
        self.wait(1)

        omega_lbl = Text(f"ω = {omega:g} rad/s", font_size=22, color="#455a64").next_to(circle, UR, buff=0.12)
        self.play(FadeIn(omega_lbl), run_time=0.8)
        self.wait(0.8)

        self.play(MoveAlongPath(block, circle, rate_func=linear), run_time=5)
        self.wait(1)

        r_arrow = Arrow(center, block.get_center(), color=YELLOW_D, stroke_width=5, buff=0)
        r_lbl = Text(f"r = {r_m:g} m", font_size=24, color=YELLOW_D)
        r_lbl.next_to(Line(center, block.get_center(), stroke_width=0), UP, buff=0.08)

        v = center - block.get_center()
        u = v / np.linalg.norm(v)
        fc = Arrow(block.get_center(), block.get_center() + u * 0.9, color=RED_C, stroke_width=6, buff=0)
        fc_lbl = VGroup(
            Text("Fₙ = mω²r", font_size=22, color=RED_C),
            Text(f"= {F_n:g} N", font_size=22, color=RED_C),
        ).arrange(DOWN, buff=0.06)
        fc_lbl.next_to(fc, LEFT, buff=0.12)

        self.play(GrowArrow(r_arrow), Write(r_lbl), run_time=1.1)
        self.wait(1.2)
        self.play(GrowArrow(fc), Write(fc_lbl), run_time=1.3)
        self.wait(1.5)

        def upd_r(m):
            m.put_start_and_end_on(center, np.array(block.get_center(), dtype=float))

        def upd_r_lbl(m):
            seg = Line(center, np.array(block.get_center(), dtype=float), stroke_width=0)
            m.next_to(seg, UP, buff=0.08)

        def upd_fc(m):
            bc = np.array(block.get_center(), dtype=float)
            vv = center - bc
            nn = np.linalg.norm(vv)
            if nn < 1e-6:
                return
            uu = vv / nn
            m.put_start_and_end_on(bc, bc + uu * 0.9)

        def upd_fc_lbl(m):
            m.next_to(fc, LEFT, buff=0.12)

        r_arrow.add_updater(upd_r)
        r_lbl.add_updater(upd_r_lbl)
        fc.add_updater(upd_fc)
        fc_lbl.add_updater(upd_fc_lbl)

        hint = Text(
            f"未打滑时：静摩擦力 fₛ = {F_n:g} N，方向指向圆心",
            font_size=22,
            color="#37474f",
        ).to_edge(DOWN, buff=0.42)
        self.play(FadeIn(hint, shift=UP * 0.15), run_time=1.2)
        self.wait(2)

        self.play(MoveAlongPath(block, circle, rate_func=linear), run_time=5)

        for mob in (block_lbl, r_arrow, r_lbl, fc, fc_lbl):
            mob.clear_updaters()

        self.wait(3)
