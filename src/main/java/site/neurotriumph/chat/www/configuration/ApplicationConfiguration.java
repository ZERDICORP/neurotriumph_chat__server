package site.neurotriumph.chat.www.configuration;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import site.neurotriumph.chat.www.constants.Const;

@Configuration
public class ApplicationConfiguration {
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    final Duration duration = Duration.ofMillis(Const.NN_API_TIMEOUT);
    return restTemplateBuilder
      .setConnectTimeout(duration)
      .setReadTimeout(duration)
      .build();
  }

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
