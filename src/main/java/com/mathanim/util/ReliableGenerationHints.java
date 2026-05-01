package com.mathanim.util;

/**
 * Appends stability-oriented guidance to AI prompts so complex tasks degrade gracefully instead of
 * repeatedly failing at render time.
 */
public final class ReliableGenerationHints {

  public static final String APPENDIX_ZH =
      "【渲染稳定性约束（必须遵守）】\n"
          + "- 总镜头 <= 6，construct 建议 <= 300 行；讲清核心步骤即可，避免堆砌冗长动画。\n"
          + "- 视频优先追求“教学清楚 + 画面干净 + 节奏稳定”：每镜只讲一个重点，避免一屏塞满文字和公式。\n"
          + "- 默认使用高对比、易读字号、稳定布局；关键对象位置尽量固定，减少镜头间突然跳变。\n"
          + "- 结论、标题、关键量要明确高亮；解释性文字要短句化，避免整段字幕遮挡主体图形。\n"
          + "- 单镜头内同时活跃对象尽量 <= 12；同一时刻并行动画尽量 <= 4。\n"
          + "- 优先使用 Create / FadeIn / FadeOut / Write / Transform / ReplacementTransform / Indicate。\n"
          + "- 非必要不要使用 always_redraw、复杂 updater、ValueTracker 驱动的大量连续重绘、长路径跟踪。\n"
          + "- 如果内容很多，优先改成“静态示意图 + 重点高亮 + 短文本说明”，不要把每一步都做成连续动画。\n"
          + "- 教学清晰度优先于炫技；宁可简单稳定，也不要镜头很多但无法渲染。\n"
          + "- 禁止 MathTex(..., weight=) / Tex(..., weight=)；仅 Text 支持 weight。\n"
          + "- 禁止对 MathTex 用 mobject[0][i:j] 之类硬编码切片做局部变换；对整段公式高亮，或拆成多个对象。\n"
          + "- 禁止 ReplacementTransform(某段 MathTex 的局部, 另一段 MathTex 的局部)。\n"
          + "- 3D 仅在确有必要时使用；长公式、注释、结论优先 fixed_in_frame。\n"
          + "- 易错时可将最难的一镜改成 2D 坐标示意图 + 文本，其余镜头保持教学连贯即可。\n";

  private ReliableGenerationHints() {}

  public static String appendIfEnabled(String concept, boolean enabled) {
    if (!enabled) {
      return concept != null ? concept : "";
    }
    String base = concept != null ? concept.strip() : "";
    if (base.isEmpty()) {
      return APPENDIX_ZH;
    }
    return base + "\n\n" + APPENDIX_ZH;
  }

  public static String appendRepairHints(
      String concept,
      String failureStage,
      String diagnosticSummary,
      String diagnosticHint,
      int pass,
      int maxPasses,
      boolean fallbackMode,
      boolean repeatedSameError) {
    String base = concept != null ? concept.strip() : "";
    StringBuilder sb = new StringBuilder();
    if (!base.isEmpty()) {
      sb.append(base).append("\n\n");
    }
    sb.append("【修补策略（必须遵守）】\n");
    sb.append("- 目标是得到可稳定渲染的正确版本，不是保留所有视觉细节。\n");
    if ("manim_render".equals(failureStage)) {
      sb.append("- 本次失败发生在 Manim 渲染阶段，说明场景过重、过复杂或运行时不稳定。\n");
      sb.append("- 必须主动降复杂度：删减镜头、删减对象、删减长文本、删减并行动画，保留核心教学结论即可。\n");
      sb.append("- 优先把复杂连续演示改成静态图 + 重点高亮 + 分步文字说明。\n");
      sb.append("- 能用 2D 就不要硬上 3D；能用基础几何与文本说明，就不要依赖持续更新对象。\n");
    } else if ("py_compile".equals(failureStage)) {
      sb.append("- 本次失败发生在静态检查阶段，先保证语法、导入和类结构完全正确，再考虑视觉细节。\n");
    }
    if (diagnosticSummary != null && !diagnosticSummary.isBlank()) {
      sb.append("- 失败诊断：").append(diagnosticSummary.strip()).append("\n");
    }
    if (diagnosticHint != null && !diagnosticHint.isBlank()) {
      sb.append("- 定向修补建议：").append(diagnosticHint.strip()).append("\n");
    }
    if (repeatedSameError) {
      sb.append("- 连续两轮出现同类错误，说明原修补思路无效，必须换方案，不能只做局部微调。\n");
      sb.append("- 必须主动改结构：例如 3D 改 2D、连续动画改静态示意、复杂公式变形改整段高亮。\n");
      sb.append("- 禁止沿用上一轮失败的对象组织方式、动画编排方式和镜头结构。\n");
    }
    if (pass >= 2) {
      sb.append("- 当前已进入多轮修补，禁止继续增加新镜头或新特效；只能做收缩和稳态修复。\n");
    }
    if (pass >= Math.max(2, maxPasses - 1)) {
      sb.append("- 当前已接近最后轮次：强制收缩到 3~4 个镜头、总时长 35 秒以内、单镜头对象尽量 8 个以内。\n");
      sb.append("- 最后一轮宁可输出简化教学版，也不要保留不稳定的大型场景。\n");
    }
    if (fallbackMode) {
      sb.append("\n【保底模板模式（强制执行）】\n");
      sb.append("- 忽略原方案中不必要的华丽分镜，输出“极简教学版”。\n");
      sb.append("- 强制限制为 <= 3 个镜头。\n");
      sb.append("- 强制限制为 1 个主图 + 少量文字 + 少量高亮动画。\n");
      sb.append("- 禁止 3D、禁止 always_redraw、禁止复杂 updater、禁止大规模并行动画。\n");
      sb.append("- 优先采用：标题 -> 静态示意图 -> 关键结论高亮 这类保守结构。\n");
      sb.append("- 如果原题包含多个子主题，只保留最核心的一个结论，其他内容压缩成一句文字说明。\n");
      sb.append("- 输出目标是“肯定能渲染”的教学草稿，不追求完整炫酷。\n");
    }
    sb.append("- 修补后仍需保持主类名 GeneratedScene，不要输出 markdown。\n");
    return sb.toString();
  }

  public static String appendRepairHints(
      String concept,
      String failureStage,
      String diagnosticSummary,
      String diagnosticHint,
      int pass,
      int maxPasses,
      boolean fallbackMode) {
    return appendRepairHints(
        concept,
        failureStage,
        diagnosticSummary,
        diagnosticHint,
        pass,
        maxPasses,
        fallbackMode,
        false);
  }
}
