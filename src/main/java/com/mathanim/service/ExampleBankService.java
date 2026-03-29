package com.mathanim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathanim.examples.ExampleBankEntry;
import com.mathanim.examples.ExampleBankRoot;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleBankService {

  private static final Logger log = LoggerFactory.getLogger(ExampleBankService.class);

  private final List<ExampleBankEntry> examples;

  public ExampleBankService(ObjectMapper objectMapper) {
    List<ExampleBankEntry> loaded = new ArrayList<>();
    loaded.add(ExampleBankEntry.placeholderNone());
    try (InputStream in = ExampleBankService.class.getResourceAsStream("/examples/example-bank.json")) {
      if (in != null) {
        ExampleBankRoot root = objectMapper.readValue(in, ExampleBankRoot.class);
        if (root.getExamples() != null) {
          for (ExampleBankEntry e : root.getExamples()) {
            if (e != null && e.getId() != null && !e.getId().isBlank()) {
              loaded.add(e);
            }
          }
        }
      } else {
        log.warn("classpath 上未找到 /examples/example-bank.json，参考例题列表为空。");
      }
    } catch (IOException e) {
      log.warn("加载例题库失败: {}", e.getMessage());
    }
    this.examples = Collections.unmodifiableList(loaded);
  }

  public List<ExampleBankEntry> listForCombo() {
    return examples;
  }
}
