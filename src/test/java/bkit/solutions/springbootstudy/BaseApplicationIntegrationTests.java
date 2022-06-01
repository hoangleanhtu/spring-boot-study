package bkit.solutions.springbootstudy;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests.Initializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@ContextConfiguration(initializers = Initializer.class)
public abstract class BaseApplicationIntegrationTests {

  private static final GenericContainer rabbit = new GenericContainer(
      "rabbitmq:3.10.2")
      .withExposedPorts(5672)
      .waitingFor(Wait.forListeningPort());

  static {
    rabbit.start();
  }

  public static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext, "spring.rabbitmq.port=" + rabbit.getMappedPort(5672));
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext, "spring.rabbitmq.host=" + rabbit.getHost());
    }
  }
}
