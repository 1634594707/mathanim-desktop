package com.mathanim.util;

import com.fasterxml.jackson.databind.JsonNode;

/** 解析 Chat Completions 中 message.content（字符串或多段）。 */
public final class AiResponseText {

  private AiResponseText() {}

  public static String extractAssistantText(JsonNode message) {
    if (message == null || message.isMissingNode()) {
      return "";
    }
    String contentText = extractNodeText(message.get("content"));
    if (!contentText.isBlank()) {
      return contentText;
    }
    String reasoningText = extractNodeText(message.get("reasoning_content"));
    if (!reasoningText.isBlank()) {
      return reasoningText;
    }
    return extractNodeText(message.get("text"));
  }

  public static String extractTextFromChatCompletionRoot(JsonNode root) {
    if (root == null || root.isMissingNode()) {
      return "";
    }
    String fromChoiceMessage = extractAssistantText(root.path("choices").path(0).path("message"));
    if (!fromChoiceMessage.isBlank()) {
      return fromChoiceMessage;
    }
    String fromChoiceText = extractNodeText(root.path("choices").path(0).get("text"));
    if (!fromChoiceText.isBlank()) {
      return fromChoiceText;
    }
    String outputText = extractNodeText(root.get("output_text"));
    if (!outputText.isBlank()) {
      return outputText;
    }
    JsonNode output = root.get("output");
    if (output != null && output.isArray()) {
      StringBuilder sb = new StringBuilder();
      for (JsonNode item : output) {
        String itemText = extractNodeText(item.get("content"));
        if (!itemText.isBlank()) {
          if (sb.length() > 0) {
            sb.append('\n');
          }
          sb.append(itemText);
        }
      }
      return sb.toString().strip();
    }
    return "";
  }

  private static String extractNodeText(JsonNode content) {
    if (content == null || content.isNull() || content.isMissingNode()) {
      return "";
    }
    if (content.isTextual()) {
      return content.asText();
    }
    if (content.isArray()) {
      StringBuilder sb = new StringBuilder();
      for (JsonNode part : content) {
        String type = part.path("type").asText("");
        if ("text".equals(type) || "output_text".equals(type) || type.isBlank()) {
          String partText = extractNodeText(part.get("text"));
          if (partText.isBlank()) {
            partText = extractNodeText(part.get("content"));
          }
          if (!partText.isBlank()) {
            sb.append(partText);
          }
        }
      }
      return sb.toString().strip();
    }
    if (content.isObject()) {
      if (content.hasNonNull("value")) {
        return extractNodeText(content.get("value"));
      }
      if (content.hasNonNull("text")) {
        return extractNodeText(content.get("text"));
      }
      if (content.hasNonNull("content")) {
        return extractNodeText(content.get("content"));
      }
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
