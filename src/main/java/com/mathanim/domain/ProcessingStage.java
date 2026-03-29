package com.mathanim.domain;

/** 与 ManimCat 中 ProcessingStage 概念对应，用于桌面端任务子阶段展示。 */
public enum ProcessingStage {
  NONE,
  /** 问题拆解（JSON 规划卡片） */
  PROBLEM_FRAMING,
  /** 两阶段：概念设计 / storyboard */
  SCENE_DESIGNING,
  /** 两阶段：代码生成；或单阶段整体生成 */
  CODE_GENERATING,
  AI_GENERATING,
  STATIC_CHECKING,
  RENDERING
}
