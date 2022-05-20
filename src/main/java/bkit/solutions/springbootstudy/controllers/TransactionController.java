package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.constants.TransactionApiEndpoints.TRANSFER_V1;

import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.services.TransactionService;
import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TransactionApiEndpoints.PREFIX)
public record TransactionController(TransactionService transactionService) {

  @GetMapping("{accountNumber}")
  public Collection<TransactionDto> list(@PathVariable String accountNumber) {
    return transactionService.list(accountNumber);
  }

  @PostMapping(TRANSFER_V1)
  public void transferV1(@RequestBody TransferRequest transaction) {
    transactionService.transferV1(transaction);
  }

  @PostMapping("v2/transfer")
  public void transferV2(@RequestBody TransferRequest transaction) {
    transactionService.transferV2(transaction);
  }

  @PostMapping("v2.1/transfer")
  public void transferFeeV21(@RequestBody TransferRequest transaction) {
    transactionService.transferFeeV21(transaction);
  }
}
