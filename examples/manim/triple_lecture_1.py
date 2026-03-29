# example-bank id: triple-lecture-1
# 与 resources/examples/example-bank.json 中「第一卦限三重积分」一致；类名 GeneratedScene 供流水线替换。
"""
渲染（仓库根目录）:
  python -m manim render examples/manim/triple_lecture_1.py GeneratedScene -qm --fps 30
"""
from manim import *


def _sidebar_frame(title: str, *rows, title_font: int = 22):
    title_mob = Text(title, font_size=title_font, color="#263238")
    sep = Line(LEFT * 2.25, RIGHT * 2.25, color="#90a4ae", stroke_width=1)
    body = VGroup(title_mob, sep, *rows).arrange(DOWN, aligned_edge=LEFT, buff=0.22)
    bg = SurroundingRectangle(
        body,
        buff=0.28,
        corner_radius=0.12,
        fill_color="#eceff1",
        fill_opacity=0.96,
        stroke_color="#455a64",
        stroke_width=1.8,
    )
    return VGroup(bg, body)


def build_tetra_region(geo_shift):
    three_axes = ThreeDAxes(
        x_range=[0, 1.2, 0.4],
        y_range=[0, 1.2, 0.4],
        z_range=[0, 1.2, 0.4],
        axis_config={
            "color": "#546e7a",
            "include_tip": True,
            "include_numbers": False,
            "stroke_width": 2.5,
        },
    ).move_to(ORIGIN).shift(geo_shift)

    p000 = three_axes.c2p(0, 0, 0)
    p100 = three_axes.c2p(1, 0, 0)
    p010 = three_axes.c2p(0, 1, 0)
    p001 = three_axes.c2p(0, 0, 1)

    face_xy = Polygon(
        p000, p100, p010,
        fill_color="#87ceeb",
        fill_opacity=0.22,
        stroke_width=2,
        stroke_color="#1565c0",
    )
    face_xz = Polygon(
        p000, p100, p001,
        fill_color="#87ceeb",
        fill_opacity=0.22,
        stroke_width=2,
        stroke_color="#1565c0",
    )
    face_yz = Polygon(
        p000, p010, p001,
        fill_color="#87ceeb",
        fill_opacity=0.22,
        stroke_width=2,
        stroke_color="#1565c0",
    )
    face_slant = Polygon(
        p100, p010, p001,
        fill_color="#4fc3f7",
        fill_opacity=0.38,
        stroke_width=2,
        stroke_color="#0d47a1",
    )
    tetra = VGroup(face_xy, face_xz, face_yz, face_slant)
    return three_axes, tetra


def line1_colored_iterated():
    fz, bz, gz = "#f97316", "#1565c0", "#2e7d32"
    return VGroup(
        MathTex(r"\iiint_V x\,\mathrm{d}V", color="#263238", font_size=22),
        MathTex(r"=", color="#263238", font_size=22),
        MathTex(r"\int_{0}^{1}\mathrm{d}x", color=fz, font_size=22),
        MathTex(r"\int_{0}^{1-x}\mathrm{d}y", color=bz, font_size=22),
        MathTex(r"\int_{0}^{1-x-y} x\,\mathrm{d}z", color=gz, font_size=22),
    ).arrange(RIGHT, buff=0.06)


