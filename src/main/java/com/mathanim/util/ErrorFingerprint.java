package com.mathanim.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Builds a stable fingerprint for repeated compile/render failures while ignoring volatile details
 * like line numbers, absolute paths, and caret positions.
 */
public final class ErrorFingerprint {

  private static final Pattern PY_LINE = Pattern.compile("\\bline\\s+\\d+\\b");
  private static final Pattern JAVA_LINE = Pattern.compile(":(\\d+)(?=\\D|$)");
  private static final Pattern FILE_PATH =
      Pattern.compile("([a-z]:)?[^\\s\"']*[\\\\/][^\\s\"']+\\.py", Pattern.CASE_INSENSITIVE);
  private static final Pattern CARET_LINE = Pattern.compile("(?m)^\\s*\\^+\\s*$");
  private static final Pattern NUMBER_TOKEN = Pattern.compile("\\b\\d+\\b");
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private ErrorFingerprint() {}

  public static String fingerprint(String stage, String detail) {
    String normalized = normalize(stage, detail);
    if (normalized.isBlank()) {
      return "empty";
    }
    return sha256Short(normalized);
  }

  static String normalize(String stage, String detail) {
    String combined =
        ((stage != null ? stage : "") + "\n" + (detail != null ? detail : ""))
            .toLowerCase(Locale.ROOT);
    combined = FILE_PATH.matcher(combined).replaceAll("<file>");
    combined = PY_LINE.matcher(combined).replaceAll("line <n>");
    combined = JAVA_LINE.matcher(combined).replaceAll(":<n>");
    combined = CARET_LINE.matcher(combined).replaceAll("");
    combined = NUMBER_TOKEN.matcher(combined).replaceAll("<n>");
    combined = WHITESPACE.matcher(combined).replaceAll(" ").trim();
    return combined;
  }

  private static String sha256Short(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < Math.min(8, bytes.length); i++) {
        sb.append(String.format("%02x", bytes[i]));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
