package bkit.solutions.springbootstudy.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

  @Bean
  Queue notifyBalanceQueue(AmqpProperties amqpProperties) {
    return new Queue(amqpProperties.getNotifyBalanceQueue());
  }
}
