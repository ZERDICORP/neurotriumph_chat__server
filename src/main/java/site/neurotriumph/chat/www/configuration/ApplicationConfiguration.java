package site.neurotriumph.chat.www.configuration;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
  @Bean("random")
  public Random getRandom() {
    return new Random();
  }

  @Bean("scheduledExecutorService")
  public ScheduledExecutorService getScheduledExecutorService() {
    return Executors.newScheduledThreadPool(100);
  }

  @Bean("executorService")
  public ExecutorService getExecutorService() {
    return Executors.newFixedThreadPool(100);
  }
}
