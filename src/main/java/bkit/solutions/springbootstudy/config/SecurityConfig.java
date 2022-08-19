package bkit.solutions.springbootstudy.config;

import bkit.solutions.springbootstudy.repositories.AccessTokenRepository;
import bkit.solutions.springbootstudy.security.SecurityFilter;
import java.util.List;
import java.util.Set;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

  @Bean
  public FilterRegistrationBean<SecurityFilter> securityFilterFilterRegistrationBean(
      AccessTokenRepository accessTokenRepository) {
    FilterRegistrationBean<SecurityFilter> securityFilterFilterRegistrationBean = new FilterRegistrationBean<>();
    final List<String> authenticateUrls = List.of(
        "/lab**", "/server**", "/design**", "/development**", "/hr**", "/finance**",
        "/customer-service**", "/canteen**"
    );
    securityFilterFilterRegistrationBean.setFilter(new SecurityFilter(
        authenticateUrls,
        accessTokenRepository));
    securityFilterFilterRegistrationBean.setUrlPatterns(Set.of("/*"));
    return securityFilterFilterRegistrationBean;
  }
}
