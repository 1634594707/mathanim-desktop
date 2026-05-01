package com.mathanim.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class SettingsController {

  @FXML private TextField settingsBaseUrl;
  @FXML private PasswordField settingsApiKey;
  @FXML private TextField settingsModel;
  @FXML private TextField settingsMaxRetry;
  @FXML private TextField settingsPython;
  @FXML private Button settingsTestAiButton;
  private Consumer<String> actionHandler;

  public void setActionHandler(Consumer<String> actionHandler) { this.actionHandler = actionHandler; }

  @FXML
  private void onSaveSettings() { dispatch("onSaveSettings"); }

  @FXML
  private void onReloadSettings() { dispatch("onReloadSettings"); }

  @FXML
  private void onTestAiConnection() { dispatch("onTestAiConnection"); }

  private void dispatch(String action) {
    if (actionHandler != null) {
      actionHandler.accept(action);
    }
  }

  public TextField getSettingsBaseUrl() { return settingsBaseUrl; }
  public PasswordField getSettingsApiKey() { return settingsApiKey; }
  public TextField getSettingsModel() { return settingsModel; }
  public TextField getSettingsMaxRetry() { return settingsMaxRetry; }
  public TextField getSettingsPython() { return settingsPython; }
  public Button getSettingsTestAiButton() { return settingsTestAiButton; }
}
