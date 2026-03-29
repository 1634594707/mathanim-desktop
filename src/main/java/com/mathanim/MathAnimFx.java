package com.mathanim;

import com.mathanim.ui.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.mathanim.config.MathanimDataDirectoryInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MathAnimFx extends Application {

  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() {
    applicationContext =
        new SpringApplicationBuilder(MathAnimApplication.class)
            .headless(false)
            .initializers(new MathanimDataDirectoryInitializer())
            .run();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    loadBundledFonts();
    primaryStage.initStyle(StageStyle.UNDECORATED);
    FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fxml/MainView.fxml"));
    loader.setControllerFactory(clazz -> applicationContext.getBean(clazz));
    Parent root = loader.load();
    MainViewController mainViewController = loader.getController();
    primaryStage.setTitle("MathAnim · 课堂");
    Scene scene =
        new Scene(
            root,
            1180,
            740);
    scene.getStylesheets()
        .add(
            Objects.requireNonNull(
                    getClass().getResource("/css/mathanim-theme.css"))
                .toExternalForm());
    primaryStage.setScene(scene);
    mainViewController.setStage(primaryStage);
    primaryStage.setMinWidth(880);
    primaryStage.setMinHeight(600);
    primaryStage.show();
  }

  @Override
  public void stop() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }

  /**
   * 嵌入 UI 字体：Inter、JetBrains Mono、霞鹜文楷屏幕优化版（GB，与 ManimCat Web 书写感一致，SIL OFL）。
   * 若某文件不存在则静默回退系统字体。
   */
  private void loadBundledFonts() {
    loadFontResource("/fonts/Inter-Variable.ttf", 13);
    loadFontResource("/fonts/JetBrainsMono-Regular.ttf", 12);
    loadFontResource("/fonts/LXGWWenKaiGBScreen.ttf", 14);
  }

  private static void loadFontResource(String classpath, double size) {
    try (InputStream is = MathAnimFx.class.getResourceAsStream(classpath)) {
      if (is != null) {
        Font.loadFont(is, size);
      }
    } catch (IOException ignored) {
      // 使用系统字体
    }
  }
}
