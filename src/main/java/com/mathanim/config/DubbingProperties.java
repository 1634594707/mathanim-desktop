package com.mathanim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 一键配音：云端 TTS（OpenAI 兼容 /audio/speech）或本地 IndexTTS2 HTTP。
 */
@ConfigurationProperties(prefix = "mathanim.dubbing")
public class DubbingProperties {

  /** 云端 API 根路径，须含 /v1，例如 https://api.openai.com/v1 */
  private String apiBaseUrl = "https://api.openai.com/v1";

  private String apiKey = "";

  /** 如 tts-1、tts-1-hd */
  private String apiTtsModel = "tts-1";

  /** OpenAI 语音：alloy, echo, fable, onyx, nova, shimmer */
  private String apiTtsVoice = "nova";

  /** IndexTTS2 FastAPI 根地址，无路径后缀，如 http://127.0.0.1:9880 */
  private String localBaseUrl = "http://127.0.0.1:9880";

  /**
   * 本地推理必填：参考音色 wav 绝对路径（与 indextts2_api.py 的 ref_audio_path 一致）。
   */
  private String localRefAudioPath = "";

  /** 可执行文件，留空则从 PATH 查找 */
  private String ffmpegPath = "ffmpeg";

  public String getApiBaseUrl() {
    return apiBaseUrl;
  }

  public void setApiBaseUrl(String apiBaseUrl) {
    this.apiBaseUrl = apiBaseUrl != null ? apiBaseUrl : "";
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey != null ? apiKey : "";
  }

  public String getApiTtsModel() {
    return apiTtsModel;
  }

  public void setApiTtsModel(String apiTtsModel) {
    this.apiTtsModel = apiTtsModel != null ? apiTtsModel : "";
  }

  public String getApiTtsVoice() {
    return apiTtsVoice;
  }

  public void setApiTtsVoice(String apiTtsVoice) {
    this.apiTtsVoice = apiTtsVoice != null ? apiTtsVoice : "";
  }

  public String getLocalBaseUrl() {
    return localBaseUrl;
  }

  public void setLocalBaseUrl(String localBaseUrl) {
    this.localBaseUrl = localBaseUrl != null ? localBaseUrl : "";
  }

  public String getLocalRefAudioPath() {
    return localRefAudioPath;
  }

  public void setLocalRefAudioPath(String localRefAudioPath) {
    this.localRefAudioPath = localRefAudioPath != null ? localRefAudioPath : "";
  }

  public String getFfmpegPath() {
    return ffmpegPath;
  }

  public void setFfmpegPath(String ffmpegPath) {
    this.ffmpegPath = ffmpegPath != null ? ffmpegPath : "";
  }
}
