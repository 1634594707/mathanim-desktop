# example-bank id: physics-magnetic-field-1
# 依赖: pip install manim-physics
# 参考: manim-physics example.py、tests/test_electromagnetism.py::test_magnetic_field_multiple_wires
"""
渲染:
  python -m manim render examples/manim/physics_magnetic_field_1.py GeneratedScene -qm --fps 30
"""
from manim import *
from manim_physics import *


class GeneratedScene(ThreeDScene):
    def construct(self):
        self.camera.background_color = "#0c1222"
        wire1 = Wire(Circle(2).rotate(PI / 2, RIGHT).shift(UP * 2))
        wire2 = Wire(Circle(2).rotate(PI / 2, RIGHT).shift(UP * -2))
        mag_field = MagneticField(wire1, wire2)
        self.set_camera_orientation(PI / 3, PI / 4)
        self.add(wire1, wire2, mag_field)
        self.wait(5)
