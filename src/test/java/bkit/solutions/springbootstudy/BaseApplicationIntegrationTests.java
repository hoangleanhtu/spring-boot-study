package bkit.solutions.springbootstudy;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseApplicationIntegrationTests {

  @Container
  private static BkitRabbitMqContainer bkitRabbitMqContainer = BkitRabbitMqContainer.getInstance();
}
