package com.mathanim.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemFramingPlan {
  private String mode;
  private String headline;
  private String summary;
  private List<ProblemFramingStep> steps = new ArrayList<>();
  private String visualMotif;
  private String designerHint;

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getHeadline() {
    return headline;
  }

  public void setHeadline(String headline) {
    this.headline = headline;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public List<ProblemFramingStep> getSteps() {
    return steps;
  }

  public void setSteps(List<ProblemFramingStep> steps) {
    this.steps = steps != null ? steps : new ArrayList<>();
  }

  public String getVisualMotif() {
    return visualMotif;
  }

  public void setVisualMotif(String visualMotif) {
    this.visualMotif = visualMotif;
  }

  public String getDesignerHint() {
    return designerHint;
  }

  public void setDesignerHint(String designerHint) {
    this.designerHint = designerHint;
  }
}
