package bkit.solutions.springbootstudy.config;

import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

  @Bean
  RandomStringGenerator randomStringGenerator() {
    final RestorableUniformRandomProvider uniformRandomProvider = RandomSource.MT.create();
    return new RandomStringGenerator.Builder()
        .usingRandom(uniformRandomProvider::nextInt)
        .build();
  }
}
