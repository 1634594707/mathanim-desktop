package com.mathanim.domain;

/** 任务类型：从零生成、基于说明修改已有代码、或内置测试（无 AI）。 */
public enum JobKind {
  GENERATE,
  MODIFY,
  BUILTIN_TEST
}
