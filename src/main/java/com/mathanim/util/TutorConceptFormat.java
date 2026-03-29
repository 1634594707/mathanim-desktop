package com.mathanim.util;

import java.util.Optional;

/** 题干+解析模式写入 {@link com.mathanim.domain.RenderJob#setConcept} 时的结构化文本，便于回填表单。 */
public final class TutorConceptFormat {

  public static final String MARKER_HEAD = "【创作模式】题干与解析生成动画";
  private static final String MARKER_PROBLEM = "【题目】";
  private static final String MARKER_SOLUTION = "【解析与步骤】";
  private static final String MARKER_ANSWER = "【参考答案】";

  private TutorConceptFormat() {}

  public static String compose(String problem, String solution, String answer) {
    String p = problem != null ? problem.strip() : "";
    String s = solution != null ? solution.strip() : "";
    String a = answer != null ? answer.strip() : "";
    StringBuilder sb = new StringBuilder();
    sb.append(MARKER_HEAD).append("\n\n");
    sb.append(MARKER_PROBLEM).append("\n").append(p).append("\n\n");
    sb.append(MARKER_SOLUTION).append("\n").append(s).append("\n");
    if (!a.isEmpty()) {
      sb.append("\n").append(MARKER_ANSWER).append("\n").append(a).append("\n");
    }
    return sb.toString();
  }

  /**
   * 若文本由本类 {@link #compose} 生成，解析为三部分；否则返回 empty。
   */
  public static Optional<ParsedTutor> tryParse(String concept) {
    if (concept == null || concept.isBlank()) {
      return Optional.empty();
    }
    if (!concept.contains(MARKER_PROBLEM) || !concept.contains(MARKER_SOLUTION)) {
      return Optional.empty();
    }
    int iProb = concept.indexOf(MARKER_PROBLEM);
    int iSol = concept.indexOf(MARKER_SOLUTION);
    int iAns = concept.indexOf(MARKER_ANSWER);
    if (iProb < 0 || iSol < iProb) {
      return Optional.empty();
    }
    String problem = concept.substring(iProb + MARKER_PROBLEM.length(), iSol).strip();
    String solution;
    String answer = "";
    if (iAns >= 0 && iAns > iSol) {
      solution = concept.substring(iSol + MARKER_SOLUTION.length(), iAns).strip();
      answer = concept.substring(iAns + MARKER_ANSWER.length()).strip();
    } else {
      solution = concept.substring(iSol + MARKER_SOLUTION.length()).strip();
    }
    return Optional.of(new ParsedTutor(problem, solution, answer));
  }

  public record ParsedTutor(String problem, String solution, String answer) {}
}
