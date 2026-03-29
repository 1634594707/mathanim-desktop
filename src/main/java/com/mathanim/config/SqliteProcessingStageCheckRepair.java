package com.mathanim.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 旧版 Hibernate 在 SQLite 上为 {@code processing_stage} 生成的 CHECK 仅包含部分枚举，流水线使用
 * {@code CODE_GENERATING} 等新值时会触发 SQLITE_CONSTRAINT_CHECK。启动时去掉该 CHECK 并保留数据。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SqliteProcessingStageCheckRepair implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SqliteProcessingStageCheckRepair.class);

  private static final String OLD_NAME = "render_jobs__processing_stage_fix_old";

  private final DataSource dataSource;

  public SqliteProcessingStageCheckRepair(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try (Connection c = dataSource.getConnection()) {
      String url = c.getMetaData().getURL();
      if (url == null || !url.toLowerCase().contains("sqlite")) {
        return;
      }

      if (!tableExists(c, "render_jobs") && tableExists(c, OLD_NAME)) {
        log.warn(
            "检测到未完成的 processing_stage 迁移（无 render_jobs、存在备份表），正在从 {} 恢复…",
            OLD_NAME);
        try (Statement st = c.createStatement()) {
          c.setAutoCommit(false);
          try {
            st.execute("PRAGMA foreign_keys=OFF");
            String createSql = buildCreateTableFromPragma(c, OLD_NAME, "render_jobs");
            st.execute(createSql);
            st.execute("INSERT INTO render_jobs SELECT * FROM \"" + OLD_NAME + "\"");
            st.execute("DROP TABLE \"" + OLD_NAME + "\"");
            st.execute("PRAGMA foreign_keys=ON");
            c.commit();
            log.info("已从备份表恢复 render_jobs。");
          } catch (SQLException e) {
            c.rollback();
            log.error("恢复 render_jobs 失败：{}", e.getMessage());
            throw e;
          } finally {
            c.setAutoCommit(true);
          }
        }
        return;
      }

      String ddl;
      try (Statement st = c.createStatement();
          ResultSet rs =
              st.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='render_jobs'")) {
        if (!rs.next()) {
          return;
        }
        ddl = rs.getString(1);
      }

      if (ddl == null || ddl.isBlank()) {
        return;
      }

      String stripped = stripChecksForProcessingStage(ddl);
      if (stripped.equals(ddl)) {
        return;
      }

      log.info("检测到 render_jobs.processing_stage 的 CHECK 约束过旧，正在重建表（仅 SQLite，数据保留）…");

      try (Statement st = c.createStatement()) {
        c.setAutoCommit(false);
        try {
          st.execute("PRAGMA foreign_keys=OFF");
          st.execute("ALTER TABLE render_jobs RENAME TO \"" + OLD_NAME + "\"");
          String createSql = buildCreateTableFromPragma(c, OLD_NAME, "render_jobs");
          st.execute(createSql);
          st.execute("INSERT INTO render_jobs SELECT * FROM \"" + OLD_NAME + "\"");
          st.execute("DROP TABLE \"" + OLD_NAME + "\"");
          st.execute("PRAGMA foreign_keys=ON");
          c.commit();
          log.info("render_jobs 表约束已更新，可继续使用新 processing_stage 枚举值。");
        } catch (SQLException e) {
          c.rollback();
          log.error(
              "修复 processing_stage CHECK 失败（可手动删除 mathanim.data-dir 下的 mathanim.db 后重试）： {}",
              e.getMessage());
          throw e;
        } finally {
          c.setAutoCommit(true);
        }
      }
    }
  }

  /**
   * 按 {@code PRAGMA table_info} 重建表结构（不含任何 CHECK），列类型/NOT NULL/DEFAULT/PK 与旧表一致。
   */
  private static String buildCreateTableFromPragma(
      Connection c, String sourceTable, String targetTable) throws SQLException {
    List<ColLine> cols = new ArrayList<>();
    try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery("PRAGMA table_info(\"" + sourceTable.replace("\"", "\"\"") + "\")")) {
      while (rs.next()) {
        cols.add(
            new ColLine(
                rs.getString("name"),
                rs.getString("type"),
                rs.getInt("notnull"),
                rs.getInt("pk"),
                rs.getString("dflt_value")));
      }
    }
    if (cols.isEmpty()) {
      throw new SQLException("PRAGMA table_info 无列: " + sourceTable);
    }
    List<ColLine> pkCols =
        cols.stream()
            .filter(col -> col.pk != 0)
            .sorted(Comparator.comparingInt(col -> col.pk))
            .toList();

    StringBuilder sb = new StringBuilder("CREATE TABLE \"");
    sb.append(targetTable.replace("\"", "\"\"")).append("\" (");
    for (int i = 0; i < cols.size(); i++) {
      ColLine col = cols.get(i);
      if (i > 0) {
        sb.append(", ");
      }
      sb.append('"').append(col.name.replace("\"", "\"\"")).append('"');
      String t = col.type == null || col.type.isBlank() ? "TEXT" : col.type;
      sb.append(" ").append(t);
      if (col.notnull != 0) {
        sb.append(" NOT NULL");
      }
      boolean singleColPk = pkCols.size() == 1 && col.pk != 0;
      if (singleColPk) {
        sb.append(" PRIMARY KEY");
      }
      if (col.dflt != null && !col.dflt.isBlank()) {
        sb.append(" DEFAULT ").append(col.dflt);
      }
    }
    if (pkCols.size() > 1) {
      sb.append(", PRIMARY KEY (");
      for (int i = 0; i < pkCols.size(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append('"').append(pkCols.get(i).name.replace("\"", "\"\"")).append('"');
      }
      sb.append(")");
    }
    sb.append(")");
    return sb.toString();
  }

  private record ColLine(String name, String type, int notnull, int pk, String dflt) {}

  private static boolean tableExists(Connection c, String tableName) throws SQLException {
    try (Statement st = c.createStatement();
        ResultSet rs =
            st.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name='"
                    + tableName.replace("'", "''")
                    + "' LIMIT 1")) {
      return rs.next();
    }
  }

  /** 删除内容中包含 {@code processing_stage} 的 CHECK 子句（含列内联 {@code ... check (...)}）。 */
  static String stripChecksForProcessingStage(String ddl) {
    String s = ddl;
    int from = 0;
    while (from < s.length()) {
      int ck = indexOfIgnoreCase(s, "check", from);
      if (ck < 0) {
        break;
      }
      if (ck > 0 && isIdentChar(s.charAt(ck - 1))) {
        from = ck + 5;
        continue;
      }
      int paren = s.indexOf('(', ck + 5);
      if (paren < 0) {
        break;
      }
      int end = matchingClosingParen(s, paren);
      if (end < 0) {
        from = paren + 1;
        continue;
      }
      String checkSegment = s.substring(ck, end + 1);
      if (!checkSegment.toLowerCase().contains("processing_stage")) {
        from = end + 1;
        continue;
      }
      int removeStart = ck;
      int removeEnd = end + 1;
      while (removeStart > 0 && Character.isWhitespace(s.charAt(removeStart - 1))) {
        removeStart--;
      }
      if (removeStart > 0 && s.charAt(removeStart - 1) == ',') {
        removeStart--;
      } else if (removeEnd < s.length() && s.charAt(removeEnd) == ',') {
        removeEnd++;
      }
      s = s.substring(0, removeStart) + s.substring(removeEnd);
      from = removeStart;
    }
    return s.replaceAll(",\\s*,+", ",").replaceAll("\\(\\s*,", "(").replaceAll(",\\s*\\)", ")");
  }

  private static boolean isIdentChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private static int indexOfIgnoreCase(String s, String word, int from) {
    return s.toLowerCase().indexOf(word.toLowerCase(), from);
  }

  private static int matchingClosingParen(String s, int openIdx) {
    int depth = 0;
    for (int i = openIdx; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return -1;
  }
}
