package com.mathanim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.ai.OpenAiChatClient;
import com.mathanim.domain.PromptLocale;
import com.mathanim.domain.ProblemFramingPlan;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.prompt.PromptOverridesDto;
import com.mathanim.prompt.PromptRoleKeys;
import com.mathanim.prompt.RolePromptOverride;
import com.mathanim.util.AiResponseText;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemFramingService {

  private final OpenAiChatClient openAiChatClient;
  private final PromptResourceLoader promptResourceLoader;
  private final ObjectMapper objectMapper;

  public ProblemFramingService(
      OpenAiChatClient openAiChatClient,
      PromptResourceLoader promptResourceLoader,
      ObjectMapper objectMapper) {
    this.openAiChatClient = openAiChatClient;
    this.promptResourceLoader = promptResourceLoader;
    this.objectMapper = objectMapper;
  }

  public ProblemFramingPlan generatePlan(
      String concept,
      List<ReferenceImageItem> images,
      PromptLocale locale,
      PromptOverridesDto overrides) {
    String system = promptResourceLoader.load("prompts/roles/problem-framing.system.md");
    if (overrides != null && overrides.getRole(PromptRoleKeys.PROBLEM_FRAMING) != null) {
      RolePromptOverride ro = overrides.getRole(PromptRoleKeys.PROBLEM_FRAMING);
      if (ro.getSystem() != null && !ro.getSystem().isBlank()) {
        system = ro.getSystem();
      }
    }

    String user = buildUserPrompt(concept, locale);
    if (overrides != null && overrides.getRole(PromptRoleKeys.PROBLEM_FRAMING) != null) {
      RolePromptOverride ro = overrides.getRole(PromptRoleKeys.PROBLEM_FRAMING);
      if (ro.getUser() != null && !ro.getUser().isBlank()) {
        Map<String, String> m = new HashMap<>();
        m.put("concept", concept != null ? concept : "");
        user = promptResourceLoader.applyPlaceholders(ro.getUser(), m);
      }
    }

    String raw = openAiChatClient.chatWithImages(system, user, images);
    try {
      String json = AiResponseText.extractJsonObject(raw);
      JsonNode node = objectMapper.readTree(json);
      return ProblemFramingNormalizer.normalize(node, locale);
    } catch (Exception e) {
      throw new IllegalStateException("问题拆解 JSON 解析失败: " + e.getMessage(), e);
    }
  }

  private static String buildUserPrompt(String concept, PromptLocale locale) {
    boolean zh = locale != PromptLocale.EN_US;
    return (zh ? "用户概念：\n" : "Concept:\n")
        + (concept != null ? concept.strip() : "")
        + "\n\n"
        + (zh
            ? "请只输出一个 JSON 对象，字段：mode, headline, summary, steps[{title,content}], visualMotif, designerHint。不要 markdown。"
            : "Return exactly one JSON object with: mode, headline, summary, steps[{title,content}], visualMotif, designerHint. No markdown.");
  }
}
