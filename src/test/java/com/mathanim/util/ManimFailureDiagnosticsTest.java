package com.mathanim.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ManimFailureDiagnosticsTest {

  @Test
  void classifiesLatexFailures() {
    var d = ManimFailureDiagnostics.classify("exit=1", "tex_to_svg_file failed with latex");
    assertEquals(ManimFailureDiagnostics.FailureKind.LATEX_ENV, d.kind());
  }

  @Test
  void classifiesAxisFailures() {
    var d = ManimFailureDiagnostics.classify("exit=1", "add_numbers get_number_mobject exploded");
    assertEquals(ManimFailureDiagnostics.FailureKind.AXIS_LABELS, d.kind());
  }

  @Test
  void genericFailureStillHasRepairHint() {
    var d = ManimFailureDiagnostics.classify("exit=1", "some unknown runtime problem");
    assertEquals(ManimFailureDiagnostics.FailureKind.GENERIC_RUNTIME, d.kind());
    assertTrue(d.repairHint().contains("简化"));
  }
}
