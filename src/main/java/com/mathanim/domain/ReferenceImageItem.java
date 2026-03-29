package com.mathanim.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** OpenAI 兼容 vision：url 为 http(s) 或 data:image/...;base64,... */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceImageItem {
  private String url;
  /** auto | low | high */
  private String detail = "auto";

  public ReferenceImageItem() {}

  public ReferenceImageItem(String url, String detail) {
    this.url = url;
    this.detail = detail != null ? detail : "auto";
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }
}
