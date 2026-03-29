package com.mathanim.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class PromptOverridesParser {

  private PromptOverridesParser() {}

  public static PromptOverridesDto parse(String json, ObjectMapper mapper) {
    if (json == null || json.isBlank()) {
      return new PromptOverridesDto();
    }
    try {
      return mapper.readValue(json.strip(), PromptOverridesDto.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("提示词覆盖 JSON 无效: " + e.getMessage(), e);
    }
  }
}
