package com.mathanim.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OutputController {

  @FXML private Label outputStatusLabel;
  @FXML private HBox outputProgressBox;
  @FXML private ProgressBar outputProgressBar;
  @FXML private Label outputProgressLabel;
  @FXML private Label outputFallbackBadgeLabel;
  @FXML private Label outputSummaryStateLabel;
  @FXML private Label outputSummaryNextStepLabel;
  @FXML private Label outputSummaryCountLabel;
  @FXML private StackPane outputPreviewStack;
  @FXML private MediaView outputMediaView;
  @FXML private ScrollPane outputImageScroll;
  @FXML private ImageView outputImageView;
  @FXML private Label outputPreviewPlaceholder;
  @FXML private Label outputPreviewPathLabel;
  @FXML private Button outputPreviewFullscreenButton;
  @FXML private VBox outputVideoTransport;
  @FXML private Slider outputPreviewSeek;
  @FXML private Label outputPreviewTimeLabel;
  @FXML private Button outputPreviewPlayToggle;
  @FXML private VBox outputFailureCard;
  @FXML private Label outputFailureMetaLabel;
  @FXML private Label outputFailureSummaryLabel;
  @FXML private TextArea outputLogArea;
  @FXML private HistoryController historyPaneController;
  private Consumer<String> actionHandler;

  public void setActionHandler(Consumer<String> actionHandler) { this.actionHandler = actionHandler; }

  @FXML
  private void onOutputPreviewFullscreen() { dispatch("onOutputPreviewFullscreen"); }

  @FXML
  private void onPreviewPlayToggle() { dispatch("onPreviewPlayToggle"); }

  @FXML
  private void onCopyOutputLog() { dispatch("onCopyOutputLog"); }

  @FXML
  private void onClearOutputLog() { dispatch("onClearOutputLog"); }

  private void dispatch(String action) {
    if (actionHandler != null) {
      actionHandler.accept(action);
    }
  }

  public Label getOutputStatusLabel() { return outputStatusLabel; }
  public HBox getOutputProgressBox() { return outputProgressBox; }
  public ProgressBar getOutputProgressBar() { return outputProgressBar; }
  public Label getOutputProgressLabel() { return outputProgressLabel; }
  public Label getOutputFallbackBadgeLabel() { return outputFallbackBadgeLabel; }
  public Label getOutputSummaryStateLabel() { return outputSummaryStateLabel; }
  public Label getOutputSummaryNextStepLabel() { return outputSummaryNextStepLabel; }
  public Label getOutputSummaryCountLabel() { return outputSummaryCountLabel; }
  public StackPane getOutputPreviewStack() { return outputPreviewStack; }
  public MediaView getOutputMediaView() { return outputMediaView; }
  public ScrollPane getOutputImageScroll() { return outputImageScroll; }
  public ImageView getOutputImageView() { return outputImageView; }
  public Label getOutputPreviewPlaceholder() { return outputPreviewPlaceholder; }
  public Label getOutputPreviewPathLabel() { return outputPreviewPathLabel; }
  public Button getOutputPreviewFullscreenButton() { return outputPreviewFullscreenButton; }
  public VBox getOutputVideoTransport() { return outputVideoTransport; }
  public Slider getOutputPreviewSeek() { return outputPreviewSeek; }
  public Label getOutputPreviewTimeLabel() { return outputPreviewTimeLabel; }
  public Button getOutputPreviewPlayToggle() { return outputPreviewPlayToggle; }
  public VBox getOutputFailureCard() { return outputFailureCard; }
  public Label getOutputFailureMetaLabel() { return outputFailureMetaLabel; }
  public Label getOutputFailureSummaryLabel() { return outputFailureSummaryLabel; }
  public TextArea getOutputLogArea() { return outputLogArea; }
  public HistoryController getHistoryPaneController() { return historyPaneController; }
}
