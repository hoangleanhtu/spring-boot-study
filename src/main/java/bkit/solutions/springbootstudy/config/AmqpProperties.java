package bkit.solutions.springbootstudy.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "amqp")
@NoArgsConstructor
@Data
@Validated
public class AmqpProperties {
  @NotBlank
  private String notifyBalanceQueue;
}
