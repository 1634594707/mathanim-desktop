package com.mathanim.service;

import com.mathanim.config.ManimProperties;
import com.mathanim.domain.OutputMode;
import com.mathanim.domain.VideoQuality;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 通过子进程调用本机 Manim（不嵌入 Python）。用于环境检测与最小场景试渲染。
 */
@Service
public class ManimRenderService {

  /** AI 流水线写入的脚本名与场景类（与 prompts 一致）。 */
  public static final String GENERATED_SCENE_FILE = "generated_scene.py";
  public static final String GENERATED_SCENE_CLASS = "GeneratedScene";

  private static final String TEST_SCENE_FILE = "mathanim_builtin_test.py";
  private static final String TEST_SCENE_CLASS = "MathAnimBuiltinTestScene";

  private static final String TEST_SCENE_SOURCE = """
      from manim import *

      class MathAnimBuiltinTestScene(Scene):
          def construct(self):
              self.play(Write(Text("MathAnim")))
              self.wait(0.3)
      """;

  private final ManimProperties manimProperties;
  private final ManimProcessRegistry processRegistry;
  private final MathanimPaths mathanimPaths;

  public ManimRenderService(
      ManimProperties manimProperties,
      ManimProcessRegistry processRegistry,
      MathanimPaths mathanimPaths) {
    this.manimProperties = manimProperties;
    this.processRegistry = processRegistry;
    this.mathanimPaths = mathanimPaths;
  }

  /**
   * 若配置了 {@link ManimProperties#getExportMediaDir()}，将 Manim 产出的媒体复制到该目录，文件名为
   * {@code {jobId}.mp4} 或 {@code {jobId}.png}。未配置或复制失败返回 {@code null}。
   */
  public boolean isExportMediaDirConfigured() {
    String d = manimProperties.getExportMediaDir();
    return d != null && !d.isBlank();
  }

