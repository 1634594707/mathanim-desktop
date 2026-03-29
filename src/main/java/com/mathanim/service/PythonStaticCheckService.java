package com.mathanim.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对应 ManimCat 的静态检查第一步：py_compile（与 mypy 相比最小化）。
 */
@Service
public class PythonStaticCheckService {

  private final AiEffectiveService aiEffectiveService;

  public PythonStaticCheckService(AiEffectiveService aiEffectiveService) {
    this.aiEffectiveService = aiEffectiveService;
  }

  public ManimProcessResult pyCompile(Path workDir, String fileName) {
    List<String> cmd = new ArrayList<>();
    cmd.add(aiEffectiveService.getPythonExecutable());
    cmd.add("-m");
    cmd.add("py_compile");
    cmd.add(fileName);

    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(workDir.toFile());
    pb.redirectErrorStream(true);
    try {
      Process p = pb.start();
      String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      boolean finished = p.waitFor(120, TimeUnit.SECONDS);
      if (!finished) {
        p.destroyForcibly();
        return ManimProcessResult.fail(-1, out, "", "py_compile 超时");
      }
      int code = p.exitValue();
      if (code != 0) {
        return ManimProcessResult.fail(code, out, "", "py_compile 失败");
      }
      return ManimProcessResult.ok(out, "");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ManimProcessResult.fail(-1, "", "", "interrupted");
    } catch (IOException e) {
      return ManimProcessResult.fail(-1, "", "", e.getMessage() != null ? e.getMessage() : e.toString());
    }
  }
}
