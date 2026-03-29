package com.mathanim.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.domain.GeogebraCommandPlan;
import com.mathanim.domain.JobKind;
import com.mathanim.domain.JobStatus;
import com.mathanim.domain.OutputMode;
import com.mathanim.domain.ProcessingStage;
import com.mathanim.domain.ReferenceImageItem;
import com.mathanim.config.AiProperties;
import com.mathanim.domain.RenderJob;
import com.mathanim.domain.VideoQuality;
import com.mathanim.examples.CreationInputMode;
import com.mathanim.examples.ExampleBankEntry;
import com.mathanim.service.AiEffectiveService;
import com.mathanim.service.AppSettingsService;
import com.mathanim.service.ExampleBankService;
import com.mathanim.service.GeogebraAiService;
import com.mathanim.service.RenderJobService;
import com.mathanim.util.ReferenceImagesParser;
import com.mathanim.util.TutorConceptFormat;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class MainViewController implements Initializable {

  private static final Logger log = LoggerFactory.getLogger(MainViewController.class);

  private final RenderJobService renderJobService;
  private final AppSettingsService appSettingsService;
  private final AiEffectiveService aiEffectiveService;
  private final AiProperties aiProperties;
  private final ObjectMapper objectMapper;
  private final ExampleBankService exampleBankService;
  private final GeogebraAiService geogebraAiService;
  private final ExecutorService manimExecutorService;

  @FXML private TextArea conceptInput;
  @FXML private ToggleButton modeDirectToggle;
  @FXML private ToggleButton modeTutorToggle;
  @FXML private VBox directModePane;
  @FXML private VBox tutorModePane;
  @FXML private TextArea problemInput;
  @FXML private TextArea solutionInput;
  @FXML private TextArea answerInput;
  @FXML private ComboBox<ExampleBankEntry> exampleCombo;
  @FXML private ListView<RenderJob> jobList;
  @FXML private ChoiceBox<OutputMode> outputModeChoice;
  @FXML private ChoiceBox<VideoQuality> videoQualityChoice;
  @FXML private ChoiceBox<String> promptLocaleChoice;
  @FXML private CheckBox useProblemFramingCheck;
  @FXML private CheckBox useTwoStageCheck;
  @FXML private CheckBox reliableGenerationCheck;
  @FXML private Button oneClickGenerateButton;
  @FXML private TextArea referenceUrlsInput;
  @FXML private TextArea promptOverridesJsonInput;
  @FXML private TextArea problemPlanPreview;
  @FXML private TextArea codeInput;
  @FXML private TextArea editInstructionsInput;

  @FXML private Label studioStatusLabel;
  @FXML private Label outputStatusLabel;
  @FXML private Label historyCountLabel;

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

  private Stage outputFullscreenStage;
  private MediaView outputFullscreenMediaView;

  @FXML private TextField settingsBaseUrl;
  @FXML private PasswordField settingsApiKey;
  @FXML private TextField settingsModel;
  @FXML private TextField settingsMaxRetry;
  @FXML private TextField settingsPython;
  @FXML private Button settingsTestAiButton;

  @FXML private Tab geogebraTab;
  @FXML private TextArea geogebraProblemInput;
  @FXML private Label geogebraImageStatusLabel;
  @FXML private Button geogebraGenerateButton;
  @FXML private Button geogebraPickImageButton;
  @FXML private TextArea geogebraCommandsArea;
  @FXML private WebView geogebraWebView;
  @FXML private CheckBox geogebraEnable3dCheck;

  @FXML private HBox windowDragRegion;

  /** GeoGebra 页：题目附图（可选）。 */
  private ReferenceImageItem geogebraRefImage;
  /** 待注入到嵌入 GeoGebra 的指令（在页面 load/reload 成功后执行）。 */
  private List<String> pendingGeogebraCommands;
  /** 是否已为 WebEngine 挂上 load 监听（GGBPuppy：destroy 容器后监听器仍可复用）。 */
  private volatile boolean geogebraWebLoadListenerAttached;
  /** 是否已向 WebView 写入过首屏 GeoGebra 宿主 HTML。 */
  private volatile boolean geogebraHostContentLoadedOnce;
  private volatile boolean geogebraHtmlLoadedOnce;
  @FXML private Region resizeGrip;
  @FXML private Region resizeEdgeLeft;
  @FXML private Region resizeEdgeRight;
  @FXML private Region resizeEdgeBottom;
  @FXML private Region resizeCornerBL;

  private MediaPlayer outputMediaPlayer;
  private Media previewMediaAttached;
  private ChangeListener<Duration> previewMediaDurationListener;
  private ChangeListener<Duration> previewTimeListener;
  private ChangeListener<Boolean> previewSeekValueChangingListener;
  private ChangeListener<Number> previewSeekValueListener;
  private ChangeListener<MediaPlayer.Status> previewStatusListener;

  private Stage stage;
  private double dragOffsetX;
  private double dragOffsetY;
  private double resizeStartW;
  private double resizeStartH;
  private double resizeStartScreenX;
  private double resizeStartScreenY;
  private double resizeStartWinX;

  public MainViewController(
      RenderJobService renderJobService,
      AppSettingsService appSettingsService,
      AiEffectiveService aiEffectiveService,
      AiProperties aiProperties,
      ObjectMapper objectMapper,
      ExampleBankService exampleBankService,
      GeogebraAiService geogebraAiService,
      ExecutorService manimExecutorService) {
    this.renderJobService = renderJobService;
    this.appSettingsService = appSettingsService;
    this.aiEffectiveService = aiEffectiveService;
    this.aiProperties = aiProperties;
    this.objectMapper = objectMapper;
    this.exampleBankService = exampleBankService;
    this.geogebraAiService = geogebraAiService;
    this.manimExecutorService = manimExecutorService;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    outputModeChoice.setItems(FXCollections.observableArrayList(OutputMode.values()));
    outputModeChoice.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(OutputMode o) {
            if (o == null) {
              return "";
            }
            return o == OutputMode.VIDEO ? "视频" : "图像";
          }

          @Override
          public OutputMode fromString(String s) {
            return null;
          }
        });
    outputModeChoice.getSelectionModel().select(OutputMode.VIDEO);

    videoQualityChoice.setItems(FXCollections.observableArrayList(VideoQuality.values()));
    videoQualityChoice.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(VideoQuality q) {
            if (q == null) {
              return "";
            }
            return switch (q) {
              case LOW -> "480p（快）";
              case MEDIUM -> "720p（推荐）";
              case HIGH -> "1080p";
            };
          }

          @Override
          public VideoQuality fromString(String s) {
            return null;
          }
        });
    videoQualityChoice.getSelectionModel().select(VideoQuality.MEDIUM);

    promptLocaleChoice.setItems(FXCollections.observableArrayList("zh-CN", "en-US"));
    promptLocaleChoice.getSelectionModel().select("zh-CN");

    useTwoStageCheck.setSelected(true);
    if (reliableGenerationCheck != null) {
      reliableGenerationCheck.setSelected(true);
    }

    if (oneClickGenerateButton != null) {
      Tooltip tip = new Tooltip("与 Ctrl+Enter 相同：入队并开始 AI + Manim 渲染");
      tip.setShowDelay(Duration.millis(400));
      oneClickGenerateButton.setTooltip(tip);
    }
    if (settingsTestAiButton != null) {
      Tooltip testTip =
          new Tooltip(
              "仅检测一条纯文本能否返回（与题目图/多模态无关）。GeoGebra 带图需在服务商处选用支持视觉的模型。");
      testTip.setShowDelay(Duration.millis(350));
      settingsTestAiButton.setTooltip(testTip);
    }

    if (exampleCombo != null) {
      exampleCombo.setItems(
          FXCollections.observableArrayList(exampleBankService.listForCombo()));
      exampleCombo.setConverter(
          new StringConverter<>() {
            @Override
            public String toString(ExampleBankEntry e) {
              return e == null ? "" : e.getTitle();
            }

            @Override
            public ExampleBankEntry fromString(String s) {
              return null;
            }
          });
      exampleCombo.setCellFactory(
          lv ->
              new ListCell<>() {
                @Override
                protected void updateItem(ExampleBankEntry item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty || item == null) {
                    setText(null);
                  } else {
                    setText(item.getTitle());
                  }
                }
              });
      ListCell<ExampleBankEntry> comboButtonCell =
          new ListCell<>() {
            @Override
            protected void updateItem(ExampleBankEntry item, boolean empty) {
              super.updateItem(item, empty);
              if (empty || item == null) {
                setText(null);
              } else {
                setText(item.getTitle());
              }
            }
          };
      exampleCombo.setButtonCell(comboButtonCell);
      exampleCombo.getSelectionModel().selectFirst();
      exampleCombo
          .getSelectionModel()
          .selectedItemProperty()
          .addListener(
              (obs, prev, selected) -> {
                Tooltip tip = new Tooltip();
                tip.setShowDelay(Duration.millis(300));
                tip.setWrapText(true);
                tip.setMaxWidth(420);
                if (selected == null || selected.isPlaceholderNone()) {
                  tip.setText("从例题库快速填入左侧表单");
                  exampleCombo.setTooltip(tip);
                  return;
                }
                String ttext = "从例题库快速填入左侧表单";
                if (selected.getManimExamplePath() != null
                    && !selected.getManimExamplePath().isBlank()) {
                  ttext +=
                      "\nManim 示例："
                          + selected.getManimExamplePath()
                          + "（相对 mathanim-desktop 项目根目录）";
                }
                tip.setText(ttext);
                exampleCombo.setTooltip(tip);
                applyExampleBankEntry(selected);
              });
    }

    if (modeDirectToggle != null) {
      modeDirectToggle
          .selectedProperty()
          .addListener((obs, was, now) -> updateCreationModeVisibility());
    }
    if (modeTutorToggle != null) {
      modeTutorToggle
          .selectedProperty()
          .addListener((obs, was, now) -> updateCreationModeVisibility());
    }
    updateCreationModeVisibility();

    jobList.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(RenderJob item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setTooltip(null);
                } else {
                  String st = shortStatus(item.getStatus());
                  String line = st;
                  if (item.getProcessingStage() != null && item.getProcessingStage() != ProcessingStage.NONE) {
                    line += " · " + item.getProcessingStage();
                  }
                  line +=
                      " · "
                          + item.getOutputMode()
                          + "/"
                          + item.getVideoQuality()
                          + (item.getJobKind() == JobKind.MODIFY ? " · 改" : "");
                  line += "\n" + truncate(item.getConcept(), 68);
                  if (item.getOutputMediaPath() != null && !item.getOutputMediaPath().isBlank()) {
                    line += "\n  " + truncate(item.getOutputMediaPath(), 88);
                  }
                  setText(line);
                  String full = item.getConcept();
                  if (full != null && !full.isBlank()) {
                    Tooltip tt = new Tooltip(full);
                    tt.setWrapText(true);
                    tt.setMaxWidth(420);
                    setTooltip(tt);
                  } else {
                    setTooltip(null);
                  }
                }
              }
            });
    jobList
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldV, job) -> {
              if (job == null) {
                Platform.runLater(() -> updateOutputPreview(null));
                return;
              }
              Platform.runLater(() -> fillFromJob(job));
            });

    if (outputPreviewStack != null && outputMediaView != null) {
      StackPane.setAlignment(outputMediaView, Pos.CENTER);
      outputMediaView.setSmooth(true);
      outputMediaView.fitWidthProperty().bind(outputPreviewStack.widthProperty().subtract(32));
      outputMediaView.fitHeightProperty().bind(outputPreviewStack.heightProperty().subtract(32));
      outputPreviewStack.setOnMouseClicked(
          e -> {
            if (e.getClickCount() == 2
                && outputPreviewFullscreenButton != null
                && !outputPreviewFullscreenButton.isDisabled()) {
              onOutputPreviewFullscreen();
            }
          });
    }

    if (geogebraTab != null) {
      geogebraTab.selectedProperty()
          .addListener(
              (obs, was, now) -> {
                if (Boolean.TRUE.equals(now)) {
                  ensureGeogebraPageLoaded();
                  triggerGeogebraEmbedResize();
                }
              });
    }
    if (geogebraEnable3dCheck != null) {
      geogebraEnable3dCheck.setSelected(false);
    }

    refreshList();
    loadSettingsFields();
  }

  /** 无装饰窗口：在 {@link com.mathanim.MathAnimFx} 中于 setScene 之后调用 */
  public void setStage(Stage primaryStage) {
    this.stage = primaryStage;
    primaryStage.setOnCloseRequest(
        e -> {
          closeOutputFullscreen();
          disposeOutputMedia();
        });
    if (windowDragRegion != null) {
      windowDragRegion.setOnMousePressed(this::onWindowDragPressed);
      windowDragRegion.setOnMouseDragged(this::onWindowDragged);
      windowDragRegion.setOnMouseClicked(
          e -> {
            if (e.getClickCount() == 2 && e.getButton().equals(MouseButton.PRIMARY)) {
              onWindowMaximizeRestore();
            }
          });
    }
    if (resizeEdgeLeft != null) {
      resizeEdgeLeft.setCursor(Cursor.W_RESIZE);
    }
    if (resizeEdgeRight != null) {
      resizeEdgeRight.setCursor(Cursor.E_RESIZE);
    }
    if (resizeEdgeBottom != null) {
      resizeEdgeBottom.setCursor(Cursor.S_RESIZE);
    }
    if (resizeCornerBL != null) {
      resizeCornerBL.setCursor(Cursor.SW_RESIZE);
    }
    if (resizeGrip != null) {
      resizeGrip.setCursor(Cursor.SE_RESIZE);
    }
    attachResize(resizeEdgeLeft, ResizeZone.LEFT);
    attachResize(resizeEdgeRight, ResizeZone.RIGHT);
    attachResize(resizeEdgeBottom, ResizeZone.BOTTOM);
    attachResize(resizeCornerBL, ResizeZone.SW);
    attachResize(resizeGrip, ResizeZone.SE);
    if (stage != null) {
      stage
          .maximizedProperty()
          .addListener(
              (o, was, now) -> setResizeChromeVisible(!Boolean.TRUE.equals(now)));
      setResizeChromeVisible(!stage.isMaximized());
      if (stage.getScene() != null) {
        stage
            .getScene()
            .addEventFilter(
                KeyEvent.KEY_PRESSED,
                e -> {
                  if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                    onOneClickGenerate();
                    e.consume();
                  }
                });
      }
    }
  }

  private void onWindowDragPressed(MouseEvent e) {
    if (stage == null || !e.getButton().equals(MouseButton.PRIMARY)) {
      return;
    }
    if (stage.isMaximized()) {
      double relX = e.getSceneX();
      double relY = e.getSceneY();
      double sx = e.getScreenX();
      double sy = e.getScreenY();
      stage.setMaximized(false);
      stage.setX(sx - relX);
      stage.setY(sy - relY);
      dragOffsetX = sx - stage.getX();
      dragOffsetY = sy - stage.getY();
      return;
    }
    dragOffsetX = e.getScreenX() - stage.getX();
    dragOffsetY = e.getScreenY() - stage.getY();
  }

  private void onWindowDragged(MouseEvent e) {
    if (stage == null || !e.getButton().equals(MouseButton.PRIMARY) || stage.isMaximized()) {
      return;
    }
    stage.setX(e.getScreenX() - dragOffsetX);
    stage.setY(e.getScreenY() - dragOffsetY);
  }

  private enum ResizeZone {
    LEFT,
    RIGHT,
    BOTTOM,
    SW,
    SE
  }

  private void attachResize(Region region, ResizeZone zone) {
    if (region == null || stage == null) {
      return;
    }
    region.setOnMousePressed(
        e -> {
          if (!canResize(e)) {
            return;
          }
          resizeStartScreenX = e.getScreenX();
          resizeStartScreenY = e.getScreenY();
          resizeStartW = stage.getWidth();
          resizeStartH = stage.getHeight();
          resizeStartWinX = stage.getX();
        });
    region.setOnMouseDragged(
        e -> {
          if (!canResize(e)) {
            return;
          }
          double sx = e.getScreenX();
          double sy = e.getScreenY();
          switch (zone) {
            case LEFT -> {
              double dx = sx - resizeStartScreenX;
              double nw = resizeStartW - dx;
              if (nw >= stage.getMinWidth()) {
                stage.setWidth(nw);
                stage.setX(resizeStartWinX + dx);
              }
            }
            case RIGHT -> {
              double dx = sx - resizeStartScreenX;
              double nw = resizeStartW + dx;
              if (nw >= stage.getMinWidth()) {
                stage.setWidth(nw);
              }
            }
            case BOTTOM -> {
              double dy = sy - resizeStartScreenY;
              double nh = resizeStartH + dy;
              if (nh >= stage.getMinHeight()) {
                stage.setHeight(nh);
              }
            }
            case SW -> {
              double dx = sx - resizeStartScreenX;
              double dy = sy - resizeStartScreenY;
              double nw = resizeStartW - dx;
              double nh = resizeStartH + dy;
              if (nw >= stage.getMinWidth()) {
                stage.setWidth(nw);
                stage.setX(resizeStartWinX + dx);
              }
              if (nh >= stage.getMinHeight()) {
                stage.setHeight(nh);
              }
            }
            case SE -> {
              double dx = sx - resizeStartScreenX;
              double dy = sy - resizeStartScreenY;
              double nw = resizeStartW + dx;
              double nh = resizeStartH + dy;
              if (nw >= stage.getMinWidth()) {
                stage.setWidth(nw);
              }
              if (nh >= stage.getMinHeight()) {
                stage.setHeight(nh);
              }
            }
          }
        });
  }

  private boolean canResize(MouseEvent e) {
    return stage != null
        && !stage.isMaximized()
        && e.getButton().equals(MouseButton.PRIMARY);
  }

  private void setResizeChromeVisible(boolean visible) {
    Region[] regions =
        new Region[] {
          resizeEdgeLeft, resizeEdgeRight, resizeEdgeBottom, resizeCornerBL, resizeGrip
        };
    for (Region r : regions) {
      if (r != null) {
        r.setVisible(visible);
        r.setManaged(visible);
      }
    }
  }

  @FXML
  void onWindowMinimize() {
    if (stage != null) {
      stage.setIconified(true);
    }
  }

  @FXML
  void onWindowMaximizeRestore() {
    if (stage != null) {
      stage.setMaximized(!stage.isMaximized());
    }
  }

  @FXML
  void onWindowClose() {
    if (stage != null) {
      stage.close();
    }
  }

  private void fillFromJob(RenderJob job) {
    String concept = job.getConcept();
    if (concept != null) {
      Optional<TutorConceptFormat.ParsedTutor> parsed = TutorConceptFormat.tryParse(concept);
      if (parsed.isPresent()) {
        TutorConceptFormat.ParsedTutor t = parsed.get();
        if (modeTutorToggle != null) {
          modeTutorToggle.setSelected(true);
        }
        if (problemInput != null) {
          problemInput.setText(t.problem());
        }
        if (solutionInput != null) {
          solutionInput.setText(t.solution());
        }
        if (answerInput != null) {
          answerInput.setText(t.answer());
        }
        if (conceptInput != null) {
          conceptInput.clear();
        }
      } else {
        if (modeDirectToggle != null) {
          modeDirectToggle.setSelected(true);
        }
        if (conceptInput != null) {
          conceptInput.setText(concept);
        }
        if (problemInput != null) {
          problemInput.clear();
        }
        if (solutionInput != null) {
          solutionInput.clear();
        }
        if (answerInput != null) {
          answerInput.clear();
        }
      }
      updateCreationModeVisibility();
    }
    if (exampleCombo != null) {
      exampleCombo.getSelectionModel().selectFirst();
    }

    String path = job.getScriptPath();
    if (path != null && !path.isBlank()) {
      try {
        codeInput.setText(Files.readString(Path.of(path), StandardCharsets.UTF_8));
      } catch (Exception e) {
        log.warn("无法读取脚本: {} — {}", path, e.getMessage());
      }
    }
    if (job.getProblemPlanJson() != null && !job.getProblemPlanJson().isBlank()) {
      problemPlanPreview.setText(job.getProblemPlanJson());
    }
    if (job.getReferenceImagesJson() != null && !job.getReferenceImagesJson().isBlank()) {
      try {
        List<ReferenceImageItem> list =
            objectMapper.readValue(
                job.getReferenceImagesJson(), new TypeReference<List<ReferenceImageItem>>() {});
        referenceUrlsInput.setText(
            list.stream().map(ReferenceImageItem::getUrl).collect(Collectors.joining("\n")));
      } catch (Exception ignored) {
        referenceUrlsInput.setText(job.getReferenceImagesJson());
      }
    }
    if (job.getPromptOverridesJson() != null && !job.getPromptOverridesJson().isBlank()) {
      promptOverridesJsonInput.setText(job.getPromptOverridesJson());
    }
    if (job.getPromptLocale() != null) {
      promptLocaleChoice.getSelectionModel().select(job.getPromptLocale());
    }
    useProblemFramingCheck.setSelected(job.isUseProblemFraming());
    useTwoStageCheck.setSelected(job.isUseTwoStageAi());
    if (reliableGenerationCheck != null) {
      reliableGenerationCheck.setSelected(job.isReliableGeneration());
    }
    updateOutputPreview(job);
  }

  private void disposeOutputMedia() {
    closeOutputFullscreen();
    clearPreviewTransportListeners();
    if (outputMediaPlayer != null) {
      outputMediaPlayer.stop();
      outputMediaPlayer.dispose();
      outputMediaPlayer = null;
    }
    if (outputMediaView != null) {
      outputMediaView.setMediaPlayer(null);
    }
    resetPreviewTransportUi();
  }

  private void clearPreviewTransportListeners() {
    if (previewMediaAttached != null && previewMediaDurationListener != null) {
      previewMediaAttached.durationProperty().removeListener(previewMediaDurationListener);
    }
    previewMediaAttached = null;
    previewMediaDurationListener = null;
    if (outputMediaPlayer != null) {
      if (previewTimeListener != null) {
        outputMediaPlayer.currentTimeProperty().removeListener(previewTimeListener);
      }
      if (previewStatusListener != null) {
        outputMediaPlayer.statusProperty().removeListener(previewStatusListener);
      }
    }
    if (outputPreviewSeek != null) {
      if (previewSeekValueChangingListener != null) {
        outputPreviewSeek.valueChangingProperty().removeListener(previewSeekValueChangingListener);
      }
      if (previewSeekValueListener != null) {
        outputPreviewSeek.valueProperty().removeListener(previewSeekValueListener);
      }
    }
    previewTimeListener = null;
    previewSeekValueChangingListener = null;
    previewSeekValueListener = null;
    previewStatusListener = null;
  }

  private void resetPreviewTransportUi() {
    if (outputVideoTransport != null) {
      outputVideoTransport.setVisible(false);
      outputVideoTransport.setManaged(false);
    }
    if (outputPreviewSeek != null) {
      outputPreviewSeek.setDisable(true);
      outputPreviewSeek.setValue(0);
      outputPreviewSeek.setMax(1);
    }
    if (outputPreviewTimeLabel != null) {
      outputPreviewTimeLabel.setText("0:00 / 0:00");
    }
    if (outputPreviewPlayToggle != null) {
      outputPreviewPlayToggle.setDisable(true);
      outputPreviewPlayToggle.setText("播放");
    }
  }

  @FXML
  private void onPreviewPlayToggle() {
    if (outputMediaPlayer == null) {
      return;
    }
    if (outputMediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
      outputMediaPlayer.pause();
    } else {
      outputMediaPlayer.play();
    }
    Platform.runLater(
        () -> {
          if (outputMediaPlayer != null) {
            syncPreviewPlayToggle(outputMediaPlayer.getStatus());
          }
        });
  }

  private void syncPreviewPlayToggle(MediaPlayer.Status status) {
    if (outputPreviewPlayToggle == null) {
      return;
    }
    if (status == MediaPlayer.Status.PLAYING) {
      outputPreviewPlayToggle.setText("暂停");
    } else {
      outputPreviewPlayToggle.setText("播放");
    }
  }

  private void updatePreviewTimeLabel() {
    if (outputPreviewTimeLabel == null || outputMediaPlayer == null) {
      return;
    }
    Duration cur = outputMediaPlayer.getCurrentTime();
    Duration tot = outputMediaPlayer.getTotalDuration();
    String a = formatPreviewTime(cur);
    String b =
        tot != null && !tot.isUnknown() ? formatPreviewTime(tot) : "--:--";
    outputPreviewTimeLabel.setText(a + " / " + b);
  }

  private static String formatPreviewTime(Duration d) {
    if (d == null) {
      return "0:00";
    }
    int sec = (int) Math.floor(Math.min(Math.max(d.toSeconds(), 0), 359999));
    int m = sec / 60;
    int s = sec % 60;
    return String.format("%d:%02d", m, s);
  }

  private void applyPreviewDurationFromMedia() {
    if (outputMediaPlayer == null || outputPreviewSeek == null) {
      return;
    }
    Media m = outputMediaPlayer.getMedia();
    if (m == null) {
      return;
    }
    Duration d = m.getDuration();
    if (d != null && !d.isUnknown() && d.toSeconds() > 0) {
      outputPreviewSeek.setMax(d.toSeconds());
    }
  }

  private void attachPreviewTransportListeners(Media media) {
    if (media == null
        || outputVideoTransport == null
        || outputPreviewSeek == null
        || outputMediaPlayer == null) {
      return;
    }
    outputVideoTransport.setVisible(true);
    outputVideoTransport.setManaged(true);
    outputPreviewSeek.setDisable(false);
    if (outputPreviewPlayToggle != null) {
      outputPreviewPlayToggle.setDisable(false);
    }

    previewMediaAttached = media;
    previewMediaDurationListener = (o, a, b) -> Platform.runLater(this::applyPreviewDurationFromMedia);
    media.durationProperty().addListener(previewMediaDurationListener);

    previewTimeListener =
        (obs, oldV, newV) ->
            Platform.runLater(
                () -> {
                  if (outputPreviewSeek == null || outputMediaPlayer == null) {
                    return;
                  }
                  if (!outputPreviewSeek.isValueChanging()) {
                    double t = newV.toSeconds();
                    double max = outputPreviewSeek.getMax();
                    if (max <= 0 || t <= max) {
                      outputPreviewSeek.setValue(t);
                    }
                  }
                  updatePreviewTimeLabel();
                });
    outputMediaPlayer.currentTimeProperty().addListener(previewTimeListener);

    previewSeekValueChangingListener =
        (obs, was, changing) -> {
          if (Boolean.FALSE.equals(changing)
              && outputMediaPlayer != null
              && outputPreviewSeek != null) {
            outputMediaPlayer.seek(Duration.seconds(outputPreviewSeek.getValue()));
            Platform.runLater(this::updatePreviewTimeLabel);
          }
        };
    outputPreviewSeek.valueChangingProperty().addListener(previewSeekValueChangingListener);

    previewSeekValueListener =
        (obs, oldV, newV) -> {
          if (outputPreviewSeek != null
              && outputPreviewSeek.isValueChanging()
              && outputMediaPlayer != null) {
            outputMediaPlayer.seek(Duration.seconds(newV.doubleValue()));
            Platform.runLater(this::updatePreviewTimeLabel);
          }
        };
    outputPreviewSeek.valueProperty().addListener(previewSeekValueListener);

    previewStatusListener =
        (obs, oldV, newV) -> Platform.runLater(() -> syncPreviewPlayToggle(newV));
    outputMediaPlayer.statusProperty().addListener(previewStatusListener);
  }

  @FXML
  private void onOutputPreviewFullscreen() {
    if (stage == null) {
      return;
    }
    if (outputFullscreenStage != null && outputFullscreenStage.isShowing()) {
      return;
    }
    boolean video =
        outputMediaPlayer != null && outputMediaView != null && outputMediaView.isVisible();
    Image img = outputImageView != null ? outputImageView.getImage() : null;
    boolean image =
        img != null
            && !img.isError()
            && outputImageScroll != null
            && outputImageScroll.isVisible();
    if (!video && !image) {
      log.debug("全屏预览跳过：当前无可预览媒体");
      return;
    }

    outputFullscreenStage = new Stage();
    outputFullscreenStage.initOwner(stage);
    StackPane root = new StackPane();
    root.getStyleClass().add("output-fullscreen-root");

    if (video) {
      outputMediaView.setMediaPlayer(null);
      outputFullscreenMediaView = new MediaView(outputMediaPlayer);
      outputFullscreenMediaView.setPreserveRatio(true);
      outputFullscreenMediaView
          .fitWidthProperty()
          .bind(root.widthProperty().subtract(48));
      outputFullscreenMediaView
          .fitHeightProperty()
          .bind(root.heightProperty().subtract(48));
      root.getChildren().add(outputFullscreenMediaView);
    } else {
      ImageView big = new ImageView(img);
      big.setPreserveRatio(true);
      big.setSmooth(true);
      big.fitWidthProperty().bind(root.widthProperty().subtract(48));
      big.fitHeightProperty().bind(root.heightProperty().subtract(48));
      ScrollPane sp = new ScrollPane(big);
      sp.setFitToWidth(true);
      sp.setFitToHeight(true);
      sp.getStyleClass().add("output-image-scroll");
      root.getChildren().add(sp);
    }

    Button close = new Button("退出全屏 (Esc)");
    close.setFocusTraversable(false);
    close.getStyleClass().addAll("button", "btn-zen-outline");
    close.setOnAction(e -> closeOutputFullscreen());
    StackPane.setAlignment(close, Pos.TOP_RIGHT);
    StackPane.setMargin(close, new Insets(16));
    root.getChildren().add(close);

    Scene fs =
        new Scene(
            root, Math.min(Math.max(stage.getWidth() * 0.92, 640), 1280),
            Math.min(Math.max(stage.getHeight() * 0.92, 480), 900));
    var css = MainViewController.class.getResource("/css/mathanim-theme.css");
    if (css != null) {
      fs.getStylesheets().add(css.toExternalForm());
    }
    outputFullscreenStage.setScene(fs);
    outputFullscreenStage.setTitle("预览 — MathAnim");
    outputFullscreenStage.setFullScreen(true);
    outputFullscreenStage.setFullScreenExitHint("按 Esc 退出全屏");
    outputFullscreenStage.setOnCloseRequest(
        e -> {
          e.consume();
          closeOutputFullscreen();
        });
    fs.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.ESCAPE) {
            closeOutputFullscreen();
          }
        });
    outputFullscreenStage.show();
  }

  private void closeOutputFullscreen() {
    if (outputFullscreenMediaView != null) {
      outputFullscreenMediaView.setMediaPlayer(null);
      outputFullscreenMediaView = null;
    }
    if (outputMediaPlayer != null && outputMediaView != null) {
      outputMediaView.setMediaPlayer(outputMediaPlayer);
    }
    if (outputFullscreenStage != null) {
      outputFullscreenStage.close();
      outputFullscreenStage = null;
    }
  }

  private void updateFullscreenButtonState() {
    if (outputPreviewFullscreenButton == null) {
      return;
    }
    boolean video =
        outputMediaPlayer != null && outputMediaView != null && outputMediaView.isVisible();
    Image im = outputImageView != null ? outputImageView.getImage() : null;
    boolean image =
        im != null
            && !im.isError()
            && outputImageScroll != null
            && outputImageScroll.isVisible();
    outputPreviewFullscreenButton.setDisable(!video && !image);
  }

  private void updateOutputPreview(RenderJob job) {
    if (outputPreviewPlaceholder == null
        || outputMediaView == null
        || outputImageScroll == null
        || outputImageView == null) {
      return;
    }
    disposeOutputMedia();
    if (outputImageView != null) {
      outputImageView.setImage(null);
      outputImageView.fitWidthProperty().unbind();
      outputImageView.fitHeightProperty().unbind();
    }
    if (outputPreviewPathLabel != null) {
      outputPreviewPathLabel.setText("");
    }

    String pathStr = job != null ? job.getOutputMediaPath() : null;
    if (pathStr == null || pathStr.isBlank()) {
      showPreviewPlaceholder(
          job == null
              ? "在下方选中已完成的任务，即可预览 mp4 / png"
              : "该任务尚无输出（未完成或未导出）");
      return;
    }

    Path path = Path.of(pathStr);
    if (!Files.isRegularFile(path)) {
      if (outputPreviewPathLabel != null) {
        outputPreviewPathLabel.setText(path.getFileName().toString());
      }
      showPreviewPlaceholder("文件尚不存在或路径无效:\n" + pathStr);
      return;
    }

    if (outputPreviewPathLabel != null) {
      outputPreviewPathLabel.setText(path.getFileName().toString());
    }

    String lower = pathStr.toLowerCase();
    try {
      if (lower.endsWith(".mp4") || lower.endsWith(".m4v")) {
        outputPreviewPlaceholder.setVisible(false);
        outputPreviewPlaceholder.setManaged(false);
        if (outputImageScroll != null) {
          outputImageScroll.setVisible(false);
          outputImageScroll.setManaged(false);
        }
        outputMediaView.setVisible(true);
        outputMediaView.setManaged(true);

        String source = previewMediaUri(path);
        Media media = new Media(source);
        outputMediaPlayer = new MediaPlayer(media);
        outputMediaPlayer.setOnError(
            () ->
                Platform.runLater(
                    () -> {
                      MediaException ex =
                          outputMediaPlayer != null ? outputMediaPlayer.getError() : null;
                      String detail = ex != null ? ex.getMessage() : "unknown";
                      disposeOutputMedia();
                      showPreviewPlaceholder(
                          "无法播放视频: "
                              + detail
                              + "\n（JavaFX 对部分编码有限制，可改用系统播放器打开该文件）");
                      log.warn("预览视频失败: {} source={}", detail, source);
                    }));
        // 循环播放便于确认画面；部分环境下须在 READY 后再 seek/play，否则会长期黑屏
        outputMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        outputMediaView.setMediaPlayer(outputMediaPlayer);
        outputMediaPlayer.setOnReady(
            () ->
                Platform.runLater(
                    () -> {
                      if (outputMediaPlayer != null) {
                        applyPreviewDurationFromMedia();
                        updatePreviewTimeLabel();
                        syncPreviewPlayToggle(outputMediaPlayer.getStatus());
                        outputMediaPlayer.seek(Duration.ZERO);
                        outputMediaPlayer.play();
                      }
                    }));
        outputMediaPlayer.play();
        attachPreviewTransportListeners(media);
        if (outputPreviewStack != null) {
          outputMediaView.toFront();
        }
        updateFullscreenButtonState();
      } else if (lower.endsWith(".png")
          || lower.endsWith(".jpg")
          || lower.endsWith(".jpeg")
          || lower.endsWith(".gif")
          || lower.endsWith(".bmp")
          || lower.endsWith(".webp")) {
        outputPreviewPlaceholder.setVisible(false);
        outputPreviewPlaceholder.setManaged(false);
        outputMediaView.setVisible(false);
        outputMediaView.setManaged(false);
        outputImageScroll.setVisible(true);
        outputImageScroll.setManaged(true);

        Image img = new Image(previewMediaUri(path), true);
        img.errorProperty()
            .addListener(
                (obs, wasErr, isErr) -> {
                  if (Boolean.TRUE.equals(isErr)) {
                    Platform.runLater(
                        () -> {
                          Throwable err = img.getException();
                          String detail = err != null ? err.getMessage() : "load error";
                          showPreviewPlaceholder("无法加载图像: " + detail);
                          log.warn("预览图像失败: {}", detail);
                        });
                  }
                });
        outputImageView.setImage(img);
        outputImageView.fitWidthProperty().bind(outputImageScroll.widthProperty().subtract(24));
        updateFullscreenButtonState();
      } else {
        showPreviewPlaceholder("应用内暂不支持此格式预览，请在外部打开:\n" + path.getFileName());
      }
    } catch (RuntimeException e) {
      showPreviewPlaceholder("预览失败: " + e.getMessage());
    }
  }

  /** Windows 上须用规范化绝对路径的 file URI，否则 JavaFX Media 可能黑屏或不解码。 */
  private static String previewMediaUri(Path path) {
    return path.toAbsolutePath().normalize().toUri().toString();
  }

  private void showPreviewPlaceholder(String message) {
    disposeOutputMedia();
    if (outputImageView != null) {
      outputImageView.setImage(null);
      outputImageView.fitWidthProperty().unbind();
      outputImageView.fitHeightProperty().unbind();
    }
    if (outputMediaView != null) {
      outputMediaView.setVisible(false);
      outputMediaView.setManaged(false);
    }
    if (outputImageScroll != null) {
      outputImageScroll.setVisible(false);
      outputImageScroll.setManaged(false);
    }
    outputPreviewPlaceholder.setText(message);
    outputPreviewPlaceholder.setVisible(true);
    outputPreviewPlaceholder.setManaged(true);
    updateFullscreenButtonState();
  }

  private OutputMode selectedOutputMode() {
    OutputMode o = outputModeChoice.getSelectionModel().getSelectedItem();
    return o != null ? o : OutputMode.VIDEO;
  }

  private VideoQuality selectedVideoQuality() {
    VideoQuality q = videoQualityChoice.getSelectionModel().getSelectedItem();
    return q != null ? q : VideoQuality.MEDIUM;
  }

  private String selectedPromptLocale() {
    String s = promptLocaleChoice.getSelectionModel().getSelectedItem();
    return s != null ? s : "zh-CN";
  }

  /**
   * 与 ManimCat 主路径对齐：入队后立刻跑 AI + Manim，无需再点「AI → 渲染」。
   */
  @FXML
  private void onOneClickGenerate() {
    buildAndEnqueue()
        .ifPresent(
            job -> {
              appendLog("--- 一键生成: " + job.getId() + " ---");
              refreshList();
              Platform.runLater(
                  () -> {
                    jobList.getSelectionModel().select(0);
                    renderJobService.runAiWorkflowAsync(job.getId(), uiLog(), this::touchUi);
                  });
            });
  }

  @FXML
  private void onEnqueue() {
    buildAndEnqueue()
        .ifPresent(
            job -> {
              appendLog("--- 已入队: " + job.getId() + " ---");
              refreshList();
            });
  }

  /** 按当前表单创建 {@link JobKind#GENERATE} 任务；失败时写日志并返回 empty。 */
  private Optional<RenderJob> buildAndEnqueue() {
    String text = buildConceptForPipeline();
    if (text == null || text.isBlank()) {
      appendLog(isTutorMode() ? "请填写题目与解析。" : "请填写创作描述。");
      return Optional.empty();
    }
    if (isTutorMode()) {
      String p = problemInput != null && problemInput.getText() != null
          ? problemInput.getText().strip()
          : "";
      String s = solutionInput != null && solutionInput.getText() != null
          ? solutionInput.getText().strip()
          : "";
      if (p.isEmpty() || s.isEmpty()) {
        appendLog("题干模式：请同时填写「题目」与「解析与步骤」。");
        return Optional.empty();
      }
    }
    String refJson;
    try {
      refJson =
          ReferenceImagesParser.toJson(
              ReferenceImagesParser.parseLines(referenceUrlsInput.getText()), objectMapper);
    } catch (Exception e) {
      appendLog("参考图处理失败: " + e.getMessage());
      return Optional.empty();
    }
    String planJson = problemPlanPreview.getText();
    if (planJson != null && planJson.isBlank()) {
      planJson = null;
    }
    String overrides = promptOverridesJsonInput.getText();
    if (overrides != null && overrides.isBlank()) {
      overrides = null;
    }
    RenderJob job =
        renderJobService.enqueue(
            text.strip(),
            selectedOutputMode(),
            selectedVideoQuality(),
            useProblemFramingCheck.isSelected(),
            useTwoStageCheck.isSelected(),
            planJson,
            refJson,
            selectedPromptLocale(),
            overrides,
            reliableGenerationCheck != null && reliableGenerationCheck.isSelected());
    return Optional.of(job);
  }

  @FXML
  private void onProblemFramingPreview() {
    String text = buildConceptForPipeline();
    if (text == null || text.isBlank()) {
      appendLog("请先填写创作内容（直接描述或题干+解析）。");
      return;
    }
    appendLog("--- 问题拆解（仅预览）---");
    renderJobService.runProblemFramingPreviewAsync(
        text.strip(),
        referenceUrlsInput.getText(),
        selectedPromptLocale(),
        promptOverridesJsonInput.getText(),
        msg -> Platform.runLater(() -> log.info("[framing] {}", msg)),
        json -> Platform.runLater(() -> problemPlanPreview.setText(json)),
        () -> Platform.runLater(this::refreshList));
  }

  @FXML
  private void onCheckManim() {
    appendLog("--- 检测 Manim ---");
    renderJobService.checkManimVersionAsync(uiLog(), this::touchUi);
  }

  @FXML
  private void onRunBuiltinTest() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    appendLog("--- 内置测试渲染: " + selected.getId() + " ---");
    renderJobService.runBuiltinTestAsync(selected.getId(), uiLog(), this::touchUi);
  }

  @FXML
  private void onAiWorkflow() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    appendLog("--- AI + 渲染流水线: " + selected.getId() + " ---");
    renderJobService.runAiWorkflowAsync(selected.getId(), uiLog(), this::touchUi);
  }

  @FXML
  private void onModifyAndRun() {
    String concept = buildConceptForPipeline();
    if (concept == null || concept.isBlank()) {
      concept = "修改脚本";
    } else {
      concept = concept.strip();
    }
    String code = codeInput.getText();
    if (code == null || code.isBlank()) {
      appendLog("请在「修改脚本」区粘贴或加载 Python。");
      return;
    }
    String ins = editInstructionsInput.getText();
    if (ins == null || ins.isBlank()) {
      appendLog("请填写编辑说明。");
      return;
    }
    var job =
        renderJobService.enqueueModify(
            concept,
            code,
            ins.strip(),
            selectedOutputMode(),
            selectedVideoQuality(),
            reliableGenerationCheck != null && reliableGenerationCheck.isSelected());
    appendLog("--- 已创建修改任务: " + job.getId() + " ---");
    refreshList();
    renderJobService.runAiWorkflowAsync(job.getId(), uiLog(), this::touchUi);
  }

  @FXML
  private void onCancelJob() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    renderJobService.cancelJobAsync(selected.getId(), uiLog(), this::touchUi);
  }

  @FXML
  private void onViewJobConcept() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    String concept = selected.getConcept();
    if (concept == null || concept.isBlank()) {
      appendLog("该任务没有题干内容。");
      return;
    }
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("任务题干");
    alert.setHeaderText("任务 ID: " + selected.getId());
    TextArea ta = new TextArea(concept);
    ta.setEditable(false);
    ta.setWrapText(true);
    ta.setPrefRowCount(14);
    ta.setPrefWidth(520);
    alert.getDialogPane().setContent(ta);
    alert.getDialogPane().setPrefWidth(560);
    var css = MainViewController.class.getResource("/css/mathanim-theme.css");
    if (css != null) {
      alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }
    alert.showAndWait();
  }

  @FXML
  private void onCopyJobConcept() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    String concept = selected.getConcept();
    if (concept == null || concept.isBlank()) {
      appendLog("该任务没有题干内容。");
      return;
    }
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(concept);
    if (clipboard.setContent(content)) {
      appendLog("已复制题干到剪贴板。");
    } else {
      appendLog("复制到剪贴板失败。");
    }
  }

  @FXML
  private void onDeleteJob() {
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      appendLog("请先在列表中选中一条任务。");
      return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("删除任务");
    confirm.setHeaderText("确定删除该任务？");
    confirm.setContentText(
        "将从数据库移除此记录；若任务正在运行会先尝试结束渲染进程。\n已导出到文件夹的 mp4/png 不会自动删除。");
    var css = MainViewController.class.getResource("/css/mathanim-theme.css");
    if (css != null) {
      confirm.getDialogPane().getStylesheets().add(css.toExternalForm());
    }
    Optional<ButtonType> answer = confirm.showAndWait();
    if (answer.isEmpty() || answer.get() != ButtonType.OK) {
      return;
    }
    UUID id = selected.getId();
    renderJobService.deleteJobAsync(id, msg -> Platform.runLater(() -> appendLog(msg)), this::touchUi);
  }

  @FXML
  private void onSaveSettings() {
    Integer maxRetry = null;
    String mr = settingsMaxRetry.getText();
    if (mr != null && !mr.isBlank()) {
      try {
        maxRetry = Integer.parseInt(mr.strip());
        if (maxRetry <= 0) {
          appendLog("最大修补轮次须为正整数。");
          return;
        }
      } catch (NumberFormatException e) {
        appendLog("最大修补轮次格式无效。");
        return;
      }
    }
    appSettingsService.save(
        nullToBlank(settingsBaseUrl.getText()),
        nullToBlank(settingsApiKey.getText()),
        nullToBlank(settingsModel.getText()),
        maxRetry,
        nullToBlank(settingsPython.getText()));
    appendLog("已保存 API 设置。");
  }

  @FXML
  private void onReloadSettings() {
    loadSettingsFields();
    appendLog("已从数据库重新加载 API 设置（空项显示为 application.yml 默认值）。");
  }

  @FXML
  private void onTestAiConnection() {
    String base =
        firstNonBlank(trimOrEmpty(settingsBaseUrl.getText()), aiEffectiveService.getBaseUrl());
    String key =
        firstNonBlank(trimOrEmpty(settingsApiKey.getText()), aiEffectiveService.getApiKey());
    String model =
        firstNonBlank(trimOrEmpty(settingsModel.getText()), aiEffectiveService.getModel());
    if (key.isBlank()) {
      appendLog("请先填写 API Key（或写入 application.yml 的 mathanim.ai.api-key）。");
      return;
    }
    if (base.isBlank() || model.isBlank()) {
      appendLog("Base URL 与 Model 不能为空。");
      return;
    }
    if (settingsTestAiButton != null) {
      settingsTestAiButton.setDisable(true);
    }
    appendLog("正在测试 AI 连接: " + base + " · " + model);
    appendLog("（本测试仅为纯文本一条消息；不检测题目图/多模态。若 GeoGebra 带图报 image_url 错误，请换支持视觉的模型。）");
    renderJobService.testAiConnectionAsync(
        base,
        key,
        model,
        uiLog(),
        () ->
            Platform.runLater(
                () -> {
                  if (settingsTestAiButton != null) {
                    settingsTestAiButton.setDisable(false);
                  }
                }));
  }

  private void loadSettingsFields() {
    var s = appSettingsService.getOrCreate();
    settingsBaseUrl.setText(effectiveDisplay(s.getBaseUrl(), aiProperties.getBaseUrl()));
    settingsApiKey.setText(blankToEmpty(s.getApiKey()));
    settingsModel.setText(effectiveDisplay(s.getModel(), aiProperties.getModel()));
    settingsMaxRetry.setText(s.getMaxRetryPasses() != null ? String.valueOf(s.getMaxRetryPasses()) : "");
    settingsPython.setText(effectiveDisplay(s.getPythonExecutable(), aiProperties.getPythonExecutable()));
  }

  /** 库中为空时显示 yml 默认值，避免首次打开看不到 DeepSeek 等预设。 */
  private static String effectiveDisplay(String dbValue, String yamlFallback) {
    if (dbValue != null && !dbValue.isBlank()) {
      return dbValue;
    }
    return yamlFallback != null ? yamlFallback : "";
  }

  private static String firstNonBlank(String a, String b) {
    if (a != null && !a.isBlank()) {
      return a.strip();
    }
    return b != null ? b.strip() : "";
  }

  private static String trimOrEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String nullToBlank(String s) {
    return s == null ? "" : s;
  }

  private static String blankToEmpty(String s) {
    return s == null ? "" : s;
  }

  private Consumer<String> uiLog() {
    return msg ->
        Platform.runLater(
            () -> {
              log.info("[pipeline] {}", msg);
              refreshList();
            });
  }

  private void touchUi() {
    Platform.runLater(this::refreshList);
  }

  private void appendLog(String msg) {
    log.info("[desk] {}", msg);
  }

  private void refreshList() {
    List<RenderJob> list = renderJobService.listRecent();
    RenderJob selected = jobList.getSelectionModel().getSelectedItem();
    UUID selectedId = selected != null ? selected.getId() : null;
    jobList.setItems(FXCollections.observableArrayList(list));
    updateChrome(list);
    if (selectedId != null) {
      Optional<RenderJob> found =
          list.stream().filter(j -> j.getId().equals(selectedId)).findFirst();
      if (found.isPresent()) {
        jobList.getSelectionModel().select(found.get());
        updateOutputPreview(found.get());
      } else {
        updateOutputPreview(null);
      }
    }
  }

  private void updateChrome(List<RenderJob> jobs) {
    if (studioStatusLabel == null || outputStatusLabel == null || historyCountLabel == null) {
      return;
    }
    long active =
        jobs.stream()
            .filter(j -> j.getStatus() == JobStatus.QUEUED || j.getStatus() == JobStatus.PROCESSING)
            .count();
    if (active > 0) {
      studioStatusLabel.setText("> 进行中 · " + active + " 个任务");
      studioStatusLabel.getStyleClass().removeAll("studio-prompt-idle", "studio-prompt-busy");
      studioStatusLabel.getStyleClass().add("studio-prompt-busy");
      var proc = jobs.stream().filter(j -> j.getStatus() == JobStatus.PROCESSING).findFirst();
      if (proc.isPresent()) {
        RenderJob j = proc.get();
        ProcessingStage st = j.getProcessingStage();
        String suffix =
            st != null && st != ProcessingStage.NONE ? " · " + formatProcessingStageZh(st) : "";
        setOutputChromeClasses(ChromeMode.PIPELINE);
        outputStatusLabel.setText("渲染中" + suffix);
      } else {
        setOutputChromeClasses(ChromeMode.PIPELINE);
        outputStatusLabel.setText("排队中");
      }
    } else {
      studioStatusLabel.setText("> 空闲");
      studioStatusLabel.getStyleClass().removeAll("studio-prompt-idle", "studio-prompt-busy");
      studioStatusLabel.getStyleClass().add("studio-prompt-idle");
      RenderJob head = jobs.isEmpty() ? null : jobs.get(0);
      boolean hasOutput =
          head != null
              && head.getStatus() == JobStatus.COMPLETED
              && head.getOutputMediaPath() != null
              && !head.getOutputMediaPath().isBlank();
      if (hasOutput) {
        setOutputChromeClasses(ChromeMode.READY);
        outputStatusLabel.setText("已就绪");
      } else {
        setOutputChromeClasses(ChromeMode.WAITING);
        outputStatusLabel.setText("等待输出");
      }
    }
    historyCountLabel.setText(String.format("%02d", jobs.size()));
  }

  private enum ChromeMode {
    WAITING,
    PIPELINE,
    READY
  }

  private void setOutputChromeClasses(ChromeMode mode) {
    outputStatusLabel.getStyleClass().removeAll("output-status-live", "output-status-ready");
    if (mode == ChromeMode.PIPELINE) {
      outputStatusLabel.getStyleClass().add("output-status-live");
    } else if (mode == ChromeMode.READY) {
      outputStatusLabel.getStyleClass().add("output-status-ready");
    }
  }

  private static String shortStatus(JobStatus s) {
    return switch (s) {
      case QUEUED -> "排队";
      case PROCESSING -> "运行";
      case COMPLETED -> "完成";
      case FAILED -> "失败";
    };
  }

  /** 状态栏展示用，避免直接显示枚举英文名。 */
  private static String formatProcessingStageZh(ProcessingStage st) {
    if (st == null) {
      return "";
    }
    return switch (st) {
      case NONE -> "";
      case PROBLEM_FRAMING -> "问题拆解";
      case SCENE_DESIGNING -> "分镜设计";
      case CODE_GENERATING -> "代码生成";
      case AI_GENERATING -> "AI 生成";
      case STATIC_CHECKING -> "静态检查";
      case RENDERING -> "Manim 渲染";
    };
  }

  private static String truncate(String s, int max) {
    String t = s.replace('\n', ' ');
    return t.length() <= max ? t : t.substring(0, max) + "…";
  }

  private boolean isTutorMode() {
    return modeTutorToggle != null && modeTutorToggle.isSelected();
  }

  private void updateCreationModeVisibility() {
    if (directModePane == null || tutorModePane == null) {
      return;
    }
    boolean direct = modeDirectToggle == null || modeDirectToggle.isSelected();
    directModePane.setVisible(direct);
    directModePane.setManaged(direct);
    tutorModePane.setVisible(!direct);
    tutorModePane.setManaged(!direct);
  }

  private void applyExampleBankEntry(ExampleBankEntry e) {
    if (e == null) {
      return;
    }
    if (e.getInputMode() == CreationInputMode.TUTOR_WITH_SOLUTION) {
      if (modeTutorToggle != null) {
        modeTutorToggle.setSelected(true);
      }
      if (problemInput != null) {
        problemInput.setText(nullToEmpty(e.getProblem()));
      }
      if (solutionInput != null) {
        solutionInput.setText(nullToEmpty(e.getSolution()));
      }
      if (answerInput != null) {
        answerInput.setText(nullToEmpty(e.getAnswer()));
      }
      if (conceptInput != null) {
        conceptInput.clear();
      }
    } else {
      if (modeDirectToggle != null) {
        modeDirectToggle.setSelected(true);
      }
      if (conceptInput != null) {
        conceptInput.setText(nullToEmpty(e.getDirectPrompt()));
      }
      if (problemInput != null) {
        problemInput.clear();
      }
      if (solutionInput != null) {
        solutionInput.clear();
      }
      if (answerInput != null) {
        answerInput.clear();
      }
    }
    updateCreationModeVisibility();
  }

  /** 写入任务 concept 的完整文本：直接模式为单框；题干模式为结构化段落。 */
  private String buildConceptForPipeline() {
    if (!isTutorMode()) {
      String t = conceptInput != null ? conceptInput.getText() : null;
      return t != null ? t.strip() : "";
    }
    String p =
        problemInput != null && problemInput.getText() != null
            ? problemInput.getText().strip()
            : "";
    String s =
        solutionInput != null && solutionInput.getText() != null
            ? solutionInput.getText().strip()
            : "";
    String a =
        answerInput != null && answerInput.getText() != null
            ? answerInput.getText().strip()
            : "";
    return TutorConceptFormat.compose(p, s, a);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  @FXML
  private void onGeogebraPickImage() {
    if (stage == null) {
      return;
    }
    FileChooser ch = new FileChooser();
    ch.setTitle("选择题目图");
    ch.getExtensionFilters()
        .add(
            new FileChooser.ExtensionFilter(
                "图片", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp"));
    java.io.File f = ch.showOpenDialog(stage);
    if (f == null) {
      return;
    }
    try {
      byte[] bytes = Files.readAllBytes(f.toPath());
      String mime = Files.probeContentType(f.toPath());
      if (mime == null || !mime.startsWith("image/")) {
        mime = "image/png";
      }
      String dataUrl = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
      geogebraRefImage = new ReferenceImageItem(dataUrl, "auto");
      if (geogebraImageStatusLabel != null) {
        geogebraImageStatusLabel.setText(
            "已选图：" + f.getName() + "（约 " + (bytes.length / 1024) + " KB）");
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "读图失败：" + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onGeogebraClearImage() {
    geogebraRefImage = null;
    if (geogebraImageStatusLabel != null) {
      geogebraImageStatusLabel.setText("");
    }
  }

  @FXML
  private void onGeogebraGenerate() {
    String text = geogebraProblemInput != null ? geogebraProblemInput.getText().strip() : "";
    List<ReferenceImageItem> refs =
        geogebraRefImage != null ? List.of(geogebraRefImage) : List.of();
    if (text.isEmpty() && refs.isEmpty()) {
      new Alert(Alert.AlertType.WARNING, "请填写描述或上传题目图。").showAndWait();
      return;
    }
    if (!aiEffectiveService.isConfigured()) {
      new Alert(Alert.AlertType.WARNING, "请先在「设置」中配置 API。").showAndWait();
      return;
    }
    ensureGeogebraPageLoaded();
    if (geogebraGenerateButton != null) {
      geogebraGenerateButton.setDisable(true);
    }
    manimExecutorService.execute(
        () -> {
          try {
            GeogebraCommandPlan plan = geogebraAiService.generate(text, refs);
            Platform.runLater(
                () -> {
                  if (geogebraCommandsArea != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String c : plan.getCommands()) {
                      if (c != null && !c.isBlank()) {
                        if (!sb.isEmpty()) {
                          sb.append('\n');
                        }
                        sb.append(c.strip());
                      }
                    }
                    if (plan.getNotes() != null && !plan.getNotes().isBlank()) {
                      sb.append("\n\n# ").append(plan.getNotes().replace('\n', ' '));
                    }
                    geogebraCommandsArea.setText(sb.toString());
                  }
                  pendingGeogebraCommands =
                      plan.getCommands().stream()
                          .filter(c -> c != null && !c.isBlank())
                          .map(String::strip)
                          .collect(Collectors.toCollection(ArrayList::new));
                  applyPendingGeogebraToWebEmbed();
                });
          } catch (Exception e) {
            log.warn("GeoGebra AI 失败", e);
            Platform.runLater(
                () ->
                    new Alert(Alert.AlertType.ERROR, formatGeogebraAiError(e))
                        .showAndWait());
          } finally {
            Platform.runLater(
                () -> {
                  if (geogebraGenerateButton != null) {
                    geogebraGenerateButton.setDisable(false);
                  }
                });
          }
        });
  }

  @FXML
  private void onGeogebraCopyCommands() {
    String t = geogebraCommandsArea != null ? geogebraCommandsArea.getText() : "";
    if (t == null || t.isBlank()) {
      new Alert(Alert.AlertType.INFORMATION, "没有可复制的指令。").showAndWait();
      return;
    }
    ClipboardContent cc = new ClipboardContent();
    cc.putString(t);
    Clipboard.getSystemClipboard().setContent(cc);
  }

  @FXML
  private void onGeogebraReapplyCommands() {
    List<String> cmds = parseGeogebraCommandsFromArea();
    if (cmds.isEmpty()) {
      new Alert(Alert.AlertType.WARNING, "请先在文本框中填写至少一条指令（每行一条）。")
          .showAndWait();
      return;
    }
    pendingGeogebraCommands = cmds;
    applyPendingGeogebraToWebEmbed();
  }

  private List<String> parseGeogebraCommandsFromArea() {
    if (geogebraCommandsArea == null) {
      return List.of();
    }
    return geogebraCommandsArea.getText().lines()
        .map(MainViewController::stripGgbLineComment)
        .map(String::strip)
        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
        .collect(Collectors.toList());
  }

  /** 与 GGBPuppy {@code parseCommands} 一致：去掉 {@code //} 及其后内容。 */
  /** 当上游明确拒绝 image_url 时，补充说明「测试连接」与多模态无关。 */
  private static String formatGeogebraAiError(Throwable e) {
    String m = e.getMessage() != null ? e.getMessage() : String.valueOf(e);
    if (looksLikeModelRejectsVisionInput(m)) {
      return "生成失败：\n"
          + m
          + "\n\n说明：当前「设置」里的模型不接受图片，仅支持文字。界面里「测试连接」成功只表示纯文本对话可用，不代表能读题目截图。"
          + "\n请到模型服务商控制台（例如火山方舟）选用支持多模态/视觉的型号后，把 Model 改成该名称再试；或去掉题目图，只用文字描述。";
    }
    return "生成失败：" + m;
  }

  private static boolean looksLikeModelRejectsVisionInput(String message) {
    if (message == null || message.isBlank()) {
      return false;
    }
    String u = message.toLowerCase(Locale.ROOT);
    if (u.contains("image_url") || u.contains("image input") || u.contains("image inputs")) {
      return true;
    }
    if (u.contains("do not support") && u.contains("image")) {
      return true;
    }
    if (u.contains("not support") && u.contains("multimodal")) {
      return true;
    }
    if (u.contains("不支持") && (u.contains("图") || u.contains("图像") || u.contains("图片"))) {
      return true;
    }
    return u.contains("vision") && (u.contains("unsupported") || u.contains("not support"));
  }

  private static String stripGgbLineComment(String line) {
    if (line == null) {
      return "";
    }
    int i = line.indexOf("//");
    return i == -1 ? line : line.substring(0, i);
  }

  /**
   * WebView 在 Tab 切换后尺寸才稳定；通知页内脚本对 GeoGebra 调用 {@code setSize}，避免白屏。
   */
  private void triggerGeogebraEmbedResize() {
    if (geogebraWebView == null) {
      return;
    }
    PauseTransition p = new PauseTransition(Duration.millis(350));
    p.setOnFinished(
        e ->
            geogebraWebView
                .getEngine()
                .executeScript(
                    "(function(){ if(window.resizeAppletToView) window.resizeAppletToView(); })();"));
    p.play();
  }

  /**
   * 首次进入 GeoGebra 页时注入宿主页（对齐 GGBPuppy：用 {@link WebEngine#loadContent} 传入由 Java
   * 替换 classic/3d 的 HTML，避免 jar: URL 与缓存问题）。
   */
  private void ensureGeogebraPageLoaded() {
    if (geogebraWebView == null) {
      return;
    }
    WebEngine eng = geogebraWebView.getEngine();
    attachGeogebraWebLoadListener(eng);
    if (!geogebraHostContentLoadedOnce) {
      geogebraHostContentLoadedOnce = true;
      loadGeogebraHostHtml(eng);
    }
  }

  private void attachGeogebraWebLoadListener(WebEngine eng) {
    synchronized (this) {
      if (geogebraWebLoadListenerAttached) {
        return;
      }
      geogebraWebLoadListenerAttached = true;
      eng.setOnAlert(webEvent -> log.warn("[GeoGebra JS] {}", webEvent.getData()));
      eng.getLoadWorker()
          .stateProperty()
          .addListener(
              (obs, old, st) -> {
                if (st == Worker.State.SUCCEEDED) {
                  geogebraHtmlLoadedOnce = true;
                  triggerGeogebraEmbedResize();
                  runPendingGeogebraCommandsWhenReady();
                }
              });
    }
  }

  private void loadGeogebraHostHtml(WebEngine eng) {
    String template;
    try (InputStream in = MainViewController.class.getResourceAsStream("/html/geogebra-host.html")) {
      if (in == null) {
        log.error("缺少资源 html/geogebra-host.html");
        return;
      }
      template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("读取 geogebra-host.html 失败", e);
      return;
    }
    boolean use3d = geogebraEnable3dCheck != null && geogebraEnable3dCheck.isSelected();
    String html =
        template
            .replace("___GGB_APP___", use3d ? "3d" : "classic")
            .replace("___GGB_USE_3D___", Boolean.toString(use3d));
    eng.loadContent(html, "text/html");
  }

  @FXML
  private void onGeogebraReloadHost() {
    if (geogebraWebView == null) {
      return;
    }
    WebEngine eng = geogebraWebView.getEngine();
    attachGeogebraWebLoadListener(eng);
    loadGeogebraHostHtml(eng);
  }

  /**
   * 在已加载的嵌入页上应用 {@link #pendingGeogebraCommands}；避免每次 AI 都 {@code
   * WebEngine.reload()}（整页重载易导致白屏、与 inject 竞态）。
   */
  private void applyPendingGeogebraToWebEmbed() {
    if (geogebraWebView == null) {
      return;
    }
    ensureGeogebraPageLoaded();
    triggerGeogebraEmbedResize();
    runPendingGeogebraCommandsWhenReady();
  }

  private void runPendingGeogebraCommandsWhenReady() {
    if (pendingGeogebraCommands == null || pendingGeogebraCommands.isEmpty()) {
      return;
    }
    tryApplyGeogebraCommands(120);
  }

  private void tryApplyGeogebraCommands(int attemptsLeft) {
    if (geogebraWebView == null) {
      return;
    }
    if (attemptsLeft <= 0) {
      log.warn("GeoGebra API 未在预期时间内就绪，未执行本批指令（请确认可访问 geogebra.org 且 WebView 未拦截脚本）");
      return;
    }
    WebEngine eng = geogebraWebView.getEngine();
    Object ok =
        eng.executeScript(
            "(function(){"
                + "var api=window.ggbApplet||window.ggbEmbedMathAnim||window.ggbApi;"
                + "return !!(api&&typeof api.evalCommand==='function');"
                + "})()");
    if (Boolean.TRUE.equals(ok)) {
      runGeogebraEvalScript(eng, pendingGeogebraCommands);
    } else {
      PauseTransition p = new PauseTransition(Duration.millis(250));
      p.setOnFinished(e -> tryApplyGeogebraCommands(attemptsLeft - 1));
      p.play();
    }
  }

  private void runGeogebraEvalScript(WebEngine eng, List<String> cmds) {
    if (cmds == null || cmds.isEmpty() || eng == null) {
      return;
    }
    try {
      List<String> clean =
          cmds.stream()
              .filter(c -> c != null && !c.isBlank())
              .map(String::strip)
              .map(MainViewController::normalizeGeogebraInputLine)
              .toList();
      if (clean.isEmpty()) {
        return;
      }
      String json = objectMapper.writeValueAsString(clean);
      String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
      String script =
          "(function(){"
              + "var b64='"
              + b64
              + "';"
              + "var bin=atob(b64);var bytes=new Uint8Array(bin.length);"
              + "for(var i=0;i<bin.length;i++)bytes[i]=bin.charCodeAt(i);"
              + "var j=new TextDecoder('utf-8').decode(bytes);"
              + "var cmds=JSON.parse(j);"
              + "var api=window.ggbApplet||window.ggbEmbedMathAnim||window.ggbApi;"
              + "if(!api||typeof api.evalCommand!=='function')return 'no-api';"
              + "try{if(typeof api.reset==='function')api.reset();}catch(e0){}"
              + "try{api.evalCommand('ShowAxes(true)');}catch(eA){}"
              + "var bad=[];"
              + "for(var k=0;k<cmds.length;k++){"
              + "var line=cmds[k]; if(!line)continue;"
              + "try{var ok=api.evalCommand(line); if(ok===false)bad.push((k+1)+': '+line);}"
              + "catch(err){bad.push((k+1)+': '+line+' | '+err);}"
              + "}"
              + "try{api.evalCommand('ShowAxes(true)');}catch(eB){}"
              + "try{if(api.recalculateAllIfNeeded)api.recalculateAllIfNeeded();}catch(eC){}"
              + "return bad.length===0?'ok':bad.join(String.fromCharCode(10));"
              + "})();";
      Object diag = eng.executeScript(script);
      if (diag != null) {
        String s = diag.toString().strip();
        if (!s.isEmpty() && !"ok".equalsIgnoreCase(s)) {
          log.warn("[GeoGebra] 部分指令未成功或 API 异常，请核对代数区/改指令。详情:\n{}", s);
        }
      }
    } catch (Exception ex) {
      log.warn("执行 GeoGebra 指令脚本失败", ex);
    }
  }

  /**
   * GeoGebra 英文输入不接受 θ、π 等 Unicode，evalCommand 会失败且界面仍像「空白」。
   */
  private static String normalizeGeogebraInputLine(String line) {
    if (line == null) {
      return "";
    }
    return line
        .replace('\u03B8', 't')
        .replace('\u0398', 'T')
        .replace("\u03C0", "pi")
        .replace("\u03B1", "alpha")
        .replace("\u03B2", "beta");
  }
}
