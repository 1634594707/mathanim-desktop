package com.mathanim.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 与 ManimCat {@code PromptOverrides.roles} 对齐（subset）。
 *
 * <p>角色键名请使用 {@link PromptRoleKeys} 常量；语义上对应 golutra 式「多成员 handoff」——每个 role 一套
 * system/user 覆盖，由流水线按阶段选用，见 {@code prompts/PIPELINE.md}。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptOverridesDto {

  private Map<String, RolePromptOverride> roles = new HashMap<>();
  private Map<String, String> shared = new HashMap<>();

  public Map<String, RolePromptOverride> getRoles() {
    return roles;
  }

  public void setRoles(Map<String, RolePromptOverride> roles) {
    this.roles = roles != null ? roles : new HashMap<>();
  }

  public Map<String, String> getShared() {
    return shared;
  }

  public void setShared(Map<String, String> shared) {
    this.shared = shared != null ? shared : new HashMap<>();
  }

  public RolePromptOverride getRole(String key) {
    return roles != null ? roles.get(key) : null;
  }
}
