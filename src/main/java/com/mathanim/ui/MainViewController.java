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

import javafx.scene.Node;

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

import javafx.scene.control.ProgressBar;

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

import javafx.scene.control.Labeled;

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

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import java.io.IOException;

import java.io.InputStream;

import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;

import java.util.Base64;

import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ExecutorService;

import java.util.Optional;

import java.util.ResourceBundle;

import java.util.UUID;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.atomic.AtomicLong;

import java.util.function.BooleanSupplier;

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
  private final SharedState sharedState;

  @FXML private StudioController studioPaneController;
  @FXML private OutputController outputPaneController;
  @FXML private GeogebraController geogebraPaneController;
  @FXML private SettingsController settingsPaneController;



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

  @FXML private TextField jobSearchField;

  @FXML private ChoiceBox<String> jobFilterChoice;

  @FXML private Button jobToggleFavoriteButton;

  @FXML private HBox batchOperationBar;

  @FXML private Label batchSelectionLabel;

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

  @FXML private Label outputStatusLabel;

  @FXML private HBox outputProgressBox;

  @FXML private ProgressBar outputProgressBar;

  @FXML private Label outputProgressLabel;

  @FXML private Label outputFallbackBadgeLabel;

  @FXML private Label outputSummaryStateLabel;

  @FXML private Label outputSummaryNextStepLabel;

  @FXML private Label outputSummaryCountLabel;

  @FXML private Label historyCountLabel;

  @FXML private Label conceptCharCount;



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

  private Button retrySelectedButton;

  private Button retryFallbackButton;



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

  private static final int JOB_LOG_LRU_MAX = 50;

  private final Map<UUID, StringBuilder> jobLogBuffers = new LinkedHashMap<>(64, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<UUID, StringBuilder> eldest) {
      return size() > JOB_LOG_LRU_MAX;
    }
  };

  private final Map<UUID, Integer> jobProgress = new HashMap<>();

  private static final int JOB_PAGE_SIZE = 50;

  private final ConcurrentHashMap<UUID, ActivePipelineState> activePipelines = new ConcurrentHashMap<>();

  private int jobListPage = 0;

  private boolean jobListHasMore = true;

  private String currentSearchKeyword = "";

  private String currentFilterName = "全部";

  private long totalJobCount = 0;

  private final AtomicLong jobListQueryGeneration = new AtomicLong();

  private final AtomicBoolean jobPageLoadInFlight = new AtomicBoolean(false);

  private final StringBuilder sessionLogBuffer = new StringBuilder();

  private final java.util.Set<UUID> selectedJobIds = new java.util.HashSet<>();

  private static final class ActivePipelineState {
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
  }

  private record JobPageResult(List<RenderJob> jobs, long totalCount, boolean hasMore) {}



  public MainViewController(

      RenderJobService renderJobService,

      AppSettingsService appSettingsService,

      AiEffectiveService aiEffectiveService,

      AiProperties aiProperties,

      ObjectMapper objectMapper,

      ExampleBankService exampleBankService,

      GeogebraAiService geogebraAiService,

      ExecutorService manimExecutorService,

      SharedState sharedState) {

    this.renderJobService = renderJobService;

    this.appSettingsService = appSettingsService;

    this.aiEffectiveService = aiEffectiveService;

    this.aiProperties = aiProperties;

    this.objectMapper = objectMapper;

    this.exampleBankService = exampleBankService;

    this.geogebraAiService = geogebraAiService;

    this.manimExecutorService = manimExecutorService;

    this.sharedState = sharedState;

  }



  @Override

  public void initialize(URL location, ResourceBundle resources) {
    bindSubviewControllers();

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

    if (allowFallbackModeCheck != null) {

      allowFallbackModeCheck.setSelected(true);

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



    applyRuntimeCopy();



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

    installHistoryRetryActions();



    jobList.setCellFactory(

        lv ->

            new ListCell<>() {

              @Override

              protected void updateItem(RenderJob item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                  setText(null);

                  setTooltip(null);

                  getStyleClass().remove("job-cell-fallback");

                  getStyleClass().remove("job-cell-failed");

                } else {

                  getStyleClass().remove("job-cell-fallback");

                  getStyleClass().remove("job-cell-failed");

                  if (item.isFallbackModeActive()) {

                    getStyleClass().add("job-cell-fallback");

                  }

                  if (item.getStatus() == JobStatus.FAILED) {

                    getStyleClass().add("job-cell-failed");

                  }

                  String st = displayJobState(item);

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

                  if (item.isFallbackModeActive()) {

                    line += " 路 保底";

                  }

                  line += "\n" + truncate(item.getConcept(), 68);

                  if (item.getFailureSummary() != null && !item.getFailureSummary().isBlank()) {

                    line += "\n  ! " + truncate(item.getFailureSummary(), 56);

                  }

                  if (item.getOutputMediaPath() != null && !item.getOutputMediaPath().isBlank()) {

                    line += "\n  " + truncate(item.getOutputMediaPath(), 88);

                  }

                  setText(line);

                  StringBuilder full = new StringBuilder();

                  if (item.getConcept() != null && !item.getConcept().isBlank()) {

                    full.append(item.getConcept().strip());

                  }

                  if (item.getFailureSummary() != null && !item.getFailureSummary().isBlank()) {

                    if (full.length() > 0) {

                      full.append("\n\n");

                    }

                    full.append("失败诊断：").append(item.getFailureSummary().strip());

                  }

                  if (item.getFailureRepairHint() != null && !item.getFailureRepairHint().isBlank()) {

                    if (full.length() > 0) {

                      full.append("\n");

                    }

                    full.append("修补建议：").append(item.getFailureRepairHint().strip());

                  }

                  if (full.length() > 0) {

                    Tooltip tt = new Tooltip(full.toString());

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
              sharedState.setSelectedJob(job);

              if (job == null) {

                Platform.runLater(

                    () -> {

                      updateOutputPreview(null);

                      refreshOutputLog(null);

                      updateFallbackBadge(false);

                      updateOutputStatusTooltip(null);

                      updateFailureSummaryCard(null);

                      updateOutputSummary(java.util.List.copyOf(jobList.getItems()), null);

                    });

                return;

              }

              Platform.runLater(() -> fillFromJob(job));

            });

    jobList

        .getSelectionModel()

        .selectedItemProperty()

        .addListener((obs, oldV, job) -> Platform.runLater(this::updateRetryButtons));



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

    

    // 初始化搜索和筛选功能

    initializeSearchAndFilter();

  }

  private void bindSubviewControllers() {
    if (studioPaneController != null) {
      studioPaneController.setActionHandler(this::dispatchSubviewAction);
      conceptInput = studioPaneController.getConceptInput();
      modeDirectToggle = studioPaneController.getModeDirectToggle();
      modeTutorToggle = studioPaneController.getModeTutorToggle();
      directModePane = studioPaneController.getDirectModePane();
      tutorModePane = studioPaneController.getTutorModePane();
      problemInput = studioPaneController.getProblemInput();
      solutionInput = studioPaneController.getSolutionInput();
      answerInput = studioPaneController.getAnswerInput();
      exampleCombo = studioPaneController.getExampleCombo();
      outputModeChoice = studioPaneController.getOutputModeChoice();
      videoQualityChoice = studioPaneController.getVideoQualityChoice();
      promptLocaleChoice = studioPaneController.getPromptLocaleChoice();
      useProblemFramingCheck = studioPaneController.getUseProblemFramingCheck();
      useTwoStageCheck = studioPaneController.getUseTwoStageCheck();
      reliableGenerationCheck = studioPaneController.getReliableGenerationCheck();
      allowFallbackModeCheck = studioPaneController.getAllowFallbackModeCheck();
      oneClickGenerateButton = studioPaneController.getOneClickGenerateButton();
      referenceUrlsInput = studioPaneController.getReferenceUrlsInput();
      promptOverridesJsonInput = studioPaneController.getPromptOverridesJsonInput();
      problemPlanPreview = studioPaneController.getProblemPlanPreview();
      codeInput = studioPaneController.getCodeInput();
      editInstructionsInput = studioPaneController.getEditInstructionsInput();
      studioStatusLabel = studioPaneController.getStudioStatusLabel();
      conceptCharCount = studioPaneController.getConceptCharCount();
    }
    if (outputPaneController != null) {
      outputPaneController.setActionHandler(this::dispatchSubviewAction);
      outputStatusLabel = outputPaneController.getOutputStatusLabel();
      outputProgressBox = outputPaneController.getOutputProgressBox();
      outputProgressBar = outputPaneController.getOutputProgressBar();
      outputProgressLabel = outputPaneController.getOutputProgressLabel();
      outputFallbackBadgeLabel = outputPaneController.getOutputFallbackBadgeLabel();
      outputSummaryStateLabel = outputPaneController.getOutputSummaryStateLabel();
      outputSummaryNextStepLabel = outputPaneController.getOutputSummaryNextStepLabel();
      outputSummaryCountLabel = outputPaneController.getOutputSummaryCountLabel();
      outputPreviewStack = outputPaneController.getOutputPreviewStack();
      outputMediaView = outputPaneController.getOutputMediaView();
      outputImageScroll = outputPaneController.getOutputImageScroll();
      outputImageView = outputPaneController.getOutputImageView();
      outputPreviewPlaceholder = outputPaneController.getOutputPreviewPlaceholder();
      outputPreviewPathLabel = outputPaneController.getOutputPreviewPathLabel();
      outputPreviewFullscreenButton = outputPaneController.getOutputPreviewFullscreenButton();
      outputVideoTransport = outputPaneController.getOutputVideoTransport();
      outputPreviewSeek = outputPaneController.getOutputPreviewSeek();
      outputPreviewTimeLabel = outputPaneController.getOutputPreviewTimeLabel();
      outputPreviewPlayToggle = outputPaneController.getOutputPreviewPlayToggle();
      outputFailureCard = outputPaneController.getOutputFailureCard();
      outputFailureMetaLabel = outputPaneController.getOutputFailureMetaLabel();
      outputFailureSummaryLabel = outputPaneController.getOutputFailureSummaryLabel();
      outputLogArea = outputPaneController.getOutputLogArea();
      HistoryController historyPaneController = outputPaneController.getHistoryPaneController();
      if (historyPaneController != null) {
        historyPaneController.setActionHandler(this::dispatchSubviewAction);
        jobList = historyPaneController.getJobList();
        jobSearchField = historyPaneController.getJobSearchField();
        jobFilterChoice = historyPaneController.getJobFilterChoice();
        jobToggleFavoriteButton = historyPaneController.getJobToggleFavoriteButton();
        batchOperationBar = historyPaneController.getBatchOperationBar();
        batchSelectionLabel = historyPaneController.getBatchSelectionLabel();
        historyCountLabel = historyPaneController.getHistoryCountLabel();
      }
    }
    if (geogebraPaneController != null) {
      geogebraPaneController.setActionHandler(this::dispatchSubviewAction);
      geogebraProblemInput = geogebraPaneController.getGeogebraProblemInput();
      geogebraImageStatusLabel = geogebraPaneController.getGeogebraImageStatusLabel();
      geogebraGenerateButton = geogebraPaneController.getGeogebraGenerateButton();
      geogebraPickImageButton = geogebraPaneController.getGeogebraPickImageButton();
      geogebraCommandsArea = geogebraPaneController.getGeogebraCommandsArea();
      geogebraWebView = geogebraPaneController.getGeogebraWebView();
      geogebraEnable3dCheck = geogebraPaneController.getGeogebraEnable3dCheck();
    }
    if (settingsPaneController != null) {
      settingsPaneController.setActionHandler(this::dispatchSubviewAction);
      settingsBaseUrl = settingsPaneController.getSettingsBaseUrl();
      settingsApiKey = settingsPaneController.getSettingsApiKey();
      settingsModel = settingsPaneController.getSettingsModel();
      settingsMaxRetry = settingsPaneController.getSettingsMaxRetry();
      settingsPython = settingsPaneController.getSettingsPython();
      settingsTestAiButton = settingsPaneController.getSettingsTestAiButton();
    }
  }

  private void dispatchSubviewAction(String action) {
    switch (action) {
      case "onOneClickGenerate" -> onOneClickGenerate();
      case "onEnqueue" -> onEnqueue();
      case "onCheckManim" -> onCheckManim();
      case "onProblemFramingPreview" -> onProblemFramingPreview();
      case "onAiWorkflow" -> onAiWorkflow();
      case "onRunBuiltinTest" -> onRunBuiltinTest();
      case "onCancelJob" -> onCancelJob();
      case "onModifyAndRun" -> onModifyAndRun();
      case "onOutputPreviewFullscreen" -> onOutputPreviewFullscreen();
      case "onPreviewPlayToggle" -> onPreviewPlayToggle();
      case "onCopyOutputLog" -> onCopyOutputLog();
      case "onClearOutputLog" -> onClearOutputLog();
      case "onViewJobConcept" -> onViewJobConcept();
      case "onCopyJobConcept" -> onCopyJobConcept();
      case "onDeleteJob" -> onDeleteJob();
      case "onToggleFavorite" -> onToggleFavorite();
      case "onBatchDelete" -> onBatchDelete();
      case "onBatchRetry" -> onBatchRetry();
      case "onBatchRetryFallback" -> onBatchRetryFallback();
      case "onSaveSettings" -> onSaveSettings();
      case "onReloadSettings" -> onReloadSettings();
      case "onTestAiConnection" -> onTestAiConnection();
      case "onGeogebraPickImage" -> onGeogebraPickImage();
      case "onGeogebraClearImage" -> onGeogebraClearImage();
      case "onGeogebraGenerate" -> onGeogebraGenerate();
      case "onGeogebraCopyCommands" -> onGeogebraCopyCommands();
      case "onGeogebraReapplyCommands" -> onGeogebraReapplyCommands();
      case "onGeogebraReloadHost" -> onGeogebraReloadHost();
      default -> log.warn("未知子视图动作: {}", action);
    }
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

    if (allowFallbackModeCheck != null) {

      allowFallbackModeCheck.setSelected(job.isAllowFallbackMode());

    }

    updateFallbackBadge(job.isFallbackModeActive());

    updateOutputStatusTooltip(job);

    updateFailureSummaryCard(job);

    updateOutputPreview(job);

    refreshOutputLog(job);

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

              appendJobLog(job.getId(), "--- 创建任务并立即执行: " + job.getId() + " ---");

              refreshList();

              Platform.runLater(

                  () -> {

                    jobList.getSelectionModel().select(0);

                    startAiWorkflow(job.getId());

                  });

            });

  }



  @FXML

  private void onEnqueue() {

    buildAndEnqueue()

        .ifPresent(

            job -> {

              appendJobLog(job.getId(), "--- 已保存草稿任务: " + job.getId() + " ---");

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

            reliableGenerationCheck != null && reliableGenerationCheck.isSelected(),

            allowFallbackModeCheck != null && allowFallbackModeCheck.isSelected());

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

        sessionUiLog(),

        json -> Platform.runLater(() -> problemPlanPreview.setText(json)),

        () -> Platform.runLater(this::refreshList));

  }



  @FXML

  private void onCheckManim() {

    appendLog("--- 检测 Manim ---");

    renderJobService.checkManimVersionAsync(sessionUiLog(), this::touchUi);

  }



  @FXML

  private void onRunBuiltinTest() {

    RenderJob selected = jobList.getSelectionModel().getSelectedItem();

    if (selected == null) {

      appendLog("请先在列表中选中一条任务。");

      return;

    }

    appendJobLog(selected.getId(), "--- 运行内置测试: " + selected.getId() + " ---");

    startBuiltinTest(selected.getId());

  }



  @FXML

  private void onAiWorkflow() {

    RenderJob selected = jobList.getSelectionModel().getSelectedItem();

    if (selected == null) {

      appendLog("请先在列表中选中一条任务。");

      return;

    }

    appendJobLog(selected.getId(), "--- 执行选中任务: " + selected.getId() + " ---");

    startAiWorkflow(selected.getId());

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

            reliableGenerationCheck != null && reliableGenerationCheck.isSelected(),

            allowFallbackModeCheck != null && allowFallbackModeCheck.isSelected());

    appendJobLog(job.getId(), "--- 已创建修改任务并立即执行: " + job.getId() + " ---");

    refreshList();

    startAiWorkflow(job.getId());

  }



  @FXML

  private void onCancelJob() {

    RenderJob selected = jobList.getSelectionModel().getSelectedItem();

    if (selected == null) {

      appendLog("请先在列表中选中一条任务。");

      return;

    }

    ActivePipelineState state = activePipelines.get(selected.getId());
    if (state != null) {
      state.cancelRequested.set(true);
    }

    appendJobLog(selected.getId(), "--- 请求取消任务 ---");

    renderJobService.cancelJobAsync(selected.getId(), jobUiLog(selected.getId()), this::touchUi);

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

    appendLog("--- 删除任务: " + id + " ---");

    renderJobService.deleteJobAsync(id, sessionUiLog(), this::touchUi);

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

    StringBuilder resultBuffer = new StringBuilder();

    appendLog("正在测试 AI 连接: " + base + " · " + model);

    appendLog("（本测试仅验证纯文本应答；不检测题目图/多模态能力。）");

    renderJobService.testAiConnectionAsync(

        base,

        key,

        model,

        msg ->

            Platform.runLater(

                () -> {

                  if (resultBuffer.length() > 0) {

                    resultBuffer.append(System.lineSeparator()).append(System.lineSeparator());

                  }

                  resultBuffer.append(msg);

                }),

        () ->

            Platform.runLater(

                () -> {

                  String resultText = resultBuffer.toString().strip();

                  if (resultText.isBlank()) {

                    resultText = "AI 连接测试已结束，但没有返回结果。";

                  }

                  appendLog(resultText);

                  showAiTestResultDialog(base, model, resultText);

                  if (settingsTestAiButton != null) {

                    settingsTestAiButton.setDisable(false);

                  }

                }));

  }

  private void showAiTestResultDialog(String baseUrl, String model, String resultText) {

    boolean success = resultText != null && resultText.startsWith("AI 连接正常");

    Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);

    alert.setTitle(success ? "AI 连接成功" : "AI 连接失败");

    alert.setHeaderText(model + " · " + baseUrl);

    TextArea ta = new TextArea(resultText == null ? "" : resultText);

    ta.setEditable(false);

    ta.setWrapText(true);

    ta.setPrefRowCount(10);

    ta.setPrefWidth(560);

    alert.getDialogPane().setContent(ta);

    alert.getDialogPane().setPrefWidth(620);

    var css = MainViewController.class.getResource("/css/mathanim-theme.css");

    if (css != null) {

      alert.getDialogPane().getStylesheets().add(css.toExternalForm());

    }

    alert.showAndWait();

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



  @FXML

  private void onCopyOutputLog() {

    String t = outputLogArea != null ? outputLogArea.getText() : "";

    if (t == null || t.isBlank()) {

      appendLog("当前没有可复制的日志。");

      return;

    }

    ClipboardContent cc = new ClipboardContent();

    cc.putString(t);

    Clipboard.getSystemClipboard().setContent(cc);

    appendLog("已复制当前日志到剪贴板。");

  }



  @FXML

  private void onClearOutputLog() {

    RenderJob selected = jobList != null ? jobList.getSelectionModel().getSelectedItem() : null;

    if (selected != null) {

      jobLogBuffers.remove(selected.getId());

      refreshOutputLog(selected);

      appendLog("已清空当前任务的会话日志。");

      return;

    }

    sessionLogBuffer.setLength(0);

    refreshOutputLog(null);

    appendLog("已清空本次会话日志。");

  }



  private Consumer<String> sessionUiLog() {

    return msg ->

        Platform.runLater(

            () -> {

              appendSessionLog(msg);

              refreshList();

            });

  }



  private Consumer<String> jobUiLog(UUID jobId) {

    return msg ->

        Platform.runLater(

            () -> {

              appendJobLog(jobId, msg);

              refreshList();

            });

  }

  private Consumer<Integer> jobUiProgress(UUID jobId) {

    return progress ->

        Platform.runLater(

            () -> {

              if (!isPipelineActive(jobId)) {

                return;

              }

              if (progress == null) {

                return;

              }

              int normalized = Math.max(0, Math.min(progress, 100));

              jobProgress.put(jobId, normalized);

              refreshList();

            });

  }

  private BooleanSupplier jobTimeoutDecision(UUID jobId) {

    return () -> isPipelineCancellationRequested(jobId) ? false : confirmRenderTimeout(jobId);

  }

  private boolean confirmRenderTimeout(UUID jobId) {

    AtomicBoolean keepWaiting = new AtomicBoolean(false);

    CountDownLatch latch = new CountDownLatch(1);

    Platform.runLater(

        () -> {

          try {

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle("渲染可能卡住");

            alert.setHeaderText("任务渲染超过预设时限");

            alert.setContentText("任务 " + jobId + " 仍未完成。你可以继续等待，或立即取消这次渲染。");

            ButtonType waitButton = new ButtonType("继续等待");

            ButtonType cancelButton = new ButtonType("取消渲染");

            alert.getButtonTypes().setAll(waitButton, cancelButton);

            ButtonType selected =

                alert.showAndWait().orElse(cancelButton);

            keepWaiting.set(selected == waitButton);

            appendJobLog(jobId, keepWaiting.get() ? "检测到超时，已选择继续等待。" : "检测到超时，已选择取消渲染。");

          } finally {

            latch.countDown();

          }

        });

    try {

      latch.await();

    } catch (InterruptedException e) {

      Thread.currentThread().interrupt();

      return false;

    }

    return keepWaiting.get();

  }

  private void startBuiltinTest(UUID jobId) {
    ActivePipelineState state = registerActivePipeline(jobId);
    if (state == null) {
      appendJobLog(jobId, "该任务已在运行中，忽略重复启动。");
      return;
    }

    renderJobService.runBuiltinTestAsync(

        jobId,
        jobUiLog(jobId),
        jobUiProgress(jobId),
        jobTimeoutDecision(jobId),
        () -> finishActivePipeline(jobId, state));

  }

  private void startAiWorkflow(UUID jobId) {
    ActivePipelineState state = registerActivePipeline(jobId);
    if (state == null) {
      appendJobLog(jobId, "该任务已在运行中，忽略重复启动。");
      return;
    }

    renderJobService.runAiWorkflowAsync(

        jobId,
        jobUiLog(jobId),
        jobUiProgress(jobId),
        jobTimeoutDecision(jobId),
        () -> finishActivePipeline(jobId, state));

  }

  private ActivePipelineState registerActivePipeline(UUID jobId) {
    ActivePipelineState state = new ActivePipelineState();
    return activePipelines.putIfAbsent(jobId, state) == null ? state : null;
  }

  private void finishActivePipeline(UUID jobId, ActivePipelineState state) {
    activePipelines.remove(jobId, state);
    Platform.runLater(() -> {
      jobProgress.remove(jobId);
      refreshList();
    });
  }

  private boolean isPipelineActive(UUID jobId) {
    return jobId != null && activePipelines.containsKey(jobId);
  }

  private boolean isPipelineCancellationRequested(UUID jobId) {
    ActivePipelineState state = activePipelines.get(jobId);
    return state != null && state.cancelRequested.get();
  }



  private void appendSessionLog(String msg) {

    log.info("[desk] {}", msg);

    appendLine(sessionLogBuffer, msg);

    RenderJob selected = jobList != null ? jobList.getSelectionModel().getSelectedItem() : null;

    if (selected == null) {

      refreshOutputLog(null);

    }

  }



  private void appendJobLog(UUID jobId, String msg) {

    log.info("[pipeline][{}] {}", jobId, msg);

    appendLine(jobLogBuffers.computeIfAbsent(jobId, ignored -> new StringBuilder()), msg);

    RenderJob selected = jobList != null ? jobList.getSelectionModel().getSelectedItem() : null;

    if (selected != null && selected.getId().equals(jobId)) {

      refreshOutputLog(selected);

    }

  }



  private static void appendLine(StringBuilder sb, String msg) {

    if (sb.length() > 0) {

      sb.append(System.lineSeparator());

    }

    sb.append(msg == null ? "" : msg.strip());

  }



  private void touchUi() {

    Platform.runLater(this::refreshList);

  }



  private void appendLog(String msg) {

    appendSessionLog(msg);

  }



  private void refreshOutputLog(RenderJob job) {

    if (outputLogArea == null) {

      return;

    }

    outputLogArea.setText(buildOutputLogText(job));

    outputLogArea.positionCaret(outputLogArea.getText().length());

    outputLogArea.setScrollTop(Double.MAX_VALUE);

  }



  private void updateFallbackBadge(boolean active) {

    if (outputFallbackBadgeLabel == null) {

      return;

    }

    outputFallbackBadgeLabel.setVisible(active);

    outputFallbackBadgeLabel.setManaged(active);

    if (outputStatusLabel != null) {

      outputStatusLabel.getStyleClass().remove("output-status-fallback");

      if (active && !outputStatusLabel.getStyleClass().contains("output-status-fallback")) {

        outputStatusLabel.getStyleClass().add("output-status-fallback");

      }

    }

  }



  private void updateOutputStatusTooltip(RenderJob job) {

    if (outputStatusLabel == null) {

      return;

    }

    if (job == null) {

      outputStatusLabel.setTooltip(null);

      if (outputFallbackBadgeLabel != null) {

        outputFallbackBadgeLabel.setTooltip(null);

      }

      return;

    }

    StringBuilder tip = new StringBuilder();

    tip.append("状态: ").append(displayJobState(job));

    if (job.getProcessingStage() != null && job.getProcessingStage() != ProcessingStage.NONE) {

      tip.append("\n阶段: ").append(formatProcessingStageZh(job.getProcessingStage()));

    }

    Integer progress = jobProgress.get(job.getId());

    if (progress != null && job.getStatus() == JobStatus.PROCESSING) {

      tip.append("\n渲染进度: ").append(progress).append('%');

    }

    if (job.getFailureSummary() != null && !job.getFailureSummary().isBlank()) {

      tip.append("\n失败诊断: ").append(job.getFailureSummary().strip());

    }

    if (job.getFailureRepairHint() != null && !job.getFailureRepairHint().isBlank()) {

      tip.append("\n修补建议: ").append(job.getFailureRepairHint().strip());

    }

    Tooltip tooltip = new Tooltip(tip.toString());

    tooltip.setWrapText(true);

    tooltip.setMaxWidth(460);

    outputStatusLabel.setTooltip(tooltip);

    if (outputFallbackBadgeLabel != null) {

      outputFallbackBadgeLabel.setTooltip(tooltip);

    }

  }



  private void updateFailureSummaryCard(RenderJob job) {

    if (outputFailureCard == null || outputFailureSummaryLabel == null) {

      return;

    }

    boolean visible =

        job != null

            && ((job.getFailureSummary() != null && !job.getFailureSummary().isBlank())

                || (job.getFailureRepairHint() != null && !job.getFailureRepairHint().isBlank())

                || job.getStatus() == JobStatus.FAILED);

    outputFailureCard.setVisible(visible);

    outputFailureCard.setManaged(visible);

    if (!visible) {

      outputFailureSummaryLabel.setText("");

      if (outputFailureMetaLabel != null) {

        outputFailureMetaLabel.setText("");

      }

      return;

    }

    StringBuilder text = new StringBuilder();

    if (job.getFailureSummary() != null && !job.getFailureSummary().isBlank()) {

      text.append(job.getFailureSummary().strip());

    }

    if (job.getFailureRepairHint() != null && !job.getFailureRepairHint().isBlank()) {

      if (text.length() > 0) {

        text.append("\n\n");

      }

      text.append("建议修补：").append(job.getFailureRepairHint().strip());

    }

    if (text.length() == 0 && job.getErrorMessage() != null && !job.getErrorMessage().isBlank()) {

      text.append(job.getErrorMessage().strip());

    }

    outputFailureSummaryLabel.setText(text.toString());

    if (outputFailureMetaLabel != null) {

      String meta = displayJobState(job);

      if (job.isFallbackModeActive()) {

        meta += " · 保底模式";

      }

      outputFailureMetaLabel.setText(meta);

    }

  }



  private void installHistoryRetryActions() {

    if (jobList == null || retrySelectedButton != null || retryFallbackButton != null) {

      return;

    }

    if (!(jobList.getParent() instanceof VBox parent)) {

      return;

    }

    VBox retryCard = new VBox(8);

    retryCard.setAlignment(Pos.CENTER_LEFT);

    retryCard.getStyleClass().add("history-action-card");



    Label title = new Label("失败后快速处理");

    title.getStyleClass().add("history-action-title");



    retrySelectedButton = new Button("重试选中任务");

    retrySelectedButton.getStyleClass().addAll("button", "btn-zen-primary");

    retrySelectedButton.setFocusTraversable(false);

    retrySelectedButton.setOnAction(e -> onRetrySelected(false));



    retryFallbackButton = new Button("保底重试");

    retryFallbackButton.getStyleClass().addAll("button", "btn-zen-outline", "btn-retry-fallback");

    retryFallbackButton.setFocusTraversable(false);

    retryFallbackButton.setOnAction(e -> onRetrySelected(true));



    HBox buttonRow = new HBox(8);

    buttonRow.setAlignment(Pos.CENTER_LEFT);

    buttonRow.getChildren().addAll(retrySelectedButton, retryFallbackButton);



    Label hint = new Label("普通重试会沿用原配置，保底重试会强制进入简化生成策略。");

    hint.getStyleClass().add("history-action-hint");

    hint.setWrapText(true);



    retryCard.getChildren().addAll(title, buttonRow, hint);

    int insertIndex = Math.max(0, parent.getChildren().indexOf(jobList));

    parent.getChildren().add(insertIndex, retryCard);

    updateRetryButtons();

  }



  private void updateRetryButtons() {

    boolean enabled = false;

    if (jobList != null) {

      RenderJob selected = jobList.getSelectionModel().getSelectedItem();

      enabled = selected != null;

    }

    if (retrySelectedButton != null) {

      retrySelectedButton.setDisable(!enabled);

    }

    if (retryFallbackButton != null) {

      retryFallbackButton.setDisable(!enabled);

    }

  }



  private void onRetrySelected(boolean forceFallbackMode) {

    RenderJob selected = jobList.getSelectionModel().getSelectedItem();

    if (selected == null) {

      appendLog("请先在列表中选中一条任务。");

      return;

    }

    appendJobLog(

        selected.getId(),

        forceFallbackMode

            ? "--- 创建保底重试任务并立即执行 ---"

            : "--- 创建重试任务并立即执行 ---");

    renderJobService.retryJobAsync(

        selected.getId(),

        forceFallbackMode,

        jobUiLog(selected.getId()),

        retried ->

            Platform.runLater(

                () -> {

                  refreshList();

                  jobList.getSelectionModel().select(retried);

                  startAiWorkflow(retried.getId());

                }),

        this::touchUi);

  }



  private void applyRuntimeCopy() {

    if (oneClickGenerateButton != null) {

      oneClickGenerateButton.setText("立即生成并执行");

    }

    if (useProblemFramingCheck != null) {

      useProblemFramingCheck.setText("问题拆解");

    }

    if (useTwoStageCheck != null) {

      useTwoStageCheck.setText("AI 多步生成（解题→分镜→代码）");

    }

    if (reliableGenerationCheck != null) {

      reliableGenerationCheck.setText("稳定优先（减少复杂动画，优先成功率）");

    }

    if (allowFallbackModeCheck != null) {

      allowFallbackModeCheck.setText("允许自动保底模式（多次失败后切换简化生成）");

    }

    if (outputFallbackBadgeLabel != null) {

      outputFallbackBadgeLabel.setText("保底模式中");

    }

    if (outputPreviewFullscreenButton != null) {

      outputPreviewFullscreenButton.setText("全屏预览");

    }

    if (settingsTestAiButton != null) {

      settingsTestAiButton.setText("测试 AI 连接");

    }

    if (geogebraGenerateButton != null) {

      geogebraGenerateButton.setText("生成 GeoGebra 指令");

    }

    if (geogebraPickImageButton != null) {

      geogebraPickImageButton.setText("选择参考图");

    }

    if (conceptInput != null) {

      conceptInput.setPromptText("例如：用 Manim 演示薄透镜成像，突出主光轴、焦点和会聚过程");

    }

    if (problemInput != null) {

      problemInput.setPromptText("粘贴题目原文");

    }

    if (solutionInput != null) {

      solutionInput.setPromptText("写出关键步骤、推导或讲解重点，方便分镜与配图");

    }

    if (answerInput != null) {

      answerInput.setPromptText("可选：标准答案、结论或数值结果");

    }

    if (referenceUrlsInput != null) {

      referenceUrlsInput.setPromptText("每行一个图片 URL");

    }

    if (promptOverridesJsonInput != null) {

      promptOverridesJsonInput.setPromptText("{\"scene_designer\":\"...\"}");

    }

    if (problemPlanPreview != null) {

      problemPlanPreview.setPromptText("仅预览问题拆解后，这里会显示规划 JSON");

    }

    if (editInstructionsInput != null) {

      editInstructionsInput.setPromptText("例如：减少镜头数量，去掉 3D 和复杂 updater");

    }

    if (outputPreviewPlaceholder != null) {

      outputPreviewPlaceholder.setText("当前还没有可预览的图片或视频");

    }

    if (outputPreviewPlayToggle != null) {

      outputPreviewPlayToggle.setText("播放");

    }

    if (outputFailureSummaryLabel != null) {

      outputFailureSummaryLabel.setText("当前任务的失败原因与修补建议会显示在这里");

    }

    if (outputFailureCard != null

        && !outputFailureCard.getChildren().isEmpty()

        && outputFailureCard.getChildren().get(0) instanceof HBox header

        && !header.getChildren().isEmpty()

        && header.getChildren().get(0) instanceof Labeled title) {

      title.setText("失败诊断");

    }

  }



  private String buildOutputLogText(RenderJob job) {

    if (job == null) {

      String session = sessionLogBuffer.toString();

      return session.isBlank() ? "当前没有任务日志。执行检查、测试或开始生成后，日志会显示在这里。" : session;

    }

    StringBuilder text = new StringBuilder();

    text.append("任务类型: ")

        .append(job.getJobKind() == JobKind.MODIFY ? "修改任务" : "生成任务")

        .append(System.lineSeparator());

    text.append("任务 ID: ").append(job.getId()).append(System.lineSeparator());

    text.append("状态: ").append(displayJobState(job)).append(System.lineSeparator());

    if (job.getProcessingStage() != null && job.getProcessingStage() != ProcessingStage.NONE) {

      text.append("阶段: ").append(formatProcessingStageZh(job.getProcessingStage())).append(System.lineSeparator());

    }

    text.append("模式: ")

        .append(job.getOutputMode())

        .append(" / ")

        .append(job.getVideoQuality())

        .append(System.lineSeparator());

    if (job.isFallbackModeActive()) {

      text.append("保底模板模式：已启用").append(System.lineSeparator());

    }

    text.append("自动保底: ")

        .append(job.isAllowFallbackMode() ? "开启" : "关闭")

        .append(System.lineSeparator());

    if (job.getScriptPath() != null && !job.getScriptPath().isBlank()) {

      text.append("脚本: ").append(job.getScriptPath()).append(System.lineSeparator());

    }

    if (job.getOutputMediaPath() != null && !job.getOutputMediaPath().isBlank()) {

      text.append("输出: ").append(job.getOutputMediaPath()).append(System.lineSeparator());

    }

    if (job.getFailureSummary() != null && !job.getFailureSummary().isBlank()) {

      text.append(System.lineSeparator()).append("[失败诊断]").append(System.lineSeparator());

      text.append(job.getFailureSummary().strip()).append(System.lineSeparator());

    }

    if (job.getFailureRepairHint() != null && !job.getFailureRepairHint().isBlank()) {

      text.append(System.lineSeparator()).append("[修补建议]").append(System.lineSeparator());

      text.append(job.getFailureRepairHint().strip()).append(System.lineSeparator());

    }

    if (job.getErrorMessage() != null && !job.getErrorMessage().isBlank()) {

      text.append(System.lineSeparator()).append("[错误]").append(System.lineSeparator());

      text.append(job.getErrorMessage().strip()).append(System.lineSeparator());

    }

    StringBuilder buffer = jobLogBuffers.get(job.getId());

    if (buffer != null && buffer.length() > 0) {

      text.append(System.lineSeparator()).append("[运行日志]").append(System.lineSeparator());

      text.append(buffer);

    } else if (text.length() > 0) {

      text.append(System.lineSeparator())

          .append("[运行日志]")

          .append(System.lineSeparator())

          .append("当前任务还没有收集到会话日志。接下来新的运行过程会显示在这里。");

    }

    return text.toString().strip();

  }



  private void refreshList() {
    JobPageResult result = fetchJobPage(currentSearchKeyword, currentFilterName, 0, JOB_PAGE_SIZE);
    jobListQueryGeneration.incrementAndGet();
    jobPageLoadInFlight.set(false);
    jobListPage = 0;
    List<RenderJob> list = result.jobs();
    totalJobCount = result.totalCount();
    jobListHasMore = result.hasMore();

    jobProgress

        .keySet()

        .removeIf(

            id ->

                list.stream()

                    .noneMatch(

                        job -> job.getId().equals(id) && job.getStatus() == JobStatus.PROCESSING));

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

        refreshOutputLog(found.get());

        updateFallbackBadge(found.get().isFallbackModeActive());

        updateOutputStatusTooltip(found.get());

        updateFailureSummaryCard(found.get());

      } else {

        updateOutputPreview(null);

        refreshOutputLog(null);

        updateFallbackBadge(false);

        updateOutputStatusTooltip(null);

        updateFailureSummaryCard(null);

      }

    } else {

      refreshOutputLog(null);

      updateFallbackBadge(false);

      updateOutputStatusTooltip(null);

      updateFailureSummaryCard(null);

    }

    updateRetryButtons();

    updateHistoryCountLabel();

  }



  private void updateHistoryCountLabel() {
    if (historyCountLabel != null) {
      historyCountLabel.setText(String.format("%02d", totalJobCount));
    }
  }

  private void loadMoreJobPages() {
    if (!jobListHasMore || !jobPageLoadInFlight.compareAndSet(false, true)) {
      return;
    }
    String keyword = currentSearchKeyword;
    String filter = currentFilterName;
    long generation = jobListQueryGeneration.get();
    int nextPage = jobListPage + 1;
    try {
      JobPageResult result = fetchJobPage(keyword, filter, nextPage, JOB_PAGE_SIZE);
      Platform.runLater(() -> {
        try {
          if (generation != jobListQueryGeneration.get()
              || !keyword.equals(currentSearchKeyword)
              || !filter.equals(currentFilterName)) {
            return;
          }
          jobListPage = nextPage;
          jobListHasMore = result.hasMore();
          totalJobCount = result.totalCount();
          if (!result.jobs().isEmpty()) {
            jobList.getItems().addAll(result.jobs());
          }
          updateHistoryCountLabel();
        } finally {
          jobPageLoadInFlight.set(false);
        }
      });
    } catch (RuntimeException e) {
      Platform.runLater(() -> appendLog("加载更多任务失败: " + e.getMessage()));
      jobPageLoadInFlight.set(false);
    }
  }

  private JobPageResult fetchJobPage(String keyword, String filter, int pageIndex, int pageSize) {
    String normalizedKeyword = keyword != null ? keyword : "";
    String normalizedFilter = filter != null ? filter : "全部";
    Pageable pageable = PageRequest.of(pageIndex, pageSize);
    if ("收藏".equals(normalizedFilter)) {
      List<RenderJob> filtered =
          renderJobService.listFavorited().stream()
              .filter(
                  j ->
                      normalizedKeyword.isBlank()
                          || (j.getConcept() != null
                              && j.getConcept().toLowerCase(Locale.ROOT)
                                  .contains(normalizedKeyword.toLowerCase(Locale.ROOT))))
              .toList();
      return sliceInMemory(filtered, pageIndex, pageSize);
    }
    if ("保底".equals(normalizedFilter)) {
      List<RenderJob> filtered =
          renderJobService.searchAndFilter(normalizedKeyword, null).stream()
              .filter(RenderJob::isFallbackModeActive)
              .toList();
      return sliceInMemory(filtered, pageIndex, pageSize);
    }
    if ("草稿".equals(normalizedFilter)) {
      List<RenderJob> filtered =
          renderJobService.searchAndFilter(normalizedKeyword, null).stream()
              .filter(j -> "草稿".equals(displayJobState(j)))
              .toList();
      return sliceInMemory(filtered, pageIndex, pageSize);
    }
    JobStatus status = resolveFilterStatus(normalizedFilter);
    Page<RenderJob> page = renderJobService.searchAndFilterPaged(normalizedKeyword, status, pageable);
    return new JobPageResult(page.getContent(), page.getTotalElements(), page.hasNext());
  }

  private static JobPageResult sliceInMemory(List<RenderJob> jobs, int pageIndex, int pageSize) {
    int from = Math.min(pageIndex * pageSize, jobs.size());
    int to = Math.min(from + pageSize, jobs.size());
    boolean hasMore = to < jobs.size();
    return new JobPageResult(jobs.subList(from, to), jobs.size(), hasMore);
  }

  private JobStatus resolveFilterStatus(String filter) {
    return switch (filter) {
      case "成功" -> JobStatus.COMPLETED;
      case "失败" -> JobStatus.FAILED;
      default -> null;
    };
  }

  private void updateChrome(List<RenderJob> jobs) {

    if (studioStatusLabel == null || outputStatusLabel == null || historyCountLabel == null) {

      return;

    }

    updateFallbackBadge(false);

    outputStatusLabel

        .getStyleClass()

        .removeAll("output-status-live", "output-status-ready", "output-status-failed", "output-status-draft");

    long active =

        jobs.stream()

            .filter(j -> j.getStatus() == JobStatus.QUEUED || j.getStatus() == JobStatus.PROCESSING)

            .count();

    RenderJob focusJob = null;

    if (active > 0) {

      studioStatusLabel.setText("> 进行中 · " + active + " 个任务");

      studioStatusLabel.getStyleClass().removeAll("studio-prompt-idle", "studio-prompt-busy");

      studioStatusLabel.getStyleClass().add("studio-prompt-busy");

      var proc = jobs.stream().filter(j -> j.getStatus() == JobStatus.PROCESSING).findFirst();

      if (proc.isPresent()) {

        RenderJob j = proc.get();

        focusJob = j;

        ProcessingStage st = j.getProcessingStage();

        String suffix =

            st != null && st != ProcessingStage.NONE ? " ? " + formatProcessingStageZh(st) : "";

        setOutputChromeClasses(ChromeMode.PIPELINE);

        updateOutputStatusTooltip(j);

        Integer renderProgress = jobProgress.get(j.getId());

        String progressSuffix =

            st == ProcessingStage.RENDERING && renderProgress != null ? " · " + renderProgress + "%" : "";

        if (j.isFallbackModeActive()) {

          updateFallbackBadge(true);

          outputStatusLabel.setText("保底模式中" + suffix + progressSuffix);

        } else {

          outputStatusLabel.setText("处理中" + suffix + progressSuffix);

        }

      } else {

        setOutputChromeClasses(ChromeMode.PIPELINE);

        RenderJob queuedHead =

            jobs.stream().filter(j -> j.getStatus() == JobStatus.QUEUED).findFirst().orElse(null);

        focusJob = queuedHead;

        updateOutputStatusTooltip(queuedHead);

        if (queuedHead != null && queuedHead.isFallbackModeActive()) {

          updateFallbackBadge(true);

          outputStatusLabel.setText("保底排队中");

        } else {

          outputStatusLabel.setText("排队中");

        }

      }

    } else {

      studioStatusLabel.setText("> 空闲");

      studioStatusLabel.getStyleClass().removeAll("studio-prompt-idle", "studio-prompt-busy");

      studioStatusLabel.getStyleClass().add("studio-prompt-idle");

      RenderJob selected = jobList != null ? jobList.getSelectionModel().getSelectedItem() : null;

      RenderJob head = selected != null ? selected : (jobs.isEmpty() ? null : jobs.get(0));

      focusJob = head;

      if (head != null && head.isFallbackModeActive()) {

        updateFallbackBadge(true);

      }

      updateOutputStatusTooltip(head);

      if (head != null && displayJobState(head).equals("草稿")) {

        setOutputChromeClasses(ChromeMode.DRAFT);

        outputStatusLabel.setText("草稿任务");

        updateHistoryCountLabel();

        updateOutputSummary(jobs, focusJob);

        return;

      }

      if (head != null && head.getStatus() == JobStatus.FAILED) {

        setOutputChromeClasses(ChromeMode.FAILED);

        outputStatusLabel.setText("生成失败");

        updateHistoryCountLabel();

        updateOutputSummary(jobs, focusJob);

        return;

      }

      if (head != null && head.getStatus() == JobStatus.QUEUED) {

        setOutputChromeClasses(ChromeMode.PIPELINE);

        outputStatusLabel.setText("排队中");

        updateHistoryCountLabel();

        updateOutputSummary(jobs, focusJob);

        return;

      }

      boolean hasOutput =

          head != null

              && head.getStatus() == JobStatus.COMPLETED

              && head.getOutputMediaPath() != null

              && !head.getOutputMediaPath().isBlank();

      if (hasOutput) {

        setOutputChromeClasses(ChromeMode.READY);

        outputStatusLabel.setText("排队中");

      } else {

        setOutputChromeClasses(ChromeMode.WAITING);

        outputStatusLabel.setText("等待输出");

      }

    }

    updateHistoryCountLabel();

    updateOutputSummary(jobs, focusJob);

    updateOutputProgress(focusJob);

  }

  private void updateOutputProgress(RenderJob job) {

    if (outputProgressBox == null || outputProgressBar == null || outputProgressLabel == null) {

      return;

    }

    boolean show = job != null && job.getStatus() == JobStatus.PROCESSING;

    outputProgressBox.setVisible(show);

    outputProgressBox.setManaged(show);

    if (!show) {

      outputProgressBar.setProgress(-1);

      outputProgressLabel.setText("准备渲染");

      return;

    }

    Integer progress = jobProgress.get(job.getId());

    if (job.getProcessingStage() == ProcessingStage.RENDERING && progress != null) {

      outputProgressBar.setProgress(progress / 100.0);

      outputProgressLabel.setText(progress + "%");

      return;

    }

    outputProgressBar.setProgress(-1);

    outputProgressLabel.setText(

        job.getProcessingStage() != null && job.getProcessingStage() != ProcessingStage.NONE

            ? formatProcessingStageZh(job.getProcessingStage())

            : "处理中");

  }



  private void updateOutputSummary(List<RenderJob> jobs, RenderJob focusJob) {

    if (outputSummaryStateLabel == null || outputSummaryNextStepLabel == null || outputSummaryCountLabel == null) {

      return;

    }

    int count = jobs == null ? 0 : jobs.size();

    outputSummaryCountLabel.setText(String.format("%02d", count));

    if (focusJob == null) {

      outputSummaryStateLabel.setText("空闲");

      outputSummaryNextStepLabel.setText("从左侧填写内容并开始生成");

      return;

    }

    String stateText = displayJobState(focusJob);

    if (focusJob.isFallbackModeActive()) {

      stateText += " · 保底";

    }

    outputSummaryStateLabel.setText(stateText);

    outputSummaryNextStepLabel.setText(buildNextStepText(focusJob));

  }



  private String buildNextStepText(RenderJob job) {

    if (job == null) {

      return "从左侧填写内容并开始生成";

    }

    if (job.getStatus() == JobStatus.PROCESSING) {

      if (job.getProcessingStage() != null && job.getProcessingStage() != ProcessingStage.NONE) {

        return "等待“" + formatProcessingStageZh(job.getProcessingStage()) + "”完成";

      }

      return "等待当前任务完成渲染";

    }

    if (job.getStatus() == JobStatus.QUEUED && "草稿".equals(displayJobState(job))) {

      return "补充描述或参数后点击“立即生成并执行”";

    }

    if (job.getStatus() == JobStatus.QUEUED) {

      return "等待进入 AI + Manim 流水线";

    }

    if (job.getStatus() == JobStatus.FAILED) {

      return "查看下方失败诊断，然后重试或保底重试";

    }

    boolean hasOutput =

        job.getOutputMediaPath() != null && !job.getOutputMediaPath().isBlank();

    if (job.getStatus() == JobStatus.COMPLETED && hasOutput) {

      return "预览结果，满意后继续复用这条任务";

    }

    if (job.getStatus() == JobStatus.COMPLETED) {

      return "任务已完成，等待输出文件同步";

    }

    return "从任务列表选择一条结果继续查看";

  }



  private enum ChromeMode {

    WAITING,

    PIPELINE,

    READY,

    FAILED,

    DRAFT

  }



  private void setOutputChromeClasses(ChromeMode mode) {

    outputStatusLabel

        .getStyleClass()

        .removeAll("output-status-live", "output-status-ready", "output-status-failed", "output-status-draft");

    if (mode == ChromeMode.PIPELINE) {

      outputStatusLabel.getStyleClass().add("output-status-live");

    } else if (mode == ChromeMode.READY) {

      outputStatusLabel.getStyleClass().add("output-status-ready");

    } else if (mode == ChromeMode.FAILED) {

      outputStatusLabel.getStyleClass().add("output-status-failed");

    } else if (mode == ChromeMode.DRAFT) {

      outputStatusLabel.getStyleClass().add("output-status-draft");

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

  private static String displayJobState(RenderJob job) {

    if (job == null) {

      return "";

    }

    if (job.getStatus() == JobStatus.QUEUED

        && job.getProcessingStage() == ProcessingStage.NONE

        && (job.getScriptPath() == null || job.getScriptPath().isBlank())

        && (job.getOutputMediaPath() == null || job.getOutputMediaPath().isBlank())

        && (job.getErrorMessage() == null || job.getErrorMessage().isBlank())) {

      return "草稿";

    }

    return shortStatus(job.getStatus());

  }



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



  // ========== 新增：搜索、筛选和批量操作功能 ==========



  private void initializeSearchAndFilter() {

    // 初始化筛选下拉框

    if (jobFilterChoice != null) {

      jobFilterChoice.setItems(FXCollections.observableArrayList(

          "全部", "草稿", "成功", "失败", "保底", "收藏"

      ));

      jobFilterChoice.getSelectionModel().select("全部");

      jobFilterChoice.getSelectionModel().selectedItemProperty().addListener(

          (obs, oldV, newV) -> applySearchAndFilter()

      );

    }



    // 初始化搜索框

    if (jobSearchField != null) {

      jobSearchField.textProperty().addListener(

          (obs, oldV, newV) -> applySearchAndFilter()

      );

    }



    // 初始化任务列表多选支持

    if (jobList != null) {

      jobList.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        if (newSkin != null) {
          Platform.runLater(() -> {
            try {
              for (javafx.scene.Node node : jobList.lookupAll(".scroll-bar")) {
                if (node instanceof javafx.scene.control.ScrollBar sb
                    && sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                  sb.valueProperty().addListener((o, ov, nv) -> {
                    if (nv.doubleValue() >= 0.95 && jobListHasMore) {
                      manimExecutorService.execute(this::loadMoreJobPages);
                    }
                  });
                }
              }
            } catch (Exception ignored) {
            }
          });
        }
      });

      jobList.setOnMouseClicked(e -> {

        if (e.isControlDown() && jobList.getSelectionModel().getSelectedItem() != null) {

          RenderJob selected = jobList.getSelectionModel().getSelectedItem();

          if (selectedJobIds.contains(selected.getId())) {

            selectedJobIds.remove(selected.getId());

          } else {

            selectedJobIds.add(selected.getId());

          }

          updateBatchOperationBar();

          jobList.refresh(); // 刷新显示

        } else if (!e.isControlDown()) {

          // 单击不按 Ctrl 时清除多选

          if (selectedJobIds.size() > 0) {

            selectedJobIds.clear();

            updateBatchOperationBar();

            jobList.refresh();

          }

        }

      });



      // 更新单元格样式以显示多选状态

      jobList.setCellFactory(lv -> new ListCell<>() {

        @Override

        protected void updateItem(RenderJob item, boolean empty) {

          super.updateItem(item, empty);

          if (empty || item == null) {

            setText(null);
            setGraphic(null);
            setTooltip(null);

            getStyleClass().removeAll("job-cell-fallback", "job-cell-failed", "job-cell-selected");

          } else {

            getStyleClass().removeAll("job-cell-fallback", "job-cell-failed", "job-cell-selected");

            

            // 多选高亮

            if (selectedJobIds.contains(item.getId())) {

              getStyleClass().add("job-cell-selected");

            }

            

            if (item.isFallbackModeActive()) {

              getStyleClass().add("job-cell-fallback");

            }

            if (item.getStatus() == JobStatus.FAILED) {

              getStyleClass().add("job-cell-failed");

            }

            

            String statusIcon = switch (item.getStatus()) {
              case COMPLETED -> "✓";
              case FAILED -> "✗";
              case PROCESSING -> "●";
              case QUEUED -> "○";
            };

            String metaLine = displayJobState(item);
            if (item.getProcessingStage() != null && item.getProcessingStage() != ProcessingStage.NONE) {
              metaLine += " · " + item.getProcessingStage();
            }
            metaLine += " · " + item.getOutputMode() + "/" + item.getVideoQuality()
                + (item.getJobKind() == JobKind.MODIFY ? " · 改" : "");
            if (item.isFavorited()) {
              metaLine = "★ " + metaLine;
            }
            if (item.isFallbackModeActive()) {
              metaLine += " 保底";
            }

            String conceptText = truncate(item.getConcept(), 72);

            String failLine = (item.getFailureSummary() != null && !item.getFailureSummary().isBlank())
                ? "! " + truncate(item.getFailureSummary(), 60) : null;

            setText(null);

            javafx.scene.text.Text icon = new javafx.scene.text.Text(statusIcon);
            icon.getStyleClass().add("job-cell-status");
            icon.setFill(switch (item.getStatus()) {
              case COMPLETED -> javafx.scene.paint.Color.web("#2e7d32");
              case FAILED -> javafx.scene.paint.Color.web("#c62828");
              case PROCESSING -> javafx.scene.paint.Color.web("#1565c0");
              case QUEUED -> javafx.scene.paint.Color.web("#9e9e9e");
            });

            javafx.scene.text.Text meta = new javafx.scene.text.Text(metaLine);
            meta.getStyleClass().add("job-cell-meta");

            javafx.scene.text.Text concept = new javafx.scene.text.Text(conceptText);
            concept.getStyleClass().add("job-cell-concept");

            VBox textBox = new VBox(2, meta, concept);
            if (failLine != null) {
              javafx.scene.text.Text fail = new javafx.scene.text.Text(failLine);
              fail.setFill(javafx.scene.paint.Color.web("#c62828"));
              fail.setStyle("-fx-font-size: 10px;");
              textBox.getChildren().add(fail);
            }

            HBox cellBox = new HBox(8, icon, textBox);
            cellBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            setGraphic(cellBox);

            

            StringBuilder full = new StringBuilder();

            if (item.getConcept() != null && !item.getConcept().isBlank()) {

              full.append(item.getConcept().strip());

            }

            if (item.getFailureSummary() != null && !item.getFailureSummary().isBlank()) {

              if (full.length() > 0) full.append("\n\n");

              full.append("失败诊断：").append(item.getFailureSummary().strip());

            }

            if (item.getFailureRepairHint() != null && !item.getFailureRepairHint().isBlank()) {

              if (full.length() > 0) full.append("\n");

              full.append("修补建议：").append(item.getFailureRepairHint().strip());

            }

            if (full.length() > 0) {

              Tooltip tt = new Tooltip(full.toString());

              tt.setWrapText(true);

              tt.setMaxWidth(420);

              setTooltip(tt);

            } else {

              setTooltip(null);

            }

          }

        }

      });

    }



    // 初始化收藏按钮状态

    if (jobList != null && jobToggleFavoriteButton != null) {

      jobList.getSelectionModel().selectedItemProperty().addListener(

          (obs, oldV, newV) -> {

            if (newV != null) {

              jobToggleFavoriteButton.setDisable(false);

              jobToggleFavoriteButton.setText(newV.isFavorited() ? "★" : "☆");

            } else {

              jobToggleFavoriteButton.setDisable(true);

              jobToggleFavoriteButton.setText("☆");

            }

          }

      );

    }

  }



  private void applySearchAndFilter() {
    String keyword = jobSearchField != null ? jobSearchField.getText() : "";
    String filter =
        jobFilterChoice != null
            ? jobFilterChoice.getSelectionModel().getSelectedItem()
            : "全部";

    currentSearchKeyword = keyword != null ? keyword : "";
    currentFilterName = filter != null ? filter : "全部";
    long generation = jobListQueryGeneration.incrementAndGet();
    jobListPage = 0;
    jobPageLoadInFlight.set(false);

    manimExecutorService.execute(() -> {
      try {
        JobPageResult result = fetchJobPage(currentSearchKeyword, currentFilterName, 0, JOB_PAGE_SIZE);
        Platform.runLater(() -> {
          if (generation != jobListQueryGeneration.get()) {
            return;
          }
          totalJobCount = result.totalCount();
          jobListHasMore = result.hasMore();
          jobList.setItems(FXCollections.observableArrayList(result.jobs()));
          updateHistoryCountLabel();
        });
      } catch (Exception e) {
        Platform.runLater(() -> appendLog("搜索/筛选失败: " + e.getMessage()));
      }
    });
  }

  private void updateBatchOperationBar() {

    if (batchOperationBar != null && batchSelectionLabel != null) {

      int count = selectedJobIds.size();

      if (count > 0) {

        batchOperationBar.setVisible(true);

        batchOperationBar.setManaged(true);

        batchSelectionLabel.setText("已选 " + count + " 项");

      } else {

        batchOperationBar.setVisible(false);

        batchOperationBar.setManaged(false);

      }

    }

  }



  @FXML

  private void onToggleFavorite() {

    RenderJob selected = jobList.getSelectionModel().getSelectedItem();

    if (selected == null) return;

    

    manimExecutorService.execute(() -> {

      try {

        renderJobService.toggleFavorite(selected.getId());

        Platform.runLater(() -> {

          appendLog((selected.isFavorited() ? "已取消收藏" : "已收藏") + "任务: " + selected.getId());

          applySearchAndFilter();

        });

      } catch (Exception e) {

        Platform.runLater(() -> appendLog("切换收藏失败: " + e.getMessage()));

      }

    });

  }



  @FXML

  private void onBatchDelete() {

    if (selectedJobIds.isEmpty()) {

      appendLog("请先选择要删除的任务（按住 Ctrl 点击任务）");

      return;

    }

    

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

    confirm.setTitle("确认批量删除");

    confirm.setHeaderText("即将删除 " + selectedJobIds.size() + " 个任务");

    confirm.setContentText("此操作不可撤销，确定继续吗？");

    

    confirm.showAndWait().ifPresent(response -> {

      if (response == ButtonType.OK) {

        List<UUID> toDelete = new ArrayList<>(selectedJobIds);

        selectedJobIds.clear();

        updateBatchOperationBar();

        

        appendLog("--- 批量删除 " + toDelete.size() + " 个任务 ---");

        renderJobService.batchDeleteAsync(

            toDelete,

            sessionUiLog(),

            () -> Platform.runLater(() -> {

              applySearchAndFilter();

              touchUi();

            })

        );

      }

    });

  }



  @FXML

  private void onBatchRetry() {

    if (selectedJobIds.isEmpty()) {

      appendLog("请先选择要重试的任务（按住 Ctrl 点击任务）");

      return;

    }

    

    List<UUID> toRetry = new ArrayList<>(selectedJobIds);

    selectedJobIds.clear();

    updateBatchOperationBar();

    

    appendLog("--- 批量重试 " + toRetry.size() + " 个任务 ---");

    renderJobService.batchRetryAsync(

        toRetry,

        false,

        sessionUiLog(),

        jobs -> Platform.runLater(() -> {

          applySearchAndFilter();

          for (RenderJob job : jobs) {

            appendJobLog(job.getId(), "--- 开始执行重试任务 ---");

            startAiWorkflow(job.getId());

          }

        }),

        this::touchUi

    );

  }



  @FXML

  private void onBatchRetryFallback() {

    if (selectedJobIds.isEmpty()) {

      appendLog("请先选择要重试的任务（按住 Ctrl 点击任务）");

      return;

    }

    

    List<UUID> toRetry = new ArrayList<>(selectedJobIds);

    selectedJobIds.clear();

    updateBatchOperationBar();

    

    appendLog("--- 批量保底重试 " + toRetry.size() + " 个任务 ---");

    renderJobService.batchRetryAsync(

        toRetry,

        true,

        sessionUiLog(),

        jobs -> Platform.runLater(() -> {

          applySearchAndFilter();

          for (RenderJob job : jobs) {

            appendJobLog(job.getId(), "--- 开始执行保底重试任务 ---");

            startAiWorkflow(job.getId());

          }

        }),

        this::touchUi

    );

  }

}

