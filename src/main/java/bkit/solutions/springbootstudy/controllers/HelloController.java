package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.services.HelloService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hello")
@RequiredArgsConstructor
public class HelloController {
  private final HelloService helloService;

  @GetMapping("{name}")
  public Map<String, String> hello(@PathVariable String name) {
    return Map.of("hello", helloService.hello(name));
  }

  @GetMapping
  public Map<String, String> hello() {
    return Map.of("hello", "empty");
  }
}
