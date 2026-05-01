package com.mathanim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.domain.JobKind;
import com.mathanim.domain.JobStatus;
import com.mathanim.domain.ProcessingStage;
import com.mathanim.domain.PromptLocale;
import com.mathanim.domain.ProblemFramingPlan;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.domain.RenderJob;
import com.mathanim.prompt.PromptOverridesDto;
import com.mathanim.prompt.PromptOverridesParser;
import com.mathanim.config.ManimProperties;
import com.mathanim.repo.RenderJobRepository;
import com.mathanim.util.ProblemPlanMerge;
import com.mathanim.util.ErrorFingerprint;
import com.mathanim.util.ManimFailureDiagnostics;
import com.mathanim.util.ReferenceImagesParser;
import com.mathanim.util.ReliableGenerationHints;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * ManimCat Workflow：可选问题拆解 → 单阶段或两阶段 AI → py_compile → manim；失败则修补重试。
 */
@Service
public class WorkflowPipelineService {

  private final AiEffectiveService aiEffectiveService;
  private final ManimAiCodeService manimAiCodeService;
  private final PythonStaticCheckService pythonStaticCheckService;
  private final ManimRenderService manimRenderService;
  private final RenderJobRepository renderJobRepository;
  private final ProblemFramingService problemFramingService;
  private final ObjectMapper objectMapper;
  private final ManimProperties manimProperties;
  private final MathanimPaths mathanimPaths;

  public WorkflowPipelineService(
      AiEffectiveService aiEffectiveService,
      ManimAiCodeService manimAiCodeService,
      PythonStaticCheckService pythonStaticCheckService,
      ManimRenderService manimRenderService,
      RenderJobRepository renderJobRepository,
      ProblemFramingService problemFramingService,
      ObjectMapper objectMapper,
      ManimProperties manimProperties,
      MathanimPaths mathanimPaths) {
    this.aiEffectiveService = aiEffectiveService;
    this.manimAiCodeService = manimAiCodeService;
    this.pythonStaticCheckService = pythonStaticCheckService;
    this.manimRenderService = manimRenderService;
    this.renderJobRepository = renderJobRepository;
    this.problemFramingService = problemFramingService;
    this.objectMapper = objectMapper;
    this.manimProperties = manimProperties;
    this.mathanimPaths = mathanimPaths;
  }

  public void runAiThenRender(UUID jobId, Consumer<String> log) {
    runAiThenRender(jobId, log, null, null);
  }

