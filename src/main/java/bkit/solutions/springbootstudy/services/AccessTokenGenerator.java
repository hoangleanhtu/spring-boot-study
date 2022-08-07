package bkit.solutions.springbootstudy.services;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenGenerator {

  private final RandomStringGenerator randomStringGenerator;

  public String generate(int length) {
    final String random = randomStringGenerator.generate(length);
    return Base64.getUrlEncoder().encodeToString(random.getBytes());
  }
}
