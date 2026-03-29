package com.mathanim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mathanim.config.DubbingProperties;
import com.mathanim.domain.AppSettings;
import com.mathanim.domain.DubbingMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 一键配音：拉取 TTS 音频后与 mp4 用 ffmpeg 封装。支持 OpenAI 兼容 {@code /v1/audio/speech} 与 IndexTTS2
 * {@code POST /tts}。
 */
@Service
public class DubbingService {

  private static final int OPENAI_INPUT_MAX_CHARS = 4096;

  private final DubbingProperties dubbingProperties;
  private final AppSettingsService appSettingsService;
  private final AiEffectiveService aiEffectiveService;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

  public DubbingService(
      DubbingProperties dubbingProperties,
      AppSettingsService appSettingsService,
      AiEffectiveService aiEffectiveService,
      ObjectMapper objectMapper) {
    this.dubbingProperties = dubbingProperties;
    this.appSettingsService = appSettingsService;
    this.aiEffectiveService = aiEffectiveService;
    this.objectMapper = objectMapper;
  }

  /**
   * @param inputMp4 已有视频绝对路径
   * @param narrationText 配音全文
   * @param mode API 或 LOCAL
   * @param log 进度日志
   * @return 生成带配音的 mp4 绝对路径（同目录 {@code *_dubbed.mp4}）
   */
  public Path dubMp4(Path inputMp4, String narrationText, DubbingMode mode, LineLog log)
      throws IOException, InterruptedException {
    if (inputMp4 == null || !Files.isRegularFile(inputMp4)) {
      throw new IllegalArgumentException("视频文件不存在: " + inputMp4);
    }
    String text = narrationText != null ? narrationText.strip() : "";
    if (text.isEmpty()) {
      throw new IllegalArgumentException("配音文稿为空");
    }
    AppSettings s = appSettingsService.getOrCreate();
    Path tempAudio =
        Files.createTempFile("mathanim-dub-", mode == DubbingMode.API ? ".mp3" : ".wav");
    try {
      if (mode == DubbingMode.API) {
        byte[] audio = synthesizeOpenAiSpeech(text, s, log);
        Files.write(tempAudio, audio);
      } else {
        byte[] audio = synthesizeIndexTts2(text, s, log);
        Files.write(tempAudio, audio);
      }
      Path out = dubbedOutputPath(inputMp4);
      runFfmpegMux(inputMp4, tempAudio, out, s, log);
      log.line("完成: " + out.toAbsolutePath());
      return out.toAbsolutePath().normalize();
    } finally {
      try {
        Files.deleteIfExists(tempAudio);
      } catch (IOException ignored) {
      }
    }
  }

  private static Path dubbedOutputPath(Path inputMp4) {
    String name = inputMp4.getFileName().toString();
    int dot = name.lastIndexOf('.');
    String base = dot > 0 ? name.substring(0, dot) : name;
    return inputMp4.getParent().resolve(base + "_dubbed.mp4");
  }

