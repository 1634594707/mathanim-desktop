package com.mathanim.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.service.AiEffectiveService;
import com.mathanim.util.AiResponseText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * OpenAI 兼容 Chat Completions：纯文本与 vision（参考图）多模态 user 消息。
 */
@Component
public class OpenAiChatClient {

  private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);

  private static final String VISION_SUFFIX =
      "\n\n你还会收到参考图片。请根据图片中的对象、结构与关系辅助设计。";

  private final AiEffectiveService aiEffectiveService;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();

  public OpenAiChatClient(AiEffectiveService aiEffectiveService, ObjectMapper objectMapper) {
    this.aiEffectiveService = aiEffectiveService;
    this.objectMapper = objectMapper;
  }

  public String chat(String systemPrompt, String userPrompt) {
    return completeChat(systemPrompt, userPrompt, List.of(), false);
  }

  /**
   * 带参考图；若上游不支持 vision，自动降级为纯文本重试一次（适合 Manim 参考图：宁可无图也要跑通）。
   */
  public String chatWithImages(String systemPrompt, String userText, List<ReferenceImageItem> images) {
    return chatWithImages(systemPrompt, userText, images, true);
  }

  /**
   * @param allowRetryWithoutImages 为 {@code false} 时， vision 请求失败将直接抛错，不会静默丢掉图片（GeoGebra
   *     题目图等场景必须看见原图）。
   */
  public String chatWithImages(
      String systemPrompt, String userText, List<ReferenceImageItem> images, boolean allowRetryWithoutImages) {
    if (images == null || images.isEmpty()) {
      return chat(systemPrompt, userText);
    }
    if (countNonBlankImageUrls(images) == 0) {
      throw new IllegalStateException("参考图数据无效（URL 为空），请重新选择图片");
    }
    try {
      return completeChat(systemPrompt, userText, images, true);
    } catch (RuntimeException e) {
      if (allowRetryWithoutImages && shouldRetryWithoutImages(e)) {
        log.warn("Vision 请求失败，已降级为纯文本重试（本次响应将看不到图片）: {}", summarizeErr(e));
        return completeChat(systemPrompt, userText, List.of(), false);
      }
      throw e;
    }
  }

  private static int countNonBlankImageUrls(List<ReferenceImageItem> images) {
    int n = 0;
    for (ReferenceImageItem img : images) {
      if (img != null && img.getUrl() != null && !img.getUrl().isBlank()) {
        n++;
      }
    }
    return n;
  }

  private static String summarizeErr(Throwable e) {
    String m = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    return m.length() <= 400 ? m : m.substring(0, 400) + "…";
  }

  /**
   * 用显式三元组探测 Chat Completions（极小 max_tokens），用于「测试连接」；不依赖 DB 中是否已保存。
   */
  public String probeChatCompletion(String baseUrl, String apiKey, String model) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("API Key 为空");
    }
    String m = model != null ? model.trim() : "";
    if (m.isEmpty()) {
      throw new IllegalStateException("Model 为空");
    }
    String base = baseUrl != null ? baseUrl.trim().replaceAll("/+$", "") : "";
    if (base.isEmpty()) {
      throw new IllegalStateException("Base URL 为空");
    }
    ObjectNode tuning = objectMapper.createObjectNode();
    tuning.put("max_tokens", 24);
    tuning.put("temperature", 0.0);
    return completeChatWithConfig(
        base,
        apiKey.trim(),
        m,
        "You reply as briefly as possible, no punctuation unless needed.",
        "Reply with exactly this single word: PONG",
        List.of(),
        false,
        tuning);
  }

  private boolean shouldRetryWithoutImages(Throwable e) {
    String msg = e.getMessage() != null ? e.getMessage() : "";
    if (msg.length() > 800) {
      msg = msg.substring(0, 800);
    }
    return java.util.regex.Pattern.compile("image|vision|multimodal|content.?part|unsupported", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(msg)
        .find();
  }

  private String completeChat(
      String systemPrompt, String userText, List<ReferenceImageItem> images, boolean appendVisionHint) {
    if (!aiEffectiveService.isConfigured()) {
      throw new IllegalStateException("未配置 API 密钥（application.yml 或「API 设置」页）");
    }
    ObjectNode tuning = null;
    int maxTok = aiEffectiveService.getMaxCompletionTokens();
    if (maxTok > 0) {
      tuning = objectMapper.createObjectNode();
      tuning.put("max_tokens", maxTok);
    }
    return completeChatWithConfig(
        aiEffectiveService.getBaseUrl().trim().replaceAll("/+$", ""),
        aiEffectiveService.getApiKey().trim(),
        aiEffectiveService.getModel().trim(),
        systemPrompt,
        userText,
        images,
        appendVisionHint,
        tuning);
  }

  private String completeChatWithConfig(
      String baseUrlNoTrailingSlash,
      String apiKey,
      String model,
      String systemPrompt,
      String userText,
      List<ReferenceImageItem> images,
      boolean appendVisionHint,
      ObjectNode extraRootFields) {

    String url = baseUrlNoTrailingSlash + "/chat/completions";

    ObjectNode root = objectMapper.createObjectNode();
    root.put("model", model);
    if (extraRootFields != null) {
      extraRootFields.fields().forEachRemaining(e -> root.set(e.getKey(), e.getValue()));
    }
    ArrayNode messages = root.putArray("messages");
    ObjectNode sys = messages.addObject();
    sys.put("role", "system");
    sys.put("content", systemPrompt);

    ObjectNode user = messages.addObject();
    user.put("role", "user");
    if (images == null || images.isEmpty()) {
      user.put("content", userText);
    } else {
      String text = appendVisionHint ? userText + VISION_SUFFIX : userText;
      ArrayNode parts = user.putArray("content");
      ObjectNode t = parts.addObject();
      t.put("type", "text");
      t.put("text", text);
      int appended = 0;
      for (ReferenceImageItem img : images) {
        if (img.getUrl() == null || img.getUrl().isBlank()) {
          continue;
        }
        ObjectNode ip = parts.addObject();
        ip.put("type", "image_url");
        ObjectNode iu = ip.putObject("image_url");
        iu.put("url", img.getUrl().trim());
        String d = img.getDetail();
        if (d != null && !d.isBlank()) {
          iu.put("detail", d);
        }
        appended++;
      }
      if (appended == 0) {
        throw new IllegalStateException("参考图列表非空但无有效 image_url，请重新选择图片");
      }
    }

    final String body;
    try {
      body = objectMapper.writeValueAsString(root);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(10))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

    final HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new IllegalStateException("AI 网络错误: " + e.getMessage(), e);
    }

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException(
          "AI 请求失败 HTTP " + response.statusCode() + ": " + truncate(response.body(), 2000));
    }

    try {
      JsonNode tree = objectMapper.readTree(response.body());
      String text = AiResponseText.extractTextFromChatCompletionRoot(tree);
      if (text == null || text.isBlank()) {
        throw new IllegalStateException(
            "AI 响应无有效文本内容，响应摘要: " + truncate(response.body(), 1200));
      }
      return text;
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException("AI 响应 JSON 解析失败", e);
    }
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max) + "…";
  }
}
