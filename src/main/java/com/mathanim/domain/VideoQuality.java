package com.mathanim.domain;

/** Manim 画质档位（CLI 质量标志）。 */
public enum VideoQuality {
  LOW("-ql"),
  MEDIUM("-qm"),
  HIGH("-qh");

  private final String flag;

  VideoQuality(String flag) {
    this.flag = flag;
  }

  public String getFlag() {
    return flag;
  }
}