  /** 临时文件无扩展名时按内容判断；OpenAI 返回 mp3，Index 返回 wav。 */
  private byte[] synthesizeOpenAiSpeech(String text, AppSettings s, LineLog log) throws IOException, InterruptedException {
    String base = firstNonBlank(s.getDubbingApiBaseUrl(), dubbingProperties.getApiBaseUrl());
    String key = firstNonBlank(s.getDubbingApiKey(), dubbingProperties.getApiKey());
    if (key.isBlank()) {
      key = aiEffectiveService.getApiKey();
    }
    String model = firstNonBlank(s.getDubbingApiTtsModel(), dubbingProperties.getApiTtsModel());
    String voice = firstNonBlank(s.getDubbingApiTtsVoice(), dubbingProperties.getApiTtsVoice());
    if (key.isBlank()) {
      throw new IllegalStateException(
          "云端配音需要 API Key：请填写「配音 API Key」、mathanim.dubbing.api-key，或与聊天共用的 Key。");
    }
    String t = text;
    if (t.length() > OPENAI_INPUT_MAX_CHARS) {
      log.line("警告：文稿超过 " + OPENAI_INPUT_MAX_CHARS + " 字，已截断。");
      t = t.substring(0, OPENAI_INPUT_MAX_CHARS);
    }
    String url = stripTrailingSlash(base) + "/audio/speech";
    log.line("请求云端 TTS: " + url + " · model=" + model);
    ObjectNode root = objectMapper.createObjectNode();
    root.put("model", model);
    root.put("input", t);
    root.put("voice", voice);
    String body = objectMapper.writeValueAsString(root);
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(5))
            .header("Authorization", "Bearer " + key)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
    HttpResponse<byte[]> resp =
        httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
    if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
      String hint =
          resp.body() != null && resp.body().length > 0
              ? new String(resp.body(), 0, Math.min(resp.body().length, 800), StandardCharsets.UTF_8)
              : "";
      throw new IllegalStateException("TTS HTTP " + resp.statusCode() + " " + hint);
    }
    return resp.body();
  }

  private byte[] synthesizeIndexTts2(String text, AppSettings s, LineLog log) throws IOException, InterruptedException {
    String base = firstNonBlank(s.getDubbingLocalBaseUrl(), dubbingProperties.getLocalBaseUrl());
    String ref = firstNonBlank(s.getDubbingRefAudioPath(), dubbingProperties.getLocalRefAudioPath());
    if (ref.isBlank()) {
      throw new IllegalStateException(
          "本地 IndexTTS2 需要参考音频：请填写「参考音色 wav」绝对路径（ref_audio_path）。");
    }
    Path refPath = Path.of(ref);
    if (!Files.isRegularFile(refPath)) {
      throw new IllegalStateException("参考音频不存在: " + refPath.toAbsolutePath());
    }
    String url = stripTrailingSlash(base) + "/tts";
    log.line("请求本地 TTS: " + url);
    ObjectNode root = objectMapper.createObjectNode();
    root.put("text", text);
    root.put("ref_audio_path", refPath.toAbsolutePath().toString().replace('\\', '/'));
    String body = objectMapper.writeValueAsString(root);
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
    HttpResponse<byte[]> resp =
        httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
    if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
      String hint =
          resp.body() != null && resp.body().length > 0
              ? new String(resp.body(), 0, Math.min(resp.body().length, 1200), StandardCharsets.UTF_8)
              : "";
      throw new IllegalStateException("本地 TTS HTTP " + resp.statusCode() + " " + hint);
    }
    return resp.body();
  }

  private void runFfmpegMux(Path video, Path audio, Path output, AppSettings s, LineLog log)
      throws IOException, InterruptedException {
    String ffmpeg = firstNonBlank(s.getDubbingFfmpegPath(), dubbingProperties.getFfmpegPath());
    if (ffmpeg.isBlank()) {
      ffmpeg = "ffmpeg";
    }
    Files.createDirectories(output.getParent());
    List<String> cmd = new ArrayList<>();
    cmd.add(ffmpeg);
    cmd.add("-y");
    cmd.add("-i");
    cmd.add(video.toAbsolutePath().toString());
    cmd.add("-i");
    cmd.add(audio.toAbsolutePath().toString());
    cmd.add("-c:v");
    cmd.add("copy");
    cmd.add("-c:a");
    cmd.add("aac");
    cmd.add("-b:a");
    cmd.add("192k");
    cmd.add("-map");
    cmd.add("0:v:0");
    cmd.add("-map");
    cmd.add("1:a:0");
    cmd.add("-shortest");
    cmd.add(output.toAbsolutePath().toString());
    log.line("运行 ffmpeg 封装…");
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = p.waitFor();
    if (code != 0) {
      throw new IllegalStateException("ffmpeg 失败 (exit=" + code + "): " + truncate(out, 1500));
    }
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max) + "…";
  }

  private static String stripTrailingSlash(String u) {
    if (u == null) {
      return "";
    }
    String t = u.strip();
    while (t.endsWith("/")) {
      t = t.substring(0, t.length() - 1);
    }
    return t;
  }

  private static String firstNonBlank(String a, String b) {
    if (a != null && !a.isBlank()) {
      return a.strip();
    }
    return b != null ? b.strip() : "";
  }

  /** 日志行回调。 */
  public interface LineLog {
    void line(String msg);
  }
}
