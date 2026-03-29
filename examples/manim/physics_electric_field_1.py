# example-bank id: physics-electric-field-1
# 依赖: pip install manim-physics
# 参考: manim-physics tests/test_electromagnetism.py::test_electric_field
"""
渲染:
  python -m manim render examples/manim/physics_electric_field_1.py GeneratedScene -qm --fps 30
"""
from manim import *
from manim_physics import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE
        hint = Text("点电荷电场叠加（矢量场示意）", font_size=32, color="#1e293b").to_edge(UP, buff=0.35)
        self.play(FadeIn(hint), run_time=0.8)

        charge1 = Charge(-1, LEFT + DOWN)
        charge2 = Charge(2, RIGHT + DOWN)
        charge3 = Charge(-1, UP)
        field = ElectricField(charge1, charge2, charge3)
        self.add(charge1, charge2, charge3)
        self.add(field)
        self.wait(4)
