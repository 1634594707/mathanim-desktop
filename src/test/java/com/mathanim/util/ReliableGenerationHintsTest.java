package com.mathanim.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReliableGenerationHintsTest {

  @Test
  void disabledReturnsOriginal() {
    assertFalse(ReliableGenerationHints.appendIfEnabled("abc", false).contains("稳定"));
  }

  @Test
  void enabledAppendsBlock() {
    String s = ReliableGenerationHints.appendIfEnabled("task", true);
    assertTrue(s.contains("task"));
    assertTrue(s.contains("MathTex"));
  }
}
