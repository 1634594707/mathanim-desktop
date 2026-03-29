package com.mathanim.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/** AI 输出的 GeoGebra 作图计划：按顺序送给 {@code evalCommand} 的指令列表。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeogebraCommandPlan {

  private List<String> commands = new ArrayList<>();
  private String notes;

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands != null ? commands : new ArrayList<>();
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
