package bkit.solutions.springbootstudy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public abstract class AbstractApplicationIntegrationTests {

}
