package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.dtos.CreateAccountRequest;
import bkit.solutions.springbootstudy.dtos.DepositRequest;
import bkit.solutions.springbootstudy.services.AccountService;
import bkit.solutions.springbootstudy.services.TransactionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("account")
public record AccountController(TransactionService transactionService, AccountService accountService) {

  @PostMapping("{accountNumber}/deposit")
  public AccountResponse deposit(@PathVariable String accountNumber, @RequestBody DepositRequest depositRequest) {
    return transactionService.deposit(accountNumber, depositRequest.amount());
  }

  @PostMapping
  public AccountResponse create(@RequestBody CreateAccountRequest accountRequest) {
    return accountService.create(accountRequest.accountNumber());
  }
}
