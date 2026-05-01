package com.mathanim.config;

import com.mathanim.domain.JobStatus;
import com.mathanim.repo.RenderJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Startup maintenance for the local SQLite database: backup first, then clean old failed jobs.
 */
@Component
public class DatabaseMaintenanceStartup {

  private static final Logger log = LoggerFactory.getLogger(DatabaseMaintenanceStartup.class);
  private static final int FAILED_JOB_RETENTION_DAYS = 90;
  private static final DateTimeFormatter BACKUP_NAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

  private final MathanimRootProperties rootProperties;
  private final RenderJobRepository renderJobRepository;

  public DatabaseMaintenanceStartup(
      MathanimRootProperties rootProperties, RenderJobRepository renderJobRepository) {
    this.rootProperties = rootProperties;
    this.renderJobRepository = renderJobRepository;
  }

  @Order(-10)
  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void onApplicationReady() {
    backupDatabase();
    cleanupOldFailedJobs();
  }

  private void backupDatabase() {
    Path dbFile = Path.of(rootProperties.getDataDir()).resolve("mathanim.db").toAbsolutePath().normalize();
    if (!Files.isRegularFile(dbFile)) {
      return;
    }
    Path backupDir =
        Path.of(rootProperties.getDataDir()).resolve("backups").toAbsolutePath().normalize();
    String fileName = "mathanim-" + BACKUP_NAME_FORMAT.format(Instant.now()) + ".db";
    Path backupFile = backupDir.resolve(fileName);
    try {
      Files.createDirectories(backupDir);
      Files.copy(dbFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
      log.info("已创建数据库备份：{}", backupFile);
    } catch (IOException e) {
      log.warn("数据库备份失败：{}", e.getMessage());
    }
  }

  private void cleanupOldFailedJobs() {
    Instant cutoff = Instant.now().minusSeconds(FAILED_JOB_RETENTION_DAYS * 24L * 60L * 60L);
    long deleted = renderJobRepository.deleteByStatusAndCreatedAtBefore(JobStatus.FAILED, cutoff);
    if (deleted > 0) {
      log.info("已清理 {} 条超过 {} 天的失败任务记录。", deleted, FAILED_JOB_RETENTION_DAYS);
    }
  }
}
