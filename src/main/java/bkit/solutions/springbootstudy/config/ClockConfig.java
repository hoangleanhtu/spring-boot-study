package bkit.solutions.springbootstudy.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

  @Bean
  Clock clock(@Value("${mock-clock-now:}") String mockClock) {
    if (StringUtils.isNotBlank(mockClock)) {
      return Clock.fixed(Instant.parse(mockClock), ZoneOffset.UTC);
    }
    return Clock.systemDefaultZone();
  }
}
