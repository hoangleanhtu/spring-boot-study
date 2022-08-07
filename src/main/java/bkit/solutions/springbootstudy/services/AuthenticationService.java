package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.entities.UserDemoEntity;
import bkit.solutions.springbootstudy.exceptions.InvalidUsernameAndPasswordException;
import bkit.solutions.springbootstudy.repositories.UserDemoRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private static final int ACCESS_TOKEN_LENGTH = 48;
  private final UserDemoRepository userDemoRepository;
  private final AccessTokenGenerator accessTokenGenerator;

  public String login(String username, String password) throws InvalidUsernameAndPasswordException {
    final Optional<UserDemoEntity> maybeUser = userDemoRepository.findOneByUsernameAndPassword(
        username, password);

    if (maybeUser.isEmpty()) {
      throw new InvalidUsernameAndPasswordException(username);
    }

    return accessTokenGenerator.generate(ACCESS_TOKEN_LENGTH);
  }
}
