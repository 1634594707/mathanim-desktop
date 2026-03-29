package com.mathanim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.ai.OpenAiChatClient;
import com.mathanim.domain.GeogebraCommandPlan;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.util.AiResponseText;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeogebraAiService {

  private final OpenAiChatClient openAiChatClient;
  private final PromptResourceLoader promptResourceLoader;
  private final ObjectMapper objectMapper;

  public GeogebraAiService(
      OpenAiChatClient openAiChatClient,
      PromptResourceLoader promptResourceLoader,
      ObjectMapper objectMapper) {
    this.openAiChatClient = openAiChatClient;
    this.promptResourceLoader = promptResourceLoader;
    this.objectMapper = objectMapper;
  }

  /**
   * 根据文字与可选题目图生成 GeoGebra {@code evalCommand} 指令序列。
   */
  public GeogebraCommandPlan generate(String problemText, List<ReferenceImageItem> images) {
    String system = promptResourceLoader.load("prompts/geogebra-commands.system.md");
    String user = buildUserPrompt(problemText);
    String raw =
        images != null && !images.isEmpty()
            ? openAiChatClient.chatWithImages(system, user, images, false)
            : openAiChatClient.chat(system, user);
    String json = AiResponseText.extractJsonObject(raw);
    try {
      GeogebraCommandPlan plan = objectMapper.readValue(json, GeogebraCommandPlan.class);
      if (plan.getCommands() == null || plan.getCommands().isEmpty()) {
        throw new IllegalStateException("AI 未返回有效的 commands 数组");
      }
      return plan;
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("解析 GeoGebra JSON 失败: " + e.getMessage(), e);
    }
  }

  private static String buildUserPrompt(String problemText) {
    String body = problemText != null ? problemText.strip() : "";
    if (body.isEmpty()) {
      body = "（仅根据题目截图作图，无额外文字描述。）";
    }
    return "用户题目如下。必须严格对齐题干中的条件、字母与一般参数（如 a,b,k、直线 l 与 l₁、椭圆 C 等）；"
        + "若需用数值演示须在 notes 中首句声明为「示意」。请按系统说明只输出 JSON：\n"
        + body;
  }
}
