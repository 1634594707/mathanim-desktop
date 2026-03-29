package com.mathanim.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteProcessingStageCheckRepairTest {

  @Test
  void stripsColumnInlineCheck() {
    String ddl =
        "CREATE TABLE render_jobs (id varchar(255) not null primary key, "
            + "processing_stage varchar(32) check (processing_stage in ('NONE','AI_GENERATING','STATIC_CHECKING','RENDERING')))";
    String out = SqliteProcessingStageCheckRepair.stripChecksForProcessingStage(ddl);
    assertFalse(out.toLowerCase().contains("check"));
    assertTrue(out.contains("processing_stage"));
  }

  @Test
  void stripsDoubleParenCheck() {
    String ddl =
        "CREATE TABLE \"render_jobs\" (\"processing_stage\" varchar(32) check ((\"processing_stage\" in ('NONE','AI_GENERATING'))))";
    String out = SqliteProcessingStageCheckRepair.stripChecksForProcessingStage(ddl);
    assertFalse(out.toLowerCase().contains("check"));
  }

  @Test
  void leavesUnrelatedChecks() {
    String ddl = "CREATE TABLE t (status varchar check (status in ('A','B')), processing_stage text)";
    String out = SqliteProcessingStageCheckRepair.stripChecksForProcessingStage(ddl);
    assertTrue(out.toLowerCase().contains("check"));
    assertTrue(out.contains("status"));
  }
}
