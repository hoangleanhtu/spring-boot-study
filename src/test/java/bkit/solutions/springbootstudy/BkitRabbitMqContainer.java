package bkit.solutions.springbootstudy;

import org.testcontainers.containers.RabbitMQContainer;

public class BkitRabbitMqContainer extends RabbitMQContainer {

  private static BkitRabbitMqContainer container;
  private BkitRabbitMqContainer() {
    super("rabbitmq:3.10.2");
  }

  public static BkitRabbitMqContainer getInstance() {
    if (container == null) {
      container = new BkitRabbitMqContainer();
    }

    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("spring.rabbitmq.host", container.getHost());
    System.setProperty("spring.rabbitmq.port", container.getAmqpPort() + "");
  }
}
