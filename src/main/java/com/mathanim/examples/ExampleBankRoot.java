package com.mathanim.examples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleBankRoot {

  private List<ExampleBankEntry> examples;

  public List<ExampleBankEntry> getExamples() {
    return examples;
  }

  public void setExamples(List<ExampleBankEntry> examples) {
    this.examples = examples;
  }
}
