package com.mathanim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.domain.JobKind;
import com.mathanim.domain.JobStatus;
import com.mathanim.domain.OutputMode;
import com.mathanim.domain.ProcessingStage;
import com.mathanim.domain.PromptLocale;
import com.mathanim.domain.ProblemFramingPlan;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.domain.RenderJob;
import com.mathanim.domain.VideoQuality;
import com.mathanim.ai.OpenAiChatClient;
import com.mathanim.prompt.PromptOverridesDto;
import com.mathanim.prompt.PromptOverridesParser;
import com.mathanim.repo.RenderJobRepository;
import com.mathanim.util.ReferenceImagesParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Service
public class RenderJobService {

  private final RenderJobRepository renderJobRepository;
  private final ManimRenderService manimRenderService;
  private final WorkflowPipelineService workflowPipelineService;
  private final ManimProcessRegistry manimProcessRegistry;
  private final ExecutorService manimExecutorService;
  private final ProblemFramingService problemFramingService;
  private final ObjectMapper objectMapper;
  private final OpenAiChatClient openAiChatClient;

  public RenderJobService(
      RenderJobRepository renderJobRepository,
      ManimRenderService manimRenderService,
      WorkflowPipelineService workflowPipelineService,
      ManimProcessRegistry manimProcessRegistry,
      ExecutorService manimExecutorService,
      ProblemFramingService problemFramingService,
      ObjectMapper objectMapper,
      OpenAiChatClient openAiChatClient) {
    this.renderJobRepository = renderJobRepository;
    this.manimRenderService = manimRenderService;
    this.workflowPipelineService = workflowPipelineService;
    this.manimProcessRegistry = manimProcessRegistry;
    this.manimExecutorService = manimExecutorService;
    this.problemFramingService = problemFramingService;
    this.objectMapper = objectMapper;
    this.openAiChatClient = openAiChatClient;
  }

  @Transactional(readOnly = true)
  public List<RenderJob> listRecent() {
    return renderJobRepository.findAllByOrderByCreatedAtDesc();
  }

  /**
   * 进程结束后内存中的流水线已不存在，但数据库里可能仍为 {@link JobStatus#PROCESSING}（崩溃、强杀、断电等）。
   * 启动时将这些任务标记为失败，避免界面长期显示「渲染中」。
   *
   * @return 被收尾的任务条数
   */
  @Transactional
  public int recoverStaleProcessingJobs() {
    List<RenderJob> stuck = renderJobRepository.findAllByStatus(JobStatus.PROCESSING);
    int n = 0;
    for (RenderJob job : stuck) {
      job.setStatus(JobStatus.FAILED);
      job.setProcessingStage(ProcessingStage.NONE);
      job.setErrorMessage(RenderJob.INTERRUPTED_MESSAGE);
      job.setFailureSummary("任务在上次运行中被中断");
      job.setFailureRepairHint("重新执行该任务，必要时先检查环境、Manim 和 Python 配置。");
      job.setFallbackModeActive(false);
      renderJobRepository.save(job);
      manimProcessRegistry.destroy(job.getId());
      n++;
    }
    return n;
  }

  @Transactional
  public RenderJob enqueue(String concept) {
    return enqueue(
        concept,
        OutputMode.VIDEO,
        VideoQuality.MEDIUM,
        false,
        true,
        null,
        null,
        "zh-CN",
        null,
        true,
        true);
  }

  @Transactional
  public RenderJob enqueue(String concept, OutputMode outputMode, VideoQuality videoQuality) {
    return enqueue(
        concept, outputMode, videoQuality, false, true, null, null, "zh-CN", null, true, true);
  }

  @Transactional
  public RenderJob enqueue(
      String concept,
      OutputMode outputMode,
      VideoQuality videoQuality,
      boolean useProblemFraming,
      boolean useTwoStageAi,
      String problemPlanJson,
      String referenceImagesJson,
      String promptLocale,
      String promptOverridesJson,
      boolean reliableGeneration,
      boolean allowFallbackMode) {
    RenderJob job = new RenderJob();
    job.setConcept(concept);
    job.setOutputMode(outputMode);
    job.setVideoQuality(videoQuality);
    job.setJobKind(JobKind.GENERATE);
    job.setStatus(JobStatus.QUEUED);
    job.setUseProblemFraming(useProblemFraming);
    job.setUseTwoStageAi(useTwoStageAi);
    job.setReliableGeneration(reliableGeneration);
    job.setAllowFallbackMode(allowFallbackMode);
    job.setProblemPlanJson(problemPlanJson);
    job.setReferenceImagesJson(referenceImagesJson);
    job.setPromptLocale(promptLocale != null ? promptLocale : "zh-CN");
    job.setPromptOverridesJson(promptOverridesJson);
    return renderJobRepository.save(job);
  }

