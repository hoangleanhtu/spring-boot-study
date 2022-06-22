package bkit.solutions.springbootstudy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configurable
@ConfigurationProperties("mockserver")
@Getter
@Setter
public class MockserverPortForwardConfig {
  private String forwardTo;
}
