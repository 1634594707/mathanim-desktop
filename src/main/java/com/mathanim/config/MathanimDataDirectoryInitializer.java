package com.mathanim.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 在数据源等 Bean 初始化前创建 {@code mathanim.data-dir}，避免 SQLite 因父目录不存在而失败。
 */
public class MathanimDataDirectoryInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    ConfigurableEnvironment env = applicationContext.getEnvironment();
    String raw = env.getProperty("mathanim.data-dir");
    String dataDir =
        (raw == null || raw.isBlank())
            ? Path.of(System.getProperty("user.home"), ".mathanim").toString()
            : raw.strip();
    try {
      Files.createDirectories(Path.of(dataDir));
    } catch (IOException e) {
      throw new IllegalStateException("无法创建数据目录: " + dataDir, e);
    }
  }
}
