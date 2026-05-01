package com.mathanim.ui;

import com.mathanim.domain.OutputMode;
import com.mathanim.domain.VideoQuality;
import com.mathanim.examples.ExampleBankEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class StudioController {

  @FXML private ToggleButton modeDirectToggle;
  @FXML private ToggleButton modeTutorToggle;
  @FXML private VBox directModePane;
  @FXML private VBox tutorModePane;
  @FXML private TextArea conceptInput;
  @FXML private Label conceptCharCount;
  @FXML private TextArea problemInput;
  @FXML private TextArea solutionInput;
  @FXML private TextArea answerInput;
  @FXML private ComboBox<ExampleBankEntry> exampleCombo;
  @FXML private ChoiceBox<OutputMode> outputModeChoice;
  @FXML private ChoiceBox<VideoQuality> videoQualityChoice;
  @FXML private ChoiceBox<String> promptLocaleChoice;
  @FXML private CheckBox useProblemFramingCheck;
  @FXML private CheckBox useTwoStageCheck;
  @FXML private CheckBox reliableGenerationCheck;
  @FXML private CheckBox allowFallbackModeCheck;
  @FXML private Button oneClickGenerateButton;
  @FXML private TextArea referenceUrlsInput;
  @FXML private TextArea promptOverridesJsonInput;
  @FXML private TextArea problemPlanPreview;
  @FXML private TextArea codeInput;
  @FXML private TextArea editInstructionsInput;
  @FXML private Label studioStatusLabel;
  private Consumer<String> actionHandler;

  public void setActionHandler(Consumer<String> actionHandler) { this.actionHandler = actionHandler; }

  @FXML
  private void onOneClickGenerate() { dispatch("onOneClickGenerate"); }

  @FXML
  private void onEnqueue() { dispatch("onEnqueue"); }

  @FXML
  private void onCheckManim() { dispatch("onCheckManim"); }

  @FXML
  private void onProblemFramingPreview() { dispatch("onProblemFramingPreview"); }

  @FXML
  private void onAiWorkflow() { dispatch("onAiWorkflow"); }

  @FXML
  private void onRunBuiltinTest() { dispatch("onRunBuiltinTest"); }

  @FXML
  private void onCancelJob() { dispatch("onCancelJob"); }

  @FXML
  private void onModifyAndRun() { dispatch("onModifyAndRun"); }

  private void dispatch(String action) {
    if (actionHandler != null) {
      actionHandler.accept(action);
    }
  }

  public ToggleButton getModeDirectToggle() { return modeDirectToggle; }
  public ToggleButton getModeTutorToggle() { return modeTutorToggle; }
  public VBox getDirectModePane() { return directModePane; }
  public VBox getTutorModePane() { return tutorModePane; }
  public TextArea getConceptInput() { return conceptInput; }
  public Label getConceptCharCount() { return conceptCharCount; }
  public TextArea getProblemInput() { return problemInput; }
  public TextArea getSolutionInput() { return solutionInput; }
  public TextArea getAnswerInput() { return answerInput; }
  public ComboBox<ExampleBankEntry> getExampleCombo() { return exampleCombo; }
  public ChoiceBox<OutputMode> getOutputModeChoice() { return outputModeChoice; }
  public ChoiceBox<VideoQuality> getVideoQualityChoice() { return videoQualityChoice; }
  public ChoiceBox<String> getPromptLocaleChoice() { return promptLocaleChoice; }
  public CheckBox getUseProblemFramingCheck() { return useProblemFramingCheck; }
  public CheckBox getUseTwoStageCheck() { return useTwoStageCheck; }
  public CheckBox getReliableGenerationCheck() { return reliableGenerationCheck; }
  public CheckBox getAllowFallbackModeCheck() { return allowFallbackModeCheck; }
  public Button getOneClickGenerateButton() { return oneClickGenerateButton; }
  public TextArea getReferenceUrlsInput() { return referenceUrlsInput; }
  public TextArea getPromptOverridesJsonInput() { return promptOverridesJsonInput; }
  public TextArea getProblemPlanPreview() { return problemPlanPreview; }
  public TextArea getCodeInput() { return codeInput; }
  public TextArea getEditInstructionsInput() { return editInstructionsInput; }
  public Label getStudioStatusLabel() { return studioStatusLabel; }
}
