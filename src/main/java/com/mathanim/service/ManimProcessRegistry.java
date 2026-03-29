package com.mathanim.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 与 ManimCat 进程注册表类似：用于取消正在执行的 Manim。 */
@Component
public class ManimProcessRegistry {

  private final ConcurrentHashMap<UUID, Process> processes = new ConcurrentHashMap<>();

  public void register(UUID jobId, Process process) {
    if (jobId != null && process != null) {
      processes.put(jobId, process);
    }
  }

  public void unregister(UUID jobId) {
    if (jobId != null) {
      processes.remove(jobId);
    }
  }

  /** 终止正在运行的子进程（若存在）。 */
  public void destroy(UUID jobId) {
    if (jobId == null) {
      return;
    }
    Process p = processes.remove(jobId);
    if (p != null) {
      p.destroyForcibly();
    }
  }
}
