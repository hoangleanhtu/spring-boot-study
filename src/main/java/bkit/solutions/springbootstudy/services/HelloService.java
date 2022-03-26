package bkit.solutions.springbootstudy.services;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

  public String hello(String name) {
    return "Mr/Ms " + name;
  }
}
