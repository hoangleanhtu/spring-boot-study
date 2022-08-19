package bkit.solutions.springbootstudy.security;

import bkit.solutions.springbootstudy.entities.AccessTokenInfo;
import bkit.solutions.springbootstudy.entities.UserRole;
import bkit.solutions.springbootstudy.exceptions.InvalidAccessTokenException;
import bkit.solutions.springbootstudy.exceptions.UnAuthorizedException;
import bkit.solutions.springbootstudy.repositories.AccessTokenRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

@RequiredArgsConstructor
public class SecurityFilter implements Filter {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  private static final Map<UserRole, Set<String>> PERMISSIONS_MAP = Map.of(
      UserRole.ENGINEER, Set.of("/lab**", "/server**", "/design**", "/development**"),
      UserRole.OFFICE, Set.of("/hr**", "/finance**", "/customer-service**"),
      UserRole.MANAGER, Set.of("/**"),
      UserRole.CUSTOMER, Set.of("/customer-service**")
  );

  private final List<String> authenticateUrls;
  private final AccessTokenRepository accessTokenRepository;

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    if (requireAuthentication(request)) {
      try {
        checkAccessToken(request);
      } catch (InvalidAccessTokenException e) {
        ((HttpServletResponse) servletResponse).sendError(401);
        return;
      } catch (UnAuthorizedException e) {
        ((HttpServletResponse) servletResponse).sendError(403);
        return;
      }
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  private void checkAccessToken(HttpServletRequest request)
      throws InvalidAccessTokenException, UnAuthorizedException {
    // TODO tu.hoang implement
    final String accessToken = request.getHeader("Authorization");
    if (StringUtils.isBlank(accessToken)) {
      throw new InvalidAccessTokenException();
    }

    final Optional<AccessTokenInfo> userInfo = accessTokenRepository.findById(accessToken);
    if (userInfo.isEmpty()) {
      throw new InvalidAccessTokenException();
    }

    final String path = getPath(request);
    if (PATH_MATCHER.match("/canteen**", path)) {
      return;
    }

    final AccessTokenInfo accessTokenInfo = userInfo.get();
    if (PERMISSIONS_MAP.get(accessTokenInfo.getRole()).stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path))) {
      return;
    }

    throw new UnAuthorizedException();
  }

  private boolean requireAuthentication(HttpServletRequest request) {
    if (authenticateUrls == null) {
      return false;
    }

    final String path = getPath(request);
    return authenticateUrls.stream()
        .anyMatch(it -> PATH_MATCHER.match(it, path));
  }

  private String getPath(HttpServletRequest request) {
    return request.getRequestURI().substring(request.getContextPath().length());
  }
}
