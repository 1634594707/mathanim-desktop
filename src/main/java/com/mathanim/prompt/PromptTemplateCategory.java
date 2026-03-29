package com.mathanim.prompt;

/**
 * 提示词模板大类：先选学科，再选该学科下的具体模板（见 {@link PromptTemplateKind}）。
 */
public enum PromptTemplateCategory {

  /** 数学类（分析、级数、几何证明等） */
  MATH("数学"),

  /** 物理类（力学、电磁等示意与矢量） */
  PHYSICS("物理"),

  /** 与学科无关的通用工程约束或全角色内置 */
  GENERAL("通用");

  private final String displayName;

  PromptTemplateCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
