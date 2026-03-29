package com.mathanim.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManimProcessResultTest {

  @Test
  void okCarriesStdout() {
    ManimProcessResult r = ManimProcessResult.ok("v1.0\n", "");
    assertTrue(r.success());
    assertEquals(0, r.exitCode());
    assertEquals("v1.0\n", r.stdout());
  }

  @Test
  void failCarriesMessage() {
    ManimProcessResult r = ManimProcessResult.fail(2, "out", "err", "boom");
    assertFalse(r.success());
    assertEquals(2, r.exitCode());
    assertEquals("boom", r.message());
  }
}
