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

  @GetMapping("{accountNumber}")
  public Collection<TransactionDto> list(@PathVariable String accountNumber) {
    return transactionService.list(accountNumber);
  }

  @PostMapping("v1/transfer")
  public AccountResponse transferV1(@RequestBody TransferRequest transaction) {
    return transactionService.transferV1(transaction);
  }

  @PostMapping("v2/transfer")
  public AccountResponse transferV2(@RequestBody TransferRequest transaction) {
    return transactionService.transferV2(transaction);
  }
}
