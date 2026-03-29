package com.mathanim.service;

public record ManimProcessResult(boolean success, int exitCode, String stdout, String stderr, String message) {

  public static ManimProcessResult ok(String stdout, String stderr) {
    return new ManimProcessResult(true, 0, stdout, stderr, "ok");
  }

  public static ManimProcessResult fail(int exitCode, String stdout, String stderr, String message) {
    return new ManimProcessResult(false, exitCode, stdout, stderr, message);
  }
}
