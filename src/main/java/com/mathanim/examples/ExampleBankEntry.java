package com.mathanim.examples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleBankEntry {

  public static ExampleBankEntry placeholderNone() {
    ExampleBankEntry e = new ExampleBankEntry();
    e.id = "__none__";
    e.title = "（不套用例题）";
    e.inputMode = CreationInputMode.DIRECT_PROMPT;
    return e;
  }

  private String id;
  /** 相对仓库根的 Manim 参考脚本，供提示/人工对照；可为空。 */
  private String manimExamplePath;
  private String title;
  private CreationInputMode inputMode;
  private String problem;
  private String solution;
  private String answer;
  private String directPrompt;

  public boolean isPlaceholderNone() {
    return "__none__".equals(id);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getManimExamplePath() {
    return manimExamplePath;
  }

  public void setManimExamplePath(String manimExamplePath) {
    this.manimExamplePath = manimExamplePath;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public CreationInputMode getInputMode() {
    return inputMode;
  }

  public void setInputMode(CreationInputMode inputMode) {
    this.inputMode = inputMode;
  }

  public String getProblem() {
    return problem;
  }

  public void setProblem(String problem) {
    this.problem = problem;
  }

  public String getSolution() {
    return solution;
  }

  public void setSolution(String solution) {
    this.solution = solution;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getDirectPrompt() {
    return directPrompt;
  }

  public void setDirectPrompt(String directPrompt) {
    this.directPrompt = directPrompt;
  }
}
