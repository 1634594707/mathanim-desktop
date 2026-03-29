package com.mathanim.prompt;

/**
 * {@code promptOverridesJson} 中 {@link PromptOverridesDto#getRoles()} 的键名，与流水线阶段一一对应。
 *
 * <p>设计对照 golutra 的「多成员并行 / 串行 handoff」：此处每个 key 表示一个逻辑 Agent（专用 system/user），由 {@link
 * com.mathanim.service.WorkflowPipelineService} 按序调用，无需多进程，但语义上仍是「编排」。
 */
public final class PromptRoleKeys {

  public static final String PROBLEM_FRAMING = "problemFraming";
  /** 解题 + 可视目标（两阶段流水线中插在分镜之前） */
  public static final String SOLUTION_ANALYSIS = "solutionAnalysis";
  public static final String CONCEPT_DESIGNER = "conceptDesigner";
  public static final String CODE_GENERATION = "codeGeneration";
  public static final String CODE_RETRY = "codeRetry";

  private PromptRoleKeys() {}
}
