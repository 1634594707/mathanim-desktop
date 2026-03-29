package com.mathanim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mathanim.manim")
public class ManimProperties {

  /**
   * 启动 Manim 的前缀命令，例如 {@code manim} 或 {@code python}、{@code -m}、{@code manim} 拆成多段。
   */
  private List<String> commandPrefix = new ArrayList<>(List.of("manim"));

  /**
   * 已废弃用途：流水线画质由任务 {@link com.mathanim.domain.VideoQuality}（-ql/-qm/-qh）决定。仅保留兼容 YAML
   * 键；勿指望此项改变一键生成画质。
   */
  private String qualityFlag = "-ql";

  /**
   * 传给 Manim 的 {@code --fps}。ManimCat 默认 15 易显卡顿；此处默认 30 更顺滑。设为 0 则不传参（使用 Manim
   * 预设）。
   */
  private int frameRate = 30;

  /**
   * Manim {@code --renderer}：{@code cairo}（默认，CPU 光栅）或 {@code opengl}（GPU 绘制路径，需本机 OpenGL
   * 与驱动正常）。留空则不传参，与 Manim 默认一致（cairo）。
   */
  private String renderer = "";

  /** 单个子进程最长等待时间（秒）。 */
  private int timeoutSeconds = 600;

  /**
   * 为 true 时使用 {@code manim render ...}；为 false 时使用与旧版一致的前置参数风格（默认，兼容性更好）。
   */
  private boolean useRenderSubcommand = false;

  /**
   * 渲染成功后，将最终 mp4/png 复制到此目录（扁平命名 {@code {jobId}.mp4}），便于对照验证。留空则不改
   * 路径，仍指向任务目录下 Manim 生成的文件。
   */
  private String exportMediaDir = "";

  /**
   * 任务工作目录根：每个任务为 {@code <jobs-base-dir>/<jobId>/}，内含 {@code generated_scene.py}、{@code
   * media/} 等。留空则使用 {@code <mathanim.data-dir>/jobs}（见 {@link com.mathanim.service.MathanimPaths}）。
   */
  private String jobsBaseDir = "";

  public List<String> getCommandPrefix() {
    return commandPrefix;
  }

  public void setCommandPrefix(List<String> commandPrefix) {
    this.commandPrefix = commandPrefix;
  }

  public String getQualityFlag() {
    return qualityFlag;
  }

  public void setQualityFlag(String qualityFlag) {
    this.qualityFlag = qualityFlag;
  }

  public int getFrameRate() {
    return frameRate;
  }

  public void setFrameRate(int frameRate) {
    this.frameRate = frameRate;
  }

  public String getRenderer() {
    return renderer;
  }

  public void setRenderer(String renderer) {
    this.renderer = renderer != null ? renderer : "";
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }

  public boolean isUseRenderSubcommand() {
    return useRenderSubcommand;
  }

  public void setUseRenderSubcommand(boolean useRenderSubcommand) {
    this.useRenderSubcommand = useRenderSubcommand;
  }

  public String getExportMediaDir() {
    return exportMediaDir;
  }

  public void setExportMediaDir(String exportMediaDir) {
    this.exportMediaDir = exportMediaDir != null ? exportMediaDir : "";
  }

  public String getJobsBaseDir() {
    return jobsBaseDir;
  }

  public void setJobsBaseDir(String jobsBaseDir) {
    this.jobsBaseDir = jobsBaseDir != null ? jobsBaseDir : "";
  }
}
