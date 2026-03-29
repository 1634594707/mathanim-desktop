package com.mathanim.service;

import com.mathanim.config.AiProperties;
import com.mathanim.domain.AppSettings;
import org.springframework.stereotype.Service;

/**
 * 合并 {@code application.yml} 与 SQLite {@link AppSettings}：非空的数据库字段覆盖 yml（与 ManimCat
 * customApiConfig 思路一致）。
 */
@Service
public class AiEffectiveService {

  private final AiProperties aiProperties;
  private final AppSettingsService appSettingsService;

  public AiEffectiveService(AiProperties aiProperties, AppSettingsService appSettingsService) {
    this.aiProperties = aiProperties;
    this.appSettingsService = appSettingsService;
  }

  public String getBaseUrl() {
    AppSettings s = appSettingsService.getOrCreate();
    if (s.getBaseUrl() != null && !s.getBaseUrl().isBlank()) {
      return s.getBaseUrl().trim();
    }
    return aiProperties.getBaseUrl();
  }

  public String getApiKey() {
    AppSettings s = appSettingsService.getOrCreate();
    if (s.getApiKey() != null && !s.getApiKey().isBlank()) {
      return s.getApiKey().trim();
    }
    return aiProperties.getApiKey() != null ? aiProperties.getApiKey().trim() : "";
  }

  public String getModel() {
    AppSettings s = appSettingsService.getOrCreate();
    if (s.getModel() != null && !s.getModel().isBlank()) {
      return s.getModel().trim();
    }
    return aiProperties.getModel();
  }

  public int getMaxRetryPasses() {
    AppSettings s = appSettingsService.getOrCreate();
    if (s.getMaxRetryPasses() != null && s.getMaxRetryPasses() > 0) {
      return s.getMaxRetryPasses();
    }
    return aiProperties.getMaxRetryPasses();
  }

  /** 与 ManimCat {@code AI_MAX_TOKENS} 思路类似；须遵守各供应商上限（如 DeepSeek ≤8192）。0 表示不显式限制。 */
  public int getMaxCompletionTokens() {
    return aiProperties.getMaxCompletionTokens();
  }

  public String getPythonExecutable() {
    AppSettings s = appSettingsService.getOrCreate();
    if (s.getPythonExecutable() != null && !s.getPythonExecutable().isBlank()) {
      return s.getPythonExecutable().trim();
    }
    return aiProperties.getPythonExecutable();
  }

  public boolean isConfigured() {
    return apiKeyPresent(getApiKey());
  }

  private static boolean apiKeyPresent(String key) {
    return key != null && !key.isBlank();
  }
}