class GeneratedScene(ThreeDScene):
    def construct(self):
        self.camera.background_color = WHITE

        title = VGroup(
            Text("计算三重积分", color=BLACK, font_size=32),
            MathTex(r"\iiint_V x\,\mathrm{d}V", color=BLACK, font_size=36),
            Text(
                "其中 V 为第一卦限内由平面 x+y+z=1 与三个坐标面围成的四面体",
                color=BLACK,
                font_size=28,
            ),
        ).arrange(DOWN, buff=0.3)
        self.play(Write(title), run_time=1.5)
        self.play(Indicate(title, color="#f97316"), run_time=1)
        self.wait(2)
        self.play(FadeOut(title), run_time=0.5)

        self.set_camera_orientation(
            phi=58 * DEGREES,
            theta=-48 * DEGREES,
            gamma=0,
            zoom=0.68,
        )
        geo_shift = LEFT * 1.55 + DOWN * 0.15

        three_axes, tetra = build_tetra_region(geo_shift)
        p000 = three_axes.c2p(0, 0, 0)
        p100 = three_axes.c2p(1, 0, 0)
        p010 = three_axes.c2p(0, 1, 0)
        p001 = three_axes.c2p(0, 0, 1)

        v_labels = [
            Text("(0,0,0)", color="#263238", font_size=18).next_to(p000, DOWN + LEFT, buff=0.08),
            Text("(1,0,0)", color="#263238", font_size=18).next_to(p100, DOWN + RIGHT, buff=0.06),
            Text("(0,1,0)", color="#263238", font_size=18).next_to(p010, LEFT, buff=0.08),
            Text("(0,0,1)", color="#263238", font_size=18).next_to(p001, UP, buff=0.08),
        ]
        for lab in v_labels:
            self.add_fixed_orientation_mobjects(lab)

        side0 = _sidebar_frame(
            "与左图对照",
            Text("• 左图为积分区域 V（四面体）", font_size=19, color="#37474f"),
            Text("• 斜面即 x+y+z=1", font_size=19, color="#37474f"),
        )
        side0.to_edge(RIGHT, buff=0.22).shift(UP * 0.35)
        self.add_fixed_in_frame_mobjects(side0)

        self.play(Create(three_axes), run_time=0.8)
        self.play(FadeIn(side0, shift=RIGHT * 0.15), run_time=0.55)
        self.play(Create(tetra), run_time=1.2)
        self.play(*[Write(lab) for lab in v_labels], run_time=0.9)
        self.play(Indicate(tetra, color="#f97316"), run_time=0.9)
        self.begin_ambient_camera_rotation(rate=0.1, about="theta")
        self.wait(5)
        self.stop_ambient_camera_rotation()
        self.wait(1.2)

        edge_base = Line(p000, p100, color="#d32f2f", stroke_width=3.5)
        edge_hyp = Line(p100, p010, color="#d32f2f", stroke_width=3.5)
        self.play(Create(edge_base), Create(edge_hyp), run_time=0.9)
        self.play(Indicate(edge_base), Indicate(edge_hyp), run_time=0.7)

        proj_tri = Polygon(
            p000, p100, p010,
            fill_color="#ffe0b2",
            fill_opacity=0.5,
            stroke_color="#e65100",
            stroke_width=2.5,
        )
        self.play(FadeIn(proj_tri), run_time=0.8)

        t = 0.28
        z_bot = three_axes.c2p(t, t, 0)
        z_top = three_axes.c2p(t, t, 1 - 2 * t)
        z_seg = Line(z_bot, z_top, color="#2e7d32", stroke_width=4)
        z_dot_bot = Dot(z_bot, radius=0.06, color="#2e7d32")
        z_dot_top = Dot(z_top, radius=0.06, color="#2e7d32")

        self.play(FadeOut(side0), run_time=0.25)
        side1 = _sidebar_frame(
            "与左图对照",
            Text("红色：底面三角形边界（z=0）", font_size=18, color="#c62828"),
            Text("橙色：投影区域 D_xy（俯视图）", font_size=18, color="#e65100"),
            MathTex(
                r"D_{xy}=\{x\geq0,\,y\geq0,\,x+y\leq1\}",
                color="#263238",
                font_size=22,
            ),
            Text("绿色：固定 (x,y) 沿 z 穿过区域", font_size=18, color="#2e7d32"),
            MathTex(r"0\leq z\leq 1-x-y", color="#2e7d32", font_size=24),
        )
        side1.to_edge(RIGHT, buff=0.22).shift(UP * 0.12)
        self.add_fixed_in_frame_mobjects(side1)

        self.play(FadeIn(side1, shift=RIGHT * 0.12), run_time=0.85)
        self.play(Create(z_seg), FadeIn(z_dot_bot), FadeIn(z_dot_top), run_time=0.9)
        self.play(Indicate(z_seg, color="#66bb6a"), Indicate(proj_tri, color="#fb8c00"), run_time=1.1)
        self.wait(1.5)

        self.play(
            FadeOut(three_axes),
            FadeOut(tetra),
            *[FadeOut(lab) for lab in v_labels],
            FadeOut(edge_base),
            FadeOut(edge_hyp),
            FadeOut(proj_tri),
            FadeOut(z_seg),
            FadeOut(z_dot_bot),
            FadeOut(z_dot_top),
            FadeOut(side1),
            run_time=0.45,
        )

        self.set_camera_orientation(
            phi=0 * DEGREES,
            theta=-90 * DEGREES,
            gamma=0,
            zoom=0.92,
        )

        two_shift = LEFT * 1.35
        two_axes = Axes(
            x_range=[0, 1.2, 0.4],
            y_range=[0, 1.2, 0.4],
            axis_config={
                "color": "#546e7a",
                "include_tip": True,
                "include_numbers": False,
                "stroke_width": 2.5,
            },
            x_length=4.6,
            y_length=4.6,
        ).move_to(ORIGIN).shift(two_shift)

        proj_tri2 = Polygon(
            two_axes.c2p(0, 0),
            two_axes.c2p(1, 0),
            two_axes.c2p(0, 1),
            stroke_color="#1565c0",
            stroke_width=2.5,
            fill_color="#87ceeb",
            fill_opacity=0.35,
        )

        side2 = _sidebar_frame(
            "与左图对照",
            Text("在 D_xy 上：先定 y，再定 x", font_size=19, color="#37474f"),
            MathTex(
                r"x\in[0,1],\quad y\in[0,1-x]",
                color="#263238",
                font_size=24,
            ),
            Text("橙色线：与左图示意同序", font_size=17, color="#e65100"),
        )
        side2.to_edge(RIGHT, buff=0.22).shift(UP * 0.2)
        self.add_fixed_in_frame_mobjects(side2)

        self.play(Create(two_axes), Create(proj_tri2), FadeIn(side2), run_time=1)

        x_line = Line(
            two_axes.c2p(0, 0),
            two_axes.c2p(1, 0),
            color="#f97316",
            stroke_width=4,
        )
        self.play(Create(x_line), Indicate(x_line), run_time=1.1)
        self.play(FadeOut(x_line), run_time=0.25)

        y_line = Line(
            two_axes.c2p(0.4, 0),
            two_axes.c2p(0.4, 0.6),
            color="#f97316",
            stroke_width=4,
        )
        self.play(Create(y_line), Indicate(y_line), run_time=1.1)
        self.wait(1.2)

        self.play(
            FadeOut(two_axes),
            FadeOut(proj_tri2),
            FadeOut(side2),
            FadeOut(y_line),
            run_time=0.45,
        )

        self.set_camera_orientation(
            phi=58 * DEGREES,
            theta=-48 * DEGREES,
            gamma=0,
            zoom=0.54,
        )
        geo_keep = LEFT * 2.5 + DOWN * 0.08
        axes_v, tetra_v = build_tetra_region(geo_keep)
        self.play(Create(axes_v), Create(tetra_v), run_time=1.1)
        self.play(Indicate(tetra_v, color="#f97316", scale_factor=1.02), run_time=0.7)

        legend = VGroup(
            Text("与左图颜色一致", font_size=19, color="#37474f"),
            VGroup(
                Square(side_length=0.18, fill_color="#2e7d32", fill_opacity=1, stroke_width=0),
                Text("对 z（最内）", font_size=17, color="#2e7d32"),
            ).arrange(RIGHT, buff=0.1),
            VGroup(
                Square(side_length=0.18, fill_color="#1565c0", fill_opacity=1, stroke_width=0),
                Text("对 y（中间）", font_size=17, color="#1565c0"),
            ).arrange(RIGHT, buff=0.1),
            VGroup(
                Square(side_length=0.18, fill_color="#f97316", fill_opacity=1, stroke_width=0),
                Text("对 x（最外）", font_size=17, color="#f97316"),
            ).arrange(RIGHT, buff=0.1),
        ).arrange(DOWN, aligned_edge=LEFT, buff=0.14)
        legend.to_edge(RIGHT, buff=0.18).shift(UP * 0.55)

        l1 = line1_colored_iterated()
        l2 = MathTex(
            r"= \int_{0}^{1}\mathrm{d}x\int_{0}^{1-x}x(1-x-y)\,\mathrm{d}y",
            color="#263238",
            font_size=22,
        )
        l3 = MathTex(
            r"= \int_{0}^{1}\frac{x(1-x)^2}{2}\,\mathrm{d}x",
            color="#263238",
            font_size=22,
        )
        l4 = MathTex(
            r"= \frac{1}{2}\Big(\frac{1}{2}-\frac{2}{3}+\frac{1}{4}\Big) = \frac{1}{24}",
            color="#263238",
            font_size=22,
        )
        calc_block = VGroup(l1, l2, l3, l4).arrange(DOWN, buff=0.32, aligned_edge=LEFT)
        calc_block.scale(0.82)
        calc_block.to_edge(RIGHT, buff=0.15).shift(LEFT * 1.05 + DOWN * 0.35)

        for mob in [*calc_block, legend]:
            self.add_fixed_in_frame_mobjects(mob)

        self.play(FadeIn(legend, shift=LEFT * 0.08), run_time=0.65)
        self.play(Write(l1), run_time=1.1)
        self.play(Indicate(l1, color="#ffcc80"), run_time=0.55)
        self.play(Write(l2), run_time=0.85)
        self.play(Indicate(l2, color="#ffcc80"), run_time=0.5)
        self.play(Write(l3), run_time=0.85)
        self.play(Indicate(l3, color="#ffcc80"), run_time=0.5)
        self.play(Write(l4), run_time=0.95)
        self.play(Indicate(l4, color="#ffcc80"), run_time=0.55)
        self.wait(1.5)

        self.play(FadeOut(calc_block), FadeOut(legend), run_time=0.4)

        final_result = MathTex(
            r"\iiint_V x\,\mathrm{d}V = \frac{1}{24}",
            color="#263238",
            font_size=40,
        )
        side_final = _sidebar_frame(
            "与左图对照",
            Text("结果", font_size=19, color="#37474f"),
            Text("区域 V 即左侧四面体", font_size=17, color="#78909c"),
        )
        side_final.to_edge(RIGHT, buff=0.22).shift(UP * 0.15)
        final_result.to_edge(RIGHT, buff=0.22).shift(LEFT * 1.15 + UP * 0.25)

        self.add_fixed_in_frame_mobjects(final_result, side_final)

        self.play(FadeIn(side_final), run_time=0.5)
        self.play(Write(final_result), run_time=1)
        self.play(Indicate(final_result, color="#f97316", scale_factor=1.05), run_time=1)
        self.wait(2.5)
