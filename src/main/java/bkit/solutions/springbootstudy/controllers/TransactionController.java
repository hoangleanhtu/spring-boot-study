package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.services.TransactionService;
import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transactions")
public record TransactionController(TransactionService transactionService) {

  @GetMapping
  public Collection<TransactionDto> list() {
    return transactionService.list();
  }
}
