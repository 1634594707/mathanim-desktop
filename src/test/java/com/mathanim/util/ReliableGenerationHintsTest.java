package com.mathanim.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReliableGenerationHintsTest {

  @Test
  void disabledReturnsOriginal() {
    assertFalse(ReliableGenerationHints.appendIfEnabled("abc", false).contains("渲染稳定性约束"));
  }

  @Test
  void enabledAppendsBlock() {
    String s = ReliableGenerationHints.appendIfEnabled("task", true);
    assertTrue(s.contains("task"));
    assertTrue(s.contains("always_redraw"));
  }

  @Test
  void repairHintsIncludeDiagnosticContext() {
    String s =
        ReliableGenerationHints.appendRepairHints(
            "topic", "manim_render", "场景过重", "改成静态图", 4, 4, false);
    assertTrue(s.contains("场景过重"));
    assertTrue(s.contains("改成静态图"));
    assertTrue(s.contains("3~4 个镜头"));
  }

  @Test
  void fallbackModeAddsTemplateGuidance() {
    String s =
        ReliableGenerationHints.appendRepairHints(
            "topic", "manim_render", "场景过重", "改成静态图", 3, 4, true);
    assertTrue(s.contains("保底模板模式"));
    assertTrue(s.contains("<= 3 个镜头"));
    assertTrue(s.contains("极简教学版"));
  }

  @Test
  void repeatedSameErrorAddsChangeDirectionGuidance() {
    String s =
        ReliableGenerationHints.appendRepairHints(
            "topic", "manim_render", "场景过重", "改成静态图", 3, 4, false, true);
    assertTrue(s.contains("连续两轮出现同类错误"));
    assertTrue(s.contains("必须换方案"));
    assertTrue(s.contains("3D 改 2D"));
  }
}
