package com.mathanim.util;

import java.util.Locale;

/**
 * Classifies common Manim runtime failures into coarse categories so retries can adapt instead of
 * repeating the same strategy.
 */
public final class ManimFailureDiagnostics {

  public enum FailureKind {
    LATEX_ENV,
    MATHTEX_API,
    AXIS_LABELS,
    DASH_API,
    CAMERA_OR_LAYOUT,
    SCENE_TOO_COMPLEX,
    GENERIC_RUNTIME
  }

  public record Diagnostic(FailureKind kind, String summary, String repairHint) {}

  private ManimFailureDiagnostics() {}

  public static Diagnostic classify(String message, String stdout) {
    String hay = ((message != null ? message : "") + "\n" + (stdout != null ? stdout : ""))
        .toLowerCase(Locale.ROOT);

    if (containsAny(hay, "tex_to_svg_file", "latex", "tex live", "miktex", "unicode character")) {
      return new Diagnostic(
          FailureKind.LATEX_ENV,
          "检测到 LaTeX / MathTex 相关渲染失败",
          "优先把普通标签改成 Text，避免复杂 MathTex；只保留必要公式。");
    }
    if (containsAny(hay, "unexpected keyword argument 'weight'", "indexerror")
        || (hay.contains("mathtex") && containsAny(hay, "[0][", "replacementtransform"))) {
      return new Diagnostic(
          FailureKind.MATHTEX_API,
          "检测到 MathTex / Tex API 使用不稳",
          "删除不兼容参数，避免对 MathTex 局部切片做变换，改成整段高亮或拆分对象。");
    }
    if (containsAny(hay, "add_numbers", "get_number_mobject", "numberline", "include_numbers")) {
      return new Diagnostic(
          FailureKind.AXIS_LABELS,
          "检测到坐标轴数字或刻度标签相关失败",
          "关闭自动数字刻度，改用 Text 或 DecimalNumber 手动标注。");
    }
    if (containsAny(hay, "set_dash", "dash_length", "dashed_ratio", "dashedvmobject")) {
      return new Diagnostic(
          FailureKind.DASH_API,
          "检测到虚线 API 相关失败",
          "直线用 DashedLine，虚线圆用 DashedVMobject(Circle(...))。");
    }
    if (containsAny(hay, "set_camera_orientation", "threedscene", "opengl", "fixed_in_frame")) {
      return new Diagnostic(
          FailureKind.CAMERA_OR_LAYOUT,
          "检测到 3D 相机或场景布局相关失败",
          "减少 3D，保持主体居中，长文本改 fixed_in_frame，必要时直接降为 2D。");
    }
    if (containsAny(hay, "killed", "memory", "out of memory", "brokenpipe", "recursionerror")
        || (containsAny(hay, "animation", "using cached") && hay.length() > 1500)) {
      return new Diagnostic(
          FailureKind.SCENE_TOO_COMPLEX,
          "场景可能过重或对象过多，导致运行时不稳定",
          "强制减少镜头、对象和并行动画，优先改成静态示意图。");
    }
    return new Diagnostic(
        FailureKind.GENERIC_RUNTIME,
        "检测到通用运行时失败",
        "优先简化镜头、减少对象和特效，保留核心教学结论。");
  }

  private static boolean containsAny(String hay, String... needles) {
    for (String needle : needles) {
      if (hay.contains(needle.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }
}
