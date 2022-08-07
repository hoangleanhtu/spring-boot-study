package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.config.AccessTokenProperties;
import bkit.solutions.springbootstudy.entities.AccessTokenInfo;
import bkit.solutions.springbootstudy.entities.UserDemoEntity;
import bkit.solutions.springbootstudy.exceptions.InvalidUsernameAndPasswordException;
import bkit.solutions.springbootstudy.repositories.AccessTokenRepository;
import bkit.solutions.springbootstudy.repositories.UserDemoRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserDemoRepository userDemoRepository;
  private final AccessTokenGenerator accessTokenGenerator;
  private final PasswordEncoder passwordEncoder;
  private final AccessTokenProperties accessTokenProperties;
  private final AccessTokenRepository accessTokenRepository;

  public String login(String username, String password) throws InvalidUsernameAndPasswordException {
    final Optional<UserDemoEntity> maybeUser = userDemoRepository.findOneByUsername(username);

    if (maybeUser.isEmpty() || !passwordEncoder.matches(password, maybeUser.get().getPassword())) {
      throw new InvalidUsernameAndPasswordException(username);
    }

    final UserDemoEntity userDemoEntity = maybeUser.get();
    final String accessToken = accessTokenGenerator.generate(accessTokenProperties.getLength());
    accessTokenRepository.save(AccessTokenInfo.builder()
        .accessToken(accessToken)
        .username(username)
        .role(userDemoEntity.getRole())
        .expiration(accessTokenProperties.getExpiration().toSeconds())
        .build());
    return accessToken;
  }
}
