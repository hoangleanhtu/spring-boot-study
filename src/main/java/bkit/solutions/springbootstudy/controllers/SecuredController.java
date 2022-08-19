package bkit.solutions.springbootstudy.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredController {

  @GetMapping("/hr")
  @Operation(security = { @SecurityRequirement(name = "auth-token") })
  public Map<String, String> hr() {
    return Map.of("room", "HR");
  }

  @GetMapping("/lab")
  @Operation(security = { @SecurityRequirement(name = "auth-token") })
  public Map<String, String> lab() {
    return Map.of("room", "Lab");
  }

  @GetMapping("/canteen")
  @Operation(security = { @SecurityRequirement(name = "auth-token") })
  public Map<String, String> canteen() {
    return Map.of("room", "Canteen");
  }
}
