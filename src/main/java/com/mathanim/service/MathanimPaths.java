package com.mathanim.service;

import com.mathanim.config.ManimProperties;
import com.mathanim.config.MathanimRootProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 解析任务工作目录、缓存目录等，与 {@code mathanim.data-dir}、{@code mathanim.manim.jobs-base-dir} 一致。
 */
@Component
public class MathanimPaths {

  private final MathanimRootProperties root;
  private final ManimProperties manim;

  public MathanimPaths(MathanimRootProperties root, ManimProperties manim) {
    this.root = root;
    this.manim = manim;
  }

  /**
   * AI 流水线与 Manim 渲染使用的任务目录（含 {@code generated_scene.py}）。
   */
  public Path resolveJobWorkDir(UUID jobId) {
    if (jobId == null) {
      throw new IllegalArgumentException("jobId");
    }
    String base = manim.getJobsBaseDir();
    if (base != null && !base.isBlank()) {
      return Path.of(base.strip()).resolve(jobId.toString()).toAbsolutePath().normalize();
    }
    return Path.of(root.getDataDir())
        .resolve("jobs")
        .resolve(jobId.toString())
        .toAbsolutePath()
        .normalize();
  }

  /** 内置 Manim 试渲染等使用的临时缓存根。 */
  public Path cacheRendersBaseDir() {
    return Path.of(root.getDataDir()).resolve("cache").resolve("renders").toAbsolutePath().normalize();
  }
}
