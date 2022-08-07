package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.AuthenticationApiEndpoints;
import bkit.solutions.springbootstudy.dtos.PostLoginRequest;
import bkit.solutions.springbootstudy.dtos.PostLoginResponse;
import bkit.solutions.springbootstudy.exceptions.InvalidUsernameAndPasswordException;
import bkit.solutions.springbootstudy.services.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthenticationApiEndpoints.PREFIX)
public record AuthenticationController(AuthenticationService authenticationService) {

  @PostMapping("login")
  PostLoginResponse login(@RequestBody PostLoginRequest request)
      throws InvalidUsernameAndPasswordException {
    return this.authenticationService.login(request.getUsername(),
        request.getPassword());
  }
}
