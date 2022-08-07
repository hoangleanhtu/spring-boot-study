package bkit.solutions.springbootstudy.exceptions;

import lombok.Getter;

@Getter
public class InvalidUsernameAndPasswordException extends Exception {
  private final String username;
  public InvalidUsernameAndPasswordException(String username) {
    this.username = username;
  }

}