  public void runAiThenRender(
      UUID jobId,
      Consumer<String> log,
      Consumer<Integer> progressCallback,
      BooleanSupplier onTimeoutContinue) {
    RenderJob job =
        renderJobRepository
            .findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + jobId));

    if (isUserCancelled(job)) {
      log.accept("任务已取消，跳过流水线。");
      return;
    }

    if (!aiEffectiveService.isConfigured()) {
      fail(job, "未配置 API 密钥（application.yml 或「API 设置」页）。");
      log.accept("失败：未配置 AI。请在 yml 或界面中填写密钥。");
      return;
    }

    if (job.getJobKind() == JobKind.MODIFY) {
      if (job.getSourceCode() == null || job.getSourceCode().isBlank()) {
        fail(job, "修改任务缺少脚本内容（sourceCode）。");
        log.accept(job.getErrorMessage());
        return;
      }
      if (job.getEditInstructions() == null || job.getEditInstructions().isBlank()) {
        fail(job, "修改任务缺少编辑说明（editInstructions）。");
        log.accept(job.getErrorMessage());
        return;
      }
    }

    int maxPasses = aiEffectiveService.getMaxRetryPasses();
    PromptOverridesDto promptOverrides =
        PromptOverridesParser.parse(job.getPromptOverridesJson(), objectMapper);

    job.setStatus(JobStatus.PROCESSING);
    job.setErrorMessage(null);
    job.setFailureSummary(null);
    job.setFailureRepairHint(null);
    job.setFallbackModeActive(false);
    job.setOutputMediaPath(null);
    renderJobRepository.save(job);

    Path workDir = mathanimPaths.resolveJobWorkDir(jobId);
    try {
      Files.createDirectories(workDir);
    } catch (IOException e) {
      fail(job, "创建工作目录失败: " + e.getMessage());
      log.accept(job.getErrorMessage());
      return;
    }

    List<ReferenceImageItem> refs =
        ReferenceImagesParser.parseJson(job.getReferenceImagesJson(), objectMapper);
    PromptLocale loc = PromptLocale.fromHyphenated(job.getPromptLocale());

    String mergedConcept = job.getConcept();
    if (job.getJobKind() == JobKind.GENERATE) {
      if (job.isUseProblemFraming()
          && (job.getProblemPlanJson() == null || job.getProblemPlanJson().isBlank())) {
        if (isCancelled(jobId)) {
          log.accept("任务已取消，停止流水线。");
          return;
        }
        job = renderJobRepository.findById(jobId).orElseThrow();
        job.setProcessingStage(ProcessingStage.PROBLEM_FRAMING);
        renderJobRepository.save(job);
        log.accept("[阶段] 问题拆解（Problem Framing）…");
        try {
          ProblemFramingPlan plan =
              problemFramingService.generatePlan(job.getConcept(), refs, loc, promptOverrides);
          job.setProblemPlanJson(objectMapper.writeValueAsString(plan));
          renderJobRepository.save(job);
          log.accept("规划: " + plan.getHeadline());
        } catch (Exception e) {
          fail(job, "问题拆解失败: " + e.getMessage());
          log.accept(job.getErrorMessage());
          return;
        }
      }
      ProblemFramingPlan attached = parseProblemPlan(job.getProblemPlanJson());
      mergedConcept = ProblemPlanMerge.mergeIntoConcept(job.getConcept(), attached);
    }

    String aiConcept =
        ReliableGenerationHints.appendIfEnabled(mergedConcept, job.isReliableGeneration());

    Path script = workDir.resolve(ManimRenderService.GENERATED_SCENE_FILE);
    String code = null;
    String lastFailureStage = "";
    String lastFailureDetail = "";
    String lastFailureSummary = "";
    String lastFailureRepairHint = "";
    String previousFailureFingerprint = null;
    String firstFailureFingerprint = null;
    boolean repeatedSameError = false;
    int renderFailureCount = 0;

    for (int pass = 1; pass <= maxPasses; pass++) {
      if (isCancelled(jobId)) {
        log.accept("任务已取消，停止流水线。");
        return;
      }

      log.accept("[轮次 " + pass + "/" + maxPasses + "]");

      job = renderJobRepository.findById(jobId).orElseThrow();
      job.setProcessingStage(ProcessingStage.CODE_GENERATING);
      job.setFallbackModeActive(job.isForceFallbackMode() || renderFailureCount >= 2);
      renderJobRepository.save(job);
      log.accept(
          "[阶段] AI "
              + (pass == 1
                  ? (job.getJobKind() == JobKind.MODIFY
                      ? "按说明修改"
                      : (job.isUseTwoStageAi()
                          ? "多步生成（①解题与可视 → ②分镜 → ③代码）"
                          : "生成代码"))
                  : "修补")
              + "…");

      try {
        if (pass == 1) {
          if (job.getJobKind() == JobKind.MODIFY) {
            code =
                manimAiCodeService.editSceneCode(
                    aiConcept, job.getSourceCode(), job.getEditInstructions());
          } else {
            code = manimAiCodeService.generateInitialCode(job, aiConcept, log);
          }
        } else {
          boolean fallbackMode = job.isForceFallbackMode() || (job.isAllowFallbackMode() && renderFailureCount >= 2);
          if (fallbackMode) {
            log.accept("[策略] 已进入保底模板模式：后续修补将强制输出极简教学版。");
          }
          String repairConcept =
              ReliableGenerationHints.appendRepairHints(
                  aiConcept,
                  lastFailureStage,
                  lastFailureSummary,
                  lastFailureRepairHint,
                  pass,
                  maxPasses,
                  fallbackMode,
                  repeatedSameError);
          code =
              manimAiCodeService.repairSceneCode(
                  repairConcept, code, lastFailureStage, lastFailureDetail, promptOverrides);
        }
      } catch (RuntimeException e) {
        fail(job, "AI 失败: " + e.getMessage());
        log.accept(job.getErrorMessage());
        return;
      }

      try {
        Files.writeString(script, code, StandardCharsets.UTF_8);
      } catch (IOException e) {
        fail(job, "写入脚本失败: " + e.getMessage());
        log.accept(job.getErrorMessage());
        return;
      }

      job = renderJobRepository.findById(jobId).orElseThrow();
      job.setScriptPath(script.toAbsolutePath().toString());
      job.setProcessingStage(ProcessingStage.STATIC_CHECKING);
      renderJobRepository.save(job);
      log.accept("[阶段] 静态检查 (py_compile)…");

      if (isCancelled(jobId)) {
        log.accept("任务已取消，停止流水线。");
        return;
      }

      ManimProcessResult compile =
          pythonStaticCheckService.pyCompile(workDir, ManimRenderService.GENERATED_SCENE_FILE);
      if (!compile.success()) {
        lastFailureStage = "py_compile";
        lastFailureDetail = compile.message() + "\n" + compile.stdout();
        lastFailureSummary = "静态检查未通过";
        lastFailureRepairHint = "先修正语法、导入和类定义，再考虑动画细节。";
        String currentFailureFingerprint =
            ErrorFingerprint.fingerprint(lastFailureStage, lastFailureDetail);
        if (firstFailureFingerprint == null) {
          firstFailureFingerprint = currentFailureFingerprint;
        }
        repeatedSameError = currentFailureFingerprint.equals(previousFailureFingerprint);
        job = renderJobRepository.findById(jobId).orElseThrow();
        job.setFailureSummary(lastFailureSummary);
        job.setFailureRepairHint(lastFailureRepairHint);
        renderJobRepository.save(job);
        log.accept("错误指纹: " + currentFailureFingerprint);
        if (repeatedSameError) {
          failRepeatedFailure(
              job,
              log,
              "静态检查连续两轮相同错误，AI 无法自动修复此错误。",
              lastFailureDetail,
              currentFailureFingerprint);
          return;
        }
        previousFailureFingerprint = currentFailureFingerprint;
        if (pass >= maxPasses) {
          job = renderJobRepository.findById(jobId).orElseThrow();
          boolean sameAsFirst = currentFailureFingerprint.equals(firstFailureFingerprint);
          failAtMaxPasses(
              job,
              "静态检查失败（已达最大轮次）: " + lastFailureDetail,
              sameAsFirst,
              currentFailureFingerprint);
          log.accept(job.getErrorMessage());
          return;
        }
        if (!compile.stdout().isBlank()) {
          log.accept(trim(compile.stdout(), 6000));
        } else {
          log.accept(compile.message());
        }
        log.accept("静态检查未通过，准备下一轮 AI 修补…");
        continue;
      }

      job = renderJobRepository.findById(jobId).orElseThrow();
      job.setProcessingStage(ProcessingStage.RENDERING);
      renderJobRepository.save(job);
      int fps = manimProperties.getFrameRate();
      String fpsNote = fps > 0 ? " · " + fps + "fps" : "";
      String ren = manimProperties.getRenderer();
      String renNote =
          ren != null && !ren.isBlank() ? " · renderer=" + ren.strip() : "";
      log.accept(
          "[阶段] Manim 渲染（"
              + job.getOutputMode()
              + " / "
              + job.getVideoQuality()
              + fpsNote
              + renNote
              + "）…");

      if (isCancelled(jobId)) {
        log.accept("任务已取消，停止流水线。");
        return;
      }

      ManimProcessResult render =
          manimRenderService.renderSceneInWorkDir(
              workDir,
              ManimRenderService.GENERATED_SCENE_FILE,
              ManimRenderService.GENERATED_SCENE_CLASS,
              jobId,
              job.getVideoQuality(),
              job.getOutputMode(),
              progressCallback,
              onTimeoutContinue);

      if (render.success()) {
        job = renderJobRepository.findById(jobId).orElseThrow();
        if (isCancelled(jobId)) {
          log.accept("任务已取消，停止流水线。");
          return;
        }
        job.setStatus(JobStatus.COMPLETED);
        Path rendered = Path.of(render.message());
        Path exported =
            manimRenderService.copyToExportDir(rendered, jobId, job.getOutputMode());
        String storedPath =
            exported != null ? exported.toAbsolutePath().toString() : rendered.toAbsolutePath().toString();
        job.setOutputMediaPath(storedPath);
        job.setErrorMessage(null);
        job.setFailureSummary(null);
        job.setFailureRepairHint(null);
        job.setFallbackModeActive(false);
        job.setProcessingStage(ProcessingStage.NONE);
        renderJobRepository.save(job);
        log.accept("完成: " + storedPath);
        if (exported != null) {
          log.accept(
              "（Manim 原始文件: "
                  + rendered.toAbsolutePath()
                  + "，已复制到验证目录便于查找。）");
        } else if (manimRenderService.isExportMediaDirConfigured()) {
          log.accept("（复制到验证目录失败，已保留 Manim 输出路径。）");
        }
        return;
      }

      lastFailureStage = "manim_render";
      lastFailureDetail = render.message() + "\n" + render.stdout();
      ManimFailureDiagnostics.Diagnostic diag =
          ManimFailureDiagnostics.classify(render.message(), render.stdout());
      renderFailureCount++;
      lastFailureSummary = diag.summary();
      lastFailureRepairHint = diag.repairHint();
      String currentFailureFingerprint =
          ErrorFingerprint.fingerprint(lastFailureStage, lastFailureDetail);
      if (firstFailureFingerprint == null) {
        firstFailureFingerprint = currentFailureFingerprint;
      }
      repeatedSameError = currentFailureFingerprint.equals(previousFailureFingerprint);
      job = renderJobRepository.findById(jobId).orElseThrow();
      job.setFailureSummary(lastFailureSummary);
      job.setFailureRepairHint(lastFailureRepairHint);
      renderJobRepository.save(job);
      log.accept("错误指纹: " + currentFailureFingerprint);
      log.accept("失败诊断: " + diag.summary());
      log.accept("修补方向: " + diag.repairHint());
      if (repeatedSameError) {
        failRepeatedFailure(
            job,
            log,
            "连续两轮出现同类渲染错误，AI 无法自动修复此错误。",
            lastFailureDetail,
            currentFailureFingerprint);
        return;
      }
      previousFailureFingerprint = currentFailureFingerprint;
      if (pass >= maxPasses) {
        job = renderJobRepository.findById(jobId).orElseThrow();
        boolean sameAsFirst = currentFailureFingerprint.equals(firstFailureFingerprint);
        failAtMaxPasses(
            job,
            diag.summary() + "\n" + lastFailureDetail,
            sameAsFirst,
            currentFailureFingerprint);
        log.accept("渲染失败（已达最大轮次）: " + render.message());
        if (!render.stdout().isBlank()) {
          log.accept(trim(render.stdout(), 6000));
        }
        return;
      }
      log.accept("渲染未通过，准备下一轮修补…");
    }
  }

  private ProblemFramingPlan parseProblemPlan(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, ProblemFramingPlan.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private boolean isCancelled(UUID jobId) {
    return renderJobRepository.findById(jobId).map(this::isUserCancelled).orElse(true);
  }

  private boolean isUserCancelled(RenderJob j) {
    return j.getStatus() == JobStatus.FAILED
        && RenderJob.USER_CANCELLED_MESSAGE.equals(j.getErrorMessage());
  }

  private void failRepeatedFailure(
      RenderJob job,
      Consumer<String> log,
      String summary,
      String detail,
      String fingerprint) {
    job = renderJobRepository.findById(job.getId()).orElseThrow();
    job.setStatus(JobStatus.FAILED);
    job.setFailureSummary("AI 无法自动修复此错误");
    job.setFailureRepairHint("连续两轮错误指纹相同，建议手动修改脚本或明显简化方案后再试。");
    job.setErrorMessage(trim(summary + "\n错误指纹: " + fingerprint + "\n" + detail, 4000));
    job.setProcessingStage(ProcessingStage.NONE);
    renderJobRepository.save(job);
    log.accept(summary);
  }

  private void failAtMaxPasses(
      RenderJob job, String detail, boolean sameAsFirst, String fingerprint) {
    job.setStatus(JobStatus.FAILED);
    job.setErrorMessage(trim(detail + "\n错误指纹: " + fingerprint, 4000));
    job.setProcessingStage(ProcessingStage.NONE);
    if (sameAsFirst) {
      job.setFailureSummary("首尾轮错误一致");
      job.setFailureRepairHint("首轮与末轮错误指纹相同，可能需要手动修改脚本或重写分镜结构。");
    }
    renderJobRepository.save(job);
  }

  private void fail(RenderJob job, String message) {
    job.setStatus(JobStatus.FAILED);
    job.setErrorMessage(trim(message, 4000));
    if (job.getFailureSummary() == null || job.getFailureSummary().isBlank()) {
      job.setFailureSummary("任务执行失败");
    }
    if (job.getFailureRepairHint() == null || job.getFailureRepairHint().isBlank()) {
      job.setFailureRepairHint("查看错误详情和运行日志，优先从环境、脚本复杂度或提示词约束继续排查。");
    }
    job.setProcessingStage(ProcessingStage.NONE);
    renderJobRepository.save(job);
  }

  private static String trim(String s, int max) {
    if (s == null) {
      return null;
    }
    return s.length() <= max ? s : s.substring(0, max) + "…";
  }
}
