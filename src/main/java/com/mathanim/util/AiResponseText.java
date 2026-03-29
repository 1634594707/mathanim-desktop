package com.mathanim.util;

import com.fasterxml.jackson.databind.JsonNode;

/** 解析 Chat Completions 中 message.content（字符串或多段）。 */
public final class AiResponseText {

  private AiResponseText() {}

  public static String extractAssistantText(JsonNode message) {
    if (message == null || message.isMissingNode()) {
      return "";
    }
    JsonNode content = message.get("content");
    if (content == null || content.isNull()) {
      return "";
    }
    if (content.isTextual()) {
      return content.asText();
    }
    if (content.isArray()) {
      StringBuilder sb = new StringBuilder();
      for (JsonNode part : content) {
        if (part.path("type").asText().equals("text")) {
          sb.append(part.path("text").asText(""));
        }
      }
      return sb.toString().strip();
    }
    return "";
  }

  public static String extractJsonObject(String text) {
    String cleaned = stripCodeFence(text.strip());
    int start = cleaned.indexOf('{');
    int end = cleaned.lastIndexOf('}');
    if (start == -1 || end == -1 || end <= start) {
      throw new IllegalStateException("响应中未找到 JSON 对象");
    }
    return cleaned.substring(start, end + 1);
  }

  public static String stripCodeFence(String t) {
    String s = t.strip();
    if (s.startsWith("```json")) {
      s = s.substring(7).strip();
    } else if (s.startsWith("```")) {
      s = s.substring(3).strip();
    }
    if (s.endsWith("```")) {
      s = s.substring(0, s.length() - 3).strip();
    }
    return s;
  }

  public static String extractDesign(String text) {
    if (text == null) {
      return "";
    }
    var m = java.util.regex.Pattern.compile("<design>([\\s\\S]*?)</design>", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(text);
    if (m.find()) {
      return m.group(1).strip();
    }
    return text.strip();
  }

  public static String extractCodeAnchors(String text) {
    if (text == null) {
      return "";
    }
    var m = java.util.regex.Pattern.compile("### START ###([\\s\\S]*?)### END ###").matcher(text);
    if (m.find()) {
      return m.group(1).strip();
    }
    return stripMarkdownCode(text);
  }

  private static String stripMarkdownCode(String text) {
    var m = java.util.regex.Pattern.compile("```(?:python)?([\\s\\S]*?)```", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(text);
    if (m.find()) {
      return m.group(1).strip();
    }
    return text.strip();
  }
}
