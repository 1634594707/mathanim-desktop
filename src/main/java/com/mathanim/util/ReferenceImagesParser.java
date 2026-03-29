package com.mathanim.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.domain.ReferenceImageItem;

import java.util.ArrayList;
import java.util.List;

public final class ReferenceImagesParser {

  public static final int MAX_IMAGES = 4;

  private ReferenceImagesParser() {}

  /** JSON 数组，或 null/空。 */
  public static List<ReferenceImageItem> parseJson(String json, ObjectMapper mapper) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      List<ReferenceImageItem> list =
          mapper.readValue(json.strip(), new TypeReference<List<ReferenceImageItem>>() {});
      return trim(list);
    } catch (Exception e) {
      throw new IllegalArgumentException("参考图 JSON 无效: " + e.getMessage(), e);
    }
  }

  /** 每行一个 URL（http/https 或 data:image）。 */
  public static List<ReferenceImageItem> parseLines(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    List<ReferenceImageItem> out = new ArrayList<>();
    for (String line : text.split("\n")) {
      String u = line.strip();
      if (u.isEmpty() || u.startsWith("#")) {
        continue;
      }
      if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("data:image")) {
        out.add(new ReferenceImageItem(u, "auto"));
      }
      if (out.size() >= MAX_IMAGES) {
        break;
      }
    }
    return out;
  }

  public static String toJson(List<ReferenceImageItem> items, ObjectMapper mapper) {
    try {
      return mapper.writeValueAsString(trim(items));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static List<ReferenceImageItem> trim(List<ReferenceImageItem> list) {
    if (list == null || list.isEmpty()) {
      return List.of();
    }
    return list.subList(0, Math.min(list.size(), MAX_IMAGES));
  }
}
