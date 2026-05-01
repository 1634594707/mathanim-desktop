package com.mathanim.service;

import com.mathanim.domain.AppSettings;
import com.mathanim.repo.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSettingsService {

  private final AppSettingsRepository appSettingsRepository;

  public AppSettingsService(AppSettingsRepository appSettingsRepository) {
    this.appSettingsRepository = appSettingsRepository;
  }

  @Transactional(readOnly = true)
  public AppSettings getOrCreate() {
    return appSettingsRepository
        .findById(AppSettings.SINGLETON_ID)
        .orElseGet(
            () -> {
              AppSettings s = new AppSettings();
              s.setId(AppSettings.SINGLETON_ID);
              return appSettingsRepository.save(s);
            });
  }

  @Transactional
  public AppSettings save(
      String baseUrl,
      String apiKey,
      String model,
      Integer maxRetryPasses,
      String pythonExecutable) {
    AppSettings s = getOrCreate();
    s.setBaseUrl(baseUrl);
    s.setApiKey(apiKey);
    s.setModel(model);
    s.setMaxRetryPasses(maxRetryPasses);
    s.setPythonExecutable(pythonExecutable);
    return appSettingsRepository.save(s);
  }

}
