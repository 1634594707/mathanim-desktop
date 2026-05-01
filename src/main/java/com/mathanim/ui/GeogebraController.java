package com.mathanim.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class GeogebraController {

  @FXML private TextArea geogebraProblemInput;
  @FXML private Label geogebraImageStatusLabel;
  @FXML private Button geogebraGenerateButton;
  @FXML private Button geogebraPickImageButton;
  @FXML private TextArea geogebraCommandsArea;
  @FXML private WebView geogebraWebView;
  @FXML private CheckBox geogebraEnable3dCheck;
  private Consumer<String> actionHandler;

  public void setActionHandler(Consumer<String> actionHandler) { this.actionHandler = actionHandler; }

  @FXML
  private void onGeogebraPickImage() { dispatch("onGeogebraPickImage"); }

  @FXML
  private void onGeogebraClearImage() { dispatch("onGeogebraClearImage"); }

  @FXML
  private void onGeogebraGenerate() { dispatch("onGeogebraGenerate"); }

  @FXML
  private void onGeogebraCopyCommands() { dispatch("onGeogebraCopyCommands"); }

  @FXML
  private void onGeogebraReapplyCommands() { dispatch("onGeogebraReapplyCommands"); }

  @FXML
  private void onGeogebraReloadHost() { dispatch("onGeogebraReloadHost"); }

  private void dispatch(String action) {
    if (actionHandler != null) {
      actionHandler.accept(action);
    }
  }

  public TextArea getGeogebraProblemInput() { return geogebraProblemInput; }
  public Label getGeogebraImageStatusLabel() { return geogebraImageStatusLabel; }
  public Button getGeogebraGenerateButton() { return geogebraGenerateButton; }
  public Button getGeogebraPickImageButton() { return geogebraPickImageButton; }
  public TextArea getGeogebraCommandsArea() { return geogebraCommandsArea; }
  public WebView getGeogebraWebView() { return geogebraWebView; }
  public CheckBox getGeogebraEnable3dCheck() { return geogebraEnable3dCheck; }
}
