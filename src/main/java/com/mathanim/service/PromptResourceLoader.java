package com.mathanim.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class PromptResourceLoader {

  public String load(String classpathLocation) {
    try {
      ClassPathResource res = new ClassPathResource(classpathLocation);
      return StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("缺少提示词资源: " + classpathLocation, e);
    }
  }

  /** 简单 {{key}} 替换；{{apiIndexModule}}、{{sharedSpecification}} 置空（桌面端）。 */
  public String applyPlaceholders(String template, Map<String, String> values) {
    String out = template;
    out = out.replace("{{apiIndexModule}}", "");
    out = out.replace("{{sharedSpecification}}", "");
    if (values != null) {
      for (Map.Entry<String, String> e : values.entrySet()) {
        out = out.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
      }
    }
    return out;
  }
}
