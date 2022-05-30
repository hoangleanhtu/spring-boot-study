package bkit.solutions.springbootstudy;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests.Initializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles({"test"})
@ContextConfiguration(initializers = Initializer.class)
public abstract class BaseApplicationIntegrationTests {

  @Container
  private static RabbitMQContainer rabbit = new RabbitMQContainer(
      "rabbitmq:3.10.2-management");

  public static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext, "spring.rabbitmq.port=" + rabbit.getAmqpPort());
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext, "spring.rabbitmq.host=" + rabbit.getHost());
    }
  }
}
