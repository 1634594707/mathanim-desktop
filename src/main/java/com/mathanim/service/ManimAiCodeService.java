package com.mathanim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.ai.OpenAiChatClient;
import com.mathanim.domain.OutputMode;
import com.mathanim.domain.PromptLocale;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.domain.RenderJob;
import com.mathanim.prompt.PromptOverridesDto;
import com.mathanim.prompt.PromptOverridesParser;
import com.mathanim.prompt.PromptRoleKeys;
import com.mathanim.prompt.RolePromptOverride;
import com.mathanim.util.AiResponseText;
import com.mathanim.util.ReferenceImagesParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class ManimAiCodeService {

  private static final int MAX_REPAIR_CODE_CHARS = 14_000;
  private static final int MAX_EDIT_CODE_CHARS = 14_000;
  private static final int MAX_EDIT_INSTRUCTION_CHARS = 8_000;
  private static final int MAX_REPAIR_ERROR_CHARS = 8_000;

  private static final String SYSTEM_GUARD =
      "你是 Manim Community Edition 脚本助手。只输出可运行的 Python 源码，不要 markdown 代码块。\n"
          + "硬性要求：主场景类名必须为 GeneratedScene。\n"
          + "导入与基类：默认首行 from manim import *，且 class GeneratedScene(Scene)。\n"
          + "若题目需要 manim-physics（刚体 pymunk、单摆、点电荷/磁场、透镜与光线、波动画等）：首行用 from manim_physics import *（已内含 manim），"
          + "基类用库提供的 SpaceScene、ThreeDScene 等；禁止从文档复制未写完的 SpaceScene 半成品。\n"
          + "刚体在容器内碰撞：优先采用 manim-physics 官方范式（地面 + 左右墙、开口向上；默认 Circle() 与 Square() 的尺寸与 test_rigid_mechanics 一致；"
          + "make_rigid_body / make_static_body）。默认不要加顶墙，除非用户明确要求「封闭箱」或「四面墙」。高帧率渲染可减轻穿模（如 --fps 60）。";

  private static final String SYSTEM_REPAIR =
      "You output a single complete Python file for Manim CE. Fix errors from the log. No markdown fences.";

  private static final String SYSTEM_EDIT =
      "You output a single complete Python file for Manim CE following the user's edit instructions. No markdown fences.";

  private final OpenAiChatClient openAiChatClient;
  private final PromptResourceLoader promptResourceLoader;
  private final ObjectMapper objectMapper;

  public ManimAiCodeService(
      OpenAiChatClient openAiChatClient,
      PromptResourceLoader promptResourceLoader,
      ObjectMapper objectMapper) {
    this.openAiChatClient = openAiChatClient;
    this.promptResourceLoader = promptResourceLoader;
    this.objectMapper = objectMapper;
  }

  /** 生成任务首轮代码（单阶段或两阶段）。不向调用方汇报子阶段进度。 */
  public String generateInitialCode(RenderJob job, String mergedConcept) {
    return generateInitialCode(job, mergedConcept, null);
  }

  /**
   * 生成首轮代码；若 {@code progressLog} 非空且开启两阶段，会依次输出「解题与可视规划 → 分镜 → 代码」日志，便于排查
   * “长时间无输出”体感。
   */
  public String generateInitialCode(
      RenderJob job, String mergedConcept, Consumer<String> progressLog) {
    PromptLocale loc = PromptLocale.fromHyphenated(job.getPromptLocale());
    PromptOverridesDto ov =
        PromptOverridesParser.parse(job.getPromptOverridesJson(), objectMapper);
    List<ReferenceImageItem> refs =
        ReferenceImagesParser.parseJson(job.getReferenceImagesJson(), objectMapper);
    if (job.isUseTwoStageAi()) {
      return generateTwoStage(mergedConcept, job.getOutputMode(), refs, loc, ov, progressLog);
    }
    return generateSingleStage(mergedConcept, job.getOutputMode(), refs, loc, ov);
  }

  private String generateSingleStage(
      String mergedConcept,
      OutputMode outputMode,
      List<ReferenceImageItem> refs,
      PromptLocale locale,
      PromptOverridesDto overrides) {
    String template = loadClasspath("prompts/manim-scene-generation.txt");
    String user = template.replace("{{CONCEPT}}", mergedConcept != null ? mergedConcept.strip() : "");
    if (outputMode == OutputMode.IMAGE) {
      user += "\n\n输出为单帧静图风格：构图完整、适合 -s 单帧渲染。";
    }
    RolePromptOverride cg = overrides != null ? overrides.getRole(PromptRoleKeys.CODE_GENERATION) : null;
    String system = SYSTEM_GUARD;
    if (cg != null && cg.getSystem() != null && !cg.getSystem().isBlank()) {
      system = cg.getSystem();
    }
    if (cg != null && cg.getUser() != null && !cg.getUser().isBlank()) {
      Map<String, String> m = new HashMap<>();
      m.put("CONCEPT", mergedConcept != null ? mergedConcept : "");
      user = promptResourceLoader.applyPlaceholders(cg.getUser(), m);
    }
    String raw = openAiChatClient.chatWithImages(system, user, refs);
    return normalizePython(stripMarkdownFences(raw));
  }

  private String generateTwoStage(
      String mergedConcept,
      OutputMode outputMode,
      List<ReferenceImageItem> refs,
      PromptLocale locale,
      PromptOverridesDto overrides,
      Consumer<String> progressLog) {
    String seed = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    if (progressLog != null) {
      progressLog.accept("[阶段] ① 解题与可视目标分析（尚无代码，请稍候）…");
    }
    String analysis =
        runSolutionAnalysis(mergedConcept, outputMode, refs, locale, overrides, progressLog);
    String augmentedConcept = mergedConcept != null ? mergedConcept : "";
    if (analysis != null && !analysis.isBlank()) {
      augmentedConcept = augmentedConcept + "\n\n【解题与可视规划】\n" + analysis;
      if (progressLog != null) {
        int cap = 3200;
        String excerpt = analysis.length() > cap ? analysis.substring(0, cap) + "\n… (已截断)" : analysis;
        progressLog.accept("--- ① 摘要（日志节选，完整上下文已交给分镜）---\n" + excerpt);
      }
    }

    if (progressLog != null) {
      progressLog.accept("[阶段] ② 工程分镜（英文指令，供代码生成）…");
    }
    String designerSystem = promptResourceLoader.load("prompts/roles/concept-designer.system.md");
    RolePromptOverride cd = overrides != null ? overrides.getRole(PromptRoleKeys.CONCEPT_DESIGNER) : null;
    if (cd != null && cd.getSystem() != null && !cd.getSystem().isBlank()) {
      designerSystem = cd.getSystem();
    }
    String designerUser = buildConceptDesignerUser(augmentedConcept, seed, outputMode, locale);
    if (cd != null && cd.getUser() != null && !cd.getUser().isBlank()) {
      Map<String, String> m = new HashMap<>();
      m.put("concept", augmentedConcept);
      m.put("seed", seed);
      m.put("outputMode", outputMode.name().toLowerCase());
      designerUser = promptResourceLoader.applyPlaceholders(cd.getUser(), m);
    }

    String designRaw = openAiChatClient.chatWithImages(designerSystem, designerUser, refs);
    String sceneDesign = AiResponseText.extractDesign(designRaw);

    if (progressLog != null) {
      progressLog.accept("[阶段] ③ Manim 代码生成…");
    }
    String coderSystem = promptResourceLoader.load("prompts/roles/code-generation.system.md");
    RolePromptOverride cgr = overrides != null ? overrides.getRole(PromptRoleKeys.CODE_GENERATION) : null;
    if (cgr != null && cgr.getSystem() != null && !cgr.getSystem().isBlank()) {
      coderSystem = cgr.getSystem();
    }
    String coderUser = buildCodeGenerationUser(augmentedConcept, seed, sceneDesign, outputMode, locale);
    if (cgr != null && cgr.getUser() != null && !cgr.getUser().isBlank()) {
      Map<String, String> m = new HashMap<>();
      m.put("concept", augmentedConcept);
      m.put("seed", seed);
      m.put("sceneDesign", sceneDesign);
      m.put("outputMode", outputMode.name().toLowerCase());
      coderUser = promptResourceLoader.applyPlaceholders(cgr.getUser(), m);
    }

    String codeRaw = openAiChatClient.chat(coderSystem, coderUser);
    String code = AiResponseText.extractCodeAnchors(codeRaw);
    return normalizePython(code);
  }

  private String runSolutionAnalysis(
      String mergedConcept,
      OutputMode outputMode,
      List<ReferenceImageItem> refs,
      PromptLocale locale,
      PromptOverridesDto overrides,
      Consumer<String> progressLog) {
    String system = promptResourceLoader.load("prompts/roles/solution-analysis.system.md");
    RolePromptOverride sa = overrides != null ? overrides.getRole(PromptRoleKeys.SOLUTION_ANALYSIS) : null;
    if (sa != null && sa.getSystem() != null && !sa.getSystem().isBlank()) {
      system = sa.getSystem();
    }
    String user = buildSolutionAnalysisUser(mergedConcept, outputMode, locale);
    if (sa != null && sa.getUser() != null && !sa.getUser().isBlank()) {
      Map<String, String> m = new HashMap<>();
      m.put("concept", mergedConcept != null ? mergedConcept : "");
      m.put("outputMode", outputMode.name().toLowerCase());
      user = promptResourceLoader.applyPlaceholders(sa.getUser(), m);
    }
    try {
      String raw = openAiChatClient.chatWithImages(system, user, refs);
      return raw != null ? raw.strip() : "";
    } catch (RuntimeException e) {
      if (progressLog != null) {
        progressLog.accept("① 解题规划阶段异常（跳过后续将仅用题干）: " + e.getMessage());
      }
      return "";
    }
  }

  private static String buildSolutionAnalysisUser(
      String concept, OutputMode outputMode, PromptLocale locale) {
    boolean zh = locale != PromptLocale.EN_US;
    StringBuilder sb = new StringBuilder();
    if (zh) {
      sb.append("请严格按照 system 角色输出：Part A 解题 + Part B 可视规划；不要写 Python。\n\n");
    } else {
      sb.append("Follow the system role: Part A solution + Part B visualization plan; no Python.\n\n");
    }
    sb.append("题目 / 任务：\n").append(concept != null ? concept : "").append("\n\n");
    sb.append("输出模式：").append(outputMode.name().toLowerCase()).append("\n");
    return sb.toString();
  }

  private static String buildConceptDesignerUser(
      String concept, String seed, OutputMode outputMode, PromptLocale locale) {
    boolean zh = locale != PromptLocale.EN_US;
    StringBuilder sb = new StringBuilder();
    sb.append(zh ? "请为下列概念生成 <design> 包裹的工程级分镜（英文指令）。\n\n" : "Produce a <design> wrapped engineering storyboard.\n\n");
    sb.append("Concept:\n").append(concept != null ? concept : "").append("\n\n");
    sb.append("Seed: ").append(seed).append("\n");
    sb.append("Output mode: ").append(outputMode.name().toLowerCase()).append("\n");
    if (outputMode == OutputMode.IMAGE) {
      sb.append(zh ? "\n静图模式：每镜独立、无跨镜运行时依赖。\n" : "\nImage mode: one static composition per shot.\n");
    }
    return sb.toString();
  }

  private static String buildCodeGenerationUser(
      String concept,
      String seed,
      String sceneDesign,
      OutputMode outputMode,
      PromptLocale locale) {
    boolean zh = locale != PromptLocale.EN_US;
    StringBuilder sb = new StringBuilder();
    if (sceneDesign != null && !sceneDesign.isBlank()) {
      sb.append("Storyboard:\n\n").append(sceneDesign).append("\n\n");
    }
    sb.append("Concept:\n").append(concept != null ? concept : "").append("\n");
    sb.append("Seed: ").append(seed).append("\n");
    sb.append("Output mode: ").append(outputMode.name().toLowerCase()).append("\n\n");
    sb.append(
        zh
            ? "将分镜转为可运行 Manim 代码。主类名必须为 GeneratedScene。用 ### START ### 与 ### END ### 包裹代码。\n屏幕文字语言：中文。\n"
            : "Convert the storyboard to runnable Manim code. Class name must be GeneratedScene. Wrap code with ### START ### and ### END ###.\nOn-screen text: English.\n");
    return sb.toString();
  }

  private static String normalizePython(String code) {
    if (code == null) {
      return "";
    }
    return code.replace("class MainScene", "class GeneratedScene");
  }

  public String editSceneCode(String concept, String previousCode, String instructions) {
    String template = loadClasspath("prompts/manim-scene-edit.txt");
    String code = truncate(previousCode, MAX_EDIT_CODE_CHARS);
    String ins = truncate(instructions, MAX_EDIT_INSTRUCTION_CHARS);
    String user =
        template
            .replace("{{CONCEPT}}", concept != null ? concept.strip() : "")
            .replace("{{PREVIOUS_CODE}}", code)
            .replace("{{INSTRUCTIONS}}", ins);
    String raw = openAiChatClient.chat(SYSTEM_EDIT, user);
    return normalizePython(stripMarkdownFences(raw));
  }

  public String repairSceneCode(
      String concept, String previousCode, String stage, String errorOutput, PromptOverridesDto overrides) {
    String template = loadClasspath("prompts/manim-scene-repair.txt");
    RolePromptOverride rr = overrides != null ? overrides.getRole(PromptRoleKeys.CODE_RETRY) : null;
    if (rr != null && rr.getUser() != null && !rr.getUser().isBlank()) {
      Map<String, String> m = new HashMap<>();
      m.put("CONCEPT", concept != null ? concept.strip() : "");
      m.put("PREVIOUS_CODE", truncate(previousCode, MAX_REPAIR_CODE_CHARS));
      m.put("STAGE", stage);
      m.put("ERROR", truncate(errorOutput, MAX_REPAIR_ERROR_CHARS));
      template = promptResourceLoader.applyPlaceholders(rr.getUser(), m);
    } else {
      String code = truncate(previousCode, MAX_REPAIR_CODE_CHARS);
      String err = truncate(errorOutput, MAX_REPAIR_ERROR_CHARS);
      template =
          template
              .replace("{{CONCEPT}}", concept != null ? concept.strip() : "")
              .replace("{{PREVIOUS_CODE}}", code)
              .replace("{{STAGE}}", stage)
              .replace("{{ERROR}}", err);
    }
    String system = SYSTEM_REPAIR;
    if (overrides != null && overrides.getRole(PromptRoleKeys.CODE_RETRY) != null) {
      RolePromptOverride rrSys = overrides.getRole(PromptRoleKeys.CODE_RETRY);
      if (rrSys != null && rrSys.getSystem() != null && !rrSys.getSystem().isBlank()) {
        system = rrSys.getSystem();
      }
    }
    String raw = openAiChatClient.chat(system, template);
    return normalizePython(stripMarkdownFences(raw));
  }

  /** 兼容旧调用（无覆盖）。 */
  public String repairSceneCode(String concept, String previousCode, String stage, String errorOutput) {
    return repairSceneCode(concept, previousCode, stage, errorOutput, new PromptOverridesDto());
  }

  private static String loadClasspath(String classpath) {
    try {
      ClassPathResource res = new ClassPathResource(classpath);
      return StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("缺少 classpath 资源: " + classpath, e);
    }
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max) + "\n# ... [truncated]";
  }

  private static String stripMarkdownFences(String s) {
    String t = s.strip();
    if (!t.startsWith("```")) {
      return t;
    }
    int nl = t.indexOf('\n');
    if (nl < 0) {
      return t;
    }
    int end = t.lastIndexOf("```");
    if (end <= nl) {
      return t;
    }
    return t.substring(nl + 1, end).strip();
  }
}
