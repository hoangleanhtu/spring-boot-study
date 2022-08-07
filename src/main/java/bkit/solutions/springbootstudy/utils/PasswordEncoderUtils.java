package bkit.solutions.springbootstudy.utils;

import bkit.solutions.springbootstudy.config.PasswordConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtils {

  public static void main(String[] args) {
    final PasswordConfig passwordConfig = new PasswordConfig();
    final PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();
    for (String password : args) {
      System.out.printf("%s is encoded %s%n", password, passwordEncoder.encode(password));
    }
  }
}
