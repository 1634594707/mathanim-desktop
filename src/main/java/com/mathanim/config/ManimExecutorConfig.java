package com.mathanim.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties({
  MathanimRootProperties.class,
  ManimProperties.class,
  AiProperties.class
})
public class ManimExecutorConfig {

  @Bean(destroyMethod = "shutdown")
  public ExecutorService manimExecutorService() {
    return Executors.newFixedThreadPool(2, r -> {
      Thread t = new Thread(r, "manim-worker");
      t.setDaemon(true);
      return t;
    });
  }
}
