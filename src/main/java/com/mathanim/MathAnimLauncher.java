package com.mathanim;

import javafx.application.Application;

import java.nio.charset.Charset;

/**
 * JVM 入口：先启动 JavaFX，再在 {@link MathAnimFx#init()} 中拉起 Spring 容器。
 */
public final class MathAnimLauncher {

  public static void main(String[] args) {
    configureConsoleLoggingCharset();
    Application.launch(MathAnimFx.class, args);
  }

  private static void configureConsoleLoggingCharset() {
    String charsetName =
        firstNonBlank(
            System.getProperty("sun.stdout.encoding"),
            System.getProperty("native.encoding"),
            Charset.defaultCharset().name());
    if (charsetName == null || charsetName.isBlank()) {
      return;
    }
    try {
      if (Charset.isSupported(charsetName)) {
        System.setProperty("logging.charset.console", charsetName);
      }
    } catch (IllegalArgumentException ignored) {
      // Keep Spring Boot defaults if the host encoding name is invalid.
    }
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.strip();
      }
    }
    return null;
  }

  private MathAnimLauncher() {}
}
