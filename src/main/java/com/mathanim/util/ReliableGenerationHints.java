package com.mathanim.util;

/**
 * 在「稳定优先」开启时追加到送入 AI 的 concept 末尾，降低高难度任务首次渲染失败率（manim exit=1）。
 */
public final class ReliableGenerationHints {

  public static final String APPENDIX_ZH =
      "【渲染稳定性约束（必须遵守）】\n"
          + "- 总镜头 ≤6，construct 建议 ≤200 行；讲清核心步骤即可，避免堆砌冗长代数动画。\n"
          + "- 禁止 MathTex(..., weight=) / Tex(..., weight=)（仅 Text 支持 weight）；加粗用 LaTeX \\mathbf 或增大 font_size。\n"
          + "- 禁止对 MathTex 用 mobject[0][i:j] 硬编码子对象切片做 Indicate/ReplacementTransform；对整段公式 Indicate，或 FadeOut+Write。\n"
          + "- 禁止 ReplacementTransform(某段 MathTex 的局部, 另一段 MathTex 的局部)。\n"
          + "- ThreeDScene 相机（减少「角度怪、裁切、公式歪」）：\n"
          + "  · set_camera_orientation 建议 phi=60°～75°、theta=-40°～-55°、gamma=0、zoom=0.65～0.85（zoom<1 为拉远）；勿用过小 zoom 导致只拍到边缘。\n"
          + "  · ThreeDAxes 与主体几何用 move_to(ORIGIN)，禁止大位移如 .shift(LEFT*3) 把四面体/坐标系挤出画面。\n"
          + "  · 长公式、投影条件、旁注：用 add_fixed_in_frame_mobjects（或右侧竖排公式），勿把整段 MathTex 仅 to_corner 摆在 3D 里随透视歪斜。\n"
          + "  · 顶点旁短标签可用 add_fixed_orientation_mobjects；需要「正对屏幕」的说明一律 fixed_in_frame。\n"
          + "- 易错时可将最难的一镜改为 2D Axes 示意图 + 文字，其余仍可用 3D。\n";

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
}
