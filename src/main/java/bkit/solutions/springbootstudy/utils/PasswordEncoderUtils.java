package bkit.solutions.springbootstudy.utils;

import bkit.solutions.springbootstudy.config.SecurityConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtils {

  public static void main(String[] args) {
    final SecurityConfig securityConfig = new SecurityConfig();
    final PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    for (String password : args) {
      System.out.printf("%s is encoded %s%n", password, passwordEncoder.encode(password));
    }
  }
}
