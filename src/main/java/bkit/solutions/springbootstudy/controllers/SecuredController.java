package bkit.solutions.springbootstudy.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredController {

  @GetMapping("/hr")
  public Map<String, String> hr() {
    return Map.of("room", "HR");
  }

  @GetMapping("/lab")
  public Map<String, String> lab() {
    return Map.of("room", "Lab");
  }

  @GetMapping("/canteen")
  public Map<String, String> canteen() {
    return Map.of("room", "Canteen");
  }
}
