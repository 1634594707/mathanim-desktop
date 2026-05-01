package com.mathanim.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiResponseTextTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldExtractClassicChatCompletionContent() throws Exception {
    JsonNode root = objectMapper.readTree("""
        {
          "choices": [
            {
              "message": {
                "content": "print('ok')"
              }
            }
          ]
        }
        """);

    assertEquals("print('ok')", AiResponseText.extractTextFromChatCompletionRoot(root));
  }

  @Test
  void shouldExtractMultipartTextContent() throws Exception {
    JsonNode root = objectMapper.readTree("""
        {
          "choices": [
            {
              "message": {
                "content": [
                  {"type": "text", "text": "line1"},
                  {"type": "text", "text": "line2"}
                ]
              }
            }
          ]
        }
        """);

    assertEquals("line1line2", AiResponseText.extractTextFromChatCompletionRoot(root));
  }

  @Test
  void shouldFallbackToReasoningContentWhenContentEmpty() throws Exception {
    JsonNode root = objectMapper.readTree("""
        {
          "choices": [
            {
              "message": {
                "content": "",
                "reasoning_content": "fallback text"
              }
            }
          ]
        }
        """);

    assertEquals("fallback text", AiResponseText.extractTextFromChatCompletionRoot(root));
  }

  @Test
  void shouldExtractResponsesApiStyleOutputText() throws Exception {
    JsonNode root = objectMapper.readTree("""
        {
          "output": [
            {
              "content": [
                {"type": "output_text", "text": "generated code"}
              ]
            }
          ]
        }
        """);

    assertEquals("generated code", AiResponseText.extractTextFromChatCompletionRoot(root));
  }
}
