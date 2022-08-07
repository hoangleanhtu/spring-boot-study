package bkit.solutions.springbootstudy.config;

import java.time.Duration;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "access-token")
@NoArgsConstructor
@Data
@Validated
public class AccessTokenProperties {
  @NotNull
  private Integer length;

  @NotNull
  private Duration expiration;
}
