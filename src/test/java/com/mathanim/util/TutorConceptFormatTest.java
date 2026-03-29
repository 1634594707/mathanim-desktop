package com.mathanim.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TutorConceptFormatTest {

  @Test
  void composeAndParseRoundtrip() {
    String c = TutorConceptFormat.compose("题", "解", "答");
    var p = TutorConceptFormat.tryParse(c);
    assertTrue(p.isPresent());
    assertEquals("题", p.get().problem());
    assertEquals("解", p.get().solution());
    assertEquals("答", p.get().answer());
  }

  @Test
  void composeWithoutAnswer() {
    String c = TutorConceptFormat.compose("题", "解", "");
    var p = TutorConceptFormat.tryParse(c);
    assertTrue(p.isPresent());
    assertEquals("", p.get().answer());
  }
}
