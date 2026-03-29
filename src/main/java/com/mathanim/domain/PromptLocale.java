package com.mathanim.domain;

/** 与 ManimCat PromptLocale 对齐。 */
public enum PromptLocale {
  ZH_CN,
  EN_US;

  public String toHyphenated() {
    return this == EN_US ? "en-US" : "zh-CN";
  }

  public static PromptLocale fromHyphenated(String s) {
    if (s != null && s.toLowerCase().startsWith("en")) {
      return EN_US;
    }
    return ZH_CN;
  }
}
