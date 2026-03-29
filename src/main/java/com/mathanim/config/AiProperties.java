package com.mathanim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI 兼容接口（/v1/chat/completions），与 ManimCat 的 customApiConfig 一致思路。
 */
@ConfigurationProperties(prefix = "mathanim.ai")
public class AiProperties {

  /** 例如 https://api.openai.com/v1 或国内兼容网关根路径（不含 /chat/completions）。 */
  private String baseUrl = "https://api.openai.com/v1";

  private String apiKey = "";

  private String model = "gpt-4o-mini";

  /** 用于 py_compile，与 Manim 所用 Python 环境一致为佳。 */
  private String pythonExecutable = "python";

  /**
   * 静态检查或 Manim 渲染失败时，最多进行多少轮「生成/修补」（含首轮生成）。
   * 与 ManimCat 中多轮 AI 修补思路一致。
   */
  private int maxRetryPasses = 3;

  /**
   * 每次 Chat Completions 请求的 {@code max_tokens}。DeepSeek 等平台常见上限为 8192，超过会 HTTP 400；其它供应商若允许更大可
   * 在此调高。设为 0 表示不发送该字段，由上游默认。
   */
  private int maxCompletionTokens = 8192;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getPythonExecutable() {
    return pythonExecutable;
  }

  public void setPythonExecutable(String pythonExecutable) {
    this.pythonExecutable = pythonExecutable;
  }

  public int getMaxRetryPasses() {
    return maxRetryPasses;
  }

  public void setMaxRetryPasses(int maxRetryPasses) {
    this.maxRetryPasses = Math.max(1, maxRetryPasses);
  }

  public int getMaxCompletionTokens() {
    return maxCompletionTokens;
  }

  public void setMaxCompletionTokens(int maxCompletionTokens) {
    this.maxCompletionTokens = Math.max(0, maxCompletionTokens);
  }

  public boolean isConfigured() {
    return apiKey != null && !apiKey.isBlank();
  }
}
