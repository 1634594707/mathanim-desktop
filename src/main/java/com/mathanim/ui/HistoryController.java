package com.mathanim.ui;

import com.mathanim.domain.RenderJob;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class HistoryController {

  @FXML private ListView<RenderJob> jobList;
  @FXML private TextField jobSearchField;
  @FXML private ChoiceBox<String> jobFilterChoice;
  @FXML private Button jobToggleFavoriteButton;
  @FXML private HBox batchOperationBar;
  @FXML private Label batchSelectionLabel;
  @FXML private Label historyCountLabel;
  private Consumer<String> actionHandler;

  public void setActionHandler(Consumer<String> actionHandler) { this.actionHandler = actionHandler; }

  @FXML
  private void onViewJobConcept() { dispatch("onViewJobConcept"); }

  @FXML
  private void onCopyJobConcept() { dispatch("onCopyJobConcept"); }

  @FXML
  private void onDeleteJob() { dispatch("onDeleteJob"); }

  @FXML
  private void onToggleFavorite() { dispatch("onToggleFavorite"); }

  @FXML
  private void onBatchDelete() { dispatch("onBatchDelete"); }

  @FXML
  private void onBatchRetry() { dispatch("onBatchRetry"); }

  @FXML
  private void onBatchRetryFallback() { dispatch("onBatchRetryFallback"); }

  private void dispatch(String action) {
    if (actionHandler != null) {
      actionHandler.accept(action);
    }
  }

  public ListView<RenderJob> getJobList() { return jobList; }
  public TextField getJobSearchField() { return jobSearchField; }
  public ChoiceBox<String> getJobFilterChoice() { return jobFilterChoice; }
  public Button getJobToggleFavoriteButton() { return jobToggleFavoriteButton; }
  public HBox getBatchOperationBar() { return batchOperationBar; }
  public Label getBatchSelectionLabel() { return batchSelectionLabel; }
  public Label getHistoryCountLabel() { return historyCountLabel; }
}
