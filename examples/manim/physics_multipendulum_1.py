# example-bank id: physics-multipendulum-1
# 依赖: pip install manim-physics pymunk（刚体/摆需 pymunk）
# 参考: manim-physics tests/test_pendulum.py、MultiPendulum 文档示例
"""
渲染:
  python -m manim render examples/manim/physics_multipendulum_1.py GeneratedScene -qm --fps 30
"""
from manim_physics import *


class GeneratedScene(SpaceScene):
    def construct(self):
        self.camera.background_color = "#0f172a"
        title = Text("双摆（manim-physics）", font_size=36, color="#e2e8f0").to_edge(UP, buff=0.35)
        self.add(title)

        p = MultiPendulum(RIGHT, LEFT)
        self.add(p)
        self.make_rigid_body(*p.bobs)
        p.start_swinging()
        trace = TracedPath(p.bobs[-1].get_center, stroke_color=BLUE_C, stroke_width=2.5)
        self.add(trace)
        self.wait(14)
