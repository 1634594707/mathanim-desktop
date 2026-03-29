# example-bank id: physics-standing-waves-1
# 依赖: pip install manim-physics
# 参考: manim-physics tests/test_wave.py::test_standingwave、StandingWave 文档
"""
渲染:
  python -m manim render examples/manim/physics_standing_waves_1.py GeneratedScene -qm --fps 30
"""
from manim import *
from manim_physics import *


class GeneratedScene(Scene):
    def construct(self):
        self.camera.background_color = WHITE
        cap = Text("弦上驻波：n=1～4 模态", font_size=32, color="#1e293b").to_edge(UP, buff=0.35)
        self.play(FadeIn(cap), run_time=0.7)

        wave1 = StandingWave(1)
        wave2 = StandingWave(2)
        wave3 = StandingWave(3)
        wave4 = StandingWave(4)
        waves = VGroup(wave1, wave2, wave3, wave4)
        waves.arrange(DOWN, buff=0.55).move_to(ORIGIN)
        labels = VGroup(
            Text("n=1", font_size=22, color="#334155"),
            Text("n=2", font_size=22, color="#334155"),
            Text("n=3", font_size=22, color="#334155"),
            Text("n=4", font_size=22, color="#334155"),
        )
        for w, lab in zip(waves, labels):
            lab.next_to(w, LEFT, buff=0.35)
        self.add(waves, labels)
        for wave in waves:
            wave.start_wave()
        self.wait(8)