  public Path copyToExportDir(Path sourceMedia, UUID jobId, OutputMode outputMode) {
    if (sourceMedia == null || jobId == null) {
      return null;
    }
    String dir = manimProperties.getExportMediaDir();
    if (dir == null || dir.isBlank()) {
      return null;
    }
    try {
      Path destDir = Path.of(dir.trim());
      Files.createDirectories(destDir);
      String ext = outputMode == OutputMode.IMAGE ? ".png" : ".mp4";
      Path dest = destDir.resolve(jobId + ext);
      Files.copy(sourceMedia, dest, StandardCopyOption.REPLACE_EXISTING);
      return dest;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * 执行 {@code manim --version}（或 command-prefix --version）。
   */
  public ManimProcessResult checkVersion() {
    List<String> cmd = new ArrayList<>(manimProperties.getCommandPrefix());
    cmd.add("--version");
    return runProcess(cmd, Path.of(".").toAbsolutePath(), 60, null);
  }

  /**
   * 在临时目录写入最小场景并渲染；成功时 {@link ManimProcessResult#message()} 为 mp4 绝对路径。
   */
  public ManimProcessResult renderBuiltinTestScene() {
    return renderBuiltinTestScene(null);
  }

  /**
   * 内置测试渲染；传入 {@code jobId} 时可被 {@link ManimProcessRegistry#destroy(UUID)} 取消。
   */
  public ManimProcessResult renderBuiltinTestScene(UUID jobId) {
    Path workDir;
    try {
      Path base = mathanimPaths.cacheRendersBaseDir();
      Files.createDirectories(base);
      workDir = Files.createTempDirectory(base, "test-");
      Path script = workDir.resolve(TEST_SCENE_FILE);
      Files.writeString(script, TEST_SCENE_SOURCE, StandardCharsets.UTF_8);
    } catch (IOException e) {
      return ManimProcessResult.fail(-1, "", "", "无法创建工作目录: " + e.getMessage());
    }

    return runManimScene(
        workDir, TEST_SCENE_FILE, TEST_SCENE_CLASS, jobId, VideoQuality.LOW, OutputMode.VIDEO);
  }

  /**
   * 在已有工作目录中渲染指定场景（相对 workDir 的文件名）；默认低画质、视频。
   */
  public ManimProcessResult renderSceneInWorkDir(Path workDir, String sceneFileName, String sceneClassName) {
    return renderSceneInWorkDir(
        workDir, sceneFileName, sceneClassName, null, VideoQuality.LOW, OutputMode.VIDEO);
  }

  /**
   * 在已有工作目录中渲染；支持画质、输出模式与任务取消。
   */
  public ManimProcessResult renderSceneInWorkDir(
      Path workDir,
      String sceneFileName,
      String sceneClassName,
      UUID jobId,
      VideoQuality quality,
      OutputMode outputMode) {
    return runManimScene(workDir, sceneFileName, sceneClassName, jobId, quality, outputMode);
  }

  private ManimProcessResult runManimScene(
      Path workDir,
      String sceneFileName,
      String sceneClassName,
      UUID jobId,
      VideoQuality quality,
      OutputMode outputMode) {
    List<String> cmd = buildManimCommand(workDir, sceneFileName, sceneClassName, quality, outputMode);

    ManimProcessResult run = runProcess(cmd, workDir, manimProperties.getTimeoutSeconds(), jobId);
    if (!run.success()) {
      return run;
    }

    try {
      Path media =
          outputMode == OutputMode.IMAGE
              ? findLatestFile(workDir, ".png")
              : findOutputVideo(workDir, sceneFileName, sceneClassName, quality);
      if (media == null) {
        String hint = outputMode == OutputMode.IMAGE ? "png" : "mp4";
        return ManimProcessResult.fail(
            run.exitCode(),
            run.stdout(),
            run.stderr(),
            "渲染成功但未在输出目录找到 " + hint + "，请查看上方输出");
      }
      return new ManimProcessResult(true, 0, run.stdout(), run.stderr(), media.toAbsolutePath().toString());
    } catch (IOException e) {
      return ManimProcessResult.fail(run.exitCode(), run.stdout(), run.stderr(), e.getMessage());
    }
  }

  private List<String> buildManimCommand(
      Path workDir,
      String sceneFileName,
      String sceneClassName,
      VideoQuality quality,
      OutputMode outputMode) {
    List<String> cmd = new ArrayList<>(manimProperties.getCommandPrefix());
    if (manimProperties.isUseRenderSubcommand()) {
      cmd.add("render");
    }
    cmd.add(quality.getFlag());
    int fps = manimProperties.getFrameRate();
    if (fps > 0) {
      cmd.add("--fps");
      cmd.add(Integer.toString(fps));
    }
    Path mediaDir = workDir.resolve("media").toAbsolutePath().normalize();
    cmd.add("--media_dir");
    cmd.add(mediaDir.toString());

    String renderer = manimProperties.getRenderer();
    if (renderer != null && !renderer.isBlank()) {
      String r = renderer.strip().toLowerCase();
      cmd.add("--renderer");
      cmd.add(r);
      // OpenGL 默认可能不写成片或路径与 Cairo 不一致；显式要求写 mp4（Manim CLI --write_to_movie）
      if ("opengl".equals(r) && outputMode == OutputMode.VIDEO) {
        cmd.add("--write_to_movie");
        cmd.add("--format");
        cmd.add("mp4");
      }
    }
    if (outputMode == OutputMode.IMAGE) {
      cmd.add("--format");
      cmd.add("png");
      cmd.add("-s");
    }
    cmd.add(sceneFileName);
    cmd.add(sceneClassName);
    return cmd;
  }

  /**
   * 与 ManimCat {@code findVideoFile} 一致：先试约定路径 {@code
   * media/videos/<stem>/<480p|720p|1080p><fps>/<Scene>.mp4}，再在作业目录下找最新 {@code .mp4}（默认排除
   * partial 片段以免误判）。
   */
  private Path findOutputVideo(
      Path workDir, String sceneFileName, String sceneClassName, VideoQuality quality)
      throws IOException {
    int fps = manimProperties.getFrameRate();
    if (fps <= 0) {
      fps = 30;
    }
    String stem =
        sceneFileName.endsWith(".py")
            ? sceneFileName.substring(0, sceneFileName.length() - 3)
            : sceneFileName;
    String res =
        switch (quality) {
          case LOW -> "480p";
          case MEDIUM -> "720p";
          case HIGH -> "1080p";
        };
    Path expected =
        workDir
            .resolve("media")
            .resolve("videos")
            .resolve(stem)
            .resolve(res + fps)
            .resolve(sceneClassName + ".mp4")
            .normalize();
    if (Files.isRegularFile(expected)) {
      return expected;
    }

    Path noPartial = findLatestMovie(workDir, true);
    if (noPartial != null) {
      return noPartial;
    }
    return findLatestMovie(workDir, false);
  }

  private static Path findLatestMovie(Path root, boolean excludePartials) throws IOException {
    if (!Files.isDirectory(root)) {
      return null;
    }
    try (Stream<Path> walk = Files.walk(root)) {
      return walk
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().toLowerCase().endsWith(".mp4"))
          .filter(
              p ->
                  !excludePartials
                      || !pathNorm(p).contains("partial_movie_files"))
          .max(Comparator.comparingLong(ManimRenderService::lastModifiedOr0))
          .orElse(null);
    }
  }

  private static String pathNorm(Path p) {
    return p.toString().replace('\\', '/').toLowerCase();
  }

  private static long lastModifiedOr0(Path p) {
    try {
      return Files.getLastModifiedTime(p).toMillis();
    } catch (IOException e) {
      return 0L;
    }
  }

  private static Path findLatestFile(Path root, String suffixLower) throws IOException {
    try (Stream<Path> walk = Files.walk(root)) {
      String suf = suffixLower.toLowerCase();
      return walk
          .filter(p -> p.toString().toLowerCase().endsWith(suf))
          .max(
              Comparator.comparingLong(
                  p -> {
                    try {
                      return Files.getLastModifiedTime(p).toMillis();
                    } catch (IOException e) {
                      return 0L;
                    }
                  }))
          .orElse(null);
    }
  }

  private ManimProcessResult runProcess(
      List<String> command, Path workingDir, int timeoutSeconds, UUID jobId) {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(workingDir.toFile());
    pb.redirectErrorStream(true);
    try {
      Process p = pb.start();
      if (jobId != null) {
        processRegistry.register(jobId, p);
      }
      try {
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
          p.destroyForcibly();
          return ManimProcessResult.fail(-1, out, "", "进程超时（>" + timeoutSeconds + "s）");
        }
        int code = p.exitValue();
        if (code != 0) {
          return ManimProcessResult.fail(code, out, "", "exit=" + code);
        }
        return ManimProcessResult.ok(out, "");
      } finally {
        if (jobId != null) {
          processRegistry.unregister(jobId);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ManimProcessResult.fail(-1, "", "", "interrupted");
    } catch (IOException e) {
      return ManimProcessResult.fail(-1, "", "", e.getMessage() != null ? e.getMessage() : e.toString());
    }
  }
}
