package com.mathanim.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 单例行（id=1）：桌面内覆盖 application.yml 中的 AI 与 Python 路径。 */
@Entity
@Table(name = "app_settings")
public class AppSettings {

  public static final long SINGLETON_ID = 1L;

  @Id
  private Long id = SINGLETON_ID;

  @Column(length = 512)
  private String baseUrl;

  @Column(length = 2048)
  private String apiKey;

  @Column(length = 256)
  private String model;

  /** 非空且大于 0 时覆盖 yml 的 max-retry-passes。 */
  private Integer maxRetryPasses;

  @Column(length = 256)
  private String pythonExecutable;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Integer getMaxRetryPasses() {
    return maxRetryPasses;
  }

  public void setMaxRetryPasses(Integer maxRetryPasses) {
    this.maxRetryPasses = maxRetryPasses;
  }

  public String getPythonExecutable() {
    return pythonExecutable;
  }

  public void setPythonExecutable(String pythonExecutable) {
    this.pythonExecutable = pythonExecutable;
  }
}
