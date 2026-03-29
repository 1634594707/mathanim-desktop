package com.mathanim.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "render_jobs")
public class RenderJob {

  /** 与 {@link JobStatus#FAILED} 配合表示用户取消（避免在 SQLite 上扩展 status 枚举约束）。 */
  public static final String USER_CANCELLED_MESSAGE = "用户已取消";

  /** 应用重启或进程异常退出时，将仍标记为运行中的任务收尾为此说明。 */
  public static final String INTERRUPTED_MESSAGE =
      "上次运行未正常结束（应用重启或进程中断）。请重新点击「一键生成」或「AI → 渲染」。";

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 8192)
  private String concept;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private JobStatus status = JobStatus.QUEUED;

  @Column(length = 2048)
  private String outputMediaPath;

  @Column(length = 4096)
  private String errorMessage;

  /** TEXT 映射，避免 SQLite 上过时的 CHECK(processing_stage in (...)) 拦新枚举值。 */
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "TEXT")
  private ProcessingStage processingStage = ProcessingStage.NONE;

  /**
   * 旧库升级时可为空；{@link #getOutputMode()} 对调用方给出默认。
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 16)
  private OutputMode outputMode = OutputMode.VIDEO;

  @Enumerated(EnumType.STRING)
  @Column(length = 16)
  private VideoQuality videoQuality = VideoQuality.MEDIUM;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private JobKind jobKind = JobKind.GENERATE;

  /** 修改流水线：当前脚本全文（与 ManimCat modify 对齐）。 */
  @Lob
  @Column(columnDefinition = "TEXT")
  private String sourceCode;

  /** 修改流水线：自然语言编辑说明。 */
  @Column(length = 8192)
  private String editInstructions;

  /** 生成的 Manim 脚本路径（若已跑过 AI 流水线）。 */
  @Column(length = 2048)
  private String scriptPath;

  /**
   * 是否在流水线中执行问题拆解（无规划 JSON 时先调用 AI）。
   * <p>使用 {@link Boolean} 且列可空，便于 SQLite 在旧表上 {@code ALTER TABLE ADD COLUMN}（{@code NOT NULL}
   * 无默认值会被 SQLite 拒绝）；旧行 {@code null} 在 getter 中视为 false。
   */
  @Column
  private Boolean useProblemFraming;

  /**
   * 是否使用 ManimCat 风格两阶段（概念设计 → 代码）。false 时为单段生成。
   * <p>旧行 {@code null} 在 getter 中视为 true（与原先字段默认值一致）。
   */
  @Column
  private Boolean useTwoStageAi;

  /**
   * 是否在生成/修补提示中追加「稳定优先」约束（少分镜、禁危险 API）。
   * <p>旧行 {@code null} 在 {@link #isReliableGeneration()} 中视为 true。
   */
  @Column
  private Boolean reliableGeneration;

  /** {@link com.mathanim.domain.ProblemFramingPlan} 的 JSON。 */
  @Lob
  @Column(columnDefinition = "TEXT")
  private String problemPlanJson;

  /** 参考图列表 JSON：[{ "url": "...", "detail": "auto" }]。 */
  @Lob
  @Column(columnDefinition = "TEXT")
  private String referenceImagesJson;

  @Column(length = 16)
  private String promptLocale = "zh-CN";

  /** {@link com.mathanim.prompt.PromptOverridesDto} JSON。 */
  @Lob
  @Column(columnDefinition = "TEXT")
  private String promptOverridesJson;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @PrePersist
  void prePersist() {
    if (outputMode == null) {
      outputMode = OutputMode.VIDEO;
    }
    if (videoQuality == null) {
      videoQuality = VideoQuality.MEDIUM;
    }
    if (jobKind == null) {
      jobKind = JobKind.GENERATE;
    }
    if (useProblemFraming == null) {
      useProblemFraming = Boolean.FALSE;
    }
    if (useTwoStageAi == null) {
      useTwoStageAi = Boolean.TRUE;
    }
    if (reliableGeneration == null) {
      reliableGeneration = Boolean.TRUE;
    }
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getConcept() {
    return concept;
  }

  public void setConcept(String concept) {
    this.concept = concept;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public String getOutputMediaPath() {
    return outputMediaPath;
  }

  public void setOutputMediaPath(String outputMediaPath) {
    this.outputMediaPath = outputMediaPath;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public ProcessingStage getProcessingStage() {
    return processingStage;
  }

  public void setProcessingStage(ProcessingStage processingStage) {
    this.processingStage = processingStage;
  }

  public String getScriptPath() {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath) {
    this.scriptPath = scriptPath;
  }

  public OutputMode getOutputMode() {
    return outputMode != null ? outputMode : OutputMode.VIDEO;
  }

  public void setOutputMode(OutputMode outputMode) {
    this.outputMode = outputMode;
  }

  public VideoQuality getVideoQuality() {
    return videoQuality != null ? videoQuality : VideoQuality.MEDIUM;
  }

  public void setVideoQuality(VideoQuality videoQuality) {
    this.videoQuality = videoQuality;
  }

  public JobKind getJobKind() {
    return jobKind != null ? jobKind : JobKind.GENERATE;
  }

  public void setJobKind(JobKind jobKind) {
    this.jobKind = jobKind;
  }

  public String getSourceCode() {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  public String getEditInstructions() {
    return editInstructions;
  }

  public void setEditInstructions(String editInstructions) {
    this.editInstructions = editInstructions;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isUseProblemFraming() {
    return Boolean.TRUE.equals(useProblemFraming);
  }

  public void setUseProblemFraming(boolean useProblemFraming) {
    this.useProblemFraming = useProblemFraming;
  }

  public boolean isUseTwoStageAi() {
    return useTwoStageAi == null || useTwoStageAi;
  }

  public void setUseTwoStageAi(boolean useTwoStageAi) {
    this.useTwoStageAi = useTwoStageAi;
  }

  public boolean isReliableGeneration() {
    return reliableGeneration == null || Boolean.TRUE.equals(reliableGeneration);
  }

  public void setReliableGeneration(boolean reliableGeneration) {
    this.reliableGeneration = reliableGeneration;
  }

  public String getProblemPlanJson() {
    return problemPlanJson;
  }

  public void setProblemPlanJson(String problemPlanJson) {
    this.problemPlanJson = problemPlanJson;
  }

  public String getReferenceImagesJson() {
    return referenceImagesJson;
  }

  public void setReferenceImagesJson(String referenceImagesJson) {
    this.referenceImagesJson = referenceImagesJson;
  }

  public String getPromptLocale() {
    return promptLocale != null ? promptLocale : "zh-CN";
  }

  public void setPromptLocale(String promptLocale) {
    this.promptLocale = promptLocale;
  }

  public String getPromptOverridesJson() {
    return promptOverridesJson;
  }

  public void setPromptOverridesJson(String promptOverridesJson) {
    this.promptOverridesJson = promptOverridesJson;
  }
}
