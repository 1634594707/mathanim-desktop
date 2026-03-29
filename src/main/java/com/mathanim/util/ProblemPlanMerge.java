package com.mathanim.util;

import com.mathanim.domain.ProblemFramingPlan;
import com.mathanim.domain.ProblemFramingStep;

import java.util.List;

/** 对齐 ManimCat {@code mergeProblemPlanIntoConcept}。 */
public final class ProblemPlanMerge {

  private ProblemPlanMerge() {}

  public static String mergeIntoConcept(String concept, ProblemFramingPlan plan) {
    if (plan == null) {
      return concept != null ? concept : "";
    }
    String c = concept != null ? concept.strip() : "";
    List<ProblemFramingStep> steps = plan.getSteps();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < steps.size(); i++) {
      ProblemFramingStep s = steps.get(i);
      if (s == null || s.getContent() == null || s.getContent().isBlank()) {
        continue;
      }
      String title = s.getTitle() != null ? s.getTitle() : "步骤 " + (i + 1);
      sb.append(i + 1).append(". ").append(title).append(": ").append(s.getContent().strip()).append('\n');
    }
    String stepsBlock = sb.toString().strip();
    return String.join(
        "\n",
        c,
        "",
        "[Problem Framing Context]",
        "Mode: " + (plan.getMode() != null ? plan.getMode() : "invent"),
        "Headline: " + nullToEmpty(plan.getHeadline()),
        "Summary: " + nullToEmpty(plan.getSummary()),
        "Steps:",
        stepsBlock,
        "Visual Motif: " + nullToEmpty(plan.getVisualMotif()),
        "Designer Hint: " + nullToEmpty(plan.getDesignerHint()));
  }

  private static String nullToEmpty(String s) {
    return s != null ? s : "";
  }
}
