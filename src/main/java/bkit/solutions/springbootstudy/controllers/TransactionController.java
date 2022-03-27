package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.services.TransactionService;
import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transactions")
public record TransactionController(TransactionService transactionService) {

  @GetMapping("{account_number}")
  public Collection<TransactionDto> list(@PathVariable String accountNumber) {
    return transactionService.list(accountNumber);
  }

  @PostMapping
  public AccountResponse transfer(@RequestBody TransferRequest transaction) {
    return transactionService.transfer(transaction);
  }

}
