package com.mathanim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mathanim.domain.PromptLocale;
import com.mathanim.domain.ProblemFramingPlan;
import com.mathanim.domain.ProblemFramingStep;

import java.util.ArrayList;
import java.util.List;

/** 对齐 ManimCat {@code normalizePlan}。 */
public final class ProblemFramingNormalizer {

  private ProblemFramingNormalizer() {}

  public static ProblemFramingPlan normalize(JsonNode raw, PromptLocale locale) {
    if (raw == null || !raw.isObject()) {
      throw new IllegalStateException("问题拆解响应不是 JSON 对象");
    }
    boolean en = locale == PromptLocale.EN_US;
    String fallbackStepTitle = en ? "Step" : "步骤";
    String fallbackStepContent =
        en
            ? "Continue clarifying the visual direction and storytelling order for this part."
            : "继续细化这一段的可视化表达和叙事顺序。";
    String fallbackHeadline = en ? "A fresh visualization plan" : "新的可视化方案";
    String fallbackSummary =
        en ? "The expression path has been organized more clearly." : "整理出一个更清晰的表达路径。";
    String fallbackMotif =
        en ? "Cat paws are sorting the steps across the card." : "猫爪在卡片上整理出步骤。";
    String fallbackHint =
        en
            ? "The next designer stage should expand these three steps into concrete animation design."
            : "下一阶段继续把三步扩成具体动画设计。";

    List<ProblemFramingStep> normalizedSteps = new ArrayList<>();
    JsonNode stepsNode = raw.get("steps");
    if (stepsNode != null && stepsNode.isArray()) {
      for (JsonNode s : stepsNode) {
        if (normalizedSteps.size() >= 5) {
          break;
        }
        if (s == null || !s.isObject()) {
          continue;
        }
        String title = sanitizeString(s.get("title"), "");
        String content = sanitizeString(s.get("content"), "");
        if (content.isEmpty()) {
          continue;
        }
        ProblemFramingStep step = new ProblemFramingStep();
        step.setTitle(title.isEmpty() ? fallbackStepTitle + " " + (normalizedSteps.size() + 1) : title);
        step.setContent(content);
        normalizedSteps.add(step);
      }
    }
    while (normalizedSteps.size() < 3) {
      ProblemFramingStep step = new ProblemFramingStep();
      step.setTitle(fallbackStepTitle + " " + (normalizedSteps.size() + 1));
      step.setContent(fallbackStepContent);
      normalizedSteps.add(step);
    }

    ProblemFramingPlan plan = new ProblemFramingPlan();
    String mode = raw.has("mode") ? raw.get("mode").asText("invent") : "invent";
    plan.setMode("clarify".equalsIgnoreCase(mode) ? "clarify" : "invent");
    plan.setHeadline(sanitizeString(raw.get("headline"), fallbackHeadline));
    plan.setSummary(sanitizeString(raw.get("summary"), fallbackSummary));
    plan.setSteps(normalizedSteps);
    plan.setVisualMotif(
        sanitizeString(
            raw.has("visualMotif") ? raw.get("visualMotif") : raw.get("visual_motif"), fallbackMotif));
    plan.setDesignerHint(
        sanitizeString(
            raw.has("designerHint") ? raw.get("designerHint") : raw.get("designer_hint"), fallbackHint));
    return plan;
  }

  private static String sanitizeString(JsonNode n, String fallback) {
    if (n == null || n.isNull() || !n.isTextual()) {
      return fallback;
    }
    String t = n.asText("").trim().replaceAll("\\s+", " ");
    return t.isEmpty() ? fallback : t;
  }
}
