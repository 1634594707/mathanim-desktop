package com.mathanim.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ErrorFingerprintTest {

  @Test
  void ignoresLineNumbersAndPaths() {
    String a =
        "Traceback\n  File \"D:/tmp/job/generated_scene.py\", line 87, in construct\nValueError: bad axis";
    String b =
        "Traceback\n  File \"C:/Users/Administrator/work/generated_scene.py\", line 142, in construct\nValueError: bad axis";

    assertEquals(
        ErrorFingerprint.fingerprint("py_compile", a),
        ErrorFingerprint.fingerprint("py_compile", b));
  }

  @Test
  void differentStagesProduceDifferentFingerprints() {
    String detail = "ValueError: bad axis labels";

    assertNotEquals(
        ErrorFingerprint.fingerprint("py_compile", detail),
        ErrorFingerprint.fingerprint("manim_render", detail));
  }
}