  /** 与 ManimCat modify 对齐：基于现有脚本与编辑说明生成新任务并入队。 */
  @Transactional
  public RenderJob enqueueModify(
      String concept,
      String sourceCode,
      String editInstructions,
      OutputMode outputMode,
      VideoQuality videoQuality,
      boolean reliableGeneration,
      boolean allowFallbackMode) {
    RenderJob job = new RenderJob();
    job.setConcept(concept);
    job.setSourceCode(sourceCode);
    job.setEditInstructions(editInstructions);
    job.setOutputMode(outputMode);
    job.setVideoQuality(videoQuality);
    job.setJobKind(JobKind.MODIFY);
    job.setStatus(JobStatus.QUEUED);
    job.setReliableGeneration(reliableGeneration);
    job.setAllowFallbackMode(allowFallbackMode);
    return renderJobRepository.save(job);
  }

  @Transactional
  public void cancelJob(UUID jobId) {
    RenderJob job =
        renderJobRepository
            .findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + jobId));
    if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.PROCESSING) {
      return;
    }
    job.setStatus(JobStatus.FAILED);
    job.setProcessingStage(ProcessingStage.NONE);
    job.setErrorMessage(RenderJob.USER_CANCELLED_MESSAGE);
    job.setFailureSummary("用户已取消任务");
    job.setFailureRepairHint("如需继续，可重新执行该任务。");
    job.setFallbackModeActive(false);
    renderJobRepository.save(job);
    manimProcessRegistry.destroy(jobId);
  }

  /**
   * 后台检测 {@code manim --version}，日志通过 {@code log} 输出（调用方负责切回 JavaFX 线程）。
   */
  public void checkManimVersionAsync(Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            log.accept("正在执行 Manim 版本检测…");
            ManimProcessResult r = manimRenderService.checkVersion();
            if (r.success()) {
              log.accept(r.stdout().strip());
            } else {
              log.accept("检测失败: " + r.message());
              if (!r.stdout().isBlank()) {
                log.accept(r.stdout());
              }
            }
          } catch (RuntimeException e) {
            log.accept("异常: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  /**
   * 对指定任务执行内置测试场景（子进程 Manim），结果写回数据库。
   */
  public void runBuiltinTestAsync(UUID jobId, Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            runBuiltinTestSync(jobId, log);
          } catch (RuntimeException e) {
            log.accept("异常: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  /**
   * ManimCat Workflow 对齐：OpenAI 兼容接口生成/修改脚本 → py_compile → manim（需配置 API 密钥）。
   */
  public void runAiWorkflowAsync(UUID jobId, Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            workflowPipelineService.runAiThenRender(jobId, log);
          } catch (RuntimeException e) {
            log.accept("异常: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  /**
   * 测试 OpenAI 兼容 Chat Completions（如 DeepSeek）；使用当前传入的 URL/Key/Model，可与界面框内未保存的草稿一致。
   */
  public void testAiConnectionAsync(
      String baseUrl, String apiKey, String model, Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            String reply = openAiChatClient.probeChatCompletion(baseUrl, apiKey, model);
            log.accept("AI 连接正常，模型应答: " + reply.strip());
          } catch (RuntimeException e) {
            log.accept("AI 连接失败: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  /**
   * 仅生成问题拆解 JSON（预览），不入队；结果通过 {@code onPlanJson} 回传。
   */
  public void runProblemFramingPreviewAsync(
      String concept,
      String referenceUrlLines,
      String promptLocaleHyphen,
      String promptOverridesJson,
      Consumer<String> log,
      Consumer<String> onPlanJson,
      Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            List<ReferenceImageItem> refs = ReferenceImagesParser.parseLines(referenceUrlLines);
            PromptLocale loc = PromptLocale.fromHyphenated(promptLocaleHyphen);
            PromptOverridesDto ov =
                PromptOverridesParser.parse(promptOverridesJson, objectMapper);
            ProblemFramingPlan plan =
                problemFramingService.generatePlan(concept, refs, loc, ov);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
            onPlanJson.accept(json);
            log.accept("问题拆解完成，已填入预览区。");
          } catch (Exception e) {
            log.accept("问题拆解失败: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  public void cancelJobAsync(UUID jobId, Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            cancelJob(jobId);
            log.accept("已请求取消任务: " + jobId);
          } catch (RuntimeException e) {
            log.accept("取消失败: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  /**
   * 从数据库移除任务；若仍在队列或执行中会先尝试终止关联 Manim 子进程。已导出到磁盘的 mp4/png 不会删除。
   */
  @Transactional
  public void deleteJob(UUID jobId) {
    RenderJob job =
        renderJobRepository
            .findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + jobId));
    if (job.getStatus() == JobStatus.QUEUED || job.getStatus() == JobStatus.PROCESSING) {
      manimProcessRegistry.destroy(jobId);
    }
    renderJobRepository.deleteById(jobId);
  }

  public void deleteJobAsync(UUID jobId, Consumer<String> log, Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            deleteJob(jobId);
            log.accept("已删除任务: " + jobId);
          } catch (RuntimeException e) {
            log.accept("删除失败: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  @Transactional
  public RenderJob retryJob(UUID sourceJobId, boolean forceFallbackMode) {
    RenderJob source =
        renderJobRepository
            .findById(sourceJobId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + sourceJobId));

    RenderJob job = new RenderJob();
    job.setConcept(source.getConcept());
    job.setOutputMode(source.getOutputMode());
    job.setVideoQuality(source.getVideoQuality());
    job.setJobKind(source.getJobKind());
    job.setUseProblemFraming(source.isUseProblemFraming());
    job.setUseTwoStageAi(source.isUseTwoStageAi());
    job.setReliableGeneration(source.isReliableGeneration());
    job.setAllowFallbackMode(forceFallbackMode || source.isAllowFallbackMode());
    job.setForceFallbackMode(forceFallbackMode);
    job.setProblemPlanJson(source.getProblemPlanJson());
    job.setReferenceImagesJson(source.getReferenceImagesJson());
    job.setPromptLocale(source.getPromptLocale());
    job.setPromptOverridesJson(source.getPromptOverridesJson());
    job.setSourceCode(source.getSourceCode());
    job.setEditInstructions(source.getEditInstructions());
    job.setStatus(JobStatus.QUEUED);
    job.setProcessingStage(ProcessingStage.NONE);
    job.setErrorMessage(null);
    job.setFailureSummary(null);
    job.setFailureRepairHint(null);
    job.setFallbackModeActive(forceFallbackMode);
    job.setOutputMediaPath(null);
    job.setScriptPath(null);
    return renderJobRepository.save(job);
  }

  public void retryJobAsync(
      UUID sourceJobId,
      boolean forceFallbackMode,
      Consumer<String> log,
      Consumer<RenderJob> onCreated,
      Runnable onComplete) {
    manimExecutorService.execute(
        () -> {
          try {
            RenderJob retried = retryJob(sourceJobId, forceFallbackMode);
            log.accept(
                (forceFallbackMode ? "已创建保底重试任务: " : "已创建重试任务: ") + retried.getId());
            onCreated.accept(retried);
          } catch (RuntimeException e) {
            log.accept("重试创建失败: " + e.getMessage());
          } finally {
            onComplete.run();
          }
        });
  }

  private void runBuiltinTestSync(UUID jobId, Consumer<String> log) {
    RenderJob job =
        renderJobRepository
            .findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + jobId));

    job.setStatus(JobStatus.PROCESSING);
    job.setProcessingStage(ProcessingStage.RENDERING);
    job.setErrorMessage(null);
    job.setFailureSummary(null);
    job.setFailureRepairHint(null);
    job.setFallbackModeActive(false);
    job.setOutputMediaPath(null);
    renderJobRepository.save(job);
    log.accept("任务 " + jobId + " 已进入 PROCESSING，开始渲染内置场景…");

    ManimProcessResult r = manimRenderService.renderBuiltinTestScene(jobId);
    job = renderJobRepository.findById(jobId).orElseThrow();

    if (r.success()) {
      job.setStatus(JobStatus.COMPLETED);
      job.setProcessingStage(ProcessingStage.NONE);
      job.setOutputMediaPath(r.message());
      job.setErrorMessage(null);
      job.setFailureSummary(null);
      job.setFailureRepairHint(null);
      log.accept("完成，输出: " + r.message());
    } else {
      job.setStatus(JobStatus.FAILED);
      job.setProcessingStage(ProcessingStage.NONE);
      job.setErrorMessage(trim(r.message() + "\n" + r.stdout(), 4000));
      job.setFailureSummary("内置测试渲染失败");
      job.setFailureRepairHint("优先检查 Manim、Python、字体或本地渲染依赖是否正常。");
      log.accept("失败: " + r.message());
      if (!r.stdout().isBlank()) {
        log.accept(trim(r.stdout(), 6000));
      }
    }
    renderJobRepository.save(job);
  }

  private static String trim(String s, int max) {
    if (s == null) {
      return null;
    }
    return s.length() <= max ? s : s.substring(0, max) + "…";
  }
}
