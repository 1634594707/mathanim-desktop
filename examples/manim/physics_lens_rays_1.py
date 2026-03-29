# example-bank id: physics-lens-rays-1
# 依赖: pip install manim-physics
# 参考: manim-physics tests/test_lensing.py::test_rays_lens
"""
渲染:
  python -m manim render examples/manim/physics_lens_rays_1.py GeneratedScene -qm --fps 30
"""
import numpy as np
from manim import *
from manim_physics import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = "#f8fafc"
        cap = Text("薄透镜与平行光入射（几何光学示意）", font_size=30, color="#0f172a").to_edge(UP, buff=0.35)
        self.play(FadeIn(cap), run_time=0.7)

        lens_style = {"fill_opacity": 0.45, "color": BLUE_D}
        a = Lens(-100, 1, **lens_style).shift(LEFT)
        a2 = Lens(100, 1, **lens_style).shift(RIGHT)
        rays = [
            Ray(LEFT * 5 + UP * i, RIGHT, 8, [a, a2], color=RED_C)
            for i in np.linspace(-2, 2, 10)
        ]
        self.add(a, a2, *rays)
        self.wait(4)
