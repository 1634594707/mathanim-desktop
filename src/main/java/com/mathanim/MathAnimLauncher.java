package com.mathanim;

import javafx.application.Application;

/**
 * JVM 入口：先启动 JavaFX，再在 {@link MathAnimFx#init()} 中拉起 Spring 容器。
 */
public final class MathAnimLauncher {

  public static void main(String[] args) {
    Application.launch(MathAnimFx.class, args);
  }

  private MathAnimLauncher() {}
}
