package bkit.solutions.springbootstudy;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests.Initializer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpForward;
import org.mockserver.model.HttpRequest;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@MockServerTest
@EnableConfigurationProperties({MockserverPortForwardConfig.class})
public abstract class BaseApplicationIntegrationTests {

  private static final GenericContainer rabbit = new GenericContainer(
      "rabbitmq:3.10.2")
      .withExposedPorts(5672)
      .waitingFor(Wait.forListeningPort());

  static {
    rabbit.start();
  }

  protected MockServerClient mockServerClient;

  @Autowired
  private MockserverPortForwardConfig mockserverPortForwardConfig;

  @BeforeEach
  void beforeEach() {
    final String forwardHost = mockserverPortForwardConfig.getForwardHost();
    if (StringUtils.isNotBlank(forwardHost)) {
      mockServerClient
          .when(
              HttpRequest.request()
          )
          .withPriority(Integer.MAX_VALUE)
          .forward(
              HttpForward.forward()
                  .withHost(forwardHost)
                  .withPort(mockserverPortForwardConfig.getForwardPort())
          );
    };

  }

  public static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
          "spring.rabbitmq.port=" + rabbit.getMappedPort(5672));
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
          "spring.rabbitmq.host=" + rabbit.getHost());
    }
  }
}
