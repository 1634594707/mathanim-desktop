package com.mathanim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * 应用数据根目录（SQLite、默认任务目录、缓存等），对应配置键 {@code mathanim.data-dir}。
 */
@ConfigurationProperties(prefix = "mathanim")
public class MathanimRootProperties {

  /**
   * 数据根目录绝对路径。留空则运行时回退为 {@code ${user.home}/.mathanim}。
   */
  private String dataDir = "";

  public String getDataDir() {
    if (dataDir == null || dataDir.isBlank()) {
      return Path.of(System.getProperty("user.home"), ".mathanim").toString();
    }
    return dataDir.strip();
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir != null ? dataDir : "";
  }
}
