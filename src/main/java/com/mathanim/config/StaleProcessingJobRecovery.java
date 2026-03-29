package com.mathanim.config;

import com.mathanim.service.RenderJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时收尾上次未正常结束的「运行中」任务，避免 UI 误显示渲染中。
 */
@Component
public class StaleProcessingJobRecovery {

  private static final Logger log = LoggerFactory.getLogger(StaleProcessingJobRecovery.class);

  private final RenderJobService renderJobService;

  public StaleProcessingJobRecovery(RenderJobService renderJobService) {
    this.renderJobService = renderJobService;
  }

  @Order(0)
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    int n = renderJobService.recoverStaleProcessingJobs();
    if (n > 0) {
      log.warn("已将 {} 条卡在「运行中」的任务标记为失败（上次未正常结束）", n);
    }
  }
}
